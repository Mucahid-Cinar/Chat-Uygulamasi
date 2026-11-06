package com.mucahid.mm_chat;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.mucahid.mm_chat.adapter.PrivateMessageAdapter;
import com.mucahid.mm_chat.model.PrivateMessage;
import com.mucahid.mm_chat.util.ChatRequestActions;
import com.mucahid.mm_chat.util.ContactKeyStore;
import com.mucahid.mm_chat.util.Db;
import com.mucahid.mm_chat.util.E2eeManager;
import com.mucahid.mm_chat.util.ImageLoader;
import com.mucahid.mm_chat.util.MessageCrypto;
import com.mucahid.mm_chat.util.SafetyNumber;
import com.mucahid.mm_chat.util.TimeUtils;

import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    /** Shown when a message was not encrypted for this device's key (e.g. after a reinstall). */
    private static final String UNREADABLE_MESSAGE = "🔒 Bu mesaj bu cihazda okunamıyor";

    private String receiverId;
    private String receiverName;
    private String senderId;
    /** The receiver's published public key; null until loaded or if they have none yet. */
    private String receiverPublicKey;

    private TextView lastSeenTime;
    private TextView lastSeenLabel;
    private EditText messageInput;
    private ImageButton sendButton;
    private ImageView safetyNumberButton;
    private TextView banner;
    private RecyclerView recyclerView;

    private final List<PrivateMessage> messages = new ArrayList<>();
    private PrivateMessageAdapter adapter;

    private final ExecutorService decryptExecutor = Executors.newSingleThreadExecutor();

    private ContactKeyStore keyStore;
    /** The receiver's key differs from the one remembered on this device and was not reviewed yet. */
    private boolean keyChangePending;
    /** False once the receiver removes us (or we remove them) from contacts; sending is then refused. */
    private boolean isContact = true;

    private DatabaseReference rootRef;
    private DatabaseReference conversationRef;
    private DatabaseReference receiverRef;
    private DatabaseReference contactRef;
    private ChildEventListener messagesListener;
    private ValueEventListener lastSeenListener;
    private ValueEventListener contactListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN
                | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        receiverId = getIntent().getStringExtra(Db.EXTRA_USER_ID);
        receiverName = getIntent().getStringExtra(Db.EXTRA_USER_NAME);
        String receiverImage = getIntent().getStringExtra(Db.EXTRA_USER_IMAGE);
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null || TextUtils.isEmpty(receiverId)) {
            finish();
            return;
        }

        senderId = currentUser.getUid();
        rootRef = Db.database().getReference();
        keyStore = new ContactKeyStore(this, senderId);

        TextView profileName = findViewById(R.id.profile_name);
        CircleImageView profileImage = findViewById(R.id.profile_image);
        ImageView backButton = findViewById(R.id.back_button);
        sendButton = findViewById(R.id.chat_send_button);
        safetyNumberButton = findViewById(R.id.safety_number_button);
        banner = findViewById(R.id.chat_banner);
        lastSeenTime = findViewById(R.id.profile_last_seen_time);
        lastSeenLabel = findViewById(R.id.profile_last_seen);
        messageInput = findViewById(R.id.chat_message_input);

        recyclerView = findViewById(R.id.private_messages_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new PrivateMessageAdapter(messages, senderId);
        recyclerView.setAdapter(adapter);

        profileName.setText(receiverName);
        ImageLoader.loadProfileImage(receiverImage, profileImage);

        backButton.setOnClickListener(v -> finish());

        sendButton.setOnClickListener(v -> {
            String text = messageInput.getText().toString().trim();
            if (!text.isEmpty()) {
                sendMessage(text);
            }
        });

        messageInput.setOnClickListener(v -> scrollToBottom(false));
        safetyNumberButton.setOnClickListener(v -> showSafetyNumber());
        banner.setOnClickListener(v -> {
            if (!isContact) {
                confirmDeleteConversation();
            } else if (keyChangePending) {
                showSafetyNumber();
            }
        });

        listenMessages();
        listenLastSeen();
        listenContactStatus();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        decryptExecutor.shutdownNow();
        if (conversationRef != null && messagesListener != null) {
            conversationRef.removeEventListener(messagesListener);
        }
        if (receiverRef != null && lastSeenListener != null) {
            receiverRef.removeEventListener(lastSeenListener);
        }
        if (contactRef != null && contactListener != null) {
            contactRef.removeEventListener(contactListener);
        }
    }

    private void listenContactStatus() {
        contactRef = rootRef.child(Db.CONTACTS).child(senderId).child(receiverId);
        contactListener = contactRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                isContact = snapshot.exists();
                updateSecurityUi();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    /** Remembers the receiver's key on first sight and notices when it changes. */
    private void onReceiverKeyLoaded() {
        keyChangePending = receiverPublicKey != null
                && keyStore.check(receiverId, receiverPublicKey) == ContactKeyStore.KeyStatus.CHANGED;
        updateSecurityUi();
    }

    private void updateSecurityUi() {
        boolean verified = receiverPublicKey != null && keyStore.isVerified(receiverId, receiverPublicKey);
        safetyNumberButton.setImageResource(verified ? R.drawable.ic_verified_user_24 : R.drawable.ic_lock_24);

        if (!isContact) {
            banner.setText("Bu kişi artık kişileriniz arasında değil. Mesaj gönderemezsiniz. "
                    + "Sohbeti silmek için dokunun.");
            banner.setVisibility(View.VISIBLE);
        } else if (keyChangePending) {
            banner.setText("⚠ " + receiverName + " adlı kişinin güvenlik numarası değişti. Uygulamayı yeniden "
                    + "kurmuş veya telefon değiştirmiş olabilir. Kontrol etmek için dokunun.");
            banner.setVisibility(View.VISIBLE);
        } else {
            banner.setVisibility(View.GONE);
        }
        messageInput.setEnabled(isContact);
        sendButton.setEnabled(isContact);
    }

    /** Deletes this user's copy of the conversation (used once the other person removed the contact). */
    private void confirmDeleteConversation() {
        new AlertDialog.Builder(this)
                .setTitle("Sohbeti Sil")
                .setMessage("Bu sohbetteki mesajlar sizin tarafınızdan silinecek. Bu işlem geri alınamaz.")
                .setPositiveButton("Sil", (d, w) -> ChatRequestActions.deleteConversation(senderId, receiverId)
                        .addOnSuccessListener(unused -> finish())
                        .addOnFailureListener(e -> Toast.makeText(this, "Hata", Toast.LENGTH_LONG).show()))
                .setNegativeButton("Vazgeç", null)
                .show();
    }

    /** Shows the 60-digit safety number to compare with the other person's phone. */
    private void showSafetyNumber() {
        String myPublicKey = E2eeManager.publicKey(this, senderId);
        if (receiverPublicKey == null || myPublicKey == null) {
            Toast.makeText(this, "Karşı tarafın henüz güvenlik anahtarı yok", Toast.LENGTH_LONG).show();
            return;
        }
        String keyAtOpen = receiverPublicKey;
        String number = SafetyNumber.compute(senderId, myPublicKey, receiverId, keyAtOpen);
        boolean verified = keyStore.isVerified(receiverId, keyAtOpen);

        View view = getLayoutInflater().inflate(R.layout.dialog_safety_number, null);
        TextView status = view.findViewById(R.id.safety_number_status);
        TextView digits = view.findViewById(R.id.safety_number_digits);
        TextView explanation = view.findViewById(R.id.safety_number_explanation);
        status.setText(verified ? "✓ Doğrulandı" : "Doğrulanmadı");
        digits.setText(SafetyNumber.format(number));
        explanation.setText("Bu numarayı " + receiverName + " adlı kişinin telefonunda aynı ekranda görünen "
                + "numarayla yüz yüze veya güvendiğiniz başka bir yoldan karşılaştırın. Numaralar aynıysa "
                + "mesajlarınız uçtan uca şifrelidir ve araya kimse girmemiştir.");

        new AlertDialog.Builder(this)
                .setTitle("Güvenlik Numarası")
                .setView(view)
                .setPositiveButton(verified ? "Doğrulamayı kaldır" : "Doğrulandı olarak işaretle",
                        (d, w) -> keyStore.setVerified(receiverId, keyAtOpen, !verified))
                .setNeutralButton("Kopyala", (d, w) -> {
                    ClipboardManager clipboard = getSystemService(ClipboardManager.class);
                    clipboard.setPrimaryClip(ClipData.newPlainText("Güvenlik numarası", number));
                    Toast.makeText(this, "Güvenlik numarası kopyalandı", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Kapat", null)
                .setOnDismissListener(d -> {
                    // Reviewing the number counts as acknowledging a key change.
                    if (keyChangePending && keyAtOpen.equals(receiverPublicKey)) {
                        keyStore.acknowledge(receiverId, keyAtOpen);
                        keyChangePending = false;
                    }
                    updateSecurityUi();
                })
                .show();
    }

    /**
     * End-to-end encrypted: the receiver's copy is encrypted with the receiver's public key and the
     * sender's copy with the sender's own public key, so each copy can only be read on its owner's device.
     */
    private void sendMessage(String text) {
        if (!isContact) {
            return;
        }
        if (keyChangePending && keyStore.wasVerifiedWithOtherKey(receiverId, receiverPublicKey)) {
            // A verified contact's key changed: make the user look at the new number before sending.
            Toast.makeText(this, "Güvenlik numarası değişti. Göndermeden önce kontrol edin.", Toast.LENGTH_LONG).show();
            showSafetyNumber();
            return;
        }
        if (receiverPublicKey == null) {
            Toast.makeText(this, "Karşı taraf uygulamayı açıp güncelleyene kadar güvenli mesaj gönderilemez",
                    Toast.LENGTH_LONG).show();
            return;
        }
        String senderPublicKey = E2eeManager.publicKey(this, senderId);
        String messageKey = rootRef.child(Db.MESSAGES).child(senderId).child(receiverId).push().getKey();
        if (senderPublicKey == null || messageKey == null) {
            Toast.makeText(this, "Mesaj Gönderilemedi", Toast.LENGTH_LONG).show();
            return;
        }

        String forReceiver;
        String forSender;
        try {
            forReceiver = MessageCrypto.encrypt(receiverPublicKey, text, messageKey, senderId);
            forSender = MessageCrypto.encrypt(senderPublicKey, text, messageKey, senderId);
        } catch (GeneralSecurityException | IllegalArgumentException e) {
            // IllegalArgumentException: the published public key is not valid Base64.
            Toast.makeText(this, "Mesaj Gönderilemedi", Toast.LENGTH_LONG).show();
            return;
        }

        String time = TimeUtils.currentTime();
        Map<String, Object> updates = new HashMap<>();
        updates.put(Db.MESSAGES + "/" + senderId + "/" + receiverId + "/" + messageKey, messageBody(forSender, time));
        updates.put(Db.MESSAGES + "/" + receiverId + "/" + senderId + "/" + messageKey, messageBody(forReceiver, time));
        // Moves the conversation to the top of both users' Chats tab.
        updates.put(Db.CHATS + "/" + senderId + "/" + receiverId + "/" + Db.LAST_MESSAGE_AT, ServerValue.TIMESTAMP);
        updates.put(Db.CHATS + "/" + receiverId + "/" + senderId + "/" + Db.LAST_MESSAGE_AT, ServerValue.TIMESTAMP);

        messageInput.setText("");
        rootRef.updateChildren(updates).addOnFailureListener(e -> {
            // Give the text back so a failed send is not lost.
            if (TextUtils.isEmpty(messageInput.getText())) {
                messageInput.setText(text);
                messageInput.setSelection(text.length());
            }
            Toast.makeText(ChatActivity.this, "Mesaj Gönderilemedi", Toast.LENGTH_LONG).show();
        });
    }

    private Map<String, Object> messageBody(String ciphertext, String time) {
        Map<String, Object> body = new HashMap<>();
        body.put("message", ciphertext);
        body.put("type", Db.MESSAGE_TYPE_TEXT);
        body.put("from", senderId);
        body.put("time", time);
        return body;
    }

    private void listenMessages() {
        conversationRef = rootRef.child(Db.MESSAGES).child(senderId).child(receiverId);
        messagesListener = conversationRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                PrivateMessage message = snapshot.getValue(PrivateMessage.class);
                if (message == null || snapshot.getKey() == null) {
                    return;
                }

                String messageId = snapshot.getKey();
                // Decrypt off the main thread. The single-thread executor keeps Firebase's order, and
                // results are posted back to the main thread in that same order.
                decryptExecutor.execute(() -> {
                    String plain = message.getMessage() == null || message.getFrom() == null ? null
                            : E2eeManager.decrypt(ChatActivity.this, senderId, message.getMessage(),
                                    messageId, message.getFrom());
                    message.setMessage(plain != null ? plain : UNREADABLE_MESSAGE);
                    runOnUiThread(() -> {
                        if (isDestroyed()) {
                            return;
                        }
                        messages.add(message);
                        adapter.notifyItemInserted(messages.size() - 1);
                        scrollToBottom(false);
                    });
                });
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    /** Shows the online state / last seen time of the person we are chatting with. */
    private void listenLastSeen() {
        receiverRef = rootRef.child(Db.USERS).child(receiverId);
        lastSeenListener = receiverRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Live, so a receiver who reinstalls (new key pair) is picked up immediately.
                receiverPublicKey = snapshot.child(Db.PUBLIC_KEY).getValue(String.class);
                onReceiverKeyLoaded();

                DataSnapshot userState = snapshot.child(Db.USER_STATE);
                String state = userState.child(Db.STATE).getValue(String.class);
                Long lastSeen = userState.child(Db.LAST_SEEN).getValue(Long.class);

                if (Db.STATE_ONLINE.equals(state)) {
                    lastSeenLabel.setText("Çevrimiçi");
                    lastSeenTime.setVisibility(View.INVISIBLE);
                } else if (Db.STATE_OFFLINE.equals(state) && lastSeen != null) {
                    lastSeenLabel.setText("Son Görülme:");
                    lastSeenTime.setText(TimeUtils.formatLastSeen(lastSeen));
                    lastSeenTime.setVisibility(View.VISIBLE);
                } else {
                    lastSeenLabel.setText("Çevrimdışı");
                    lastSeenTime.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void scrollToBottom(boolean smooth) {
        int count = adapter.getItemCount();
        if (count == 0) {
            return;
        }
        if (smooth) {
            recyclerView.smoothScrollToPosition(count - 1);
        } else {
            recyclerView.scrollToPosition(count - 1);
        }
    }
}
