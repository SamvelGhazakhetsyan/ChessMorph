package com.example.chessmorph_proj;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.util.Log;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private DatabaseReference database;
    private long lastBackPressedTime = 0;
    private static final int BACK_PRESS_DELAY = 1500;
    SharedPreferences prefs;
    FirebaseAuth fAuth;
    private String userId;
    DatabaseReference ref;

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

        fAuth = FirebaseAuth.getInstance();
        userId = fAuth.getCurrentUser().getUid();
        prefs = getSharedPreferences(fAuth.getCurrentUser().getUid(), MODE_PRIVATE);



        ref = FirebaseDatabase.getInstance().getReference("users").child(userId);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String imageUri = snapshot.child("avatarUrl").getValue(String.class);
                prefs.edit().putString("image", imageUri).apply();
                if(imageUri != "") {
                    ImageView avatarImageView = findViewById(R.id.imageButton2);
                    Glide.with(MainActivity.this)
                            .load(imageUri)
                            .placeholder(R.drawable.profile_images)
                            .into(avatarImageView);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), "Internet Error", Toast.LENGTH_SHORT).show();
            }
        });








        RecyclerView friendsRecycler = findViewById(R.id.friendsRecyclerMain);
        friendsRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        List<Friend> friendList = new ArrayList<>();
        FriendsAdapter adapter = new FriendsAdapter(friendList, this);
        friendsRecycler.setAdapter(adapter);

        String currentUid = userId;

        DatabaseReference friendsRef = FirebaseDatabase.getInstance()
                .getReference("users").child(currentUid).child("friends");

        friendsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot friendSnapshot : snapshot.getChildren()) {
                    String friendUid = friendSnapshot.getKey();

                    FirebaseDatabase.getInstance().getReference("users").child(friendUid)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    String name = snapshot.child("nickname").getValue(String.class);
                                    String avatar = snapshot.child("avatarUrl").getValue(String.class);
                                    friendList.add(new Friend(friendUid, name, avatar));
                                    adapter.notifyDataSetChanged();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {}
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });










        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });



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





    public class Friend {
        private String uid;
        private String name;
        private String avatarUrl;

        public Friend(String uid, String name, String avatarUrl) {
            this.uid = uid;
            this.name = name;
            this.avatarUrl = avatarUrl;
        }

        public String getUid() { return uid; }
        public String getName() { return name; }
        public String getAvatarUrl() { return avatarUrl; }
    }
    public static class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.FriendViewHolder> {
        private List<Friend> friends;
        private Context context;

        public FriendsAdapter(List<Friend> friends, Context context) {
            this.friends = friends;
            this.context = context;
        }

        public static class FriendViewHolder extends RecyclerView.ViewHolder {
            ImageView avatarImageView;
            TextView nameTextView;

            public FriendViewHolder(View itemView) {
                super(itemView);
                avatarImageView = itemView.findViewById(R.id.avatarImageView);
                nameTextView = itemView.findViewById(R.id.nameTextView);
            }
        }

        @NonNull
        @Override
        public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_friend, parent, false);
            return new FriendViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
            Friend friend = friends.get(position);
            holder.nameTextView.setText(friend.getName());

            Glide.with(context)
                    .load(friend.getAvatarUrl())
                    .placeholder(R.drawable.profile_images)
                    .error(R.drawable.profile_images)
                    .into(holder.avatarImageView);

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, ChessGameSettings.class);
                intent.putExtra("isOnlineGame", true);
                intent.putExtra("isFriendlyGame", true);
                intent.putExtra("opponentId", friend.getUid());
                context.startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return friends.size();
        }
    }


}