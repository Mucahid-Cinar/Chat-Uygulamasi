package com.mucahid.mm_chat;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.database.DatabaseReference;
import com.mucahid.mm_chat.util.Db;
import com.mucahid.mm_chat.util.E2eeManager;
import com.mucahid.mm_chat.util.LoadingDialog;

public class RegisterActivity extends AppCompatActivity {

    private EditText emailInput;
    private EditText passwordInput;

    private DatabaseReference rootRef;
    private FirebaseAuth auth;
    private LoadingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        auth = FirebaseAuth.getInstance();
        rootRef = Db.database().getReference();

        Button registerButton = findViewById(R.id.register_button);
        TextView haveAccount = findViewById(R.id.have_account);
        emailInput = findViewById(R.id.register_email);
        passwordInput = findViewById(R.id.register_password);

        loadingDialog = new LoadingDialog(this);

        haveAccount.setOnClickListener(v -> finish());

        registerButton.setOnClickListener(v -> createNewAccount());
    }

    private static String registerErrorMessage(Exception e) {
        if (e instanceof FirebaseAuthUserCollisionException) {
            return "Bu E-posta adresi başka bir kişi tarafından kullanılmakta";
        }
        if (e instanceof FirebaseAuthWeakPasswordException) {
            return "Şifre en az 6 karakter olmalıdır";
        }
        if (e instanceof FirebaseAuthInvalidCredentialsException) {
            return "Geçersiz E-posta adresi";
        }
        if (e instanceof FirebaseNetworkException) {
            return "İnternet bağlantınızı kontrol ediniz";
        }
        return "Hesap oluşturulamadı, lütfen tekrar deneyiniz";
    }

    private void createNewAccount() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString();

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError("Geçerli bir e-posta adresi giriniz");
            emailInput.requestFocus();
            return;
        }
        if (password.length() < 6) {
            passwordInput.setError("Şifre en az 6 karakter olmalıdır");
            passwordInput.requestFocus();
            return;
        }

        loadingDialog.show("Yeni hesap oluşturuluyor", "Lütfen bekleyiniz");

        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
            loadingDialog.dismiss();
            if (task.isSuccessful() && auth.getCurrentUser() != null) {
                String userId = auth.getCurrentUser().getUid();
                rootRef.child(Db.USERS).child(userId).setValue("");

                E2eeManager.onSignedIn(RegisterActivity.this, userId);
                Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(mainIntent);
                finish();

                Toast.makeText(RegisterActivity.this, "Yeni Hesap Oluşturuldu", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(RegisterActivity.this, registerErrorMessage(task.getException()), Toast.LENGTH_LONG).show();
            }
        });
    }
}
