package com.feed.news.events;

import java.time.Instant;

public class PostCreatedEvent {

    private String postId;
    private String creatorId;
    private Instant createdAt;

    public PostCreatedEvent() {}

    public PostCreatedEvent(String postId, String creatorId, Instant createdAt) {
        this.postId = postId;
        this.creatorId = creatorId;
        this.createdAt = createdAt;
    }

    public String getPostId() { return postId; }
    public String getCreatorId() { return creatorId; }
    public Instant getCreatedAt() { return createdAt; }

    public void setPostId(String postId) { this.postId = postId; }
    public void setCreatorId(String creatorId) { this.creatorId = creatorId; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
