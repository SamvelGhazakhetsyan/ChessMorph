package com.example.chessmorph_proj;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ProfileActivity extends AppCompatActivity {

    private String userId,nickname,name,surname,myNickname,myName,mySurname;
    private boolean isMyProfile;
    private long regMillis,myRegMillis;
    FirebaseAuth fAuth;
    DatabaseReference ref;
    SharedPreferences prefs;
    private Integer wins, games, chessRating, chessMorphRating;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //ориентация вертикальная
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.parseColor("#55000000")); // Темный цвет для статус-бара
        }
        isMyProfile = getIntent().getBooleanExtra("isMyProfile", true);




        fAuth = FirebaseAuth.getInstance();

        prefs = getSharedPreferences(fAuth.getCurrentUser().getUid(), MODE_PRIVATE);






        if (isMyProfile) {
            userId=fAuth.getCurrentUser().getUid();

            TextView nickText = findViewById(R.id.nickText);
            nickText.setText(prefs.getString("nickname", ""));

            TextView nameText = findViewById(R.id.nameText);
            nameText.setText(prefs.getString("name", ""));

            TextView surnameText = findViewById(R.id.surnameText);
            surnameText.setText(prefs.getString("surname", ""));

            TextView chessRating=findViewById(R.id.chessRatingInt);
            chessRating.setText(""+prefs.getInt("chessRating", 0));

            TextView chessMorphRating=findViewById(R.id.chessMorphRatingInt);
            chessMorphRating.setText(""+prefs.getInt("chessMorphRating", 0));

            games=prefs.getInt("games", 0);
            TextView gamesInt = findViewById(R.id.gamesInt);
            gamesInt.setText(""+prefs.getInt("games", 0));

            wins=prefs.getInt("wins", 0);
            TextView winsInt = findViewById(R.id.winsInt);
            winsInt.setText(""+prefs.getInt("wins", 0));

            TextView loosesInt = findViewById(R.id.lossesInt);
            int losses = games - wins;
            loosesInt.setText(""+losses);

            if (games>0){
                int winRate = (wins * 100) / games;
                TextView winRateInt = findViewById(R.id.winRateInt);
                winRateInt.setText(""+winRate+"%");
            }

            String imageUri = prefs.getString("image", "");
            if(imageUri != "") {
                ImageView avatarImageView = findViewById(R.id.imageView);
                Glide.with(ProfileActivity.this)
                        .load(imageUri)
                        .placeholder(R.drawable.profile_images)
                        .into(avatarImageView);
            }

        } else {
            userId = getIntent().getStringExtra("opponentId");
        }

        ImageButton menuButton = findViewById(R.id.menuButton);
        if(isMyProfile) {
            menuButton.setOnClickListener(v -> {
                PopupMenu popupMenu = new PopupMenu(ProfileActivity.this, v);
                popupMenu.getMenuInflater().inflate(R.menu.profile_menu, popupMenu.getMenu());

                popupMenu.setOnMenuItemClickListener(item -> {
                    int id = item.getItemId();
                    if (id == R.id.edit_profile) {
                        // Переход на EditProfileActivity
                        Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
                        startActivity(intent);
                        finish();
                        return true;
                    } else if (id == R.id.logout) {
                        AlertDialog.Builder suerToLeave = new AlertDialog.Builder(this);
                        suerToLeave.setTitle("Are you sure?");

                        suerToLeave.setPositiveButton("OK", (dialog, which) -> {
                            FirebaseAuth.getInstance().signOut();
                            startActivity(new Intent(ProfileActivity.this, Register.class));
                            finish();
                        });
                        suerToLeave.setNegativeButton("CANCEL", (dialog, which) -> {
                            return;
                        });

                        AlertDialog dialog = suerToLeave.create();
                        dialog.show();
                        return true;
                    }
                    return false;
                });

                popupMenu.show();
            });
        }else{
            menuButton.setOnClickListener(v -> {
                PopupMenu popupMenu = new PopupMenu(ProfileActivity.this, v);
                popupMenu.getMenuInflater().inflate(R.menu.profile_menu_other, popupMenu.getMenu());

                popupMenu.setOnMenuItemClickListener(item -> {
                    int id = item.getItemId();
                    if (id == R.id.report) {
                        DatabaseReference reportRef = FirebaseDatabase.getInstance()
                                .getReference("reports")
                                .child(userId); // на кого жалуются

                        String myId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        reportRef.child(myId).setValue(true)
                                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Reported", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e -> Toast.makeText(this, "Failed to report", Toast.LENGTH_SHORT).show());
                        return true;
                    } else if (id == R.id.add_friend) {
                        // Пример: добавить в друзья
                        Toast.makeText(this, "Friend request sent", Toast.LENGTH_SHORT).show();
                        // Здесь можно добавить реализацию отправки заявки в друзья
                        return true;// DOOOOOOOOOOOOOOOOOOOOOOOO ITTTTTTTTTTTTTTTTTTTTT
                    }
                    return false;
                });

                popupMenu.show();
            });
        }




        ref = FirebaseDatabase.getInstance().getReference("users").child(userId);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                nickname = snapshot.child("nickname").getValue(String.class);

                name = snapshot.child("name").getValue(String.class);

                surname = snapshot.child("surname").getValue(String.class);

                regMillis = snapshot.child("registrationDate").getValue(Long.class);

                games = snapshot.child("games").getValue(Integer.class);

                wins = snapshot.child("wins").getValue(Integer.class);

                chessRating = snapshot.child("chessRating").getValue(Integer.class);

                chessMorphRating = snapshot.child("chessMorphRating").getValue(Integer.class);

                String imageUri = snapshot.child("avatarUrl").getValue(String.class);

                if (nickname != null) {
                    TextView nickText = findViewById(R.id.nickText);
                    nickText.setText(nickname);
                }

                if (name != null) {
                    TextView nameText = findViewById(R.id.nameText);
                    nameText.setText(name);
                }

                if (surname != null) {
                    TextView surnameText = findViewById(R.id.surnameText);
                    surnameText.setText(surname);
                }

                if (games != null) {
                    TextView gamesInt = findViewById(R.id.gamesInt);
                    gamesInt.setText(""+games);
                }

                if (wins != null) {
                    TextView gamesInt = findViewById(R.id.winsInt);
                    gamesInt.setText(""+wins);
                }

                if (chessRating != null) {
                    TextView chessRatingView=findViewById(R.id.chessRatingInt);
                    chessRatingView.setText(""+chessRating);
                }

                if (chessMorphRating != null) {
                    TextView chessMorphRatingInt=findViewById(R.id.chessMorphRatingInt);
                    chessMorphRatingInt.setText(""+chessMorphRating);
                }

                TextView loosesInt = findViewById(R.id.lossesInt);
                int losses = games - wins;
                loosesInt.setText(""+losses);

                if (games>0){
                    int winRate = (wins * 100) / games;
                    TextView winRateInt = findViewById(R.id.winRateInt);
                    winRateInt.setText(""+winRate+"%");
                }

                if(imageUri!=null){
                    ImageView avatarImageView = findViewById(R.id.imageView);
                    Glide.with(ProfileActivity.this)
                            .load(imageUri)
                            .placeholder(R.drawable.profile_images)
                            .into(avatarImageView);
                }




                Date date = new Date(regMillis);
                SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
                String formattedDate = sdf.format(date);

                TextView dateText = findViewById(R.id.dateText);
                dateText.setText("Reg: "+formattedDate);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), "Internet Error", Toast.LENGTH_SHORT).show();
            }
        });


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
    public void backToMenu(View view) {
        finish();
    }


    public void edit_info(View view) {
        if(isMyProfile) {
            startActivity(new Intent(this, EditProfileActivity.class));
        }
    }
}