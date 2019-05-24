package com.anull.youchat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import java.util.Objects;

public class Login extends AppCompatActivity {

    private Button log_btn;
    private EditText log_email, log_pass;
    private FirebaseAuth logAuth;
    private TextView sendToRegister;
    private ProgressDialog logProgress;
    private DatabaseReference UserDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        log_email = findViewById(R.id.log_email);
        log_pass = findViewById(R.id.log_pass);

        log_btn = findViewById(R.id.log_btn);

        sendToRegister = findViewById(R.id.sendToRegister);

        logProgress = new ProgressDialog(this);

        logAuth = FirebaseAuth.getInstance();

        UserDatabase = FirebaseDatabase.getInstance().getReference().child("users");

        sendToRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                Intent intent = new Intent(Login.this, Register.class);
                startActivity(intent);
            }
        });

        log_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = log_email.getText().toString().trim();
                String pass = log_pass.getText().toString().trim();

                loginUser(email,pass);
            }
        });
    }

    private void loginUser(String email, String pass)
    {
        if(TextUtils.isEmpty(email)||TextUtils.isEmpty(pass))
        {
            Toast.makeText(this, "Please fill all fields.", Toast.LENGTH_SHORT).show();
        }
        else
        {
            logProgress.setTitle("Logging In User");
            logProgress.setMessage("Please wait while we login user.");
            logProgress.show();

            logAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull final Task<AuthResult> task) {

                    logProgress.dismiss();

                    if(task.isSuccessful())
                    {
                        FirebaseInstanceId.getInstance().getInstanceId()
                                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<InstanceIdResult> task) {

                                        if(!task.isSuccessful())
                                        {
                                            return;
                                        }

                                        String deviceToken = task.getResult().getToken();

                                        String currentUser = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                        UserDatabase.child(currentUser).child("device_token").setValue(deviceToken)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {

                                                        Toast.makeText(Login.this, "Logged in successfully.", Toast.LENGTH_LONG).show();
                                                        finish();
                                                        startActivity(new Intent(Login.this, MainActivity.class));

                                                    }
                                                });

                                    }
                                });

                    }
                    else
                    {
                        String lError = "";

                        try {
                            throw Objects.requireNonNull(task.getException());
                        } catch (FirebaseAuthInvalidCredentialsException e) {
                            lError = "Invalid credentials.";
                        } catch (Exception e) {
                            lError = "There was an unknown error.";
                            e.printStackTrace();
                        }

                        Toast.makeText(Login.this, lError, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}
