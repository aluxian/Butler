package com.aluxian.butler.corenlpserver;

import edu.stanford.nlp.pipeline.StanfordCoreNLP;

import java.util.Properties;

/**
 * Singleton wrapper for the StanfordCoreNLP parser
 */
public class CoreNLP {

    /** Annotators used by CoreNLP */
    private static final String ANNOTATORS = "tokenize, ssplit, pos, lemma, parse, sentiment, gender";

    /** Stanford CoreNLP instance */
    public StanfordCoreNLP stanfordCoreNLP;

    /** Singleton instance of the wrapper */
    private static CoreNLP coreNlpInstance;

    /**
     * Return an instance of the parser
     *
     * @return {@link com.aluxian.butler.corenlpserver.CoreNLP}
     */
    public static CoreNLP instance() {
        if (coreNlpInstance == null) {
            coreNlpInstance = new CoreNLP();
        }

        return coreNlpInstance;
    }

    /**
     * Initialise the parser
     */
    private CoreNLP() {
        // Properties object for the parser
        Properties properties = new Properties();
        properties.put("annotators", ANNOTATORS);

        // Initialise the CoreNLP instance
        stanfordCoreNLP = new StanfordCoreNLP(properties);
        System.out.println("StanfordCoreNLP initialised");
    }

}
