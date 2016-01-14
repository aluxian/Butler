package com.aluxian.butler.corenlpserver.services;

import com.aluxian.butler.corenlpserver.CoreNLP;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.util.CoreMap;

import javax.ws.rs.*;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;

@Path("/semgraph")
@Produces(MediaType.APPLICATION_JSON)
public class SemgraphService {

    private static Connection pgSqlConnection;

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response post(@FormParam("text") final String text) {
        // Store the input in the database
        new Thread(new BackgroundStore(text)).start();

        // Annotate the input
        Annotation document = new Annotation(text);
        CoreNLP.instance().stanfordCoreNLP.annotate(document);

        // Get the semantic graphs of the sentences
        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
        List<String> graphs = new ArrayList<String>();

        for (CoreMap sentence : sentences) {
            SemanticGraph dependencies = sentence.get(CollapsedCCProcessedDependenciesAnnotation.class);
            String formattedString = dependencies.toFormattedString();

            // Make sure it's correctly wrapped
            if (!formattedString.startsWith("[")) {
                formattedString = "[" + formattedString + "]";
            }

            // Append the lemmas list
            String[] lemmas = new String[dependencies.size()];
            int i = 0;

            for (IndexedWord word : dependencies.vertexSet()) {
                lemmas[i++] = word.word() + "=" + word.lemma();
            }

            graphs.add(formattedString + "%" + Arrays.toString(lemmas));
        }

        // Set up caching
        CacheControl cacheControl = new CacheControl();
        cacheControl.setMaxAge(3 * 24 * 60 * 60); // 3 days

        // Build the response
        Response.ResponseBuilder builder = Response.ok(graphs);
        builder.cacheControl(cacheControl);

        System.out.println("/semgraph text=" + text + " output=" + Arrays.toString(graphs.toArray()));
        return builder.build();
    }

    private static class BackgroundStore implements Runnable {

        private final String text;

        public BackgroundStore(String text) {
            this.text = text;
        }

        @Override
        public void run() {
            // Check the database connection
            try {
                if (pgSqlConnection == null || pgSqlConnection.isClosed()) {
                    pgSqlConnection = DriverManager.getConnection("jdbc:postgresql://localhost/corenlp", "corenlp",
                            "corenlp");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            // Insert the input
            PreparedStatement statement = null;

            try {
                statement = pgSqlConnection.prepareStatement("INSERT INTO semgraphs (date, text) VALUES (?, ?)");
                statement.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
                statement.setString(2, text);
                statement.execute();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (statement != null) {
                        statement.close();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

    }

}
