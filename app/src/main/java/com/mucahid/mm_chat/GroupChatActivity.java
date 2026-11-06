package com.mucahid.mm_chat;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.mucahid.mm_chat.adapter.GroupMessageAdapter;
import com.mucahid.mm_chat.model.GroupMessage;
import com.mucahid.mm_chat.util.Db;
import com.mucahid.mm_chat.util.TimeUtils;

import java.util.ArrayList;
import java.util.List;

public class GroupChatActivity extends AppCompatActivity {

    private EditText messageInput;
    private RecyclerView recyclerView;
    private final List<GroupMessage> messages = new ArrayList<>();
    private GroupMessageAdapter adapter;

    private DatabaseReference groupRef;
    private ChildEventListener messagesListener;

    private String currentUserId;
    private String currentUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        String groupName = getIntent().getStringExtra(Db.EXTRA_GROUP_NAME);
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null || TextUtils.isEmpty(groupName)) {
            finish();
            return;
        }

        currentUserId = currentUser.getUid();
        groupRef = Db.database().getReference().child(Db.GROUPS).child(groupName);

        Toolbar toolbar = findViewById(R.id.group_chat_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(groupName);
        getSupportActionBar().setSubtitle("Herkese açık grup");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        ImageButton sendButton = findViewById(R.id.group_chat_send_button);
        messageInput = findViewById(R.id.group_chat_message_input);
        recyclerView = findViewById(R.id.group_messages_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new GroupMessageAdapter(messages, currentUserId);
        recyclerView.setAdapter(adapter);

        loadCurrentUsername();
        sendButton.setOnClickListener(v -> saveMessage());
        listenMessages();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (groupRef != null && messagesListener != null) {
            groupRef.removeEventListener(messagesListener);
        }
    }

    private void listenMessages() {
        messagesListener = groupRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (!snapshot.hasChildren()) {
                    return;
                }
                GroupMessage message = snapshot.getValue(GroupMessage.class);
                if (message == null) {
                    return;
                }
                messages.add(message);
                adapter.notifyItemInserted(messages.size() - 1);
                recyclerView.scrollToPosition(messages.size() - 1);
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

    private void saveMessage() {
        String text = messageInput.getText().toString().trim();
        if (TextUtils.isEmpty(text)) {
            Toast.makeText(this, "Lütfen Bir Mesaj Giriniz", Toast.LENGTH_LONG).show();
            return;
        }
        if (currentUsername == null) {
            // Username is still loading; try again in a moment.
            Toast.makeText(this, "Lütfen Bekleyiniz", Toast.LENGTH_SHORT).show();
            return;
        }

        GroupMessage message = new GroupMessage(currentUsername, currentUserId, text,
                TimeUtils.currentDate(), TimeUtils.currentTime());
        messageInput.setText("");
        groupRef.push().setValue(message).addOnFailureListener(this, error -> {
            if (TextUtils.isEmpty(messageInput.getText())) {
                messageInput.setText(text);
                messageInput.setSelection(text.length());
            }
            Toast.makeText(this, "Mesaj gönderilemedi. Lütfen tekrar dene.", Toast.LENGTH_LONG).show();
        });
    }

    private void loadCurrentUsername() {
        Db.database().getReference().child(Db.USERS).child(currentUserId).child(Db.USERNAME)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        currentUsername = snapshot.getValue(String.class);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }
}
