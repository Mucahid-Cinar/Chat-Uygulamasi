package com.mucahid.mm_chat;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.mucahid.mm_chat.util.E2eeManager;
import com.mucahid.mm_chat.util.LoadingDialog;

public class LoginActivity extends AppCompatActivity {

    private EditText emailInput;
    private EditText passwordInput;

    private FirebaseAuth auth;
    private LoadingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Button loginButton = findViewById(R.id.login_button);
        Button phoneLoginButton = findViewById(R.id.phone_login_button);
        TextView createAccount = findViewById(R.id.create_account);
        TextView forgotPassword = findViewById(R.id.forgot_password);
        emailInput = findViewById(R.id.login_email);
        passwordInput = findViewById(R.id.login_password);

        loadingDialog = new LoadingDialog(this);
        auth = FirebaseAuth.getInstance();

        createAccount.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));

        loginButton.setOnClickListener(v -> signIn());

        forgotPassword.setOnClickListener(v -> sendPasswordReset());

        phoneLoginButton.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, PhoneLoginActivity.class)));
    }

    private void sendPasswordReset() {
        String email = emailInput.getText().toString().trim();
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError("Geçerli bir e-posta adresi giriniz");
            emailInput.requestFocus();
            return;
        }

        loadingDialog.show("Şifre Sıfırlama", "Lütfen Bekleyiniz");
        auth.sendPasswordResetEmail(email).addOnCompleteListener(this, task -> {
            loadingDialog.dismiss();
            if (task.isSuccessful()) {
                Toast.makeText(LoginActivity.this, "Şifre sıfırlama bağlantısı e-posta adresinize gönderildi", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(LoginActivity.this, "Şifre sıfırlama e-postası gönderilemedi", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        loadingDialog.dismiss();
    }

    private void signIn() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString();

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError("Geçerli bir e-posta adresi giriniz");
            emailInput.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            passwordInput.setError("Şifre kısmı boş olamaz");
            passwordInput.requestFocus();
            return;
        }

        loadingDialog.show("Giriş Yapılıyor", "Lütfen Bekleyiniz");

        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
            loadingDialog.dismiss();
            if (task.isSuccessful()) {
                E2eeManager.onSignedIn(LoginActivity.this, auth.getCurrentUser().getUid());
                Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(mainIntent);
                finish();
                Toast.makeText(LoginActivity.this, "Giriş Başarılı", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(LoginActivity.this, "E-posta veya Şifre Hatalı", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
