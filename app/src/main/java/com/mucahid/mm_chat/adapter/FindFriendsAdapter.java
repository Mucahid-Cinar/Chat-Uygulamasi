package com.mucahid.mm_chat.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mucahid.mm_chat.ProfileActivity;
import com.mucahid.mm_chat.model.User;
import com.mucahid.mm_chat.util.Db;
import com.mucahid.mm_chat.util.ImageLoader;

import java.util.List;

public class FindFriendsAdapter extends RecyclerView.Adapter<UserViewHolder> {

    private final Context context;
    private final List<User> users;

    public FindFriendsAdapter(Context context, List<User> users) {
        this.context = context;
        this.users = users;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return UserViewHolder.create(parent);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = users.get(position);

        holder.userName.setText(user.getUsername());
        holder.userStatus.setText(user.getAbout());
        ImageLoader.loadProfileImage(user.getImage(), holder.profileImage);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ProfileActivity.class);
            intent.putExtra(Db.EXTRA_USER_ID, user.getUid());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }
}
