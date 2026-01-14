package com.feed.news.api;

import org.springframework.web.bind.annotation.*;

import com.feed.news.domain.Post;
import com.feed.news.repository.PagedResult;
import com.feed.news.service.PostService;

@RestController
@RequestMapping
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    // CREATE POST
    @PostMapping("/posts")
    public Post createPost(@RequestBody CreatePostRequest request) {
        return postService.createPost(
                request.getCreatorId(),
                request.getContent()
        );
    }

    // HOME FEED
    @GetMapping("/feed/{userId}")
    public PagedResponse<Post> getFeed(
            @PathVariable String userId,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String nextToken) {

        PagedResult<Post> result =
                postService.getFeed(userId, limit, nextToken);

        return new PagedResponse<>(
                result.getItems(),
                result.getNextToken()
        );
    }

    // PROFILE FEED
}
