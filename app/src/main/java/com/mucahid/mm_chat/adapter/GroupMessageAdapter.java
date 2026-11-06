package com.mucahid.mm_chat.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mucahid.mm_chat.databinding.ItemGroupMessageReceivedBinding;
import com.mucahid.mm_chat.databinding.ItemGroupMessageSentBinding;
import com.mucahid.mm_chat.model.GroupMessage;

import java.util.List;

public class GroupMessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_SENT = 1;
    private static final int TYPE_RECEIVED = 2;

    private final List<GroupMessage> messages;
    private final String currentUserId;

    public GroupMessageAdapter(List<GroupMessage> messages, String currentUserId) {
        this.messages = messages;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_SENT) {
            return new SentHolder(ItemGroupMessageSentBinding.inflate(inflater, parent, false));
        }
        return new ReceivedHolder(ItemGroupMessageReceivedBinding.inflate(inflater, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        GroupMessage message = messages.get(position);
        if (holder instanceof SentHolder) {
            ((SentHolder) holder).bind(message);
        } else {
            ((ReceivedHolder) holder).bind(message);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return currentUserId.equals(messages.get(position).getUserId()) ? TYPE_SENT : TYPE_RECEIVED;
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    private static class ReceivedHolder extends RecyclerView.ViewHolder {

        private final ItemGroupMessageReceivedBinding binding;

        ReceivedHolder(ItemGroupMessageReceivedBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(GroupMessage message) {
            binding.userName.setText(message.getUsername());
            binding.message.setText(message.getMessage());
            binding.messageTime.setText(message.getTime());
        }
    }

    private static class SentHolder extends RecyclerView.ViewHolder {

        private final ItemGroupMessageSentBinding binding;

        SentHolder(ItemGroupMessageSentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(GroupMessage message) {
            binding.message.setText(message.getMessage());
            binding.messageTime.setText(message.getTime());
        }
    }
}
