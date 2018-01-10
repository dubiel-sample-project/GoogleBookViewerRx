package com.dubiel.sample.googlebookviewerrx;



public class GoogleBooksParameters {
    private String key;
    private String query;
    private Integer start;
    private Integer maxResults;

    public GoogleBooksParameters(String key, String query, int start, int maxResults) {
        this.key = key;
        this.query = query;
        this.start = start;
        this.maxResults = maxResults;
    }

    public String getKey() {
        return key;
    }

    public String getQuery() {
        return query;
    }

    public Integer getStart() {
        return start;
    }

    public Integer getMaxResults() {
        return maxResults;
    }
}
