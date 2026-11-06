package com.mucahid.mm_chat.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mucahid.mm_chat.databinding.ItemPrivateMessageReceivedBinding;
import com.mucahid.mm_chat.databinding.ItemPrivateMessageSentBinding;
import com.mucahid.mm_chat.model.PrivateMessage;

import java.util.List;

public class PrivateMessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_SENT = 1;
    private static final int TYPE_RECEIVED = 2;

    private final List<PrivateMessage> messages;
    private final String currentUserId;

    public PrivateMessageAdapter(List<PrivateMessage> messages, String currentUserId) {
        this.messages = messages;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_SENT) {
            return new SentHolder(ItemPrivateMessageSentBinding.inflate(inflater, parent, false));
        }
        return new ReceivedHolder(ItemPrivateMessageReceivedBinding.inflate(inflater, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        PrivateMessage message = messages.get(position);
        if (holder instanceof SentHolder) {
            ((SentHolder) holder).bind(message);
        } else {
            ((ReceivedHolder) holder).bind(message);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return currentUserId.equals(messages.get(position).getFrom()) ? TYPE_SENT : TYPE_RECEIVED;
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    private static class ReceivedHolder extends RecyclerView.ViewHolder {

        private final ItemPrivateMessageReceivedBinding binding;

        ReceivedHolder(ItemPrivateMessageReceivedBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(PrivateMessage message) {
            binding.receivedMessage.setText(message.getMessage());
            binding.messageTime.setText(message.getTime());
        }
    }

    private static class SentHolder extends RecyclerView.ViewHolder {

        private final ItemPrivateMessageSentBinding binding;

        SentHolder(ItemPrivateMessageSentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(PrivateMessage message) {
            binding.sentMessage.setText(message.getMessage());
            binding.messageTime.setText(message.getTime());
        }
    }
}
