package com.example.chessmorph_proj;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.util.Log;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //ориентация вертикальная
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
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
        finish();
    }
    public void quickPlay(View view) {
        Intent intent = new Intent(this, ChessGameSettings.class);
        intent.putExtra("isOnlineGame", true);
        startActivity(intent);
        finish();
    }
    public void fromHandToHand(View view){
        Intent intent = new Intent(this, ChessGameSettings.class);
        intent.putExtra("isOnlineGame", false);
        startActivity(intent);
        finish();
    }
}