package com.mucahid.mm_chat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mucahid.mm_chat.adapter.OzelMesajAdapter;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private String IdMesajAl,KullaniciAdMesajAl,KullaniciResimMesajAl,IdMesajGonder, aktifZaman,Token1,Token2;
    private TextView profilAdi,sonGorulme,sonGorulmeMetni;
    private CircleImageView profilResim;
    private ImageView geriDon;
    private Toolbar chatToolbar;
    private ImageButton chatGonder;
    private EditText chatMesaj;
    private ArrayList<OzelMesaj> ozelMesajList;
    private OzelMesajAdapter ozelMesajAdapter;
    private RecyclerView recyclerView;

    private FirebaseAuth mmYetki;
    private DatabaseReference mesajYolu,kullaniciYolu;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        IdMesajAl = getIntent().getExtras().get("kullanici_id").toString();
        KullaniciAdMesajAl = getIntent().getExtras().get("kullanici_adi").toString();
        KullaniciResimMesajAl = getIntent().getExtras().get("kullanici_resim").toString();

        mmYetki = FirebaseAuth.getInstance();
        profilAdi = findViewById(R.id.profil_adi);
        sonGorulme = findViewById(R.id.profil_songorulme);
        sonGorulmeMetni= findViewById(R.id.profil_songorulmemetni);
        profilResim = findViewById(R.id.profil_resmi);
        geriDon = findViewById(R.id.geri_don);
        chatGonder = findViewById(R.id.chat_gonderbutonu);
        chatMesaj = findViewById(R.id.chat_mesaj);
        recyclerView = findViewById(R.id.recycle_ozel_mesaj);
        recyclerView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
        ozelMesajList = new ArrayList<>();
        ozelMesajAdapter = new OzelMesajAdapter(ozelMesajList, mmYetki.getCurrentUser().getUid());
        recyclerView.setAdapter(ozelMesajAdapter);

        recyclerView.scrollToPosition(recyclerView.getAdapter().getItemCount());

        IdMesajGonder = mmYetki.getCurrentUser().getUid();
        mesajYolu = FirebaseDatabase.getInstance().getReference();
        kullaniciYolu = FirebaseDatabase.getInstance().getReference();

        Token1 = IdMesajAl.substring(0,8)+IdMesajGonder.substring(0,8);
        Token2 = IdMesajGonder.substring(0,8)+IdMesajAl.substring(0,8);



        geriDon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChatActivity.this,MainActivity.class);
                startActivity(intent);
            }
        });

        profilAdi.setText(KullaniciAdMesajAl);
        Picasso.get().load(KullaniciResimMesajAl).into(profilResim);

        chatGonder.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {

                if(TextUtils.isEmpty(chatMesaj.getText())){

                }
                else
                    MesajGonder();
                recyclerView.scrollToPosition(recyclerView.getAdapter().getItemCount());

            }
        });

        chatMesaj.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recyclerView.scrollToPosition(recyclerView.getAdapter().getItemCount());
            }
        });

        getMessage();
        SonGorulmeGoster();

    }



    @RequiresApi(api = Build.VERSION_CODES.O)
    private void MesajGonder() {

        DatabaseReference kullaniciMesajAnahtarYolu = mesajYolu.child("Mesajlar").child(IdMesajGonder).child(IdMesajAl).push();

        String mesajEklemeId = kullaniciMesajAnahtarYolu.getKey();

        String token = mesajEklemeId.substring(0,16);
        System.out.println(token+"GÖNDEREN");
        String mesajMetni = chatMesaj.getText().toString();
        mesajMetni = EncryptionManager.encrypt(mesajMetni,token);

        if(TextUtils.isEmpty(mesajMetni)){

        }

        else {

            String mesajGonderenYolu = "Mesajlar/"+IdMesajGonder+"/"+IdMesajAl;
            String mesajAlanYolu = "Mesajlar/"+IdMesajAl+"/"+IdMesajGonder;

            Calendar zaman = Calendar.getInstance();
            SimpleDateFormat aktifZamanFormat = new SimpleDateFormat("hh:mm");
            aktifZaman = aktifZamanFormat.format(zaman.getTime());

            Map mesajMetniGovdesi = new HashMap();
            mesajMetniGovdesi.put("Mesaj",mesajMetni);
            mesajMetniGovdesi.put("Mesaj_Turu","Metin");
            mesajMetniGovdesi.put("Kimden",IdMesajGonder);
            mesajMetniGovdesi.put("Zaman",aktifZaman);

            Map mesajGovdesiDetay = new HashMap();
            mesajGovdesiDetay.put(mesajGonderenYolu+"/"+mesajEklemeId,mesajMetniGovdesi);
            mesajGovdesiDetay.put(mesajAlanYolu+"/"+mesajEklemeId,mesajMetniGovdesi);

            mesajYolu.updateChildren(mesajGovdesiDetay).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {

                    if(task.isSuccessful()){



                    }
                    else {



                    }

                    chatMesaj.setText("");

                }
            });

        }

    }

    private void getMessage(){

        DatabaseReference kullaniciMesajAnahtarYolu = mesajYolu.child("Mesajlar").child(IdMesajGonder).child(IdMesajAl);

        String mesajEklemeId = kullaniciMesajAnahtarYolu.getKey();

        mesajYolu.child("Mesajlar").child(IdMesajGonder).child(IdMesajAl).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Iterator iteretor = snapshot.getChildren().iterator();
                snapshot.getKey();

                String token = snapshot.getKey().substring(0,16);
                System.out.println(token+"ALAN");

                while (iteretor.hasNext()){
                    OzelMesaj ozelMesaj = new OzelMesaj(
                            (((DataSnapshot) iteretor.next()).getValue()).toString(),
                            EncryptionManager.decrypt((((DataSnapshot) iteretor.next()).getValue().toString()),token),
                            (((DataSnapshot) iteretor.next()).getValue().toString()),
                            (((DataSnapshot) iteretor.next()).getValue().toString())
                    );
                    ozelMesajList.add(ozelMesaj);
                }


                ozelMesajAdapter.notifyDataSetChanged();

                recyclerView.smoothScrollToPosition(recyclerView.getAdapter().getItemCount());


            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void SonGorulmeGoster(){

        kullaniciYolu.child("Kullanicilar").child(IdMesajGonder).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.child("kullanici_durumu").hasChild("durum")){

                    String durum = snapshot.child("kullanici_durumu").child("durum").getValue().toString();
                    String zaman = snapshot.child("kullanici_durumu").child("zaman").getValue().toString();

                    if(durum.equals("Çevrimiçi")){

                        sonGorulmeMetni.setText("Çevrimiçi");
                        sonGorulme.setVisibility(View.INVISIBLE);

                    }else if(durum.equals("Çevrimdışı")){

                        sonGorulmeMetni.setText("Son Görülme: "+zaman);

                    }

                }

                else {

                    sonGorulmeMetni.setText("Çevrimdışı");
                    sonGorulme.setVisibility(View.INVISIBLE);

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}