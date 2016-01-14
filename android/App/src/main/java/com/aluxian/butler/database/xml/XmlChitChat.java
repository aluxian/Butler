package com.aluxian.butler.database.xml;

import com.aluxian.butler.database.enums.PatternType;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

@Root
public final class XmlChitChat {

    @ElementList
    private List<Question> questions;

    public List<Question> getQuestions() {
        return questions;
    }

    public static final class Question {

        @Element
        private PatternType type;

        @Element
        private String pattern;

        @ElementList
        private List<Answer> answers;

        public PatternType getType() {
            return type;
        }

        public String getPattern() {
            return pattern;
        }

        public List<Answer> getAnswers() {
            return answers;
        }

    }

    public static final class Answer {

        @Element
        private String text;

        @Element
        private int score;

        @Element(required = false)
        private String conditions;

        @Element(required = false)
        private boolean prompt;

        @ElementList(required = false)
        private List<Question> questions;

        @Element(required = false)
        private String sentiment;

        public String getText() {
            return text;
        }

        public int getScore() {
            return score;
        }

        public String getConditions() {
            return conditions;
        }

        public boolean promptForInput() {
            return prompt;
        }

        public List<Question> getQuestions() {
            return questions;
        }

        public String getSentiment() {
            return sentiment;
        }

    }

}
