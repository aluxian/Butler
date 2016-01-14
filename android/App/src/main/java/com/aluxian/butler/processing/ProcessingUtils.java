package com.aluxian.butler.processing;

import com.aluxian.butler.utils.Logger;
import com.aluxian.butler.utils.Utils;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.semgrex.SemgrexMatcher;
import edu.stanford.nlp.util.TypesafeMap;

/**
 * Utility methods class
 */
public class ProcessingUtils {

    /** Logger instance */
    @SuppressWarnings("UnusedDeclaration") private static final Logger LOG = new Logger(ProcessingUtils.class);

    /** Maps SemanticGraph node properties to their annotation class keys */
    private static final HashMap<String, Class<? extends TypesafeMap.Key<String>>> SEMGRAPH_NODE_PROPERTIES =
            new HashMap<>();

    /** Add the maps */
    static {
        SEMGRAPH_NODE_PROPERTIES.put("word", CoreAnnotations.TextAnnotation.class);
        SEMGRAPH_NODE_PROPERTIES.put("lemma", CoreAnnotations.LemmaAnnotation.class);
        SEMGRAPH_NODE_PROPERTIES.put("tag", CoreAnnotations.PartOfSpeechAnnotation.class);
    }

    /**
     * Extracts values from the given SemanticGraph and replaces variables in the provided string with them
     *
     * @param stringWithVars The string with variables to interpolate
     * @param semgrexMatcher SemgrexMatcher object to extract the values from
     * @return The initial string after the values have been inserted
     */
    public static String interpolateValuesFromSemGraph(String stringWithVars, SemgrexMatcher semgrexMatcher) {
        // Get the nodes from the semgraph
        IndexedWord[] sourceNodes = new IndexedWord[100];

        for (String nodeName : semgrexMatcher.getNodeNames()) {
            String[] splitNodeName = nodeName.split("__"); // __id__ becomes [, id]
            int nodeId = Integer.parseInt(splitNodeName[1]);
            sourceNodes[nodeId] = semgrexMatcher.getNode(nodeName);
        }

        // Find vars to interpolate
        Matcher varsMatcher = Pattern.compile("__\\d+__\\w+__").matcher(stringWithVars);

        while (varsMatcher.find()) {
            String match = varsMatcher.group();
            String[] splitMatch = match.split("__"); // __id__prop__ becomes [, id, prop]

            // Get the property name and the source node to get the value from
            int sourceNodeId = Integer.parseInt(splitMatch[1]);
            String propertyName = splitMatch[2];
            IndexedWord sourceNode = sourceNodes[sourceNodeId];

            if (sourceNode != null) {
                // Get the property's value
                String propertyValue = sourceNode.get(SEMGRAPH_NODE_PROPERTIES.get(propertyName));

                // Finally interpolate the match
                stringWithVars = stringWithVars.replace(match, propertyValue);
            }
        }

        return stringWithVars;
    }

    /**
     * @param serialized Serialized string version of the graph
     * @return A new {@link SemanticGraph} object from the string
     */
    public static SemanticGraph deserializeSemanticGraph(String serialized) {
        String serializedGraph = serialized.substring(0, serialized.indexOf("]%[") + 1);
        String serializedLemmas = serialized.substring(serialized.indexOf("]%[") + 2);

        SemanticGraph semanticGraph = SemanticGraph.valueOf(serializedGraph);
        HashMap<String, String> lemmas = Utils.hashMapFromStrings(serializedLemmas);

        for (IndexedWord word : semanticGraph.vertexListSorted()) {
            word.setLemma(lemmas.get(word.word()));
        }

        return semanticGraph;
    }

    /**
     * Evaluates an expression and returns its truth value
     *
     * @param expression Expression to evaluate
     * @return The truth value of the given expression
     */
    public static boolean logicalEval(String expression) {
        // Prepare expression
        expression = expression
                .trim()
                .replaceAll("\\s+", "")
                .replaceAll("&&", "&")
                .replaceAll("\\|\\|", "|")
                .toLowerCase();

        // Match true sub-expressions
        Pattern trueValues = Pattern.compile("(true\\|(true|false)|(true|false)\\|true|true&true|\\(true\\)|!false)");
        Matcher trueMatcher = trueValues.matcher("");

        // Match false sub-expressions
        Pattern falseValues = Pattern.compile("(true&false|false&true|false&false|\\(false\\)|false\\|false|!true)");
        Matcher falseMatcher = falseValues.matcher("");

        while (!("true".equals(expression) || "false".equals(expression))) {
            expression = trueMatcher.reset(expression).replaceAll("true");
            expression = falseMatcher.reset(expression).replaceAll("false");
        }

        return Boolean.valueOf(expression);
    }

    /**
     * Normalizes the string to be ready to be used for processing. All letters are transformed to lowercase, and common
     * constructions such as `'s` are expanded.
     *
     * @param text     Text to normalize
     * @param keepCase Whether the original letter case should be preserved
     * @return Normalized version of {link #text}
     */
    public static String normalizeText(String text, boolean keepCase) {
        if (!keepCase) {
            text = text.toLowerCase();
        }

        // Replace common constructions
        text = text
                .replaceAll("I", "i")
                .replaceAll(" u ", " you ")
                .replaceAll(" ur ", " your ")
                .replaceAll("'s", " is")
                .replaceAll("won't", "will not")
                .replaceAll("ain't", "is not")
                .replaceAll("n't", " not");

        return text;
    }

}
