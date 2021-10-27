package com.smartshehar;

/**
 * Created by ctemkar on 03/05/2016.
 * Calls to php for chat
 */
public class EndPoints {

    // localhost url -
    public static final String BASE_URL = "http://smartshehar.com/alpha/smartsheharapp/v16/sschat/v1";
    public static final String LOGIN = BASE_URL + "/user/login";
    public static final String USER = BASE_URL + "/user/_ID_";
    public static final String CHAT_ROOMS = BASE_URL + "/chat_rooms";
    public static final String CHAT_THREAD = BASE_URL + "/chat_rooms/_ID_";
    public static final String CHAT_ROOM_MESSAGE = BASE_URL + "/chat_rooms/_ID_/message";
}

