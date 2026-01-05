package com.feed.news.api;

import java.util.List;

public class PagedResponse<T> {

    private List<T> items;
    private String nextToken;

    public PagedResponse(List<T> items, String nextToken) {
        this.items = items;
        this.nextToken = nextToken;
    }

    public List<T> getItems() {
        return items;
    }

    public String getNextToken() {
        return nextToken;
    }
}
