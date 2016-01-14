package com.aluxian.butler.processing;

import com.aluxian.butler.database.PatternModel;
import com.aluxian.butler.recycler.items.TextItem;
import com.aluxian.butler.utils.Logger;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.stanford.nlp.semgraph.semgrex.SemgrexMatcher;
import edu.stanford.nlp.semgraph.semgrex.SemgrexParseException;
import edu.stanford.nlp.semgraph.semgrex.SemgrexPattern;
import retrofit.RetrofitError;

public abstract class PatternMatcher<T extends PatternModel> {

    /** Logger instance */
    @SuppressWarnings("UnusedDeclaration") private static final Logger LOG = new Logger(PatternMatcher.class);

    protected UserInput userInput;

    protected Matcher regexMatcher;
    protected SemgrexMatcher semgrexMatcher;

    public PatternMatcher(UserInput userInput) {
        this.userInput = userInput;
    }

    protected boolean testRegex(T model) {
        Pattern pattern = Pattern.compile(model.getNormalizedPattern(), Pattern.CASE_INSENSITIVE);
        regexMatcher = pattern.matcher(userInput.getText());
        return regexMatcher.matches();
    }

    protected boolean testSemgrex(T model) {
        try {
            SemgrexPattern pattern = SemgrexPattern.compile(model.getPattern());
            semgrexMatcher = pattern.matcher(userInput.getSemanticGraph());
            return semgrexMatcher.matches();
        } catch (SemgrexParseException e) {
            LOG.e(e);
        }

        return false;
    }

    protected abstract ProcessorOutput matchesRegex(T model);

    protected abstract ProcessorOutput matchesSemgrex(T model);

    protected ProcessorOutput retrofitException() {
        return new ProcessorOutput(new TextItem("I'm sorry, one of " +
                "my servers is offline. Please try again later or ask my developer about it", true));
    }

    public ProcessorOutput match(List<T> models) {
        for (T model : models) {
            switch (model.getType()) {
                case REGEX:
                    if (testRegex(model)) {
                        ProcessorOutput output = matchesRegex(model);
                        if (output != null) {
                            return output;
                        }
                    }

                    break;

                case SEMGREX:
                    if (testSemgrex(model)) {
                        try {
                            ProcessorOutput output = matchesSemgrex(model);
                            if (output != null) {
                                return output;
                            }
                        } catch (RetrofitError e) {
                            LOG.i(e.getMessage());
                            return retrofitException();
                        }
                    }

                    break;
            }
        }

        return null;
    }

}
