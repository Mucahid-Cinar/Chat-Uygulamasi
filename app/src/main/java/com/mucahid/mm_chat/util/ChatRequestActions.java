package com.mucahid.mm_chat.util;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;
import java.util.Map;

/**
 * Chat request / contact operations. Each one is a single atomic multi-path update,
 * so both users' nodes are always changed together.
 */
public final class ChatRequestActions {

    private ChatRequestActions() {
    }

    public static Task<Void> sendRequest(String fromUserId, String toUserId) {
        Map<String, Object> updates = new HashMap<>();
        updates.put(path(Db.CHAT_REQUESTS, fromUserId, toUserId, Db.REQUEST_TYPE), Db.REQUEST_SENT);
        updates.put(path(Db.CHAT_REQUESTS, toUserId, fromUserId, Db.REQUEST_TYPE), Db.REQUEST_RECEIVED);
        return root().updateChildren(updates);
    }

    /** Cancels a sent request or declines a received one. */
    public static Task<Void> cancelRequest(String userId, String otherUserId) {
        Map<String, Object> updates = new HashMap<>();
        updates.put(path(Db.CHAT_REQUESTS, userId, otherUserId), null);
        updates.put(path(Db.CHAT_REQUESTS, otherUserId, userId), null);
        return root().updateChildren(updates);
    }

    public static Task<Void> acceptRequest(String userId, String otherUserId) {
        Map<String, Object> updates = new HashMap<>();
        updates.put(path(Db.CONTACTS, userId, otherUserId, Db.CONTACT_STATUS), Db.CONTACT_SAVED);
        updates.put(path(Db.CONTACTS, otherUserId, userId, Db.CONTACT_STATUS), Db.CONTACT_SAVED);
        updates.put(path(Db.CHAT_REQUESTS, userId, otherUserId), null);
        updates.put(path(Db.CHAT_REQUESTS, otherUserId, userId), null);
        return root().updateChildren(updates);
    }

    /**
     * Removes the contact for both users and deletes this user's copy of the conversation (and its
     * entry in the Chats tab).
     * The other user's copy is left untouched.
     */
    public static Task<Void> removeContact(String userId, String otherUserId) {
        Map<String, Object> updates = new HashMap<>();
        updates.put(path(Db.CONTACTS, userId, otherUserId), null);
        updates.put(path(Db.CONTACTS, otherUserId, userId), null);
        updates.put(path(Db.MESSAGES, userId, otherUserId), null);
        updates.put(path(Db.CHATS, userId, otherUserId), null);
        return root().updateChildren(updates);
    }

    /**
     * Deletes only this user's copy of a conversation and its Chats entry, e.g. after the other
     * person removed them from contacts (removeContact is then no longer possible).
     */
    public static Task<Void> deleteConversation(String userId, String otherUserId) {
        Map<String, Object> updates = new HashMap<>();
        updates.put(path(Db.MESSAGES, userId, otherUserId), null);
        updates.put(path(Db.CHATS, userId, otherUserId), null);
        return root().updateChildren(updates);
    }

    private static DatabaseReference root() {
        return Db.database().getReference();
    }

    private static String path(String... parts) {
        return String.join("/", parts);
    }
}
