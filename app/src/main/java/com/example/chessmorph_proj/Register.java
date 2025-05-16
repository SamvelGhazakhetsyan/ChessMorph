package com.example.chessmorph_proj;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;
import java.util.Map;

public class Register extends AppCompatActivity {
    EditText mUserName,mEmail,mPassword,mPassword2;
    Button mRegBtn;
    TextView mForLog;
    FirebaseAuth fAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //ориентация вертикальная
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        mUserName=findViewById(R.id.userName);
        mEmail=findViewById(R.id.Email);
        mPassword=findViewById(R.id.password);
        mPassword2=findViewById(R.id.password2);
        mRegBtn=findViewById(R.id.regBtn);
        mForLog=findViewById(R.id.createText);

        fAuth=FirebaseAuth.getInstance();


        mRegBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userName=mUserName.getText().toString().trim();
                String email=mEmail.getText().toString().trim();
                String password=mPassword.getText().toString().trim();
                String password2=mPassword2.getText().toString().trim();

                if (!isInternetAvailable()) {
                    Toast.makeText(Register.this, "No internet connection", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(userName)){
                    mUserName.setError("Username is Required");
                    return;
                }
                if (TextUtils.isEmpty(email)){
                    mEmail.setError("Email is Required");
                    return;
                }
                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    mEmail.setError("Invalid Email");
                    return;
                }
                if(TextUtils.isEmpty(password)){
                    mPassword.setError("Password is Required");
                    return;
                }
                if (TextUtils.isEmpty(password2)){
                    mPassword2.setError("Repeat Password");
                    return;
                }
                if (password.length()<6) {
                    mPassword.setError("Password must be >=6 characters");
                    return;
                }
                if (!password.equals(password2)){
                    mPassword2.setError("Passwords do not match");
                    return;
                }


                fAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            FirebaseUser user=fAuth.getCurrentUser();
                            if (user != null){
                                user.sendEmailVerification().addOnCompleteListener(verificationTask -> {
                                    if (verificationTask.isSuccessful()) {
                                        Toast.makeText(Register.this, "Verification email sent", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(Register.this, "Failed to send verification email", Toast.LENGTH_SHORT).show();
                                    }
                                });
                                Toast.makeText(Register.this, "User Created. Please verify your email before logging in.", Toast.LENGTH_SHORT).show();


                            }
                        }else {
                            Toast.makeText(Register.this,"Error! "+task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                });

                AlertDialog.Builder verifInReg = new AlertDialog.Builder(v.getContext());
                verifInReg.setTitle("Please verify your email");
                verifInReg.setMessage("Verification email sent");

                    // Создаем кастомный макет для диалога
                View customView = getLayoutInflater().inflate(R.layout.custom_dialog, null);
                verifInReg.setView(customView);

                AlertDialog dialog = verifInReg.create();
                dialog.show();
                dialog.setCanceledOnTouchOutside(false);

                    // Настройка первой кнопки "Check"
                Button checkButton = customView.findViewById(R.id.checkButton);
                checkButton.setOnClickListener(v1 -> {
                    FirebaseUser user = fAuth.getCurrentUser();

                    // Обновляем данные пользователя, чтобы проверить актуальное состояние
                    user.reload().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            if (user.isEmailVerified()) {
                                FirebaseUser user1 = FirebaseAuth.getInstance().getCurrentUser();
                                DatabaseReference database = FirebaseDatabase.getInstance().getReference("users");
                                String uid = user.getUid();

                                Map<String, Object> profile = new HashMap<>();
                                profile.put("userId", fAuth.getCurrentUser().getUid());
                                profile.put("nickname", userName);
                                profile.put("name", "");
                                profile.put("surname", "");
                                profile.put("avatar", "");
                                profile.put("registrationDate", ServerValue.TIMESTAMP);
                                profile.put("chessRating", 1000);
                                profile.put("chessMorphRating", 1000);
                                profile.put("games", 0);
                                profile.put("wins", 0);
                                database.child(uid).setValue(profile);

                                SharedPreferences prefs = getSharedPreferences(fAuth.getCurrentUser().getUid(), MODE_PRIVATE);
                                prefs.edit().putString("nickname", userName).apply();
                                prefs.edit().putString("name", "").apply();
                                prefs.edit().putString("surname", "").apply();
                                prefs.edit().putInt("chessRating", 1000).apply();
                                prefs.edit().putInt("chessMorphRating", 1000).apply();
                                prefs.edit().putInt("games", 0).apply();
                                prefs.edit().putInt("wins", 0).apply();
                                prefs.edit().putString("image", "").apply();


                                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                finish();
                                Toast.makeText(Register.this, "Logged in successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(Register.this, "Please verify your email", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(Register.this, "Failed to reload user data", Toast.LENGTH_SHORT).show();
                        }
                    });
                    // Диалог не закрывается, так как мы не вызываем dialog.dismiss()
                });

                    // Настройка второй кнопки "Sent again"
                Button resendButton = customView.findViewById(R.id.resendButton);
                resendButton.setOnClickListener(v12 -> {
                    FirebaseUser user = fAuth.getCurrentUser();
                    user.sendEmailVerification().addOnCompleteListener(verificationTask -> {
                        if (verificationTask.isSuccessful()) {
                            Toast.makeText(Register.this, "Verification email sent", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(Register.this, "Please try later", Toast.LENGTH_SHORT).show();
                        }
                    });
                    // Диалог не закрывается, так как мы не вызываем dialog.dismiss()
                });

            }
        });

        mForLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),Login.class));
            }
        });
    }

    public boolean isInternetAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkCapabilities capabilities = cm.getNetworkCapabilities(cm.getActiveNetwork());
            return capabilities != null &&
                    (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                            || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR));
        }
        return false;
    }
}

