package com.aluxian.butler.corenlpserver.services;

import com.aluxian.butler.corenlpserver.CoreNLP;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;

import javax.ws.rs.*;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Path("/sentiment")
@Produces(MediaType.APPLICATION_JSON)
public class SentimentService {

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response post(@FormParam("text") String text) {
        // Annotate the input
        Annotation document = new Annotation(text);
        CoreNLP.instance().stanfordCoreNLP.annotate(document);

        // Get the sentences and analyze them
        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
        List<String> analyzedSentences = new ArrayList<String>();

        for (CoreMap sentence : sentences) {
            analyzedSentences.add(sentence.get(SentimentCoreAnnotations.ClassName.class));
        }

        // Set up caching
        CacheControl cacheControl = new CacheControl();
        cacheControl.setMaxAge(3 * 24 * 60 * 60); // 3 days

        // Build the response
        Response.ResponseBuilder builder = Response.ok(analyzedSentences);
        builder.cacheControl(cacheControl);

        System.out.println("/sentiment text=" + text + " output=" + Arrays.toString(analyzedSentences.toArray()));
        return builder.build();
    }

}
