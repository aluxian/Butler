package com.aluxian.butler.database.enums;

/**
 * Stores available pattern types that database models can hold
 */
public enum PatternType {
    REGEX,      // Regular expressions
    SEMGREX,    // SemanticGraph-based matching
    TREGEX      // Tree-based matching
}
