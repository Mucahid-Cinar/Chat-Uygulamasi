package com.mucahid.mm_chat;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.mucahid.mm_chat.databinding.ActivityProfileBinding;
import com.mucahid.mm_chat.model.User;
import com.mucahid.mm_chat.util.ChatRequestActions;
import com.mucahid.mm_chat.util.Db;
import com.mucahid.mm_chat.util.ImageLoader;

public class ProfileActivity extends AppCompatActivity {

    private enum RelationState { NEW, REQUEST_SENT, REQUEST_RECEIVED, FRIENDS }

    private ActivityProfileBinding binding;
    private DatabaseReference rootRef;

    private DatabaseReference requestRef;
    private DatabaseReference contactRef;
    private ValueEventListener requestListener;
    private ValueEventListener contactListener;

    private String currentUserId;
    private String profileUserId;
    private RelationState state = RelationState.NEW;

    // Latest values from the two listeners; the buttons stay disabled until both have arrived.
    private boolean requestLoaded;
    private boolean contactLoaded;
    private String requestType;
    private boolean isContact;
    private boolean actionInProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.profileToolbar.getRoot().setTitle("Profil");
        binding.profileToolbar.getRoot().setNavigationIcon(R.drawable.ic_baseline_arrow_back_24);
        binding.profileToolbar.getRoot().setNavigationOnClickListener(v -> finish());

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        profileUserId = getIntent().getStringExtra(Db.EXTRA_USER_ID);
        if (currentUser == null || TextUtils.isEmpty(profileUserId)) {
            finish();
            return;
        }

        rootRef = Db.database().getReference();
        currentUserId = currentUser.getUid();

        loadUser();

        if (currentUserId.equals(profileUserId)) {
            binding.messageRequestButton.setVisibility(View.GONE);
        } else {
            binding.messageRequestButton.setEnabled(false);
            binding.messageRequestButton.setOnClickListener(v -> onRequestButtonClicked());
            binding.declineMessageRequestButton.setOnClickListener(v ->
                    runAction(ChatRequestActions.cancelRequest(currentUserId, profileUserId), null));
            listenRelation();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (requestRef != null && requestListener != null) {
            requestRef.removeEventListener(requestListener);
        }
        if (contactRef != null && contactListener != null) {
            contactRef.removeEventListener(contactListener);
        }
    }

    private void loadUser() {
        rootRef.child(Db.USERS).child(profileUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.hasChildren()) {
                    return;
                }
                User user = snapshot.getValue(User.class);
                if (user == null) {
                    return;
                }
                ImageLoader.loadProfileImage(user.getImage(), binding.userPhoto);
                binding.userFirstNameDisplay.setText(user.getFirstName());
                binding.userLastNameDisplay.setText(user.getLastName());
                binding.userNameDisplay.setText(user.getUsername());
                binding.userStatusDisplay.setText(user.getAbout());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void listenRelation() {
        requestRef = rootRef.child(Db.CHAT_REQUESTS).child(currentUserId).child(profileUserId);
        requestListener = requestRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                requestType = snapshot.child(Db.REQUEST_TYPE).getValue(String.class);
                requestLoaded = true;
                render();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showLoadError();
            }
        });

        contactRef = rootRef.child(Db.CONTACTS).child(currentUserId).child(profileUserId);
        contactListener = contactRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                isContact = snapshot.exists();
                contactLoaded = true;
                render();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showLoadError();
            }
        });
    }

    private void showLoadError() {
        Toast.makeText(this, "Hata", Toast.LENGTH_LONG).show();
    }

    private void render() {
        if (!requestLoaded || !contactLoaded) {
            return;
        }

        if (Db.REQUEST_SENT.equals(requestType)) {
            state = RelationState.REQUEST_SENT;
        } else if (Db.REQUEST_RECEIVED.equals(requestType)) {
            state = RelationState.REQUEST_RECEIVED;
        } else if (isContact) {
            state = RelationState.FRIENDS;
        } else {
            state = RelationState.NEW;
        }

        boolean showDecline = state == RelationState.REQUEST_RECEIVED;
        binding.declineMessageRequestButton.setVisibility(showDecline ? View.VISIBLE : View.GONE);
        binding.declineMessageRequestButton.setEnabled(showDecline && !actionInProgress);
        binding.messageRequestButton.setEnabled(!actionInProgress);

        switch (state) {
            case REQUEST_SENT:
                binding.messageRequestButton.setText("Mesaj İsteği İptal");
                break;
            case REQUEST_RECEIVED:
                binding.messageRequestButton.setText("Mesaj İsteği Kabul");
                break;
            case FRIENDS:
                binding.messageRequestButton.setText("Sohbeti Sil");
                break;
            case NEW:
            default:
                binding.messageRequestButton.setText("İSTEK GÖNDER");
                break;
        }
    }

    private void onRequestButtonClicked() {
        switch (state) {
            case NEW:
                runAction(ChatRequestActions.sendRequest(currentUserId, profileUserId), "Mesaj İsteği Gönderildi");
                break;
            case REQUEST_SENT:
                runAction(ChatRequestActions.cancelRequest(currentUserId, profileUserId), null);
                break;
            case REQUEST_RECEIVED:
                runAction(ChatRequestActions.acceptRequest(currentUserId, profileUserId), null);
                break;
            case FRIENDS:
                confirmRemoveContact();
                break;
        }
    }

    /** Removing a contact also deletes your copy of the conversation, so ask first. */
    private void confirmRemoveContact() {
        new AlertDialog.Builder(this)
                .setTitle("Sohbeti Sil")
                .setMessage("Bu kişi kişilerinizden çıkarılacak ve sizdeki mesaj geçmişi silinecek. "
                        + "Bu işlem geri alınamaz.")
                .setPositiveButton("Sil", (d, w) ->
                        runAction(ChatRequestActions.removeContact(currentUserId, profileUserId), null))
                .setNegativeButton("Vazgeç", null)
                .show();
    }

    /** Buttons are disabled while the write runs; the listeners update the UI afterwards. */
    private void runAction(Task<Void> action, String successMessage) {
        actionInProgress = true;
        render();
        action.addOnCompleteListener(task -> {
            actionInProgress = false;
            if (task.isSuccessful()) {
                if (successMessage != null) {
                    Toast.makeText(ProfileActivity.this, successMessage, Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(ProfileActivity.this, "Hata", Toast.LENGTH_LONG).show();
            }
            render();
        });
    }
}
