package com.mucahid.mm_chat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.ref.Reference;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class AyarlarActivity extends AppCompatActivity {

    private Button güncelleButon;
    private EditText kullaniciAdi,hakkımda,gercekAd,gercekSoyad;
    private CircleImageView kullaniciResmi;

    private FirebaseAuth mmyetki;
    private DatabaseReference veriYolu2;
    private StorageReference kullaniciProfilResmiYolu;
    private StorageTask yuklemeGorevi;

    private String kayitliKullaniciId;

    private static final int galeriSec = 1;
    private ProgressDialog yukleniyorDialog;

    Uri resimUri;
    String myUri = "";

    private Toolbar toolBar1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ayarlar);

        mmyetki = FirebaseAuth.getInstance();
        veriYolu2 = FirebaseDatabase.getInstance().getReference();
        kullaniciProfilResmiYolu = FirebaseStorage.getInstance().getReference().child("Profil Resmi");

        kayitliKullaniciId = mmyetki.getCurrentUser().getUid();

        güncelleButon = findViewById(R.id.ayarlar_guncelle);
        kullaniciAdi = findViewById(R.id.kullanici_adi);
        hakkımda = findViewById(R.id.kullanici_durumu);
        gercekAd = findViewById(R.id.gercek_ad);
        gercekSoyad = findViewById(R.id.gercek_soyad);
        kullaniciResmi = findViewById(R.id.kullanici_foto_ayarlar);
        yukleniyorDialog = new ProgressDialog(this);

        toolBar1 = findViewById(R.id.toolbar1);
        setSupportActionBar(toolBar1);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Hesap Ayarları");

        güncelleButon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                bilgileriGüncelle();

            }
        });

        KullaniciBilgisiAl();

        kullaniciResmi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1,1)
                        .start(AyarlarActivity.this);

            }
        });

    }

    private String dosyaUzantisiAl(Uri uri){

        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();

        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK){

            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            resimUri = result.getUri();
            kullaniciResmi.setImageURI(resimUri);


        }
        else {
            Toast.makeText(AyarlarActivity.this, "Resim Seçilemedi", Toast.LENGTH_LONG).show();
        }

    }

    private void KullaniciBilgisiAl() {

        veriYolu2.child("Kullanicilar").child(kayitliKullaniciId)
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if((snapshot.exists()) && (snapshot.hasChild("KullanıcıAdı") && (snapshot.hasChild("resim")))){

                    String GercekAdiAl = snapshot.child("Ad").getValue().toString();
                    String SoyadAl = snapshot.child("Soyad").getValue().toString();
                    String KullaniciAdiAl = snapshot.child("KullanıcıAdı").getValue().toString();
                    String HakkimdaAl = snapshot.child("hakkımda").getValue().toString();
                    String ProfilResimAl = snapshot.child("resim").getValue().toString();

                    gercekAd.setText(GercekAdiAl);
                    gercekSoyad.setText(SoyadAl);
                    kullaniciAdi.setText(KullaniciAdiAl);
                    hakkımda.setText(HakkimdaAl);
                    if (!ProfilResimAl.isEmpty()){
                        Picasso.get().load(ProfilResimAl).resize(500, 500).into(kullaniciResmi);
                    }


                }
                else if((snapshot.exists())&&(snapshot.hasChild("Ad"))){

                    String GercekAdiAl = snapshot.child("Ad").getValue().toString();
                    String SoyadAl = snapshot.child("Soyad").getValue().toString();
                    String KullaniciAdiAl = snapshot.child("KullanıcıAdı").getValue().toString();
                    String HakkimdaAl = snapshot.child("hakkımda").getValue().toString();

                    gercekAd.setText(GercekAdiAl);
                    gercekSoyad.setText(SoyadAl);
                    kullaniciAdi.setText(KullaniciAdiAl);
                    hakkımda.setText(HakkimdaAl);
                    
                }
                else {
                    kullaniciAdi.setVisibility(View.VISIBLE);
                    Toast.makeText(AyarlarActivity.this, "Lütfen Profil Bilgilerinizi Giriniz", Toast.LENGTH_LONG).show();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void bilgileriGüncelle() {

        String kullaniciAdiAyarla = kullaniciAdi.getText().toString();
        String gercekAdAyarla = gercekAd.getText().toString();
        String gercekSoyadAyarla = gercekSoyad.getText().toString();
        String hakkimdaAyarla = hakkımda.getText().toString();

        if(TextUtils.isEmpty(kullaniciAdiAyarla)){
            Toast.makeText(this, "Lütfen Bir Kullanıcı Adı Giriniz", Toast.LENGTH_LONG).show();
        }
        if(TextUtils.isEmpty(gercekAdAyarla)){
            Toast.makeText(this, "Lütfen Adınızı Giriniz", Toast.LENGTH_LONG).show();
        }
        if(TextUtils.isEmpty(gercekSoyadAyarla)){
            Toast.makeText(this, "Lütfen Soyadınızı Giriniz", Toast.LENGTH_LONG).show();
        }
        if(TextUtils.isEmpty(hakkimdaAyarla)){
            Toast.makeText(this, "Hakkımda Kısmı Boş Olamaz", Toast.LENGTH_LONG).show();
        }
        else {

           if(resimUri != null)
               resimYukle();
           else
               bilgileriKaydet("");

        }

    }

    private void resimYukle() {
        yukleniyorDialog.setTitle("Bilgileriniz Güncelleniyor");
        yukleniyorDialog.setMessage("Lütfen Bekleyiniz");
        yukleniyorDialog.setCanceledOnTouchOutside(false);
        yukleniyorDialog.show();

        if(resimUri == null){

            DatabaseReference veriYolu = FirebaseDatabase.getInstance().getReference().child("Kullanicilar");
            String gonderiId = veriYolu.push().getKey();

            String kullaniciAdiAyarla = kullaniciAdi.getText().toString();
            String gercekAdAyarla = gercekAd.getText().toString();
            String gercekSoyadAyarla = gercekSoyad.getText().toString();
            String hakkimdaAyarla = hakkımda.getText().toString();

            HashMap<String,Object> profilHaritasi = new HashMap<>();
            profilHaritasi.put("uid", kayitliKullaniciId);
            profilHaritasi.put("KullanıcıAdı", kullaniciAdiAyarla);
            profilHaritasi.put("Ad", gercekAdAyarla);
            profilHaritasi.put("Soyad", gercekSoyadAyarla);
            profilHaritasi.put("hakkımda", hakkimdaAyarla);

            veriYolu.child(kayitliKullaniciId).updateChildren(profilHaritasi);

        }

        final StorageReference resimYolu = kullaniciProfilResmiYolu.child(kayitliKullaniciId).child(kayitliKullaniciId + ".jpg");

        UploadTask newRef = resimYolu.putFile(resimUri);

        newRef.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!");
                    System.out.println(task.getException().getMessage());

                    throw task.getException();
                }

                return resimYolu.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();

                    bilgileriKaydet(downloadUri.toString());

                } else {
                    yukleniyorDialog.dismiss();
                    Toast.makeText(AyarlarActivity.this, "Hata", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void bilgileriKaydet(String downloadUri){

        DatabaseReference veriYolu = FirebaseDatabase.getInstance().getReference().child("Kullanicilar");
        String gonderiId = veriYolu.push().getKey();

        String kullaniciAdiAyarla = kullaniciAdi.getText().toString();
        String gercekAdAyarla = gercekAd.getText().toString();
        String gercekSoyadAyarla = gercekSoyad.getText().toString();
        String hakkimdaAyarla = hakkımda.getText().toString();

        if(kullaniciAdiAyarla.isEmpty() || gercekAdAyarla.isEmpty() || gercekSoyadAyarla.isEmpty() || hakkimdaAyarla.isEmpty()){
            Toast.makeText(this, "Lütfen Tüm Alanları Doldurunuz", Toast.LENGTH_LONG).show();
        }
        else {
            HashMap<String,Object> profilHaritasi = new HashMap<>();
            profilHaritasi.put("uid", kayitliKullaniciId);
            profilHaritasi.put("KullanıcıAdı", kullaniciAdiAyarla);
            profilHaritasi.put("Ad", gercekAdAyarla);
            profilHaritasi.put("Soyad", gercekSoyadAyarla);
            profilHaritasi.put("hakkımda", hakkimdaAyarla);
            profilHaritasi.put("resim", downloadUri);

            veriYolu.child(kayitliKullaniciId).updateChildren(profilHaritasi).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    try
                    {
                        Thread.sleep(1500);
                        Intent intent = new Intent(AyarlarActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                    catch(InterruptedException ex)
                    {
                        Thread.currentThread().interrupt();
                    }
                }
            });

        }
    }
}