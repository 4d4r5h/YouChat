package com.anull.youchat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ChangeStatus extends AppCompatActivity {

    private Toolbar sToolbar;
    private Button change_button;
    private EditText new_status;
    private DatabaseReference mDatabase;
    private FirebaseUser sCurrentUser;
    private ProgressDialog sProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_status);

        sCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String userID = sCurrentUser.getUid();

        mDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(userID);

        new_status = (EditText) findViewById(R.id.new_status);

        change_button = findViewById(R.id.change_button);

        change_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                sProgress = new ProgressDialog(ChangeStatus.this);
                sProgress.setTitle("Changing Status");
                sProgress.setMessage("Please wait while the change is being made.");
                sProgress.setCanceledOnTouchOutside(false);
                sProgress.show();

                String status = new_status.getText().toString().trim();

                if(!TextUtils.isEmpty(status))
                {

                    mDatabase.child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            sProgress.dismiss();

                            if(task.isSuccessful())
                            {
                                finish();
                                Intent sIntent = new Intent(ChangeStatus.this, Profile.class);
                                startActivity(sIntent);
                            }
                            else
                            {
                                Toast toast = Toast.makeText(getApplicationContext(), "There was some error.", Toast.LENGTH_SHORT);
                                toast.show();
                            }

                        }
                    });

                }
                else
                {
                    sProgress.dismiss();

                    Toast.makeText(ChangeStatus.this, "Status cannot be empty.", Toast.LENGTH_LONG).show();
                }
            }
        });

        String temp = getIntent().getStringExtra("temp");

        new_status.setText(temp);

        sToolbar = (Toolbar) findViewById(R.id.status_app_bar);
        setSupportActionBar(sToolbar);
        getSupportActionBar().setTitle("Change Status");
        getSupportActionBar().show();

    }
}
