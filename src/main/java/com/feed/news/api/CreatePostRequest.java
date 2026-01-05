package com.feed.news.api;

public class CreatePostRequest {

    private String creatorId;       // remove in production, the user id is passed as Security Context
    private String content;

    public String getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
