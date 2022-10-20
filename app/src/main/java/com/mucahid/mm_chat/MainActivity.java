package com.mucahid.mm_chat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {


    private Toolbar mmToolbar;
    private ViewPager mmViewPager;
    private TabLayout mmTabLayout;
    private SekmeErisimAdapter mmsekmeErisimAdapter;
    private TextView grup_adı_olustur_yazdır;

    private FirebaseAuth mmYetki;
    private DatabaseReference KullanicilarReference;

    private String aktifKullaniciId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mmToolbar=findViewById(R.id.ana_sayfa_toolbar);
        setSupportActionBar(mmToolbar);
        getSupportActionBar().setTitle("MM Chat");

        mmViewPager=findViewById(R.id.ana_sekmeler_pager);
        mmsekmeErisimAdapter=new SekmeErisimAdapter(getSupportFragmentManager());
        mmViewPager.setAdapter(mmsekmeErisimAdapter);

        grup_adı_olustur_yazdır =findViewById(R.id.grup_olustur_ad_yazdır);

        mmTabLayout=findViewById(R.id.ana_sekmeler);
        mmTabLayout.setupWithViewPager(mmViewPager);

        mmYetki = FirebaseAuth.getInstance();
        KullanicilarReference = FirebaseDatabase.getInstance().getReference();

        mmTabLayout.getTabAt(0).setCustomView(R.layout.myview);
        mmTabLayout.getTabAt(1).setCustomView(R.layout.myview1);
        mmTabLayout.getTabAt(2).setCustomView(R.layout.myview2);
        mmTabLayout.getTabAt(3).setCustomView(R.layout.myview3);

    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser  mevcutKullanici = mmYetki.getCurrentUser();
        if(mevcutKullanici == null){
            KullaniciyiLoginActivityeGonder();
        }
        else{
            kullaniciDurumuGuncelle("Çevrimiçi");
            KullaniciyiDogrula();
        }
    }

    @Override
    protected void onStop () {
        super.onStop();
        FirebaseUser  mevcutKullanici = mmYetki.getCurrentUser();

        if(mevcutKullanici != null) {
            kullaniciDurumuGuncelle("Çevrimdışı");
        }
    }

    @Override
    protected void onDestroy () {
        super.onDestroy();
        FirebaseUser  mevcutKullanici = mmYetki.getCurrentUser();

        if(mevcutKullanici != null) {
            kullaniciDurumuGuncelle("Çevrimdışı");
        }
    }


    private void KullaniciyiDogrula() {
        String kayitliKullaniciId = mmYetki.getCurrentUser().getUid();

        KullanicilarReference.child("Kullanicilar").child(kayitliKullaniciId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if((snapshot.child("KullanıcıAdı").exists())){

                    //Toast.makeText(MainActivity.this, "Hoş Geldiniz", Toast.LENGTH_LONG).show();

                }
                else{

                    Intent hesap = new Intent(MainActivity.this,AyarlarActivity.class);
                    hesap.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(hesap);
                    finish();

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void KullaniciyiLoginActivityeGonder() {

        Intent LoginIntent = new Intent(MainActivity.this,LoginActivity.class);
        LoginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(LoginIntent);
        finish();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.secenekler_menu,menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);

        if(item.getItemId()==R.id.arkasbul_secenegi){

            Intent arkadasBul = new Intent(MainActivity.this,ArkadasBulActivity.class);
            startActivity(arkadasBul);

        }
        if(item.getItemId()==R.id.grupolustur_secenegi){

            yeniGrupIstegi();


        }
        if(item.getItemId()==R.id.ayarlar_secenegi){

            Intent ayarlar = new Intent(MainActivity.this,AyarlarActivity.class);
            startActivity(ayarlar);

        }
        if(item.getItemId()==R.id.cıkısyap_secenegi){

            kullaniciDurumuGuncelle("Çevrimdışı");
            mmYetki.signOut();
            Intent girisSayfasi = new Intent(MainActivity.this,LoginActivity.class);
            startActivity(girisSayfasi);

        }
        return true;
    }

    private void yeniGrupIstegi() {

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        View customView = getLayoutInflater().inflate(R.layout.grup_olustur_alert_dialog, null);
        dialog.setCancelable(false);
        dialog.setView(customView);

        Button btnIptal = (Button) customView.findViewById(R.id.iptalBtn);
        Button btnOlustur = (Button) customView.findViewById(R.id.olusturBtn);
        EditText grupAdi = (EditText) customView.findViewById(R.id.grupAdı);


        AlertDialog alertDialog = dialog.create();
        alertDialog.show();



        btnIptal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.cancel();
            }
        });

        btnOlustur.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String grupIsmi = grupAdi.getText().toString();

                if(TextUtils.isEmpty(grupIsmi)){
                    Toast.makeText(MainActivity.this, "Grup İsmini Giriniz !", Toast.LENGTH_LONG).show();
                }
                else {
                    yeniGrupOlustur(grupIsmi);

                }

                alertDialog.dismiss();
                mmTabLayout.selectTab(mmTabLayout.getTabAt(1));

            }
        });
    }

    private void yeniGrupOlustur(String grupIsmi) {

        KullanicilarReference.child("Gruplar").child(grupIsmi).setValue("").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()){

                    Toast.makeText(MainActivity.this, grupIsmi+" Adlı Grup Oluşturuldu", Toast.LENGTH_LONG).show();

                }
            }
        });
    }

    private void kullaniciDurumuGuncelle(String durum){

        String kaydedilenAktifZaman;

        Calendar zaman = Calendar.getInstance();

        SimpleDateFormat aktifZaman = new SimpleDateFormat("hh:mm");
        kaydedilenAktifZaman = aktifZaman.format(zaman.getTime());

        HashMap<String,Object> cevrimiciDurumuMap = new HashMap<>();
        cevrimiciDurumuMap.put("zaman",kaydedilenAktifZaman);
        cevrimiciDurumuMap.put("durum",durum);
        aktifKullaniciId = mmYetki.getCurrentUser().getUid();

        KullanicilarReference.child("Kullanicilar").child(aktifKullaniciId).child("kullanici_durumu").updateChildren(cevrimiciDurumuMap);
    }
}