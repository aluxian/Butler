package com.aluxian.butler.gcm;

import java.io.Serializable;

public final class AndroidMessage implements Serializable {

    /** The ID of the message, so that the Android client can match sent messages to their responses through GCM */
    public final String messageId;

    /** The ID of the device */
    public final String deviceId;

    /** The GCM ID of the device */
    public final String gcmId;

    /** Auth token sent by the Android client */
    public final String authToken;

    /** The {link Request} from the client */
    public final RequestType requestType;

    /** A {link RegisterRequest} object, if the {link #request} is {link RequestType.REGISTER} */
    public final RegisterRequest registerRequest;

    /** A {link FriendRequest} object, if the {link #request} is {link RequestType.FRIEND_REQUEST} */
    public final FriendRequest friendRequest;

    /** A {link FriendRequestResponse} object, if the {link #request} is {link RequestType.FRIEND_REQUEST_RESPONSE} */
    public final FriendRequestResponse friendRequestResponse;

    /** A {link TellMessage} object, if the {link #request} is {link RequestType.TELL_MESSAGE} */
    public final TellMessage tellMessage;

    /** A {link TrackEvent} object, if the {link #request} is {link RequestType.TRACK_MESSAGE} */
    public final TrackEvent trackEvent;

    public AndroidMessage(String messageId, String deviceId, String gcmId, String authToken, RequestType requestType,
                          RegisterRequest registerRequest, FriendRequest friendRequest,
                          FriendRequestResponse friendRequestResponse, TellMessage tellMessage, TrackEvent trackEvent) {
        this.messageId = messageId;
        this.deviceId = deviceId;
        this.gcmId = gcmId;
        this.authToken = authToken;
        this.requestType = requestType;
        this.registerRequest = registerRequest;
        this.friendRequest = friendRequest;
        this.friendRequestResponse = friendRequestResponse;
        this.tellMessage = tellMessage;
        this.trackEvent = trackEvent;
    }

    /**
     * Different request types
     */
    public static enum RequestType {
        REGISTER, FRIEND_REQUEST, FRIEND_REQUEST_RESPONSE, TELL_MESSAGE, TRACK_EVENT
    }

    public static interface RequestData {}

    /**
     * Object sent with {link RequestType.REGISTER} requests
     */
    public static final class RegisterRequest implements RequestData {

        /** The name of the device's owner */
        public final String ownerName;

        public RegisterRequest(String ownerName) {
            this.ownerName = ownerName;
        }

    }

    /**
     * Object sent with {link RequestType.FRIEND_REQUEST} requests
     */
    public static final class FriendRequest implements RequestData {

        public final String sourceOwnerName;
        public final String targetOwnerName;

        public FriendRequest(String sourceOwnerName, String targetOwnerName) {
            this.sourceOwnerName = sourceOwnerName;
            this.targetOwnerName = targetOwnerName;
        }

    }

    /**
     * Object sent with {link RequestType.FRIEND_REQUEST_RESPONSE} requests
     */
    public static final class FriendRequestResponse implements RequestData {

        public final String targetOwnerName;
        public final boolean response;

        public FriendRequestResponse(String targetOwnerName, boolean response) {
            this.targetOwnerName = targetOwnerName;
            this.response = response;
        }

    }

    /**
     * Object sent with {link RequestType.TELL_MESSAGE} requests
     */
    public static final class TellMessage implements RequestData {

        public final String sourceOwnerName;
        public final String targetOwnerName;
        public final String message;

        public TellMessage(String sourceOwnerName, String targetOwnerName, String message) {
            this.sourceOwnerName = sourceOwnerName;
            this.targetOwnerName = targetOwnerName;
            this.message = message;
        }

    }

    /**
     * Object sent with {link RequestType.TRACK_EVENT} requests
     */
    public static final class TrackEvent implements RequestData {

        public final String timestamp;
        public final String message;

        public TrackEvent(String timestamp, String message) {
            this.timestamp = timestamp;
            this.message = message;
        }

    }

}
