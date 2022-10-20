package com.mucahid.mm_chat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mucahid.mm_chat.Model.User;
import com.mucahid.mm_chat.databinding.ActivityProfileBinding;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Iterator;

public class ProfileActivity extends AppCompatActivity {

    String kullaniciId = "";
    private DatabaseReference kullaniciYolu,sohbetTalebiYolu,sohbetlerYolu,bildirirmYolu;
    private FirebaseAuth mmYetki;
    private ActivityProfileBinding binding;
    private String aktifDurum,aktifKullaniciId;
    private Button istekGonderBtn,istekDegerlendirBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        istekGonderBtn = binding.mesajIstegiGonder;
        istekDegerlendirBtn = binding.mesajIstegiDegerlendirme;

        kullaniciId = getIntent().getStringExtra("kullaniciId");
        sohbetTalebiYolu = FirebaseDatabase.getInstance().getReference().child("Sohbet Talebi");
        sohbetlerYolu = FirebaseDatabase.getInstance().getReference().child("Sohbetler");
        bildirirmYolu = FirebaseDatabase.getInstance().getReference().child("Bildirimler");
        mmYetki = FirebaseAuth.getInstance();
        aktifDurum = "Yeni";
        aktifKullaniciId = mmYetki.getCurrentUser().getUid();

