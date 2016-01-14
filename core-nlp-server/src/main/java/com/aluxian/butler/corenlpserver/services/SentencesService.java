package com.aluxian.butler.corenlpserver.services;

import com.aluxian.butler.corenlpserver.CoreNLP;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;

import javax.ws.rs.*;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Path("/sentences")
@Produces(MediaType.APPLICATION_JSON)
public class SentencesService {

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response post(@FormParam("text") String text, @QueryParam("limit") int limit) {
        // Annotate the input
        Annotation document = new Annotation(text);
        CoreNLP.instance().stanfordCoreNLP.annotate(document);

        // Convert the resulting CoreMaps into text
        List<CoreMap> coreMaps = document.get(CoreAnnotations.SentencesAnnotation.class);
        List<String> sentences = new ArrayList<String>();

        for (CoreMap coreMap : coreMaps) {
            sentences.add(coreMap.get(CoreAnnotations.TextAnnotation.class));

            if (sentences.size() == limit) {
                break;
            }
        }

        // Set up caching
        CacheControl cacheControl = new CacheControl();
        cacheControl.setMaxAge(3 * 24 * 60 * 60); // 3 days

        // Build the response
        Response.ResponseBuilder builder = Response.ok(sentences);
        builder.cacheControl(cacheControl);

        System.out.println("/sentences text=" + text + " limit=" + limit + " output="
                + Arrays.toString(sentences.toArray()));
        return builder.build();
    }

}
