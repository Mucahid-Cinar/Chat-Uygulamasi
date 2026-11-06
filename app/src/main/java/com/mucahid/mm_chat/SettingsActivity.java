package com.mucahid.mm_chat;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.os.BundleCompat;

import com.canhub.cropper.CropException;
import com.canhub.cropper.CropImageOptions;
import com.canhub.cropper.CropImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.mucahid.mm_chat.model.User;
import com.mucahid.mm_chat.util.CropImageResultContract;
import com.mucahid.mm_chat.util.Db;
import com.mucahid.mm_chat.util.ImageLoader;
import com.mucahid.mm_chat.util.LoadingDialog;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private static final String STATE_SELECTED_IMAGE = "selected_image";

    private EditText usernameInput;
    private EditText aboutInput;
    private EditText firstNameInput;
    private EditText lastNameInput;
    private CircleImageView userPhoto;

    private DatabaseReference usersRef;
    private StorageReference profileImagesRef;
    private String currentUserId;

    private LoadingDialog loadingDialog;
    private Uri selectedImageUri;

    private final ActivityResultLauncher<CropImageOptions> cropImageLauncher =
            registerForActivityResult(new CropImageResultContract(), result -> {
                if (result.isSuccessful() && result.getUriContent() != null) {
                    selectedImageUri = result.getUriContent();
                    userPhoto.setImageURI(selectedImageUri);
                } else if (!(result.getError() instanceof CropException.Cancellation)) {
                    // The user backing out of the cropper is not an error.
                    Toast.makeText(SettingsActivity.this, "Resim Seçilemedi", Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            finish();
            return;
        }
        currentUserId = currentUser.getUid();
        usersRef = Db.database().getReference().child(Db.USERS);
        profileImagesRef = Db.storage().getReference().child(Db.PROFILE_IMAGES);

        Button updateButton = findViewById(R.id.settings_update_button);
        usernameInput = findViewById(R.id.user_name);
        aboutInput = findViewById(R.id.about_input);
        firstNameInput = findViewById(R.id.first_name);
        lastNameInput = findViewById(R.id.last_name);
        userPhoto = findViewById(R.id.user_photo);
        loadingDialog = new LoadingDialog(this);

        Toolbar toolbar = findViewById(R.id.settings_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Hesap Ayarları");

        updateButton.setOnClickListener(v -> updateSettings());
        userPhoto.setOnClickListener(v -> openImageCropper());

        if (savedInstanceState != null) {
            selectedImageUri = BundleCompat.getParcelable(savedInstanceState, STATE_SELECTED_IMAGE, Uri.class);
        }
        if (selectedImageUri != null) {
            userPhoto.setImageURI(selectedImageUri);
        }
        // After a rotation the EditTexts restore what the user typed; do not overwrite them.
        loadUserInfo(savedInstanceState == null);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(STATE_SELECTED_IMAGE, selectedImageUri);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (loadingDialog != null) {
            loadingDialog.dismiss();
        }
    }

    private void openImageCropper() {
        CropImageOptions options = new CropImageOptions();
        options.guidelines = CropImageView.Guidelines.ON;
        options.fixAspectRatio = true;
        options.aspectRatioX = 1;
        options.aspectRatioY = 1;
        options.activityTitle = "Resmi Kırp";
        // Keep uploads small (storage.rules allow at most 5 MB); a 1024px JPEG is plenty for an avatar.
        options.outputRequestWidth = 1024;
        options.outputRequestHeight = 1024;
        options.outputRequestSizeOptions = CropImageView.RequestSizeOptions.RESIZE_INSIDE;
        options.outputCompressQuality = 85;
        cropImageLauncher.launch(options);
    }

    private void loadUserInfo(boolean fillTextFields) {
        usersRef.child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.hasChildren() ? snapshot.getValue(User.class) : null;
                if (user == null || user.getUsername() == null) {
                    if (fillTextFields) {
                        Toast.makeText(SettingsActivity.this, "Lütfen Profil Bilgilerinizi Giriniz", Toast.LENGTH_LONG).show();
                    }
                    return;
                }

                if (fillTextFields) {
                    firstNameInput.setText(user.getFirstName());
                    lastNameInput.setText(user.getLastName());
                    usernameInput.setText(user.getUsername());
                    aboutInput.setText(user.getAbout());
                }
                if (selectedImageUri == null && !TextUtils.isEmpty(user.getImage())) {
                    ImageLoader.loadProfileImage(user.getImage(), userPhoto);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private static boolean isBlank(EditText input) {
        return input.getText().toString().trim().isEmpty();
    }

    private void updateSettings() {
        if (isBlank(usernameInput)) {
            Toast.makeText(this, "Lütfen Bir Kullanıcı Adı Giriniz", Toast.LENGTH_LONG).show();
            return;
        }
        if (isBlank(firstNameInput)) {
            Toast.makeText(this, "Lütfen Adınızı Giriniz", Toast.LENGTH_LONG).show();
            return;
        }
        if (isBlank(lastNameInput)) {
            Toast.makeText(this, "Lütfen Soyadınızı Giriniz", Toast.LENGTH_LONG).show();
            return;
        }
        if (isBlank(aboutInput)) {
            Toast.makeText(this, "Hakkımda Kısmı Boş Olamaz", Toast.LENGTH_LONG).show();
            return;
        }

        loadingDialog.show("Bilgileriniz Güncelleniyor", "Lütfen Bekleyiniz");

        if (selectedImageUri != null) {
            uploadImageAndSave();
        } else {
            saveUserInfo(null);
        }
    }

    private void uploadImageAndSave() {
        StorageReference imageRef = profileImagesRef.child(currentUserId).child(currentUserId + ".jpg");

        // Explicit type: a cropped file:// uri has no content type, and storage.rules only accept images.
        StorageMetadata metadata = new StorageMetadata.Builder().setContentType("image/jpeg").build();
        imageRef.putFile(selectedImageUri, metadata)
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return imageRef.getDownloadUrl();
                })
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        saveUserInfo(task.getResult().toString());
                    } else {
                        // e.g. Storage unavailable on the free plan: still save the rest of the profile.
                        Toast.makeText(SettingsActivity.this, "Profil resmi yüklenemedi, bilgileriniz resimsiz kaydediliyor", Toast.LENGTH_LONG).show();
                        selectedImageUri = null;
                        saveUserInfo(null);
                    }
                });
    }

    /** @param imageUrl new profile image url, or null to keep the existing image. */
    private void saveUserInfo(@Nullable String imageUrl) {
        Map<String, Object> profile = new HashMap<>();
        profile.put(Db.UID, currentUserId);
        profile.put(Db.USERNAME, usernameInput.getText().toString().trim());
        profile.put(Db.FIRST_NAME, firstNameInput.getText().toString().trim());
        profile.put(Db.LAST_NAME, lastNameInput.getText().toString().trim());
        profile.put(Db.ABOUT, aboutInput.getText().toString().trim());
        if (imageUrl != null) {
            profile.put(Db.IMAGE, imageUrl);
        }

        usersRef.child(currentUserId).updateChildren(profile).addOnCompleteListener(task -> {
            loadingDialog.dismiss();
            if (task.isSuccessful()) {
                Intent mainIntent = new Intent(SettingsActivity.this, MainActivity.class);
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(mainIntent);
                finish();
            } else {
                Toast.makeText(SettingsActivity.this, "Hata", Toast.LENGTH_LONG).show();
            }
        });
    }
}