        if (!kullaniciId.isEmpty())
            KullaniciGetir(kullaniciId);
    }

    private void KullaniciGetir(String kullaniciId){
        kullaniciYolu = FirebaseDatabase.getInstance().getReference("Kullanicilar");
        kullaniciYolu.child(kullaniciId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Iterator iteretor = dataSnapshot.getChildren().iterator();

                while (iteretor.hasNext()){
                    User user = new User(
                            (((DataSnapshot) iteretor.next()).getValue()).toString(),
                            (((DataSnapshot) iteretor.next()).getValue().toString()),
                            (((DataSnapshot) iteretor.next()).getValue().toString()),
                            (((DataSnapshot) iteretor.next()).getValue().toString()),
                            (((DataSnapshot) iteretor.next()).getValue().toString()),
                            (((DataSnapshot) iteretor.next()).getValue().toString()),
                            (((DataSnapshot) iteretor.next()).getValue().toString())
                    );

                    if (user.getResim().isEmpty()) {
                        binding.kullaniciFotoAyarlar.setImageResource(R.drawable.userphoto200px);
                    } else{
                        Picasso.get().load(user.getResim()).resize(500, 500).into(binding.kullaniciFotoAyarlar);
                    }

                    //Picasso.get().load(user.getResim()).resize(500, 500).into(binding.kullaniciFotoAyarlar);

                    Uygula(user);

                    mesajIstegiGonder();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });
    }

    private void mesajIstegiGonder() {

        sohbetTalebiYolu.child(aktifKullaniciId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.hasChild(kullaniciId)){

                    String talep_turu = snapshot.child(kullaniciId).child("talep_turu").getValue().toString();

                    if(talep_turu.equals("Gonderildi")){
                        aktifDurum = "talep_gonderildi";
                        istekGonderBtn.setText("Mesaj İsteği İptal");
                    }
                    else{
                        aktifDurum = "talep_alindi";
                        istekGonderBtn.setText("Mesaj İsteği Kabul");
                        istekDegerlendirBtn.setVisibility(View.VISIBLE);
                        istekDegerlendirBtn.setEnabled(true);

                        istekDegerlendirBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                IstegiİptalEt();
                            }
                        });
                    }
                }

                else{

                    sohbetlerYolu.child(aktifKullaniciId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            if(snapshot.hasChild(kullaniciId)){

                                aktifDurum = "Arkadaş";
                                istekGonderBtn.setText("Sohbeti Sil");

                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        if (aktifKullaniciId.equals(kullaniciId))
            istekGonderBtn.setVisibility(View.INVISIBLE);
        else{
            istekGonderBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    istekGonderBtn.setEnabled(false);

                    if(aktifDurum.equals("Yeni")){

                        sohbetTalebiGonder();

                        Toast.makeText(ProfileActivity.this, "Mesaj İsteği Gönderildi", Toast.LENGTH_LONG).show();

                    }
                    if(aktifDurum.equals("talep_gonderildi")){
                        IstegiİptalEt();
                    }
                    if(aktifDurum.equals("talep_alindi")){
                        IstegiKabulEt();
                    }
                    if(aktifDurum.equals("Arkadaş")){
                        OzelSohbetiSil();
                    }

                }
            });
        }
    }

    private void OzelSohbetiSil() {

        sohbetlerYolu.child(aktifKullaniciId).child(kullaniciId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if(task.isSuccessful()){
                    sohbetlerYolu.child(kullaniciId).child(aktifKullaniciId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if(task.isSuccessful()){

                                istekGonderBtn.setEnabled(true);
                                aktifDurum = "Yeni";
                                istekGonderBtn.setText("İSTEK GÖNDER");

                                istekDegerlendirBtn.setVisibility(View.INVISIBLE);
                                istekDegerlendirBtn.setEnabled(false);

                            }
                        }
                    });
                }
            }
        });
    }

    private void IstegiKabulEt() {

        sohbetlerYolu.child(aktifKullaniciId).child(kullaniciId).child("Sohbetler").setValue("Kaydedildi")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if(task.isSuccessful()){

                            sohbetlerYolu.child(kullaniciId).child(aktifKullaniciId).child("Sohbetler").setValue("Kaydedildi")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if(task.isSuccessful()){

                                                sohbetTalebiYolu.child(aktifKullaniciId).child(kullaniciId).removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {

                                                                if(task.isSuccessful()){

                                                                    sohbetTalebiYolu.child(kullaniciId).child(aktifKullaniciId).removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {

                                                                                    istekGonderBtn.setEnabled(true);
                                                                                    aktifDurum = "Arkadaş";
                                                                                    istekGonderBtn.setText("Sohbeti Sil");
                                                                                    istekDegerlendirBtn.setVisibility(View.INVISIBLE);

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

    private void IstegiİptalEt() {

        sohbetTalebiYolu.child(aktifKullaniciId).child(kullaniciId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if(task.isSuccessful()){
                    sohbetTalebiYolu.child(kullaniciId).child(aktifKullaniciId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if(task.isSuccessful()){

                                istekGonderBtn.setEnabled(true);
                                aktifDurum = "Yeni";
                                istekGonderBtn.setText("İSTEK GÖNDER");

                                istekDegerlendirBtn.setVisibility(View.INVISIBLE);
                                istekDegerlendirBtn.setEnabled(false);

                            }
                        }
                    });
                }
            }
        });
    }

    private void sohbetTalebiGonder() {

        sohbetTalebiYolu.child(aktifKullaniciId).child(kullaniciId).child("talep_turu").setValue("Gonderildi")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if(task.isSuccessful()){
                            sohbetTalebiYolu.child(kullaniciId).child(aktifKullaniciId).child("talep_turu").setValue("Alindi")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){

                                        HashMap<String ,String> mesajBildirimMap = new HashMap<>();
                                        mesajBildirimMap.put("Kimden",aktifKullaniciId);
                                        mesajBildirimMap.put("Tur","Talep");

                                        bildirirmYolu.child(kullaniciId).push().setValue(mesajBildirimMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                                if(task.isSuccessful()){

                                                    istekGonderBtn.setEnabled(true);
                                                    aktifDurum = "talep_gonderildi";
                                                    istekGonderBtn.setText("Mesaj isteği iptal");

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


    private void Uygula(User user){
        binding.kullaniciGercekadiGoster.setText(user.getAd());
        binding.kullaniciGerceksoyadiGoster.setText(user.getSoyad());
        binding.kullaniciAdiGoster.setText(user.getKullaniciAdi());
        binding.kullaniciDurumuGoster.setText(user.getHakkimda());
    }
}