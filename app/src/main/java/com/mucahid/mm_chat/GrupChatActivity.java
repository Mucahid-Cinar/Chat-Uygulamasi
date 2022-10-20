package com.mucahid.mm_chat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Document;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class GrupChatActivity extends AppCompatActivity {

    private Toolbar mmToolbar;
    private ImageButton mesajGonderButonu;
    private EditText mesajBilgisi;
    private TextView mesajlariGoster,mesajlariGosterUser;
    private ScrollView mmScrollView;
    private RecyclerView recyclerView;
    private ArrayList<Mesaj> mesajList;
    private MesajAdapter mesajAdapter;
    private Toolbar toolBar2;

    private FirebaseAuth mmYetki;
    private DatabaseReference kullaniciYolu,grupAdiYolu,grupMesajAnahtariYolu;

    private String mevcutGrupAdi,aktifKullaniciId,aktifKullaniciAdi,aktifTarih,aktifZaman;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grup_chat);

        mevcutGrupAdi = getIntent().getExtras().get("grupAdı").toString();

        mmYetki = FirebaseAuth.getInstance();
        aktifKullaniciId = mmYetki.getCurrentUser().getUid();
        kullaniciYolu = FirebaseDatabase.getInstance().getReference().child("Kullanicilar");
        grupAdiYolu = FirebaseDatabase.getInstance().getReference().child("Gruplar").child(mevcutGrupAdi);

        mmToolbar = findViewById(R.id.grupchat_bar);
        setSupportActionBar(mmToolbar);
        getSupportActionBar().setTitle(mevcutGrupAdi);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        mesajGonderButonu = findViewById(R.id.grupchat_gonderbutonu);
        mesajBilgisi = findViewById(R.id.grupchat_mesaj);
        recyclerView = findViewById(R.id.recycle_mesaj);
        mmScrollView = findViewById(R.id.scrollView);
        mesajList = new ArrayList<>();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mesajAdapter = new MesajAdapter(mesajList);
        mesajAdapter.kullaniciId = aktifKullaniciId;
        recyclerView.setAdapter(mesajAdapter);


        kullaniciBilgisiAl();
        mesajGonderButonu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mesajiVeritabaninaKaydet();
                mesajBilgisi.setText("");

                mmScrollView.fullScroll(ScrollView.FOCUS_DOWN);

            }
        });

        getMessage();

    }

    private void getMessage(){
        grupAdiYolu.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Iterator iteretor = snapshot.getChildren().iterator();

                while (iteretor.hasNext()){
                    Mesaj mesaj = new Mesaj(
                            (((DataSnapshot) iteretor.next()).getValue()).toString(),
                            (((DataSnapshot) iteretor.next()).getValue().toString()),
                            (((DataSnapshot) iteretor.next()).getValue().toString()),
                            (((DataSnapshot) iteretor.next()).getValue().toString()),
                            (((DataSnapshot) iteretor.next()).getValue().toString())
                    );
                    mesajList.add(mesaj);
                }
                mesajAdapter.notifyDataSetChanged();
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

    @Override
    protected void onStart() {
        super.onStart();

    }

    private void mesajiVeritabaninaKaydet() {

        String mesaj = mesajBilgisi.getText().toString();
        String mesajAnahtari = grupAdiYolu.push().getKey();

        if(TextUtils.isEmpty(mesaj)){
            Toast.makeText(this, "Lütfen Bir Mesaj Giriniz", Toast.LENGTH_LONG).show();
        }
        else {

            Calendar tarih = Calendar.getInstance();
            SimpleDateFormat aktifTarihFormat = new SimpleDateFormat("MM dd, yyyy");
            aktifTarih = aktifTarihFormat.format(tarih.getTime());

            Calendar zaman = Calendar.getInstance();
            SimpleDateFormat aktifZamanFormat = new SimpleDateFormat("hh:mm");
            aktifZaman = aktifZamanFormat.format(zaman.getTime());

            HashMap<String,Object> grupMesajAnahatari = new HashMap<>();
            grupAdiYolu.updateChildren(grupMesajAnahatari);

            grupMesajAnahtariYolu = grupAdiYolu.child(mesajAnahtari);

            HashMap<String,Object> mesajBilgisiMap = new HashMap<>();

            mesajBilgisiMap.put("KullanıcıAdı",aktifKullaniciAdi);
            mesajBilgisiMap.put("mesaj",mesaj);
            mesajBilgisiMap.put("tarih",aktifTarih);
            mesajBilgisiMap.put("zaman",aktifZaman);
            mesajBilgisiMap.put("KullanıcıId",aktifKullaniciId);

            grupMesajAnahtariYolu.updateChildren(mesajBilgisiMap);

        }

    }

    private void kullaniciBilgisiAl() {

        kullaniciYolu.child(aktifKullaniciId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.exists()){
                    aktifKullaniciAdi = snapshot.child("KullanıcıAdı").getValue().toString();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}