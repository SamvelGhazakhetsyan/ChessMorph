package com.example.chessmorph_proj;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import androidx.appcompat.app.AlertDialog;

public class ChessGameSettings extends AppCompatActivity {
    private Random random = new Random();
    private boolean morphModeOn, isOnlineGame, isWhite = random.nextBoolean(),gameFound=false;
    private long time=300000, plusTime=0;
    FirebaseAuth fAuth;
    private String mode="classic", userId, gameId;
    DatabaseReference gamesRef = FirebaseDatabase.getInstance().getReference("games");
    Intent intent;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //ориентация вертикальная
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chess_game_settings);

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

        fAuth = FirebaseAuth.getInstance();
        userId=fAuth.getCurrentUser().getUid();


        isOnlineGame = getIntent().getBooleanExtra("isOnlineGame", false);


        intent = new Intent(this, Board.class);


        Spinner spinnerMode = findViewById(R.id.spinner_options_mode);

        String[] options = {"Classic", "ChessMorph"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, options);
        spinnerMode.setAdapter(adapter);

        spinnerMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 0:
                        mode="classic";
                        morphModeOn=false;
                        break;
                    case 1:
                        mode="ChessMorph";
                        morphModeOn=true;
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });






        Spinner spinnerTime = findViewById(R.id.spinner_options_time);

        String[] optionsT = {"1 minute","1|+1","5 minutes","5|+3","10 minutes","15|+5"};
        ArrayAdapter<String> adapterT = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, optionsT);
        spinnerTime.setAdapter(adapterT);
        spinnerTime.setSelection(2);

        spinnerTime.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 0:
                        time=60000;
                        plusTime=0;
                        break;
                    case 1:
                        time=60000;
                        plusTime=1000;
                        break;
                    case 2:
                        time=300000;
                        plusTime=0;
                        break;
                    case 3:
                        time=300000;
                        plusTime=3000;
                        break;
                    case 4:
                        time=600000;
                        plusTime=0;
                        break;
                    case 5:
                        time=900000;
                        plusTime=5000;
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }










    public void play(View view) {

        intent.putExtra("time", time);
        intent.putExtra("plusTime", plusTime);
        intent.putExtra("morphModeOn", morphModeOn);
        intent.putExtra("isOnlineGame", isOnlineGame);

        if (isOnlineGame) {
            gameFound=false;
            findGame(mode, (int) time, (int) plusTime, userId, () -> {
                intent.putExtra("isWhite", isWhite);
                intent.putExtra("gameId", gameId);
                intent.putExtra("userId", userId);
                if(gameFound){
                    startActivity(intent);
                    finish();
                }else{
                    View dialogView = getLayoutInflater().inflate(R.layout.dialog_searching, null);
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setView(dialogView);
                    builder.setCancelable(false);

                    AlertDialog searchingDialog = builder.create();

                    Button cancelButton = dialogView.findViewById(R.id.cancel_button);
                    cancelButton.setOnClickListener(v -> {
                        FirebaseDatabase.getInstance().getReference("games").child(gameId).removeValue();
                        searchingDialog.dismiss();
                    });

                    searchingDialog.show();

                    DatabaseReference playersRef = FirebaseDatabase.getInstance().getReference("games").child(gameId).child("players");

                    playersRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            String white = snapshot.child("whitePlayer").getValue(String.class);
                            String black = snapshot.child("blackPlayer").getValue(String.class);

                            if (white != null && !white.isEmpty() && black != null && !black.isEmpty()) {
                                searchingDialog.dismiss();
                                startActivity(intent);
                                finish();

                                playersRef.removeEventListener(this);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            Log.e("Firebase", "Ошибка ожидания второго игрока", error.toException());
                        }
                    });

                }
            });
        } else {
            intent.putExtra("gameId", gameId);
            intent.putExtra("isWhite", isWhite);
            startActivity(intent);
            finish();
        }
    }

    private void findGame(String selectedMode, int selectedTime, int selectedPlusTime, String currentUserId, Runnable onGameFound) {
        DatabaseReference gamesRef = FirebaseDatabase.getInstance().getReference("games");

        gamesRef.orderByChild("parameters/mode").equalTo(selectedMode)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        gameFound = false;

                        for (DataSnapshot gameSnapshot : snapshot.getChildren()) {
                            gameId = gameSnapshot.getKey();
                            Integer time = gameSnapshot.child("parameters/time").getValue(Integer.class);
                            Integer plusTime = gameSnapshot.child("parameters/plusTime").getValue(Integer.class);
                            String whitePlayer = gameSnapshot.child("players/whitePlayer").getValue(String.class);
                            String blackPlayer = gameSnapshot.child("players/blackPlayer").getValue(String.class);

                            if (time != null && plusTime != null && time == selectedTime && plusTime == selectedPlusTime) {
                                if (whitePlayer == null || whitePlayer.isEmpty()) {
                                    isWhite = true;
                                    gamesRef.child(gameId).child("players/whitePlayer").setValue(currentUserId);
                                    gamesRef.child(gameId).child("status").setValue("ongoing");
                                    gamesRef.child(gameId).child("turn").setValue(currentUserId);
                                    Log.d("Chess", "Присоединились к игре (белые): " + gameId);
                                    gameFound = true;
                                    break;
                                } else if (blackPlayer == null || blackPlayer.isEmpty()) {
                                    isWhite = false;
                                    gamesRef.child(gameId).child("players/blackPlayer").setValue(currentUserId);
                                    gamesRef.child(gameId).child("status").setValue("ongoing");
                                    Log.d("Chess", "Присоединились к игре (чёрные): " + gameId);
                                    gameFound = true;
                                    break;
                                }
                            }
                        }

                        if (!gameFound) {
                            createGame(selectedMode, selectedTime, selectedPlusTime, currentUserId);
                        }

                        // Запускаем код после поиска игры
                        onGameFound.run();
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Log.w("Firebase", "Ошибка поиска игры.", error.toException());
                    }
                });
    }


    private void createGame(String mode, int time, int plusTime, String userId) {
        DatabaseReference gamesRef = FirebaseDatabase.getInstance().getReference("games");
        gameId = gamesRef.push().getKey();

        if (gameId != null) {
            DatabaseReference gameRef = gamesRef.child(gameId);

            String whitePlayer = isWhite ? userId : "";
            String blackPlayer = isWhite ? "" : userId;

            Map<String, Object> gameData = new HashMap<>();
            gameData.put("parameters/mode", mode);
            gameData.put("parameters/time", time);
            gameData.put("parameters/plusTime", plusTime);
            gameData.put("players/whitePlayer", whitePlayer);
            gameData.put("players/blackPlayer", blackPlayer);
            gameData.put("turn", whitePlayer);
            gameData.put("status", "waiting"); // Ожидание игрока

            gameRef.updateChildren(gameData);
            Log.d("Chess", "Создана новая игра: " + gameId);
        }
    }







    public void backToMenu(View view) {
        finish();
    }



}