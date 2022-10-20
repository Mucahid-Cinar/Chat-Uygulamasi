package com.mucahid.mm_chat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.cloudmessaging.CloudMessagingReceiver;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceIdReceiver;
import com.google.firebase.installations.FirebaseInstallations;
import com.google.firebase.messaging.FirebaseMessaging;

public class LoginActivity extends AppCompatActivity {

    private Button girisButon, telefonGirisButon;
    private EditText kullaniciEmail, kullaniciSifre;
    private TextView yeniHesap,sifreUnutma;

    private FirebaseAuth mYetki;
    private DatabaseReference kullaniciYolu;

    ProgressDialog girisDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        girisButon=findViewById(R.id.giris_buton);
        telefonGirisButon=findViewById(R.id.telefon_giris);
        kullaniciEmail=findViewById(R.id.giris_email);
        kullaniciSifre=findViewById(R.id.giris_sifre);
        yeniHesap=findViewById(R.id.yeni_hesap);
        sifreUnutma=findViewById(R.id.sifre_unutma);

        girisDialog = new ProgressDialog(this);

        mYetki = FirebaseAuth.getInstance();
        kullaniciYolu = FirebaseDatabase.getInstance().getReference().child("Kullanicilar");

        yeniHesap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent kayitAktivityIntent=new Intent(LoginActivity.this,KayitActivity.class);
                startActivity(kayitAktivityIntent);
            }
        });

        girisButon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GirisİzniVer();
            }
        });

        telefonGirisButon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent telefonGiris = new Intent(LoginActivity.this,Telefongiris.class);
                startActivity(telefonGiris);

            }
        });

    }

    private void GirisİzniVer() {

        String email = kullaniciEmail.getText().toString();
        String sifre = kullaniciSifre.getText().toString();

        if(TextUtils.isEmpty(email)){

            Toast.makeText(this, "E-posta kısmı boş olamaz", Toast.LENGTH_SHORT).show();

        }
        if(TextUtils.isEmpty(sifre)){

            Toast.makeText(this, "Şifre kısmı boş olamaz", Toast.LENGTH_SHORT).show();

        }
        else {

            girisDialog.setTitle("Giriş Yapılıyor");
            girisDialog.setMessage("Lütfen Bekleyiniz");
            girisDialog.setCanceledOnTouchOutside(true);
            girisDialog.show();

            mYetki.signInWithEmailAndPassword(email,sifre)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if(task.isSuccessful()){

                                Intent anaSayfa = new Intent(LoginActivity.this,MainActivity.class);
                                anaSayfa.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(anaSayfa);
                                finish();
                                Toast.makeText(LoginActivity.this, "Giriş Başarılı", Toast.LENGTH_SHORT).show();
                                girisDialog.dismiss();

                            }
                            else {

                                Toast.makeText(LoginActivity.this, "E-posta veya Şifre Hatalı", Toast.LENGTH_SHORT).show();
                                girisDialog.dismiss();

                            }

                        }
                    });

        }

    }


    private void KullaniciyiAnaActivityeGonder() {
        Intent AnaActivityIntent = new Intent(LoginActivity.this,MainActivity.class);
        startActivity(AnaActivityIntent);
    }
}