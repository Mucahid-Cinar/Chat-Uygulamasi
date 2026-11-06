package com.mucahid.mm_chat.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Remembers, on this device, which public key we have seen and which one we have verified for each
 * contact (trust on first use). Used to warn when a contact's key changes, which happens when they
 * reinstall the app or change phones, but could also mean someone swapped the key.
 */
public final class ContactKeyStore {

    public enum KeyStatus {
        /** First time this contact's key is seen; it is now remembered. */
        FIRST_SEEN,
        /** Same key as before. */
        UNCHANGED,
        /** The key differs from the remembered one and the user has not acknowledged it yet. */
        CHANGED
    }

    private static final String PREF_FILE = "me_chat_contact_keys";

    private final SharedPreferences prefs;
    private final String ownerId;

    /** @param ownerId the signed-in user; several accounts can share one device. */
    public ContactKeyStore(Context context, String ownerId) {
        this.prefs = context.getApplicationContext().getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);
        this.ownerId = ownerId;
    }

    public KeyStatus check(String contactId, String publicKey) {
        String seen = prefs.getString(seenKey(contactId), null);
        if (seen == null) {
            prefs.edit().putString(seenKey(contactId), publicKey).apply();
            return KeyStatus.FIRST_SEEN;
        }
        return seen.equals(publicKey) ? KeyStatus.UNCHANGED : KeyStatus.CHANGED;
    }

    /** True if the contact had been verified with a key that has since changed. */
    public boolean wasVerifiedWithOtherKey(String contactId, String publicKey) {
        String verified = prefs.getString(verifiedKey(contactId), null);
        return verified != null && !verified.equals(publicKey);
    }

    /** Accepts a changed key (clears an old verification, which no longer applies). */
    public void acknowledge(String contactId, String publicKey) {
        SharedPreferences.Editor editor = prefs.edit().putString(seenKey(contactId), publicKey);
        if (wasVerifiedWithOtherKey(contactId, publicKey)) {
            editor.remove(verifiedKey(contactId));
        }
        editor.apply();
    }

    public boolean isVerified(String contactId, String publicKey) {
        return publicKey.equals(prefs.getString(verifiedKey(contactId), null));
    }

    public void setVerified(String contactId, String publicKey, boolean verified) {
        SharedPreferences.Editor editor = prefs.edit().putString(seenKey(contactId), publicKey);
        if (verified) {
            editor.putString(verifiedKey(contactId), publicKey);
        } else {
            editor.remove(verifiedKey(contactId));
        }
        editor.apply();
    }

    private String seenKey(String contactId) {
        return ownerId + "|" + contactId + "|seen";
    }

    private String verifiedKey(String contactId) {
        return ownerId + "|" + contactId + "|verified";
    }
}
