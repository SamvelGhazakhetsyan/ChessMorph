package com.example.chessmorph_proj;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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

public class ProfileActivity extends AppCompatActivity {

    private String userId,nickname,name,surname,myNickname,myName,mySurname;
    private boolean isMyProfile, isOnGame;
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
        isOnGame = getIntent().getBooleanExtra("isOnGame", false);



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
        isMyProfile = (userId.equals(fAuth.getCurrentUser().getUid()));

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
            ImageView avatarImageView = findViewById(R.id.playButton);

            ViewGroup.LayoutParams params = avatarImageView.getLayoutParams();
            params.width = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 50, avatarImageView.getResources().getDisplayMetrics());
            params.height = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 50, avatarImageView.getResources().getDisplayMetrics());

            avatarImageView.setLayoutParams(params);


            menuButton.setOnClickListener(v -> {
                PopupMenu popupMenu = new PopupMenu(ProfileActivity.this, v);
                popupMenu.getMenuInflater().inflate(R.menu.profile_menu_other, popupMenu.getMenu());






                String senderUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                String receiverUid = userId;
                if(true){
                    DatabaseReference userFriendsRef = FirebaseDatabase.getInstance()
                            .getReference("users").child(senderUid).child("friends").child(receiverUid);

                    userFriendsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                MenuItem item = popupMenu.getMenu().findItem(R.id.add_friend);
                                item.setTitle("unfriend");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(ProfileActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }

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
                        sendOrToggleFriendship(senderUid, receiverUid);
                        return true;
                    }
                    return false;
                });

                popupMenu.show();
            });
        }


        View friendsClickZone = findViewById(R.id.friendView);
        friendsClickZone.setOnClickListener(v -> {

        });

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
        RecyclerView friendsRecycler = findViewById(R.id.friendsRecycler);
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

        View friendClickZone = findViewById(R.id.friendView);

        friendClickZone.setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, SearchFriendsActivity.class));
        });




        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }



    public void sendOrToggleFriendship(String senderUid, String receiverUid) {
        DatabaseReference userFriendsRef = FirebaseDatabase.getInstance()
                .getReference("users").child(senderUid).child("friends").child(receiverUid);

        userFriendsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    FirebaseDatabase.getInstance()
                            .getReference("users").child(senderUid).child("friends").child(receiverUid).removeValue();
                    FirebaseDatabase.getInstance()
                            .getReference("users").child(receiverUid).child("friends").child(senderUid).removeValue();

                    Toast.makeText(ProfileActivity.this, "Deleted from friends", Toast.LENGTH_SHORT).show();
                } else {
                    checkRequestsAndProceed(senderUid, receiverUid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProfileActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkRequestsAndProceed(String senderUid, String receiverUid) {
        DatabaseReference reverseRequestRef = FirebaseDatabase.getInstance()
                .getReference("requests").child(senderUid).child(receiverUid);

        reverseRequestRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    reverseRequestRef.removeValue();

                    DatabaseReference senderFriends = FirebaseDatabase.getInstance()
                            .getReference("users").child(senderUid).child("friends").child(receiverUid);
                    DatabaseReference receiverFriends = FirebaseDatabase.getInstance()
                            .getReference("users").child(receiverUid).child("friends").child(senderUid);

                    senderFriends.setValue(true);
                    receiverFriends.setValue(true);

                    Toast.makeText(ProfileActivity.this, "Now you are friends!", Toast.LENGTH_SHORT).show();
                } else {
                    FirebaseDatabase.getInstance()
                            .getReference("requests").child(receiverUid).child(senderUid)
                            .setValue(true);

                    Toast.makeText(ProfileActivity.this, "Request sent", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProfileActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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
    public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.FriendViewHolder> {
        private List<Friend> friends;
        private Context context;

        public FriendsAdapter(List<Friend> friends, Context context) {
            this.friends = friends;
            this.context = context;
        }

        public class FriendViewHolder extends RecyclerView.ViewHolder {
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

            holder.avatarImageView.setOnClickListener(v -> {
                Intent intent = new Intent(context, ProfileActivity.class);
                intent.putExtra("isMyProfile", false);
                if(isOnGame){
                    intent.putExtra("isOnGame", true);
                }
                intent.putExtra("opponentId", friend.getUid());
                context.startActivity(intent);
                finish();
            });

            Glide.with(context)
                    .load(friend.getAvatarUrl())
                    .placeholder(R.drawable.profile_images)
                    .error(R.drawable.profile_images)
                    .into(holder.avatarImageView);
        }

        @Override
        public int getItemCount() {
            return friends.size();
        }
    }

    public void friendlyGame(View view){
        if(isOnGame){
            Toast.makeText(this, "You are already in the game", Toast.LENGTH_SHORT).show();
        }else{
            Intent intent = new Intent(this, ChessGameSettings.class);
            intent.putExtra("isOnlineGame", true);
            intent.putExtra("isFriendlyGame", true);
            intent.putExtra("opponentId", userId);
            startActivity(intent);
            finish();
        }
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