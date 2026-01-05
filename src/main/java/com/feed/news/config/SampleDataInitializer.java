package com.feed.news.config;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.feed.news.domain.Follow;
import com.feed.news.domain.Post;
import com.feed.news.repository.FollowRepository;
import com.feed.news.repository.PostRepository;

@Component
@Profile("local")
public class SampleDataInitializer implements ApplicationRunner {

    private final PostRepository postRepository;
    private final FollowRepository followRepository;

    public SampleDataInitializer(
            PostRepository postRepository,
            FollowRepository followRepository) {

        this.postRepository = postRepository;
        this.followRepository = followRepository;
    }

    @Override
    public void run(ApplicationArguments args) {

        // --- Users (logical only) ---
        String userA = "user-a";
        String userB = "user-b";

        // --- Posts ---
        Post post1 = createPost(userA, "Hello from user A");
        Post post2 = createPost(userA, "Another post from user A");
        Post post3 = createPost(userB, "User B checking in");

        postRepository.save(post1);
        postRepository.save(post2);
        postRepository.save(post3);

        // --- Follow relationship ---
        Follow follow = new Follow();
        follow.setUserFollowing(userB); // B follows A
        follow.setUserFollowed(userA);
        follow.setCreatedAt(Instant.now());

        followRepository.follow(follow);

        // --- Read-back validation ---
        List<Post> userAPosts =
                postRepository.getRecentPostsByCreator(userA, 10);

        List<Follow> followersOfA =
                followRepository.getFollowers(userA, 10);

        System.out.println("---- SAMPLE DATA CHECK ----");
        System.out.println("Posts by user A: " + userAPosts.size());
        System.out.println("Followers of user A: " + followersOfA.size());
        System.out.println("---------------------------");
    }

    private Post createPost(String creatorId, String content) {

        Post post = new Post();
        post.setId(UUID.randomUUID().toString());
        post.setCreatorId(creatorId);
        post.setContent(content);
        post.setCreatedAt(Instant.now());

        return post;
    }
}
