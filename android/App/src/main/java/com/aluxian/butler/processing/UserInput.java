package com.aluxian.butler.processing;

import com.aluxian.butler.apis.corenlp.CoreNlpService;

import edu.stanford.nlp.semgraph.SemanticGraph;

/**
 * Stores user input
 */
public final class UserInput {

    /** The text content of the input */
    private String text;

    /** The semantic graph of the first sentence in the input */
    private SemanticGraph semanticGraph;

    /** The sentiment of the text */
    private String sentiment;

    /**
     * Create a new object from unparsed text input 8
     *
     * @param text Content of the input
     */
    public UserInput(String text) {
        this.text = text;
    }

    /**
     * @return The normalized version of the text
     */
    public String getText() {
        return ProcessingUtils.normalizeText(text, true);
    }

    /**
     * SemanticGraph getter
     *
     * @return The {@link edu.stanford.nlp.semgraph.SemanticGraph} of the first sentence
     */
    public SemanticGraph getSemanticGraph() {
        if (semanticGraph == null) {
            // Use the API to parse the text and get the first sentence
            String serialized = CoreNlpService.api.getSemgraphs(getText()).get(0);
            semanticGraph = ProcessingUtils.deserializeSemanticGraph(serialized);
        }

        return semanticGraph;
    }

    public String getSentiment() {
        if (sentiment == null) {
            // Use the API to parse the text and get the sentiment of the first sentence
            sentiment = CoreNlpService.api.getSentiments(getText()).get(0);
        }

        return sentiment;
    }

    @Override
    public String toString() {
        return text;
    }

}
