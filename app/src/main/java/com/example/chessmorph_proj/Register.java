package com.example.chessmorph_proj;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;

public class Register extends AppCompatActivity {
    EditText mUserName,mEmail,mPassword,mPassword2;
    Button mRegBtn;
    TextView mCreateText;
    FirebaseAuth fAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        mCreateText=findViewById(R.id.createText);

        fAuth=FirebaseAuth.getInstance();

        mRegBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email=mEmail.getText().toString().trim();
                String password=mPassword.getText().toString().trim();
                String password2=mPassword2.getText().toString().trim();

                if (TextUtils.isEmpty(email)){
                    mEmail.setError("Email is Required");
                    return;
                }
                if(TextUtils.isEmpty(password)){
                    mPassword.setError("Password is Required");
                return;
                }
                if (TextUtils.isEmpty(password2)){
                    mPassword2.setError("write password again");
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

            }
        }

    }
}