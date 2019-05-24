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
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Objects;

import static java.lang.Character.isLowerCase;
import static java.lang.Character.isUpperCase;
import static java.lang.Character.toUpperCase;


public class Register extends AppCompatActivity {


    private Button reg_btn;
    private EditText reg_name, reg_email, reg_pass;
    private FirebaseAuth regAuth;
    private TextView sendToLogin;
    private ProgressDialog regProgress;
    private DatabaseReference database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        reg_name = findViewById(R.id.reg_name);
        reg_email = findViewById(R.id.reg_email);
        reg_pass = findViewById(R.id.reg_pass);

        reg_btn = findViewById(R.id.reg_btn);

        sendToLogin = findViewById(R.id.sendToLogin);

        regProgress = new ProgressDialog(this);

        regAuth = FirebaseAuth.getInstance();

        sendToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                Intent intent = new Intent(Register.this, Login.class);
                startActivity(intent);
            }
        });

        reg_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String name = reg_name.getText().toString().trim();
                String email = reg_email.getText().toString().trim();
                String pass = reg_pass.getText().toString().trim();

                registerUser(name,email,pass);
            }
        });
    }

    private void registerUser(final String name, String email, String pass)
    {
        if(TextUtils.isEmpty(name)||TextUtils.isEmpty(email)||TextUtils.isEmpty(pass))
        {
            Toast.makeText(this, "Please fill all fields.", Toast.LENGTH_SHORT).show();
        }
        else if(name.length() <= 3)
        {
            Toast.makeText(this, "Name is too short.", Toast.LENGTH_SHORT).show();
        }
        else
        {
            regProgress.setTitle("Registering User");
            regProgress.setMessage("Please wait while we register user.");
            regProgress.show();

            regAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    regProgress.dismiss();

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

                                        FirebaseUser current_user = FirebaseAuth.getInstance().getCurrentUser();
                                        String uid = current_user.getUid();

                                        database = FirebaseDatabase.getInstance().getReference().child("users").child(uid);

                                        HashMap<String,String> userMap = new HashMap<>();
                                        userMap.put("name", name);
                                        userMap.put("status", "I am using YouChat!");
                                        userMap.put("image", "default");
                                        userMap.put("thumb_image", "default");
                                        userMap.put("device_token", deviceToken);

                                        database.setValue(userMap);

                                        Toast.makeText(Register.this, "Registered successfully.", Toast.LENGTH_LONG).show();
                                        finish();
                                        startActivity(new Intent(Register.this, MainActivity.class));

                                    }
                                });

                    }
                    else {
                        String rError = "";

                        try {
                            throw Objects.requireNonNull(task.getException());
                        } catch (FirebaseAuthWeakPasswordException e) {
                            rError = "Use a strong password.";
                        } catch (FirebaseAuthUserCollisionException e) {
                            rError = "Account already exists.";
                        } catch (Exception e) {
                            rError = "There was an unknown error.";
                            e.printStackTrace();
                        }

                        Toast.makeText(Register.this, rError, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}
