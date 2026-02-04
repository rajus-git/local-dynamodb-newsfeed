package com.feed.news.events;

import java.time.Instant;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FanOutChunkEvent {

    // Identity
    private String postId;
    private String creatorId;

    // Business timestamp
    private Instant createdAt;

    // System / pipeline timestamp (for observability)
    private long eventTime;

    // Payload
    private List<String> followerIds;

    public FanOutChunkEvent() {}

    public FanOutChunkEvent(
            String postId,
            String creatorId,
            Instant createdAt,
            long eventTime,
            List<String> followerIds) {
        this.postId = postId;
        this.creatorId = creatorId;
        this.createdAt = createdAt;
        this.eventTime = eventTime;
        this.followerIds = followerIds;
    }

    public String getPostId() { return postId; }
    public String getCreatorId() { return creatorId; }
    public long getEventTime() { return eventTime; }
    public Instant getCreatedAt() { return createdAt; }
    public List<String> getFollowerIds() { return followerIds; }

    public void setPostId(String postId) { this.postId = postId; }
    public void setCreatorId(String creatorId) { this.creatorId = creatorId; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public void setEventTime(long eventTime) { this.eventTime = eventTime; }
    public void setFollowerIds(List<String> followerIds) { this.followerIds = followerIds; }
}
