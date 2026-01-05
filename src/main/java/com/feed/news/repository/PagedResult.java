package com.feed.news.repository;

import java.util.List;

public class PagedResult<T> {

    private final List<T> items;
    private final String nextToken;

    public PagedResult(List<T> items, String nextToken) {
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
