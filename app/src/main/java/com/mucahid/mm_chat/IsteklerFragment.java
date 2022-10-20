package com.mucahid.mm_chat;

import android.opengl.Visibility;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
 * Use the {@link IsteklerFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class IsteklerFragment extends Fragment {

    private View isteklerFragmentView;
    private RecyclerView isteklerListem;

    private DatabaseReference SohbetIstegiYolu,KullanicilarYolu,SohbetlerYolu;
    private FirebaseAuth mmYetki;

    private String aktifKullaniciId;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public IsteklerFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment IsteklerFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static IsteklerFragment newInstance(String param1, String param2) {
        IsteklerFragment fragment = new IsteklerFragment();
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

        isteklerFragmentView = inflater.inflate(R.layout.fragment_istekler, container, false);

        mmYetki = FirebaseAuth.getInstance();
        aktifKullaniciId = mmYetki.getCurrentUser().getUid();
        SohbetIstegiYolu = FirebaseDatabase.getInstance().getReference().child("Sohbet Talebi");
        KullanicilarYolu = FirebaseDatabase.getInstance().getReference().child("Kullanicilar");
        SohbetlerYolu = FirebaseDatabase.getInstance().getReference().child("Sohbetler");

        isteklerListem = isteklerFragmentView.findViewById(R.id.sohbet_istegi_listesi);
        isteklerListem.setLayoutManager(new LinearLayoutManager(getContext()));

        return isteklerFragmentView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<User> secenekler = new FirebaseRecyclerOptions.Builder<User>()
                .setQuery(SohbetIstegiYolu.child(aktifKullaniciId),User.class)
                .build();

        FirebaseRecyclerAdapter<User,IsteklerViewHolder> adapter = new FirebaseRecyclerAdapter<User, IsteklerViewHolder>(secenekler) {
            @Override
            protected void onBindViewHolder(@NonNull IsteklerViewHolder holder, int position, @NonNull User model) {

                holder.itemView.findViewById(R.id.istek_kabul_btn).setVisibility(View.VISIBLE);
                holder.itemView.findViewById(R.id.istek_red_btn).setVisibility(View.VISIBLE);

                final String kullanici_id_list = getRef(position).getKey();

                DatabaseReference talepTuruAl = getRef(position).child("talep_turu");

                talepTuruAl.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if(snapshot.exists()){
                            String tur = snapshot.getValue().toString();
                            if(tur.equals("Alindi")){

                                KullanicilarYolu.child(kullanici_id_list).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                                        if(snapshot.hasChild("resim")){
                                            final String talepKullaniciResmi = snapshot.child("resim").getValue().toString();
                                            Picasso.get().load(talepKullaniciResmi).resize(500,500).into(holder.resim);
                                        }

                                            final String talepKullaniciAdi = snapshot.child("KullanıcıAdı").getValue().toString();
                                            final String talepKullaniciDurumu = snapshot.child("hakkımda").getValue().toString();

                                            holder.kullaniciAdi.setText(talepKullaniciAdi);
                                            holder.hakkimda.setText(talepKullaniciAdi+" Adlı kullanıcı size mesaj göndermek istiyor");

                                        holder.kabulBtn.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {

                                                SohbetlerYolu.child(aktifKullaniciId).child(kullanici_id_list).child("Sohbetler")
                                                        .setValue("Kaydedildi").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {

                                                        if (task.isSuccessful()){

                                                            SohbetlerYolu.child(kullanici_id_list).child(aktifKullaniciId).child("Sohbetler")
                                                                    .setValue("Kaydedildi").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if(task.isSuccessful()){
                                                                        SohbetIstegiYolu.child(aktifKullaniciId).child(kullanici_id_list)
                                                                                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                if (task.isSuccessful()){

                                                                                    SohbetIstegiYolu.child(kullanici_id_list).child(aktifKullaniciId)
                                                                                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                        @Override
                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                            //Toast.makeText(getContext(), "", Toast.LENGTH_SHORT).show();
                                                                                        }
                                                                                    });

                                                                                }
                                                                            }
                                                                        });

                                                                    }
                                                                }
                                                            });

                                                        }

                                                    }
                                                });

                                            }
                                        });

                                        holder.iptalBtn.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {

                                                SohbetIstegiYolu.child(aktifKullaniciId).child(kullanici_id_list)
                                                        .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()){

                                                            SohbetIstegiYolu.child(kullanici_id_list).child(aktifKullaniciId)
                                                                    .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    //Toast.makeText(getContext(), "", Toast.LENGTH_SHORT).show();
                                                                }
                                                            });

                                                        }
                                                    }
                                                });

                                            }
                                        });

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }

                            else if(tur.equals("Gonderildi")){
                                holder.itemView.setVisibility(View.INVISIBLE);
                            }

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }

            @NonNull
            @Override
            public IsteklerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.kullanici_ekle_layout,parent,false);

                IsteklerViewHolder holder = new IsteklerViewHolder(view);

                return holder;

            }
        };

        isteklerListem.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        adapter.startListening();

    }

    public static class IsteklerViewHolder extends RecyclerView.ViewHolder{

        TextView kullaniciAdi,hakkimda;
        CircleImageView resim;
        Button kabulBtn,iptalBtn;

        public IsteklerViewHolder(@NonNull View itemView) {
            super(itemView);
            kullaniciAdi = itemView.findViewById(R.id.kullanici_adi);
            hakkimda = itemView.findViewById(R.id.kullanici_durumu);
            resim = itemView.findViewById(R.id.kullanici_profilresmi);
            kabulBtn = itemView.findViewById(R.id.istek_kabul_btn);
            iptalBtn = itemView.findViewById(R.id.istek_red_btn);

        }
    }
}