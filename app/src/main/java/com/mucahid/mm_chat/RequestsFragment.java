package com.mucahid.mm_chat;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mucahid.mm_chat.adapter.UserViewHolder;
import com.mucahid.mm_chat.model.User;
import com.mucahid.mm_chat.util.ChatRequestActions;
import com.mucahid.mm_chat.util.Db;
import com.mucahid.mm_chat.util.ImageLoader;

/** Lists incoming chat requests with accept / decline buttons. */
public class RequestsFragment extends Fragment {

    private RecyclerView requestsList;
    private FirebaseRecyclerAdapter<String, UserViewHolder> adapter;
    private DatabaseReference usersRef;
    private String currentUserId;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_requests, container, false);
        requestsList = view.findViewById(R.id.requests_list);
        requestsList.setLayoutManager(new LinearLayoutManager(getContext()));
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            return; // MainActivity sends the user to the login screen.
        }
        currentUserId = currentUser.getUid();
        DatabaseReference rootRef = Db.database().getReference();
        usersRef = rootRef.child(Db.USERS);

        // Only received requests; sent ones are not shown in this list.
        Query receivedRequests = rootRef.child(Db.CHAT_REQUESTS).child(currentUserId)
                .orderByChild(Db.REQUEST_TYPE)
                .equalTo(Db.REQUEST_RECEIVED);

        FirebaseRecyclerOptions<String> options = new FirebaseRecyclerOptions.Builder<String>()
                .setQuery(receivedRequests, DataSnapshot::getKey)
                .build();

        adapter = new FirebaseRecyclerAdapter<String, UserViewHolder>(options) {
            @Override public void onDataChanged() {
                if (getView() == null) return;
                android.widget.TextView state = getView().findViewById(R.id.list_state);
                state.setText(R.string.empty_requests);
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
                bindRequest(holder, userId);
            }

            @NonNull
            @Override
            public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                UserViewHolder holder = UserViewHolder.create(parent);
                holder.acceptButton.setVisibility(View.VISIBLE);
                holder.declineButton.setVisibility(View.VISIBLE);
                return holder;
            }

            @Override
            public void onViewRecycled(@NonNull UserViewHolder holder) {
                holder.clearListener();
            }
        };
        requestsList.setAdapter(adapter);
        // Listen for the whole lifetime of the view (not just while started): stopping/restarting on every
        // onStop/onStart cleared and re-filled the list mid-layout and crashed RecyclerView.
        adapter.startListening();
    }

    /**
     * On success the row disappears from the query by itself; on failure the buttons come back,
     * unless the row has meanwhile been recycled for another request.
     */
    private void runAction(UserViewHolder holder, String userId, Task<Void> action) {
        setButtonsEnabled(holder, false);
        action.addOnFailureListener(e -> {
            if (userId.equals(holder.itemView.getTag())) {
                setButtonsEnabled(holder, true);
            }
            if (getContext() != null) {
                Toast.makeText(getContext(), "Hata", Toast.LENGTH_LONG).show();
            }
        });
    }

    private static void setButtonsEnabled(UserViewHolder holder, boolean enabled) {
        holder.acceptButton.setEnabled(enabled);
        holder.declineButton.setEnabled(enabled);
    }

    @Override
    public void onDestroyView() {
        if (adapter != null) {
            adapter.stopListening();
        }
        // Recycles all rows, which removes their Firebase listeners.
        requestsList.setAdapter(null);
        super.onDestroyView();
    }

    private void bindRequest(UserViewHolder holder, String userId) {
        holder.itemView.setTag(userId);
        setButtonsEnabled(holder, true);
        holder.acceptButton.setOnClickListener(v ->
                runAction(holder, userId, ChatRequestActions.acceptRequest(currentUserId, userId)));
        holder.declineButton.setOnClickListener(v ->
                runAction(holder, userId, ChatRequestActions.cancelRequest(currentUserId, userId)));

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
                ImageLoader.loadProfileImage(user.getImage(), holder.profileImage);
                holder.userName.setText(user.getUsername());
                holder.userStatus.setText(user.getUsername() + " Adlı kullanıcı size mesaj göndermek istiyor");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
}
