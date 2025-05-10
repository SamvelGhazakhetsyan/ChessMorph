package com.example.chessmorph_proj;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {
    private DatabaseReference database;
    private long lastBackPressedTime = 0;
    private static final int BACK_PRESS_DELAY = 1500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //ориентация вертикальная
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.parseColor("#55000000")); // Темный цвет для статус-бара
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });



    }
    public void logout(View view) {
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(getApplicationContext(),Register.class));
        finish();
    }
    public void play(View view) {

        database = FirebaseDatabase.getInstance().getReference("test");

        // Запись тестового значения
        database.setValue("Hello Firebase!")
                .addOnSuccessListener(aVoid -> Log.d("Firebase", "Данные успешно записаны!"))
                .addOnFailureListener(e -> Log.e("Firebase", "Ошибка записи", e));

        startActivity(new Intent(getApplicationContext(),Board.class));
    }
    public void quickPlay(View view) {
        if (isInternetAvailable()) {
            Intent intent = new Intent(this, ChessGameSettings.class);
            intent.putExtra("isOnlineGame", true);
            startActivity(intent);
        } else {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
        }
    }
    public void fromHandToHand(View view){
        Intent intent = new Intent(this, ChessGameSettings.class);
        intent.putExtra("isOnlineGame", false);
        startActivity(intent);
    }
    public void toProfile(View view){
        Intent intent = new Intent(this, ProfileActivity.class);
        intent.putExtra("isMyProfile", true);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        long currentTime = System.currentTimeMillis();

        if (currentTime - lastBackPressedTime < BACK_PRESS_DELAY) {
            finishAffinity();
        } else {
            lastBackPressedTime = currentTime;
            Toast.makeText(this, "Press again to leave", Toast.LENGTH_SHORT).show();
        }
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