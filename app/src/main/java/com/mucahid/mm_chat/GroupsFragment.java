package com.mucahid.mm_chat;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.mucahid.mm_chat.util.Db;

import java.util.ArrayList;
import java.util.List;

/** Lists all group names; tapping one opens the group chat. */
public class GroupsFragment extends Fragment {

    private final List<String> groupNames = new ArrayList<>();
    private ArrayAdapter<String> adapter;

    private DatabaseReference groupNamesRef;
    private ValueEventListener groupsListener;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_groups, container, false);

        ListView listView = view.findViewById(R.id.groups_list);
        adapter = new ArrayAdapter<>(requireContext(), R.layout.item_group, groupNames);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, itemView, position, id) -> {
            Intent groupChatIntent = new Intent(getContext(), GroupChatActivity.class);
            groupChatIntent.putExtra(Db.EXTRA_GROUP_NAME, groupNames.get(position));
            startActivity(groupChatIntent);
        });

        // Only the names node is read, not the messages of every group.
        groupNamesRef = Db.database().getReference().child(Db.GROUP_NAMES);
        listenGroups();
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        groupNamesRef.removeEventListener(groupsListener);
    }

    private void listenGroups() {
        groupsListener = groupNamesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                groupNames.clear();
                for (DataSnapshot group : snapshot.getChildren()) {
                    groupNames.add(group.getKey());
                }
                adapter.notifyDataSetChanged();
                if (getView() != null) {
                    android.widget.TextView state = getView().findViewById(R.id.list_state);
                    state.setText(R.string.empty_groups);
                    state.setVisibility(groupNames.isEmpty() ? View.VISIBLE : View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (getView() != null) {
                    android.widget.TextView state = getView().findViewById(R.id.list_state);
                    state.setText("Gruplar yüklenemedi. Lütfen yeniden dene.");
                    state.setVisibility(View.VISIBLE);
                }
            }
        });
    }
}
