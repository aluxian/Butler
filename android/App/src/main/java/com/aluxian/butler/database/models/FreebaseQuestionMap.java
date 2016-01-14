package com.aluxian.butler.database.models;

import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.aluxian.butler.database.PatternModel;
import com.aluxian.butler.database.enums.PatternType;

/**
 * This table stores maps between user input and the actual queries that should be made instead
 */
@Table(name = "FREEBASE_QUESTION_MAPS")
public class FreebaseQuestionMap extends PatternModel {

    /** The query to map to for searching on Freebase */
    @Column(name = "Query")
    public String query;

    /** The filter to use for searching on Freebase */
    @Column(name = "Filter")
    public String filter;

    /** The minimum score to consider a search result valid */
    @Column(name = "MinScore")
    public int minScore;

    @SuppressWarnings("UnusedDeclaration")
    public FreebaseQuestionMap() {
        super();
    }

    public FreebaseQuestionMap(PatternType type, String pattern, String query, String filter, int minScore) {
        super();
        this.type = type;
        this.pattern = pattern;
        this.query = query;
        this.filter = filter;
        this.minScore = minScore;
    }

}
