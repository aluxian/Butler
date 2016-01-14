package com.aluxian.butler.apis.freebase;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Object returned by the Freebase Topic API
 */
public final class LookupResult {

    /** Properties object */
    public LookupProperty property;

    /**
     * List of properties returned from a lookup
     */
    public static final class LookupProperty {

        @SerializedName("/common/topic/description")
        public LookupValues topicDescription;

        @SerializedName("/common/topic/image")
        public LookupValues topicImage;

    }

    /**
     * Wrapper for a property value
     */
    public static final class LookupValues {

        /** The type of the values */
        public String valuetype;

        /** List of values */
        public List<LookupValue> values;

    }

    /**
     * Stores a property's value
     */
    public static final class LookupValue {

        /** Value text */
        public String text;

        /** Value ID */
        public String id;

        /** Value language */
        public String lang;

        /** The value itself */
        public Object value;

        /** Value citation */
        public LookupCitation citation;

    }

    /**
     * Stores a citation
     */
    public static final class LookupCitation {

        /** Citation provider */
        public String provider;

        /** Citation statement */
        public String statement;

        /** Citation URI */
        public String uri;

    }

}
