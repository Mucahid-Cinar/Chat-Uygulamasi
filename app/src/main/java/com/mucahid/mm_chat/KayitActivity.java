package com.mucahid.mm_chat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class KayitActivity extends AppCompatActivity {

    private Button kayitButon;
    private EditText kayitEmail, kayitSifre;
    private TextView hesabımVar;

    private DatabaseReference dbReference;
    private FirebaseAuth mYetki;

    private ProgressDialog yukleniyorDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kayit);

        mYetki = FirebaseAuth.getInstance();
        dbReference = FirebaseDatabase.getInstance().getReference();

        kayitButon=findViewById(R.id.kayit_buton);
        kayitEmail=findViewById(R.id.kayit_email);
        kayitSifre=findViewById(R.id.kayit_sifre);
        hesabımVar=findViewById(R.id.hesabım_var);

        yukleniyorDialog=new ProgressDialog(this);

        hesabımVar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent loginAktivityIntent=new Intent(KayitActivity.this,LoginActivity.class);
                startActivity(loginAktivityIntent);
            }
        });

        kayitButon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                YeniHesapOlustur();
            }
        });

    }


    private void YeniHesapOlustur() {


        String email = kayitEmail.getText().toString();
        String sifre = kayitSifre.getText().toString();

        if(TextUtils.isEmpty(email)){
            Toast.makeText(this, "E-posta alanı boş olamaz!", Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(sifre)){
            Toast.makeText(this, "Şifre alanı boş olamaz!", Toast.LENGTH_SHORT).show();
        }
        else {

            yukleniyorDialog.setTitle("Yeni hesap oluşturuluyor");
            yukleniyorDialog.setMessage("Lütfen bekleyiniz");
            yukleniyorDialog.setCanceledOnTouchOutside(true);
            yukleniyorDialog.show();

        mYetki.createUserWithEmailAndPassword(email,sifre)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if(task.isSuccessful()){

                            String kayitliKullaniciId = mYetki.getCurrentUser().getUid();
                            dbReference.child("Kullanicilar").child(kayitliKullaniciId).setValue("");

                            Intent anaSyafa=new Intent(KayitActivity.this,MainActivity.class);
                            anaSyafa.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(anaSyafa);
                            finish();

                            Toast.makeText(KayitActivity.this, "Yeni Hesap Oluşturuldu", Toast.LENGTH_SHORT).show();
                            yukleniyorDialog.dismiss();
                        }
                        else {

                            Toast.makeText(KayitActivity.this, "Bu E-posta adresi başka bir kişi tarafından kullanılmakta", Toast.LENGTH_SHORT).show();
                            yukleniyorDialog.dismiss();

                        }

                    }
                });
        }

    }
}