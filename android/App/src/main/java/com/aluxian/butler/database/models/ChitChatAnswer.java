package com.aluxian.butler.database.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import java.util.List;

/**
 * Possible answer to a {@link ChitChatQuestion}
 */
@Table(name = "CHIT_CHAT_ANSWERS")
public class ChitChatAnswer extends Model {

    /** The text of the answer */
    @Column(name = "Text")
    public String text;

    /**
     * The score of the answer; the higher this is, the more probable that this will be the chosen answer between the
     * possible ones for a ChitChatQuestion
     */
    @Column(name = "Score")
    public int score;

    /** Comma separated string of conditions; if these are not met then this answer will not be used */
    @Column(name = "Conditions")
    public String conditions;

    /** The required sentiment of the input to match this answer */
    @Column(name = "Sentiment")
    public String sentiment;

    /** Whether to start listening again after the answer is spoken */
    @Column(name = "PromptForInput")
    public boolean promptForInput;

    /** The ChitChatQuestion this answer corresponds to */
    @Column(name = "Question")
    public ChitChatQuestion question;

    @SuppressWarnings("UnusedDeclaration")
    public ChitChatAnswer() {
        super();
    }

    public ChitChatAnswer(String text, int score, String conditions, String sentiment, boolean promptForInput,
                          ChitChatQuestion parentQuestion) {
        super();
        this.text = text;
        this.score = score;
        this.conditions = conditions;
        this.sentiment = sentiment;
        this.promptForInput = promptForInput;
        this.question = parentQuestion;
    }

    /**
     * @return The questions expectedInput from the user after this answer
     */
    public List<ChitChatQuestion> getQuestions() {
        return new Select().from(ChitChatQuestion.class).where("Answer = ?", getId()).execute();
    }

}
