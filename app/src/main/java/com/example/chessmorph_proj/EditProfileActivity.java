package com.example.chessmorph_proj;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.annotations.Nullable;

import com.yalantis.ucrop.UCrop;


public class EditProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageView profileImage;
    private EditText nicknameInput, firstNameInput, lastNameInput;
    private Uri imageUri,selectedImageUri,croppedImageUri;
    private FirebaseAuth fAuth;
    private DatabaseReference userRef;
    private String userId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //ориентация вертикальная
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_profile);
        try {
            MediaManager.get();
        } catch (IllegalStateException e) {
            Map<String, String> config = new HashMap<>();
            config.put("cloud_name", "secret");
            config.put("api_key", "secret");
            config.put("api_secret", "secret");
            MediaManager.init(this, config);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.parseColor("#55000000")); // Темный цвет для статус-бара
        }
        userId=FirebaseAuth.getInstance().getUid();

        profileImage = findViewById(R.id.profileImage);
        nicknameInput = findViewById(R.id.nicknameInput);
        firstNameInput = findViewById(R.id.firstNameInput);
        lastNameInput = findViewById(R.id.lastNameInput);
        View avatarClickZone = findViewById(R.id.view2);

        fAuth = FirebaseAuth.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference("Users").child(fAuth.getUid());

        // Клик по области выбора аватарки
        avatarClickZone.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });

        ImageView profileImage = findViewById(R.id.profileImage);
        profileImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });



        ImageButton confirmButton = findViewById(R.id.confirmButton);
        confirmButton.setOnClickListener(v -> {
            String nickname = nicknameInput.getText().toString().trim();
            String name = firstNameInput.getText().toString().trim();
            String surname = lastNameInput.getText().toString().trim();

            Map<String, Object> updates = new HashMap<>();

            SharedPreferences prefs = getSharedPreferences(fAuth.getCurrentUser().getUid(), MODE_PRIVATE);

            if (!nickname.isEmpty()) {
                updates.put("nickname", nickname);
                prefs.edit().putString("nickname", nickname).apply();
            }
            if (!name.isEmpty()) {
                updates.put("name", name);
                prefs.edit().putString("name", "").apply();
            }
            if (!surname.isEmpty()) {
                updates.put("surname", surname);
                prefs.edit().putString("surname", "").apply();
            }
            if (selectedImageUri != null) {
                updates.put("imageUri", selectedImageUri.toString());
            }

            if (!updates.isEmpty()) {
                if (updates.containsKey("imageUri")) {
                    updates.remove("imageUri");
                }
                DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
                userRef.updateChildren(updates)
                        .addOnSuccessListener(unused -> Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());

                if (selectedImageUri != null) {
                    MediaManager.get().upload(selectedImageUri)
                            .option("folder", "user_avatars") // папка в Cloudinary (необязательно)
                            .option("public_id", userId) // уникальное имя файла — например, ID пользователя
                            .callback(new UploadCallback() {
                                @Override
                                public void onStart(String requestId) {
                                    // загрузка началась
                                }

                                @Override
                                public void onProgress(String requestId, long bytes, long totalBytes) {
                                    // прогресс загрузки
                                }

                                @Override
                                public void onSuccess(String requestId, Map resultData) {
                                    String imageUrl = (String) resultData.get("secure_url");
                                    Log.d("Cloudinary", "Uploaded image URL: " + imageUrl);


                                    userRef.child("avatarUrl").setValue(imageUrl);
                                    prefs.edit().putString("image", imageUrl).apply();
                                }

                                @Override
                                public void onError(String requestId, ErrorInfo error) {
                                    Log.e("Cloudinary", "Upload error: " + error.getDescription());
                                }

                                @Override
                                public void onReschedule(String requestId, ErrorInfo error) {
                                    // если Cloudinary решил отложить загрузку
                                }
                            })
                            .dispatch();
                }
            } else {
                startActivity(new Intent(EditProfileActivity.this, ProfileActivity.class));
                finish();
            }
        });



        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri sourceUri = data.getData();
            Uri destinationUri = Uri.fromFile(new File(getCacheDir(), "cropped.jpg"));

            UCrop.Options options = new UCrop.Options();

            options.setCompressionFormat(Bitmap.CompressFormat.JPEG);

            options.setCompressionQuality(80); // например, 80%

            options.withMaxResultSize(424, 424); // ширина и высота

            UCrop.of(sourceUri, destinationUri)
                    .withAspectRatio(1, 1) // квадратная обрезка
                    .withMaxResultSize(500, 500)
                    .start(this); // запускаем uCrop
        }

        // после обрезки
        else if (requestCode == UCrop.REQUEST_CROP && resultCode == RESULT_OK) {
            final Uri resultUri = UCrop.getOutput(data);
            if (resultUri != null) {
                profileImage.setImageDrawable(null);
                selectedImageUri = resultUri;
                ImageView profileImage = findViewById(R.id.profileImage);
                profileImage.setImageURI(selectedImageUri);
            }
        } else if (requestCode == UCrop.REQUEST_CROP && resultCode == UCrop.RESULT_ERROR) {
            final Throwable cropError = UCrop.getError(data);
            if (cropError != null) cropError.printStackTrace();
        }
    }




    public void backToMenu(View view) {
        startActivity(new Intent(EditProfileActivity.this, ProfileActivity.class));
        finish();
    }
}