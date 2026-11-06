package com.mucahid.mm_chat.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.mucahid.mm_chat.R;

import de.hdodenhof.circleimageview.CircleImageView;

/** View holder for R.layout.item_user, shared by the user list screens. */
public class UserViewHolder extends RecyclerView.ViewHolder {

    public final TextView userName;
    public final TextView userStatus;
    public final CircleImageView profileImage;
    public final Button acceptButton;
    public final Button declineButton;

    private DatabaseReference listenedRef;
    private ValueEventListener listener;

    public UserViewHolder(@NonNull View itemView) {
        super(itemView);
        userName = itemView.findViewById(R.id.user_name);
        userStatus = itemView.findViewById(R.id.user_status);
        profileImage = itemView.findViewById(R.id.user_profile_image);
        acceptButton = itemView.findViewById(R.id.accept_request_button);
        declineButton = itemView.findViewById(R.id.decline_request_button);
    }

    public static UserViewHolder create(@NonNull ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    /** Attaches a live listener, removing the one from the previous binding. */
    public void listen(DatabaseReference ref, ValueEventListener newListener) {
        clearListener();
        userName.setText("Yükleniyor…");
        userStatus.setText("");
        com.mucahid.mm_chat.util.ImageLoader.loadProfileImage(null, profileImage);
        listenedRef = ref;
        listener = ref.addValueEventListener(newListener);
    }

    public void clearListener() {
        if (listenedRef != null && listener != null) {
            listenedRef.removeEventListener(listener);
        }
        listenedRef = null;
        listener = null;
    }
}
