package com.aluxian.butler.main;

import android.os.AsyncTask;
import android.os.Handler;

import com.aluxian.butler.MainActivity;
import com.aluxian.butler.processing.ProcessorOutput;
import com.aluxian.butler.processing.UserInput;
import com.aluxian.butler.recycler.ConversationItem;
import com.aluxian.butler.recycler.items.TextItem;
import com.aluxian.butler.utils.Constants;
import com.aluxian.butler.utils.Logger;
import com.aluxian.butler.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Asynchronously process the spoken text from the user then add the resulting item to the conversation list
 */
public class ProcessInputTask extends AsyncTask<String, Void, ProcessorOutput> {

    /** Logger instance */
    @SuppressWarnings("UnusedDeclaration") private static final Logger LOG = new Logger(ProcessInputTask.class);

    /** MainActivity instance */
    private final MainActivity mainActivity;

    /** Timestamp when the task started */
    private long startedAt;

    /** The input being processed */
    private String input;

    public ProcessInputTask(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Override
    protected void onPreExecute() {
        startedAt = System.currentTimeMillis();

        // A delay is necessary to prevent the item adding animation not happening
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Create a new temporary item
                        mainActivity.temporaryConversationItem = new TextItem("...", true);
                        mainActivity.conversationRecyclerAdapter.addItem(mainActivity.temporaryConversationItem);
                        //temporaryConversationItem.setShouldAnimate(true);
                    }
                });
            }
        }, 100);
    }

    @Override
    protected ProcessorOutput doInBackground(String... params) {
        input = params[0];
        ProcessorOutput processorOutput = mainActivity.mainProcessor.process(new UserInput(input));

        // Delay the task so the loading indicator doesn't flash out too soon
        while (System.currentTimeMillis() - startedAt < Constants.MIN_ANSWER_DELAY) {
            if (isCancelled()) {
                return null;
            }

            try {
                Thread.sleep(100);
            } catch (InterruptedException ignored) {}
        }

        return processorOutput;
    }

    @Override
    public void onPostExecute(ProcessorOutput processorOutput) {
        mainActivity.processorOutput = processorOutput;

        // Remove the temporary item
        //temporaryConversationItem.stopAnimation();
        mainActivity.conversationRecyclerAdapter.removeItem(mainActivity.temporaryConversationItem);
        mainActivity.temporaryConversationItem = null;

        mainActivity.processorOutputQueue.add(processorOutput);

        // Make sure the overscroll colour is correct
        mainActivity.changeOverscrollColour();

        // Track this event with Mixpanel
        try {
            ConversationItem lastItem = processorOutput.items.get(processorOutput.items.size() - 1);

            JSONObject props = new JSONObject();
            props.put("User Input", input);
            props.put("Answer", processorOutput.items.get(0).getSpeakable());
            props.put("Prompt for Input", processorOutput.promptForInput);
            props.put("Processor Output", Utils.gson().toJson(processorOutput));
            props.put("Answer Type", lastItem.getClass().getSimpleName());

            mainActivity.mixpanel.track("Assistant Answer", props);
        } catch (JSONException e) {
            LOG.e(e);
        }
    }

    public String getInput() {
        return input;
    }

}
