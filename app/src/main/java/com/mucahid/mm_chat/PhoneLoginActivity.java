package com.mucahid.mm_chat;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.mucahid.mm_chat.util.E2eeManager;
import com.mucahid.mm_chat.util.LoadingDialog;

import java.util.concurrent.TimeUnit;

public class PhoneLoginActivity extends AppCompatActivity {

    private static final String STATE_VERIFICATION_ID = "verification_id";

    private Button sendCodeButton;
    private Button verifyCodeButton;
    private EditText phoneNumberInput;
    private EditText verificationCodeInput;

    private String verificationId;
    private LoadingDialog loadingDialog;
    private FirebaseAuth auth;

    private final PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks =
            new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                @Override
                public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                    signInWithPhoneCredential(credential);
                }

                @Override
                public void onVerificationFailed(@NonNull FirebaseException e) {
                    loadingDialog.dismiss();
                    String message;
                    if (e instanceof FirebaseAuthInvalidCredentialsException) {
                        message = "Girdiğiniz Telefon Numarası Hatalı";
                    } else if (e instanceof FirebaseTooManyRequestsException) {
                        message = "Çok Fazla Deneme Yapıldı, Lütfen Daha Sonra Tekrar Deneyiniz";
                    } else {
                        message = "Doğrulama Başarısız, Lütfen Tekrar Deneyiniz";
                    }
                    Toast.makeText(PhoneLoginActivity.this, message, Toast.LENGTH_LONG).show();
                    showPhoneNumberStep();
                }

                @Override
                public void onCodeSent(@NonNull String id, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                    verificationId = id;
                    loadingDialog.dismiss();
                    Toast.makeText(PhoneLoginActivity.this, "Kod Gönderildi", Toast.LENGTH_LONG).show();
                    showVerificationCodeStep();
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);

        sendCodeButton = findViewById(R.id.send_code_button);
        verifyCodeButton = findViewById(R.id.verify_code_button);
        phoneNumberInput = findViewById(R.id.phone_number_input);
        verificationCodeInput = findViewById(R.id.verification_code_input);
        TextView countryCode = findViewById(R.id.country_code);

        findViewById(R.id.change_phone_button).setOnClickListener(v -> {
            verificationId = null;
            verificationCodeInput.setText("");
            showPhoneNumberStep();
        });
        auth = FirebaseAuth.getInstance();
        loadingDialog = new LoadingDialog(this);

        sendCodeButton.setOnClickListener(v -> {
            String phoneNumber = phoneNumberInput.getText().toString().trim();
            if (!phoneNumber.matches("5[0-9]{9}")) {
                phoneNumberInput.setError("Başında 0 olmadan 10 haneli numaranı gir");
                phoneNumberInput.requestFocus();
                return;
            }

            loadingDialog.show("Telefonla Doğrulama", "Lütfen Bekleyiniz...");

            auth.setLanguageCode("tr");
            PhoneAuthOptions options = PhoneAuthOptions.newBuilder(auth)
                    .setPhoneNumber(countryCode.getText().toString() + phoneNumber)
                    .setTimeout(60L, TimeUnit.SECONDS)
                    .setActivity(PhoneLoginActivity.this)
                    .setCallbacks(callbacks)
                    .build();
            PhoneAuthProvider.verifyPhoneNumber(options);
        });

        verifyCodeButton.setOnClickListener(v -> {
            String code = verificationCodeInput.getText().toString().trim();
            if (!code.matches("[0-9]{6}")) {
                Toast.makeText(PhoneLoginActivity.this, "6 haneli doğrulama kodunu giriniz", Toast.LENGTH_SHORT).show();
                return;
            }
            if (verificationId == null) {
                // Code was not sent yet; getCredential would throw.
                Toast.makeText(PhoneLoginActivity.this, "Lütfen Bekleyiniz...", Toast.LENGTH_SHORT).show();
                return;
            }

            loadingDialog.show("Kodla Doğrulama", "Lütfen Bekleyiniz...");
            signInWithPhoneCredential(PhoneAuthProvider.getCredential(verificationId, code));
        });

        // Restore the code step if the system recreated the activity (rotation is handled in the manifest).
        if (savedInstanceState != null) {
            verificationId = savedInstanceState.getString(STATE_VERIFICATION_ID);
            if (verificationId != null) {
                showVerificationCodeStep();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STATE_VERIFICATION_ID, verificationId);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        loadingDialog.dismiss();
    }

    private void showPhoneNumberStep() {
        sendCodeButton.setVisibility(View.VISIBLE);
        phoneNumberInput.setVisibility(View.VISIBLE);
        verifyCodeButton.setVisibility(View.GONE);
        verificationCodeInput.setVisibility(View.GONE);
    }

    private void showVerificationCodeStep() {
        sendCodeButton.setVisibility(View.GONE);
        phoneNumberInput.setVisibility(View.GONE);
        verifyCodeButton.setVisibility(View.VISIBLE);
        verificationCodeInput.setVisibility(View.VISIBLE);
    }

    private void signInWithPhoneCredential(PhoneAuthCredential credential) {
        auth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
            loadingDialog.dismiss();
            if (task.isSuccessful()) {
                E2eeManager.onSignedIn(PhoneLoginActivity.this, auth.getCurrentUser().getUid());
                Toast.makeText(PhoneLoginActivity.this, "Oturumunuz Oluşturuldu", Toast.LENGTH_LONG).show();
                sendUserToMainActivity();
            } else if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                Toast.makeText(PhoneLoginActivity.this, "Doğrulama Kodu Hatalı", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(PhoneLoginActivity.this, "Oturum Oluşturulamadı Bilgileriniz Kontrol Edip Tekrar Deneyiniz", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(PhoneLoginActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}
