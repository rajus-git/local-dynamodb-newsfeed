package com.feed.news.api;

import java.time.Instant;
import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.feed.news.domain.Follow;
import com.feed.news.repository.FollowRepository;

@RestController
@RequestMapping("/follows")
public class FollowController {

    private final FollowRepository followRepository;

    public FollowController(FollowRepository followRepository) {
        this.followRepository = followRepository;
    }

    // CREATE FOLLOW
    @PostMapping
    public Follow follow(@RequestBody FollowRequest request) {

        Follow follow = new Follow();
        follow.setUserFollowing(request.getUserFollowing());
        follow.setUserFollowed(request.getUserFollowed());
        follow.setCreatedAt(Instant.now());

        followRepository.follow(follow);
        return follow;
    }

    // LIST FOLLOWERS (who follows userId)
    @GetMapping("/{userId}/followers")
    public List<Follow> getFollowers(
            @PathVariable String userId,
            @RequestParam(defaultValue = "20") int limit) {

        return followRepository.getFollowers(userId, limit);
    }

    // LIST FOLLOWING (who userId follows)
    @GetMapping("/{userId}/following")
    public List<Follow> getFollowing(
            @PathVariable String userId,
            @RequestParam(defaultValue = "20") int limit) {

        return followRepository.getFollowing(userId, limit);
    }
}
