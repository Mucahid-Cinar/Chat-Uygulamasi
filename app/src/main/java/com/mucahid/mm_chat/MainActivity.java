package com.mucahid.mm_chat;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.mucahid.mm_chat.adapter.TabsPagerAdapter;
import com.mucahid.mm_chat.util.Db;
import com.mucahid.mm_chat.util.E2eeManager;
import com.mucahid.mm_chat.util.PresenceManager;

import java.util.concurrent.atomic.AtomicBoolean;

public class MainActivity extends AppCompatActivity {

    private static final long LOGOUT_TIMEOUT_MS = 2000;

    private TabLayout tabLayout;

    private FirebaseAuth auth;
    private DatabaseReference rootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.app_name);
        getSupportActionBar().setSubtitle("Sohbete yer aç.");

        auth = FirebaseAuth.getInstance();
        rootRef = Db.database().getReference();

        ViewPager2 viewPager = findViewById(R.id.main_pager);
        viewPager.setAdapter(new TabsPagerAdapter(this));
        viewPager.setPageTransformer((page, position) -> page.setAlpha(1f - Math.min(Math.abs(position), 1f) * 0.25f));

        tabLayout = findViewById(R.id.main_tabs);
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    String[] labels = {"Sohbetler", "Gruplar", "Kişiler", "İstekler"};
                    int[] icons = {R.drawable.ic_baseline_message_24, R.drawable.ic_baseline_group_24, R.drawable.ic_baseline_person_24, R.drawable.ic_baseline_notifications_active_24};
                    tab.setText(labels[position]);
                    tab.setIcon(icons[position]);
                    tab.setContentDescription(labels[position]);
                }).attach();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            sendUserToLoginActivity();
        } else {
            // The app-wide PresenceManager handles foreground/background; this covers a fresh sign-in.
            PresenceManager.goOnline();
            // Creates this device's key pair on first use and publishes the public key when needed.
            E2eeManager.publishPublicKeyIfNeeded(this, currentUser.getUid());
            verifyUserProfile(currentUser.getUid());
        }
    }

    /** Sends the user to the settings screen until a username has been saved. */
    private void verifyUserProfile(String userId) {
        rootRef.child(Db.USERS).child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.child(Db.USERNAME).exists()) {
                    Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
                    settingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(settingsIntent);
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Profil yüklenemedi. Bağlantınızı kontrol edin.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void sendUserToLoginActivity() {
        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.menu_find_friends) {
            startActivity(new Intent(MainActivity.this, FindFriendsActivity.class));
            return true;
        }
        if (itemId == R.id.menu_create_group) {
            showCreateGroupDialog();
            return true;
        }
        if (itemId == R.id.menu_settings) {
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            return true;
        }
        if (itemId == R.id.menu_logout) {
            logout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Writes the offline state while still authenticated, then signs out. The write only completes
     * once the server confirms it, so offline we stop waiting after a moment (the server-side
     * onDisconnect marks the user offline anyway).
     */
    private void logout() {
        AtomicBoolean signedOut = new AtomicBoolean(false);
        Runnable signOut = () -> {
            if (signedOut.compareAndSet(false, true)) {
                auth.signOut();
                sendUserToLoginActivity();
            }
        };
        PresenceManager.goOffline().addOnCompleteListener(task -> signOut.run());
        new Handler(Looper.getMainLooper()).postDelayed(signOut, LOGOUT_TIMEOUT_MS);
    }

    private void showCreateGroupDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_create_group, null);
        Button cancelButton = dialogView.findViewById(R.id.cancel_button);
        Button createButton = dialogView.findViewById(R.id.create_button);
        EditText groupNameInput = dialogView.findViewById(R.id.group_name_input);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setCancelable(false)
                .setView(dialogView)
                .create();
        dialog.show();

        cancelButton.setOnClickListener(v -> {
            hideKeyboard(groupNameInput);
            dialog.cancel();
        });

        createButton.setOnClickListener(v -> {
            String groupName = groupNameInput.getText().toString().trim();

            if (TextUtils.isEmpty(groupName)) {
                Toast.makeText(MainActivity.this, "Grup İsmini Giriniz !", Toast.LENGTH_LONG).show();
                return;
            }
            if (groupName.length() > 60 || !isValidDatabaseKey(groupName)) {
                Toast.makeText(MainActivity.this, "Grup ismi . # $ [ ] / karakterlerini içeremez", Toast.LENGTH_LONG).show();
                return;
            }

            createButton.setEnabled(false);
            groupNameInput.setEnabled(false);
            createNewGroup(groupName, dialog, createButton, groupNameInput);
            hideKeyboard(groupNameInput);
        });
    }

    /** Must run while the view is still attached, i.e. before the dialog is dismissed. */
    private void hideKeyboard(View view) {
        InputMethodManager imm = getSystemService(InputMethodManager.class);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    /** Firebase keys cannot contain '.', '#', '$', '[', ']' or '/'. */
    private static boolean isValidDatabaseKey(String key) {
        return !key.matches("(?s).*[.#$\\[\\]/\\x00-\\x1F\\x7F].*");
    }

    /** Creates the group atomically; an existing group with the same name is never overwritten. */
    private void createNewGroup(String groupName, AlertDialog dialog, Button createButton, EditText groupNameInput) {
        rootRef.child(Db.GROUP_NAMES).child(groupName).runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                if (currentData.getValue() != null) {
                    return Transaction.abort();
                }
                currentData.setValue(ServerValue.TIMESTAMP);
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot snapshot) {
                if (isFinishing() || isDestroyed()) return;
                createButton.setEnabled(true);
                groupNameInput.setEnabled(true);
                if (committed) {
                    dialog.dismiss();
                    tabLayout.selectTab(tabLayout.getTabAt(TabsPagerAdapter.TAB_GROUPS));
                    Toast.makeText(MainActivity.this, groupName + " Adlı Grup Oluşturuldu", Toast.LENGTH_LONG).show();
                } else if (error == null) {
                    groupNameInput.setError("Bu isimde bir grup zaten var");
                } else {
                    groupNameInput.setError("Grup oluşturulamadı. Lütfen tekrar dene.");
                }
            }
        });
    }
}
