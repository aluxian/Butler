package com.aluxian.butler.gcm;

import java.io.Serializable;

public final class GcmServerMessage implements Serializable {

    /** The ID of the message, so that the Android client can match sent messages to their responses through GCM */
    public final String messageId;

    /** The type of the message */
    public final Type type;

    /** Content to send */
    public final Object content;

    /** The GCM ID of the device to send this to */
    public final String targetGcmId;

    /** Auth token sent by the Android client */
    public final String authToken;

    public GcmServerMessage(String messageId, Type type, Object content, String targetGcmId) {
        this.messageId = messageId;
        this.type = type;
        this.content = content;
        this.targetGcmId = targetGcmId;
        this.authToken = null;
    }

    /**
     * Different request types
     */
    public static enum Type {
        REGISTER_RESPONSE, FRIEND_REQUEST, FRIEND_REQUEST_RESPONSE, FRIEND_REQUEST_NOT_FOUND, TELL_MESSAGE, PREDICTION
    }

}
