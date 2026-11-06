package com.mucahid.mm_chat;

import android.app.Application;

import androidx.lifecycle.ProcessLifecycleOwner;

import com.google.firebase.auth.FirebaseAuth;
import com.mucahid.mm_chat.util.Db;
import com.mucahid.mm_chat.util.PresenceManager;

public class MeChatApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.USE_FIREBASE_EMULATOR) {
            connectToFirebaseEmulators();
        }
        ProcessLifecycleOwner.get().getLifecycle().addObserver(new PresenceManager());
    }

    /** Must run before any other Firebase call. Debug builds only; see EMULATOR.md. */
    private static void connectToFirebaseEmulators() {
        String host = BuildConfig.FIREBASE_EMULATOR_HOST;
        FirebaseAuth.getInstance().useEmulator(host, 9099);
        Db.database().useEmulator(host, 9000);
        Db.storage().useEmulator(host, 9199);
    }
}
