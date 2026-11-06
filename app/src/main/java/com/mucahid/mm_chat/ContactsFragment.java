package com.mucahid.mm_chat;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.mucahid.mm_chat.adapter.UserViewHolder;
import com.mucahid.mm_chat.model.User;
import com.mucahid.mm_chat.util.Db;
import com.mucahid.mm_chat.util.ImageLoader;

/** Lists the user's contacts with their "about" text; tapping one opens (or starts) the chat. */
public class ContactsFragment extends Fragment {

    private RecyclerView contactsList;
    private FirebaseRecyclerAdapter<String, UserViewHolder> adapter;
    private DatabaseReference usersRef;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contacts, container, false);
        contactsList = view.findViewById(R.id.contacts_list);
        contactsList.setLayoutManager(new LinearLayoutManager(getContext()));
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            return; // MainActivity sends the user to the login screen.
        }
        String currentUserId = currentUser.getUid();
        DatabaseReference rootRef = Db.database().getReference();
        usersRef = rootRef.child(Db.USERS);

        FirebaseRecyclerOptions<String> options = new FirebaseRecyclerOptions.Builder<String>()
                .setQuery(rootRef.child(Db.CONTACTS).child(currentUserId), DataSnapshot::getKey)
                .build();

        adapter = new FirebaseRecyclerAdapter<String, UserViewHolder>(options) {
            @Override public void onDataChanged() {
                if (getView() == null) return;
                android.widget.TextView state = getView().findViewById(R.id.list_state);
                state.setText(R.string.empty_contacts);
                state.setVisibility(getItemCount() == 0 ? View.VISIBLE : View.GONE);
            }
            @Override public void onError(@NonNull DatabaseError error) {
                if (getView() == null) return;
                android.widget.TextView state = getView().findViewById(R.id.list_state);
                state.setText("Liste yüklenemedi. Bağlantını kontrol edip yeniden aç.");
                state.setVisibility(View.VISIBLE);
            }
            @Override
            protected void onBindViewHolder(@NonNull UserViewHolder holder, int position, @NonNull String userId) {
                bindContact(holder, userId);
            }

            @NonNull
            @Override
            public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return UserViewHolder.create(parent);
            }

            @Override
            public void onViewRecycled(@NonNull UserViewHolder holder) {
                holder.clearListener();
            }
        };
        contactsList.setAdapter(adapter);
        // Listen for the whole lifetime of the view (not just while started): stopping/restarting on every
        // onStop/onStart cleared and re-filled the list mid-layout and crashed RecyclerView.
        adapter.startListening();
    }

    @Override
    public void onDestroyView() {
        if (adapter != null) {
            adapter.stopListening();
        }
        // Recycles all rows, which removes their Firebase listeners.
        contactsList.setAdapter(null);
        super.onDestroyView();
    }

    private void bindContact(UserViewHolder holder, String userId) {
        holder.itemView.setOnClickListener(null);
        holder.listen(usersRef.child(userId), new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.hasChildren()) {
                    return;
                }
                User user = snapshot.getValue(User.class);
                if (user == null) {
                    return;
                }
                holder.userName.setText(user.getUsername());
                holder.userStatus.setText(user.getAbout());
                ImageLoader.loadProfileImage(user.getImage(), holder.profileImage);

                // Contacts is where a new conversation starts.
                holder.itemView.setOnClickListener(v -> {
                    Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                    chatIntent.putExtra(Db.EXTRA_USER_ID, userId);
                    chatIntent.putExtra(Db.EXTRA_USER_NAME, user.getUsername());
                    chatIntent.putExtra(Db.EXTRA_USER_IMAGE, user.getImage());
                    startActivity(chatIntent);
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
}
