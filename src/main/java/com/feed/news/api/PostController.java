package com.feed.news.api;

import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.feed.news.repository.PagedResult;
import org.springframework.web.bind.annotation.*;

import com.feed.news.domain.Post;
import com.feed.news.repository.PostRepository;

@RestController
@RequestMapping
public class PostController {

    private final PostRepository postRepository;

    public PostController(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    @PostMapping("/posts")
    public Post createPost(@RequestBody CreatePostRequest request) {

        Post post = new Post();
        post.setId(UUID.randomUUID().toString());
        post.setCreatorId(request.getCreatorId());
        post.setContent(request.getContent());
        post.setCreatedAt(Instant.now());

        postRepository.save(post);
        return post;
    }

    @GetMapping("/feed/{userId}")
    public PagedResponse<Post> getFeed(
            @PathVariable String userId,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String nextToken) {

        PagedResult<Post> result =
                postRepository.getRecentPostsByCreator(userId, limit, nextToken);

        return new PagedResponse<>(
                result.getItems(),
                result.getNextToken()
        );
    }
}
