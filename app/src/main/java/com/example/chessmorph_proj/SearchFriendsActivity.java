package com.example.chessmorph_proj;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SearchFriendsActivity extends AppCompatActivity {



    private EditText searchEditText;
    private RecyclerView searchResultsRecycler;
    private List<User> searchResults = new ArrayList<>();
    private SearchUserAdapter adapter;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_search_friends);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //ориентация вертикальная


        searchEditText = findViewById(R.id.searchEditText);
        searchResultsRecycler = findViewById(R.id.searchResultsRecycler);
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        adapter = new SearchUserAdapter(searchResults, this, currentUserId);
        searchResultsRecycler.setLayoutManager(new LinearLayoutManager(this));
        searchResultsRecycler.setAdapter(adapter);


        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString().trim();
                if (!query.isEmpty()) {
                    searchUsersByNickname(query);
                } else {
                    searchResults.clear();
                    adapter.notifyDataSetChanged();
                }
            }
        });

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
    public class User {
        private String uid;
        private String nickname;
        private String avatarUrl;

        public User(String uid, String nickname, String avatarUrl) {
            this.uid = uid;
            this.nickname = nickname;
            this.avatarUrl = avatarUrl;
        }

        public String getUid() { return uid; }
        public String getNickname() { return nickname; }
        public String getAvatarUrl() { return avatarUrl; }
    }
    public class SearchUserAdapter extends RecyclerView.Adapter<SearchUserAdapter.UserViewHolder> {

        private List<User> userList;
        private Context context;
        private String currentUserId;

        public SearchUserAdapter(List<User> userList, Context context, String currentUserId) {
            this.userList = userList;
            this.context = context;
            this.currentUserId = currentUserId;
        }

        @NonNull
        @Override
        public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_friend_search, parent, false);
            return new UserViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
            User user = userList.get(position);

            holder.nameTextView.setText(user.getNickname());
            Glide.with(context)
                    .load(user.getAvatarUrl())
                    .placeholder(R.drawable.profile_images)
                    .into(holder.avatarImageView);
            holder.avatarImageView.setOnClickListener(v -> {
                Intent intent = new Intent(SearchFriendsActivity.this, ProfileActivity.class);
                intent.putExtra("isMyProfile", false);
                intent.putExtra("opponentId", user.getUid());
                startActivity(intent);
            });
            holder.addFriendButton.setOnClickListener(v -> {
                checkRequestsAndProceed(user.getUid(), currentUserId);
            });
        }

        @Override
        public int getItemCount() {
            return userList.size();
        }

        public class UserViewHolder extends RecyclerView.ViewHolder {
            ImageView avatarImageView;
            TextView nameTextView;
            Button addFriendButton;

            public UserViewHolder(@NonNull View itemView) {
                super(itemView);
                avatarImageView = itemView.findViewById(R.id.avatarImageView);
                nameTextView = itemView.findViewById(R.id.nameTextView);
                addFriendButton = itemView.findViewById(R.id.addFriendButton);
            }
        }
    }
    private void searchUsersByNickname(String query) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

        usersRef.orderByChild("nickname")
                .startAt(query)
                .endAt(query + "\uf8ff")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        searchResults.clear();

                        for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                            String uid = userSnapshot.getKey();
                            if (uid.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) continue;

                            String nickname = userSnapshot.child("nickname").getValue(String.class);
                            String avatarUrl = userSnapshot.child("avatarUrl").getValue(String.class);


                            if (nickname != null) {
                                searchResults.add(new User(uid, nickname, avatarUrl));
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Обработка ошибок
                    }
                });
    }


    private void checkRequestsAndProceed(String senderUid, String receiverUid) {
        DatabaseReference senderFriendRef = FirebaseDatabase.getInstance()
                .getReference("users").child(senderUid).child("friends").child(receiverUid);

        senderFriendRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot friendSnapshot) {
                if (friendSnapshot.exists()) {
                    Toast.makeText(SearchFriendsActivity.this, "You are already friends!", Toast.LENGTH_SHORT).show();
                } else {
                    // Проверяем, есть ли встречный запрос
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

                                Toast.makeText(SearchFriendsActivity.this, "Now you are friends!", Toast.LENGTH_SHORT).show();
                            } else {
                                FirebaseDatabase.getInstance()
                                        .getReference("requests").child(receiverUid).child(senderUid)
                                        .setValue(true);

                                Toast.makeText(SearchFriendsActivity.this, "Request sent", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(SearchFriendsActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SearchFriendsActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void backToMenu(View view) {
        finish();
    }

}