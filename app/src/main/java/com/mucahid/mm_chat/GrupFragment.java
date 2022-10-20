package com.mucahid.mm_chat;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link GrupFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GrupFragment extends Fragment {

    private View grupView;
    private ListView listView;
    private ArrayAdapter<String> arrayAdapter;
    private ArrayList<String> grupListesi = new ArrayList<>();

    private DatabaseReference grupAdresi;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public GrupFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment GrupFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static GrupFragment newInstance(String param1, String param2) {
        GrupFragment fragment = new GrupFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        grupView = inflater.inflate(R.layout.fragment_grup, container, false);

        grupAdresi = FirebaseDatabase.getInstance().getReference().child("Gruplar");

        listView = grupView.findViewById(R.id.grup_listview);
        arrayAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1,grupListesi);
        listView.setAdapter(arrayAdapter);

        gruplariAlGöster();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String mevcutGrupAdi = parent.getItemAtPosition(position).toString();
                Intent grupChatActivity = new Intent(getContext(),GrupChatActivity.class);
                grupChatActivity.putExtra("grupAdı",mevcutGrupAdi);
                startActivity(grupChatActivity);

            }
        });

        return grupView;
    }

    private void gruplariAlGöster() {

        grupAdresi.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                Set<String> set = new HashSet<>();
                Iterator iterator = snapshot.getChildren().iterator();

                while (iterator.hasNext()){

                    set.add(((DataSnapshot)iterator.next()).getKey());

                }

                grupListesi.clear();
                grupListesi.addAll(set);
                arrayAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}