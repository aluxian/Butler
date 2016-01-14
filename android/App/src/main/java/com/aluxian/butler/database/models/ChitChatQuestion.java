package com.aluxian.butler.database.models;

import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;
import com.aluxian.butler.database.PatternModel;
import com.aluxian.butler.database.enums.PatternType;

import java.util.List;

/**
 * The ChitChatQuestion model stores a pattern that will be matches against user input.
 * <p/>
 * If the pattern matches, then one of the {@link ChitChatAnswer}s corresponding to the ChitChatQuestion will be spoken
 * by the assistant.
 * <p/>
 * ChitChatQuestion objects are not necessarily questions, they can be any user input.
 */
@Table(name = "CHIT_CHAT_QUESTIONS")
public class ChitChatQuestion extends PatternModel {

    /** This question will only be matched for this answer */
    @Column(name = "Answer")
    public ChitChatAnswer answer;

    @SuppressWarnings("UnusedDeclaration")
    public ChitChatQuestion() {
        super();
    }

    public ChitChatQuestion(PatternType type, String pattern, ChitChatAnswer answer) {
        this.type = type;
        this.pattern = pattern;
        this.answer = answer;
    }

    /**
     * @return The answers corresponding to this question
     */
    public List<ChitChatAnswer> getAnswers() {
        return getMany(ChitChatAnswer.class, "Question");
    }

    /**
     * @return All the questions that are now a follow-up (owned by an answer)
     */
    public static List<ChitChatQuestion> getDefaultQuestions() {
        return new Select().from(ChitChatQuestion.class).where("Answer IS NULL").execute();
    }

}
