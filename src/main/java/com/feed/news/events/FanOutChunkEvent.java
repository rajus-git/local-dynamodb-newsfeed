package com.feed.news.events;

import java.time.Instant;
import java.util.List;

public class FanOutChunkEvent {

    private String postId;
    private String creatorId;
    private Instant createdAt;
    private List<String> followerIds;

    public FanOutChunkEvent() {}

    public FanOutChunkEvent(
            String postId,
            String creatorId,
            Instant createdAt,
            List<String> followerIds) {
        this.postId = postId;
        this.creatorId = creatorId;
        this.createdAt = createdAt;
        this.followerIds = followerIds;
    }

    public String getPostId() { return postId; }
    public String getCreatorId() { return creatorId; }
    public Instant getCreatedAt() { return createdAt; }
    public List<String> getFollowerIds() { return followerIds; }

    public void setPostId(String postId) { this.postId = postId; }
    public void setCreatorId(String creatorId) { this.creatorId = creatorId; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public void setFollowerIds(List<String> followerIds) { this.followerIds = followerIds; }
}
