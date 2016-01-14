package com.aluxian.butler.database.xml;

import com.aluxian.butler.database.enums.PatternType;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

@Root
public final class XmlFreebase {

    @ElementList
    private List<Map> maps;

    public List<Map> getMaps() {
        return maps;
    }

    public static final class Map {

        @Element
        private PatternType type;

        @Element
        private String pattern;

        @Element(required = false)
        private String query;

        @Element(required = false)
        private String filter;

        @Element(name = "min-score")
        private int minScore;

        public PatternType getType() {
            return type;
        }

        public String getPattern() {
            return pattern;
        }

        public String getQuery() {
            return query;
        }

        public String getFilter() {
            return filter;
        }

        public int getMinScore() {
            return minScore;
        }

    }

}
