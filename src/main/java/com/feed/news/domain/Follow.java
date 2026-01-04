package com.feed.news.domain;

import java.time.Instant;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondarySortKey;

@DynamoDbBean
public class Follow {

    private String userFollowing;
    private String userFollowed;
    private Instant createdAt;

    public Follow() {}

    @DynamoDbPartitionKey
    @DynamoDbSecondarySortKey(indexNames = "userFollowed-userFollowing-index")
    public String getUserFollowing() {
        return userFollowing;
    }

    public void setUserFollowing(String userFollowing) {
        this.userFollowing = userFollowing;
    }

    @DynamoDbSortKey
    @DynamoDbSecondaryPartitionKey(indexNames = "userFollowed-userFollowing-index")
    public String getUserFollowed() {
        return userFollowed;
    }

    public void setUserFollowed(String userFollowed) {
        this.userFollowed = userFollowed;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
