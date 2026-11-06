package com.mucahid.mm_chat.util;

import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;

/**
 * Firebase Realtime Database / Storage path and key names used across the app.
 */
public final class Db {

    private static FirebaseDatabase database;
    private static FirebaseStorage storage;

    private Db() {
    }

    /**
     * The single database instance used by the whole app. Always use this instead of
     * FirebaseDatabase.getInstance(): once an instance configured with useEmulator() has been used,
     * getInstance() no longer finds it in the SDK cache and returns a second, unconfigured instance.
     */
    public static synchronized FirebaseDatabase database() {
        if (database == null) {
            database = FirebaseDatabase.getInstance();
        }
        return database;
    }

    /** The single storage instance used by the whole app; see {@link #database()}. */
    public static synchronized FirebaseStorage storage() {
        if (storage == null) {
            storage = FirebaseStorage.getInstance();
        }
        return storage;
    }

    // Root nodes
    public static final String USERS = "Users";
    /** GroupNames/{name}: creation timestamp. Kept apart from messages so listing groups stays cheap. */
    public static final String GROUP_NAMES = "GroupNames";
    /** Groups/{name}/{pushId}: group messages. */
    public static final String GROUPS = "Groups";
    public static final String MESSAGES = "Messages";
    /** Chats/{uid}/{otherUid}/lastMessageAt: conversations of a user, for the Chats tab (newest first). */
    public static final String CHATS = "Chats";
    public static final String LAST_MESSAGE_AT = "lastMessageAt";
    public static final String CONTACTS = "Contacts";
    public static final String CHAT_REQUESTS = "ChatRequests";

    // Users/{uid}
    public static final String UID = "uid";
    public static final String USERNAME = "username";
    public static final String FIRST_NAME = "firstName";
    public static final String LAST_NAME = "lastName";
    public static final String ABOUT = "about";
    public static final String IMAGE = "image";
    /** Base64 Tink public keyset for end-to-end encrypted private messages (see E2eeManager). */
    public static final String PUBLIC_KEY = "publicKey";

    // Users/{uid}/userState
    public static final String USER_STATE = "userState";
    public static final String STATE = "state";
    /** Server timestamp (ms) of the last online/offline change. */
    public static final String LAST_SEEN = "lastSeen";
    public static final String STATE_ONLINE = "online";
    public static final String STATE_OFFLINE = "offline";

    // ChatRequests/{uid}/{otherUid}/requestType
    public static final String REQUEST_TYPE = "requestType";
    public static final String REQUEST_SENT = "sent";
    public static final String REQUEST_RECEIVED = "received";

    // Contacts/{uid}/{otherUid}/status
    public static final String CONTACT_STATUS = "status";
    public static final String CONTACT_SAVED = "saved";

    // Messages/{senderUid}/{receiverUid}/{pushId}
    public static final String MESSAGE_TYPE_TEXT = "text";

    // Firebase Storage folder for profile images
    public static final String PROFILE_IMAGES = "profile_images";

    // Intent extras
    public static final String EXTRA_USER_ID = "user_id";
    public static final String EXTRA_USER_NAME = "user_name";
    public static final String EXTRA_USER_IMAGE = "user_image";
    public static final String EXTRA_GROUP_NAME = "group_name";
}
