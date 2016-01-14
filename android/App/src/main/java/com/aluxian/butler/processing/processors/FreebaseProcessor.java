package com.aluxian.butler.processing.processors;

import com.activeandroid.query.Select;
import com.aluxian.butler.MainActivity;
import com.aluxian.butler.apis.corenlp.CoreNlpService;
import com.aluxian.butler.apis.freebase.FreebaseService;
import com.aluxian.butler.apis.freebase.LookupResult;
import com.aluxian.butler.apis.freebase.SearchResponse;
import com.aluxian.butler.database.models.FreebaseQuestionMap;
import com.aluxian.butler.processing.PatternMatcher;
import com.aluxian.butler.processing.ProcessingUtils;
import com.aluxian.butler.processing.Processor;
import com.aluxian.butler.processing.ProcessorOutput;
import com.aluxian.butler.processing.UserInput;
import com.aluxian.butler.recycler.ConversationItem;
import com.aluxian.butler.recycler.items.FreebaseItem;
import com.aluxian.butler.recycler.items.TextItem;
import com.aluxian.butler.utils.Constants;
import com.aluxian.butler.utils.Logger;

import java.util.List;

import retrofit.RetrofitError;

/**
 * The FreebaseProcessor searches for an answer to the user input using the Freebase API
 */
public class FreebaseProcessor extends Processor {

    /** Logger instance */
    @SuppressWarnings("UnusedDeclaration") private static final Logger LOG = new Logger(FreebaseProcessor.class);

    public FreebaseProcessor(MainActivity mainActivity) {
        super(mainActivity);
    }

    @Override
    public ProcessorOutput process(UserInput userInput) {
        List<FreebaseQuestionMap> questionMaps = new Select().from(FreebaseQuestionMap.class).execute();

        // Try each map
        return new PatternMatcher<FreebaseQuestionMap>(userInput) {
            @Override
            protected ProcessorOutput matchesRegex(FreebaseQuestionMap questionMap) {
                String query = questionMap.query != null ? regexMatcher.replaceFirst(questionMap.query) : "";
                String filter = questionMap.filter != null ? regexMatcher.replaceFirst(questionMap.filter) : "";

                ConversationItem item = searchFreebase(query, filter, questionMap.minScore);

                if (item != null) {
                    return new ProcessorOutput(item);
                }

                return null;
            }

            @Override
            protected ProcessorOutput matchesSemgrex(FreebaseQuestionMap questionMap) {
                String query = questionMap.query != null ?
                        ProcessingUtils.interpolateValuesFromSemGraph(questionMap.query, semgrexMatcher) : "";
                String filter = questionMap.filter != null ?
                        ProcessingUtils.interpolateValuesFromSemGraph(questionMap.filter, semgrexMatcher) : "";

                ConversationItem item = searchFreebase(query, filter, questionMap.minScore);

                if (item != null) {
                    return new ProcessorOutput(item);
                }

                return null;
            }
        }.match(questionMaps);
    }

    /**
     * Searches Freebase for the provided query, using the provided filters
     *
     * @param query    Query to search
     * @param filter   Query filter to apply
     * @param minScore The minimum score for a Freebase result to be considered valid
     * @return A {@link ConversationItem}
     */
    private ConversationItem searchFreebase(String query, String filter, int minScore) {
        SearchResponse freebaseResponse;

        try {
            freebaseResponse = FreebaseService.api.search(query, filter);
        } catch (RetrofitError e) {
            return new TextItem("I'm sorry, I could not connect to the Freebase API", true);
        }

        // If there's at least one result
        if (freebaseResponse.result.size() > 0) {
            SearchResponse.SearchResult searchResult = freebaseResponse.result.get(0);

            // And that result is accurate (has a good score)
            if (searchResult.score > minScore) {
                // Create a new Freebase list item
                LookupResult lookupResult = FreebaseService.api.lookup(searchResult.mid.substring(1));

                // Build the image url
                LookupResult.LookupValues image = lookupResult.property.topicImage;
                String imageUrl = null;

                if (image != null) {
                    String params = "?maxwidth=1000&maxheight=450&mode=fillcropmid";
                    String imageId = image.values.get(0).id;
                    imageUrl = Constants.FREEBASE_USERCONTENT_URL + "/image" + imageId + params;
                }

                // Get the description and the first sentence as the speakable text
                String description;

                if (lookupResult.property.topicDescription != null) {
                    description = (String) lookupResult.property.topicDescription.values.get(0).value;
                } else {
                    return null;
                }

                String title = searchResult.name;
                String speakableText = CoreNlpService.api.getSentences(description).get(0);

                return new FreebaseItem(title, description, imageUrl, speakableText);
            }
        }

        return null;
    }

}
