package com.mucahid.mm_chat.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.crypto.tink.KeysetHandle;
import com.google.crypto.tink.integration.android.AndroidKeysetManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * Owns this device's private key for the signed-in user and publishes the matching public key to
 * Users/{uid}/publicKey.
 *
 * <p>The private keyset is stored in SharedPreferences ({@value #KEYSET_PREF_FILE}), encrypted with a
 * master key that lives in the Android Keystore, so it never leaves the device; the file is also
 * excluded from backups (res/xml/backup_rules.xml, data_extraction_rules.xml). Each account gets its
 * own keyset, because several accounts can sign in on one device.
 *
 * <p>Publishing: the device the user signed in on most recently owns the account's key. The key is
 * published after a fresh sign-in, when the server has none, or when this device had to create a
 * new key pair - not on every launch, so two devices on one account do not keep overwriting each
 * other's key. Messages encrypted for another device's key cannot be read here (by design).
 */
public final class E2eeManager {

    private static final String TAG = "E2eeManager";
    private static final String MASTER_KEY_URI = "android-keystore://me_chat_e2ee_master_key";
    private static final String KEYSET_PREF_FILE = "me_chat_e2ee";
    private static final String STATE_PREF_FILE = "me_chat_e2ee_state";

    private static String loadedUserId;
    private static KeysetHandle privateKeyset;

    private E2eeManager() {
    }

    private static synchronized KeysetHandle privateKeyset(Context context, String userId)
            throws GeneralSecurityException, IOException {
        if (!userId.equals(loadedUserId)) {
            Context appContext = context.getApplicationContext();
            String keysetName = "keyset_" + userId;
            try {
                privateKeyset = buildKeyset(appContext, keysetName);
            } catch (GeneralSecurityException | IOException e) {
                // The stored keyset cannot be decrypted (e.g. the Keystore master key was lost). Start
                // over with a new key pair; messages for the old key are unreadable either way.
                Log.w(TAG, "Stored keyset is unusable, creating a new key pair", e);
                appContext.getSharedPreferences(KEYSET_PREF_FILE, Context.MODE_PRIVATE)
                        .edit().remove(keysetName).commit();
                privateKeyset = buildKeyset(appContext, keysetName);
                requestPublish(appContext, userId);
            }
            loadedUserId = userId;
        }
        return privateKeyset;
    }

    private static KeysetHandle buildKeyset(Context appContext, String keysetName)
            throws GeneralSecurityException, IOException {
        return new AndroidKeysetManager.Builder()
                .withSharedPref(appContext, keysetName, KEYSET_PREF_FILE)
                .withKeyTemplate(MessageCrypto.keyTemplate())
                .withMasterKeyUri(MASTER_KEY_URI)
                .build()
                .getKeysetHandle();
    }

    /** This user's public key on this device, or null if the key store is unavailable. */
    @Nullable
    public static String publicKey(Context context, String userId) {
        try {
            return MessageCrypto.exportPublicKey(privateKeyset(context, userId));
        } catch (GeneralSecurityException | IOException e) {
            Log.e(TAG, "Could not load the key pair", e);
            return null;
        }
    }

    /** Call after a successful sign-in or registration: this device becomes the account's active device. */
    public static void onSignedIn(Context context, String userId) {
        requestPublish(context.getApplicationContext(), userId);
    }

    /**
     * Publishes this device's public key if a sign-in or a new key pair asked for it, or if the
     * server has no key for the user yet. Otherwise leaves the server key alone (it may belong to
     * the user's newer device).
     */
    public static void publishPublicKeyIfNeeded(Context context, String userId) {
        Context appContext = context.getApplicationContext();
        String publicKey = publicKey(appContext, userId);
        if (publicKey == null) {
            return;
        }
        DatabaseReference keyRef = Db.database().getReference().child(Db.USERS).child(userId).child(Db.PUBLIC_KEY);
        keyRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String serverKey = snapshot.getValue(String.class);
                if (publicKey.equals(serverKey)) {
                    clearPublishRequest(appContext, userId);
                } else if (serverKey == null || isPublishRequested(appContext, userId)) {
                    keyRef.setValue(publicKey).addOnSuccessListener(unused -> clearPublishRequest(appContext, userId));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    /** The plain text, or null if the message was not encrypted for this device's key. */
    @Nullable
    public static String decrypt(Context context, String userId, String ciphertext, String messageId, String senderId) {
        try {
            return MessageCrypto.decrypt(privateKeyset(context, userId), ciphertext, messageId, senderId);
        } catch (GeneralSecurityException | IOException e) {
            Log.w(TAG, "Message " + messageId + " cannot be decrypted on this device");
            return null;
        }
    }

    private static SharedPreferences state(Context appContext) {
        return appContext.getSharedPreferences(STATE_PREF_FILE, Context.MODE_PRIVATE);
    }

    private static void requestPublish(Context appContext, String userId) {
        state(appContext).edit().putBoolean("publish_" + userId, true).apply();
    }

    private static boolean isPublishRequested(Context appContext, String userId) {
        return state(appContext).getBoolean("publish_" + userId, false);
    }

    private static void clearPublishRequest(Context appContext, String userId) {
        state(appContext).edit().remove("publish_" + userId).apply();
    }
}
