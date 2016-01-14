package com.aluxian.butler.database;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.aluxian.butler.database.enums.PatternType;
import com.aluxian.butler.processing.ProcessingUtils;

/**
 * Database models that have a pattern and patternType field
 */
public class PatternModel extends Model {

    /** The type of the pattern */
    @Column(name = "Type")
    protected PatternType type;

    /** The pattern of the input */
    @Column(name = "Pattern")
    protected String pattern;

    public void setType(PatternType type) {
        this.type = type;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public PatternType getType() {
        return type;
    }

    public String getPattern() {
        return pattern;
    }

    /**
     * @return The normalized version of the pattern
     */
    public String getNormalizedPattern() {
        return ProcessingUtils.normalizeText(pattern, true);
    }

}
