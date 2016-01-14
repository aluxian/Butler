package com.aluxian.butler.database.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.aluxian.butler.gcm.AndroidMessage;

/**
 * Models which stores messages sent and received from the cloud through GCM.
 */
@Table(name = "GCM_MESSAGES")
public class GcmMessage extends Model {

    /** The RequestType of the message */
    @Column(name = "Type")
    public AndroidMessage.RequestType type;

    /** Message sent or received */
    @Column(name = "Direction")
    public MessageDirection direction;

    /** Request data */
    @Column(name = "Data")
    public String data;

    @SuppressWarnings("UnusedDeclaration")
    public GcmMessage() {
        super();
    }

    public GcmMessage(AndroidMessage.RequestType type, MessageDirection direction) {
        super();
        this.type = type;
        this.direction = direction;
    }

    public static enum MessageDirection {
        SENT, RECEIVED
    }

}
