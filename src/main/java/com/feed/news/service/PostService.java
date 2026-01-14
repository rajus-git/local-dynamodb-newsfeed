package com.feed.news.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.feed.news.repository.PagedResult;
import org.springframework.stereotype.Service;

import com.feed.news.domain.Follow;
import com.feed.news.domain.Post;
import com.feed.news.domain.PrecomputedFeed;
import com.feed.news.repository.FollowRepository;
import com.feed.news.repository.PostRepository;
import com.feed.news.repository.PrecomputedFeedRepository;

import software.amazon.awssdk.enhanced.dynamodb.model.Page;

@Service
public class PostService {

    private static final int FEED_BATCH_SIZE = 25;

    private final PostRepository postRepository;
    private final FollowRepository followRepository;
    private final PrecomputedFeedRepository feedRepository;

    public PostService(
            PostRepository postRepository,
            FollowRepository followRepository,
            PrecomputedFeedRepository feedRepository) {
        this.postRepository = postRepository;
        this.followRepository = followRepository;
        this.feedRepository = feedRepository;
    }

    public PagedResult<Post> getFeed(String userId, int limit, String nextToken) {

        PagedResult<PrecomputedFeed> feedResult =
                feedRepository.getFeedPage(userId, limit, nextToken);

        List<String> postIds =
                feedResult.getItems().stream()
                        .map(PrecomputedFeed::getPostId)
                        .toList();

        Map<String, Post> postMap =
                postRepository.batchGetByIds(postIds);

        List<Post> posts =
                postIds.stream()
                        .map(postMap::get)
                        .toList();

        return new PagedResult<>(posts, feedResult.getNextToken());
    }


    public Post createPost(String creatorId, String content) {

        // 1) Create and save post
        Post post = new Post();
        post.setCreatorId(creatorId);
        post.setContent(content);
        post.setCreatedAt(Instant.now());

        postRepository.save(post);

        // 2) Fan-out (paged + batched)
        fanOutToFollowers(post);

        return post;
    }

    private void fanOutToFollowers(Post post) {

        Iterator<Page<Follow>> pages =
                followRepository.iterateFollowersOf(post.getCreatorId());

        List<PrecomputedFeed> batch = new ArrayList<>(FEED_BATCH_SIZE);

        while (pages.hasNext()) {
            Page<Follow> page = pages.next();

            for (Follow follow : page.items()) {

                PrecomputedFeed feedItem = new PrecomputedFeed();
                feedItem.setUserId(follow.getUserFollowing());
                feedItem.setPostId(post.getId());
                feedItem.setCreatorId(post.getCreatorId());
                feedItem.setSortKey(
                        post.getCreatedAt().toEpochMilli() + "#" + post.getId()
                );

                batch.add(feedItem);

                if (batch.size() == FEED_BATCH_SIZE) {
                    feedRepository.addToFeeds(batch);
                    batch.clear();
                }
            }
        }

        // flush remainder
        if (!batch.isEmpty()) {
            feedRepository.addToFeeds(batch);
        }
    }
}
