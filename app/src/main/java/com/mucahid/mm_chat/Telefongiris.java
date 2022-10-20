package com.mucahid.mm_chat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.security.AuthProvider;
import java.util.concurrent.TimeUnit;

public class Telefongiris extends AppCompatActivity {

    private Button dogrulamaButonu,kodDogrulamaButon,buton12;
    private EditText telefonNumarasi,dogrulamaKodu;
    private TextView ulkeKodu;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    private String mmDogrulamaId;
    private PhoneAuthProvider.ForceResendingToken mmResendToken;

    private ProgressDialog yukleniyorEfekt;

    FirebaseAuth mmYetki;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_telefongiris);

        dogrulamaButonu = findViewById(R.id.telefon_dogrulama_kodu_gönder_buton);
        kodDogrulamaButon = findViewById(R.id.telefon_dogrulama_kodu_buton);
        telefonNumarasi = findViewById(R.id.telefon_numarasi);
        dogrulamaKodu = findViewById(R.id.telefon_dogrulama);
        ulkeKodu = findViewById(R.id.ulkekodu);
        mmYetki = FirebaseAuth.getInstance();
        yukleniyorEfekt = new ProgressDialog(this);

        dogrulamaButonu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String telefonNo = telefonNumarasi.getText().toString();
                String ulkeKod = ulkeKodu.getText().toString();
                if (TextUtils.isEmpty(telefonNo)){

                    Toast.makeText(Telefongiris.this, "Lütfen Telefon Numaranızı Doğru Bir Şekilde Giriniz", Toast.LENGTH_LONG).show();

                }
                else{
                    yukleniyorEfekt.setTitle("Telefonla Doğrulama");
                    yukleniyorEfekt.setMessage("Lütfen Bekleyiniz...");
                    yukleniyorEfekt.setCanceledOnTouchOutside(false);
                    yukleniyorEfekt.show();

                    mmYetki.setLanguageCode("tr");
                    PhoneAuthOptions options =
                            PhoneAuthOptions.newBuilder(mmYetki)
                                    .setPhoneNumber(ulkeKod + telefonNo)
                                    .setTimeout(60L, TimeUnit.SECONDS)
                                    .setActivity(Telefongiris.this)
                                    .setCallbacks(callbacks)
                                    .build();
                    PhoneAuthProvider.verifyPhoneNumber(options);


                }
            }
        });



        kodDogrulamaButon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dogrulamaButonu.setVisibility(View.INVISIBLE);
                telefonNumarasi.setVisibility(View.INVISIBLE);

                String dogrulamaKod = dogrulamaKodu.getText().toString();
                
                if(TextUtils.isEmpty(dogrulamaKod)){
                    Toast.makeText(Telefongiris.this, "Doğrulama Kodu Alanı Boş Bırakılamaz", Toast.LENGTH_SHORT).show();
                }
                else {
                    yukleniyorEfekt.setTitle("Kodla Doğrulama");
                    yukleniyorEfekt.setMessage("Lütfen Bekleyiniz...");
                    yukleniyorEfekt.setCanceledOnTouchOutside(false);
                    yukleniyorEfekt.show();

                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mmDogrulamaId, dogrulamaKod);
                    telefonGirisYap(credential);

                }

            }
        });

        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {


                telefonGirisYap(phoneAuthCredential);

            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {


                yukleniyorEfekt.dismiss();

                Toast.makeText(Telefongiris.this, "Girdiğiniz Telefon Numarası Hatalı", Toast.LENGTH_LONG).show();

                dogrulamaButonu.setVisibility(View.VISIBLE);
                kodDogrulamaButon.setVisibility(View.INVISIBLE);

                telefonNumarasi.setVisibility(View.VISIBLE);
                dogrulamaKodu.setVisibility(View.INVISIBLE);

            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {

                mmDogrulamaId = verificationId;
                mmResendToken = token;

                yukleniyorEfekt.dismiss();

                Toast.makeText(Telefongiris.this, "Kod Gönderildi", Toast.LENGTH_LONG).show();

                dogrulamaButonu.setVisibility(View.INVISIBLE);
                kodDogrulamaButon.setVisibility(View.VISIBLE);

                telefonNumarasi.setVisibility(View.INVISIBLE);
                dogrulamaKodu.setVisibility(View.VISIBLE);

            }

        };


    }

    private void telefonGirisYap(PhoneAuthCredential credential) {

        mmYetki.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            yukleniyorEfekt.dismiss();
                            Toast.makeText(Telefongiris.this, "Oturumunuz Oluşturuldu", Toast.LENGTH_LONG).show();
                            kullaniciyiAnaActivityGonder();


                        } else {
                            yukleniyorEfekt.dismiss();
                            Toast.makeText(Telefongiris.this, "Oturum Oluşturulamadı Bilgileriniz Kontrol Edip Tekrar Deneyiniz", Toast.LENGTH_LONG).show();

                        }
                    }
                });
    }

    private void kullaniciyiAnaActivityGonder() {

        Intent anaSayfa = new Intent(Telefongiris.this,MainActivity.class);
        anaSayfa.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(anaSayfa);
        finish();

    }

}