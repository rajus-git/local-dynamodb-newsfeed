package com.feed.news.events;

import java.time.Instant;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PostCreatedEvent {

    private String postId;
    private String creatorId;
    private Instant createdAt;
    private long eventTime;

    public PostCreatedEvent() {}

    public PostCreatedEvent(String postId, String creatorId, Instant createdAt, long eventTime) {
        this.postId = postId;
        this.creatorId = creatorId;
        this.createdAt = createdAt;
        this.eventTime = eventTime;
    }

    public String getPostId() { return postId; }
    public String getCreatorId() { return creatorId; }
    public Instant getCreatedAt() { return createdAt; }
    public long getEventTime() { return eventTime; }

    public void setPostId(String postId) { this.postId = postId; }
    public void setCreatorId(String creatorId) { this.creatorId = creatorId; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public void setEventTime(long eventTime) { this.eventTime = eventTime; }
}
