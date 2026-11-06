package com.mucahid.mm_chat;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.mucahid.mm_chat.adapter.FindFriendsAdapter;
import com.mucahid.mm_chat.model.User;
import com.mucahid.mm_chat.util.Db;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FindFriendsActivity extends AppCompatActivity {

    private static final Locale TURKISH = new Locale("tr", "TR");

    /** Everyone except the current user. */
    private final List<User> allUsers = new ArrayList<>();
    /** What the list shows: allUsers filtered by the search text. */
    private final List<User> users = new ArrayList<>();
    private FindFriendsAdapter adapter;
    private DatabaseReference usersRef;
    private String currentUserId;

    private EditText searchInput;
    private TextView emptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            finish();
            return;
        }
        currentUserId = currentUser.getUid();

        adapter = new FindFriendsAdapter(this, users);
        usersRef = Db.database().getReference(Db.USERS);

        RecyclerView recyclerView = findViewById(R.id.find_friends_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        emptyView = findViewById(R.id.empty_view);
        emptyView.setText("Kişiler yükleniyor…");
        emptyView.setVisibility(View.VISIBLE);

        searchInput = findViewById(R.id.search_input);
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                applyFilter();
            }
        });

        Toolbar toolbar = findViewById(R.id.find_friends_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Arkadaş Bul");

        loadUsers();
    }

    private void loadUsers() {
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allUsers.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    // Skip yourself, and users who registered but have not filled in their profile ("").
                    if (currentUserId.equals(child.getKey()) || !child.hasChild(Db.USERNAME)) {
                        continue;
                    }
                    User user = child.getValue(User.class);
                    if (user != null) {
                        user.setUid(child.getKey());
                        allUsers.add(user);
                    }
                }
                applyFilter();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                emptyView.setText("Kişiler yüklenemedi. Yeniden denemek için dokun.");
                emptyView.setVisibility(View.VISIBLE);
                emptyView.setOnClickListener(v -> loadUsers());
            }
        });
    }

    /** Case-insensitive (Turkish rules, so "İ" matches "i") match on the username. */
    @SuppressLint("NotifyDataSetChanged")
    private void applyFilter() {
        emptyView.setOnClickListener(null);
        emptyView.setText("Kullanıcı bulunamadı. Farklı bir isim deneyebilirsin.");
        String query = searchInput.getText().toString().trim().toLowerCase(TURKISH);
        users.clear();
        for (User user : allUsers) {
            String username = user.getUsername() == null ? "" : user.getUsername().toLowerCase(TURKISH);
            if (query.isEmpty() || username.contains(query)) {
                users.add(user);
            }
        }
        adapter.notifyDataSetChanged();
        emptyView.setVisibility(users.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
