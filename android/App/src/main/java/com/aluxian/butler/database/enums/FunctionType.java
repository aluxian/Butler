package com.aluxian.butler.database.enums;

/**
 * Command functions that the assistant can do
 */
public enum FunctionType {
    /**
     * Call a person
     *
     * @parameter The person's name or number
     */
    CALL,

    /**
     * Send a friend request to someone's assistant
     *
     * @parameter The name of the person
     */
    FRIEND_REQUEST,

    /**
     * Respond to a friend request
     *
     * @parameter Message ID
     * @parameter Friend name
     * @parameter True/False to accept/decline
     */
    FRIEND_REQUEST_RESPOND,

    /**
     * Remember something
     *
     * @parameter The name of the thing to remember
     * @parameter What to remember
     */
    REMEMBER,

    /**
     * Control the ringer mode of the phone
     *
     * @parameter NORMAL/SILENT/VIBRATE
     */
    RINGER_MODE,

    /**
     * Speak something
     *
     * @parameter What to say
     */
    SAY,

    /**
     * Upload the logcat to Crashlytics
     */
    SEND_LOGCAT,

    /**
     * Send a message through the assistant
     *
     * @parameter A friend's name
     */
    TELL,

    /**
     * The body of the tell message
     *
     * @parameter Message body
     * @parameter The friend's name
     */
    TELL_MSG,

    /**
     * Send a text message
     *
     * @parameter The person's name or number to send it to
     */
    TEXT,

    /**
     * The text's body
     *
     * @parameter Message body
     * @parameter The person's number to send it to
     */
    TEXT_MSG,

    /**
     * Enable/disable logcat logging
     *
     * @parameter ON/OFF
     */
    TOGGLE_LOGGING
}
