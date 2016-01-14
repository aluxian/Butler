package com.aluxian.butler.corenlpserver.services;

import com.aluxian.butler.corenlpserver.CoreNLP;
import edu.stanford.nlp.ie.machinereading.structure.MachineReadingAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;

import javax.ws.rs.*;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

@Path("/gender")
@Produces(MediaType.APPLICATION_JSON)
public class GenderService {

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response post(@FormParam("text") String text) {
        // Annotate the input
        Annotation document = new Annotation(text);
        CoreNLP.instance().stanfordCoreNLP.annotate(document);

        // Get the semantic graphs of the sentences
        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
        List<List<String>> gendersList = new ArrayList<List<String>>();

        for (CoreMap sentence : sentences) {
            List<CoreLabel> tokens = sentence.get(CoreAnnotations.TokensAnnotation.class);
            List<String> tokensGenders = new ArrayList<String>();

            for (CoreLabel token : tokens) {
                tokensGenders.add(token.get(MachineReadingAnnotations.GenderAnnotation.class));
            }

            gendersList.add(tokensGenders);
        }

        // Set up caching
        CacheControl cacheControl = new CacheControl();
        cacheControl.setMaxAge(3 * 24 * 60 * 60); // 3 days

        // Build the response
        Response.ResponseBuilder builder = Response.ok(gendersList);
        builder.cacheControl(cacheControl);

        System.out.println("/semgraph text=" + text + " output=" + gendersList);
        return builder.build();
    }

}
