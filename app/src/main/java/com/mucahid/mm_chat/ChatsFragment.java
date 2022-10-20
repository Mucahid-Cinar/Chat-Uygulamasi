package com.mucahid.mm_chat;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mucahid.mm_chat.Model.User;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ChatsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChatsFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private View ChatView;
    private RecyclerView chatList;

    private DatabaseReference sohbetYolu,kullaniciYolu;
    private FirebaseAuth mmYetki;

    private String aktifKullaniciId;


    public ChatsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ChatsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ChatsFragment newInstance(String param1, String param2) {
        ChatsFragment fragment = new ChatsFragment();
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
        // Inflate the layout for this fragment
        ChatView = inflater.inflate(R.layout.fragment_chats2, container, false);

        mmYetki = FirebaseAuth.getInstance();
        aktifKullaniciId = mmYetki.getCurrentUser().getUid();
        sohbetYolu = FirebaseDatabase.getInstance().getReference().child("Sohbetler").child(aktifKullaniciId);
        kullaniciYolu = FirebaseDatabase.getInstance().getReference().child("Kullanicilar");

        chatList = ChatView.findViewById(R.id.sohbet_list);
        chatList.setLayoutManager(new LinearLayoutManager(getContext()));

        return ChatView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<User> secenekler = new FirebaseRecyclerOptions.Builder<User>()
                .setQuery(sohbetYolu,User.class)
                .build();

        FirebaseRecyclerAdapter<User,ChatViewHolder> adapter =new FirebaseRecyclerAdapter<User, ChatViewHolder>(secenekler) {
            @Override
            protected void onBindViewHolder(@NonNull ChatViewHolder holder, int position, @NonNull User model) {

                final String kullanicilarId = getRef(position).getKey();
                final String[] resimAl = {"Varsayılan Resim"};

                kullaniciYolu.child(kullanicilarId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if(snapshot.exists()){

                            if (snapshot.hasChild("resim")){

                                resimAl[0] = snapshot.child("resim").getValue().toString();

                                Picasso.get().load(resimAl[0]).resize(500,500).into(holder.profilResmi);
                            }

                            final String kullaniciAdAl = snapshot.child("KullanıcıAdı").getValue().toString();
                            final String kullaniciDurumAl = snapshot.child("hakkımda").getValue().toString();

                            holder.kullaniciAdi.setText(kullaniciAdAl);

                            if(snapshot.child("kullanici_durumu").hasChild("durum")){

                                String durum = snapshot.child("kullanici_durumu").child("durum").getValue().toString();

                                if(durum.equals("Çevrimiçi")){

                                    holder.kullaniciDurum.setText("Çevrimiçi");
                                    holder.kullaniciDurum.setTextColor(Color.argb(255,20,200,20));

                                }else{

                                    holder.kullaniciDurum.setText("Çevrimdışı");
                                    holder.kullaniciDurum.setTextColor(Color.argb(255,255,0,0));

                                }

                            }

                            else {

                                holder.kullaniciDurum.setText("Çevrimdışı");
                                holder.kullaniciDurum.setTextColor(Color.argb(255,255,0,0));

                            }

                            holder.itemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent chatActivity = new Intent (getContext(),ChatActivity.class);
                                    chatActivity.putExtra("kullanici_id",kullanicilarId);
                                    chatActivity.putExtra("kullanici_adi",kullaniciAdAl);
                                    chatActivity.putExtra("kullanici_resim", resimAl[0]);
                                    startActivity(chatActivity);

                                }
                            });

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }

            @NonNull
            @Override
            public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.kullanici_ekle_layout,parent,false);
                return new ChatViewHolder(view);

            }
        };

        chatList.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        adapter.startListening();
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder{

        CircleImageView profilResmi;
        TextView kullaniciAdi,kullaniciDurum;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);

            profilResmi = itemView.findViewById(R.id.kullanici_profilresmi);
            kullaniciAdi = itemView.findViewById(R.id.kullanici_adi);
            kullaniciDurum = itemView.findViewById(R.id.kullanici_durumu);

        }
    }

}