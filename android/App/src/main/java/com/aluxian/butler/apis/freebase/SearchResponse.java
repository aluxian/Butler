package com.aluxian.butler.apis.freebase;

import java.util.List;

/**
 * Object returned by the Freebase Search API
 */
public final class SearchResponse {

    /** Request status */
    public String status;

    /** List of results */
    public List<SearchResult> result;

    /** Suggested correction of the search */
    public List<String> correction;

    /** Number of hits */
    public int hits;

    /**
     * Stores a single search result
     */
    public static final class SearchResult {

        /** Result MID */
        public String mid;

        /** Result name */
        public String name;

        /** Notable object */
        public SearchNotable notable;

        /** Result score */
        public float score;

    }

    /**
     * Stores the notable of a search result
     */
    public static final class SearchNotable {

        /** Notable name */
        public String name;

        /** Notable ID */
        public String id;

    }

}
