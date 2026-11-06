package com.mucahid.mm_chat.util;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;
import java.util.Map;

/**
 * Keeps Users/{uid}/userState in sync with whether the app (not a single screen) is in the
 * foreground. onDisconnect() marks the user offline on the server if the app crashes or loses
 * its connection.
 */
public final class PresenceManager implements DefaultLifecycleObserver {

    @Override
    public void onStart(@NonNull LifecycleOwner owner) {
        goOnline();
    }

    @Override
    public void onStop(@NonNull LifecycleOwner owner) {
        goOffline();
    }

    /** Safe to call repeatedly, e.g. right after signing in. */
    public static void goOnline() {
        DatabaseReference stateRef = userStateRef();
        if (stateRef == null) {
            return;
        }
        stateRef.onDisconnect().setValue(state(Db.STATE_OFFLINE));
        stateRef.setValue(state(Db.STATE_ONLINE));
    }

    public static Task<Void> goOffline() {
        DatabaseReference stateRef = userStateRef();
        if (stateRef == null) {
            return Tasks.forResult(null);
        }
        return stateRef.setValue(state(Db.STATE_OFFLINE));
    }

    private static DatabaseReference userStateRef() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            return null;
        }
        return Db.database().getReference()
                .child(Db.USERS).child(user.getUid()).child(Db.USER_STATE);
    }

    private static Map<String, Object> state(String state) {
        Map<String, Object> userState = new HashMap<>();
        userState.put(Db.STATE, state);
        userState.put(Db.LAST_SEEN, ServerValue.TIMESTAMP);
        return userState;
    }
}
