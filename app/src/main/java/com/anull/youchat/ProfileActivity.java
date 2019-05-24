package com.anull.youchat;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private CircleImageView profile_image;
    private TextView profile_name, profile_status, profile_friends;
    private Button send_request_btn, dec_request_btn;
    private DatabaseReference pUsersDatabase;
    private ProgressDialog progressDialog;
    private String current_state;
    private DatabaseReference friendRequestDatabase;
    private FirebaseUser currentUserDatabase;
    private DatabaseReference friendDatabase;
    private DatabaseReference notificationDatabase;
    private DatabaseReference rootReference;
    private String currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_2);

        final String user_id = getIntent().getStringExtra("user_id");

        profile_image = (CircleImageView) findViewById(R.id.profile_image);

        profile_name = (TextView) findViewById(R.id.profile_name);
        profile_status = (TextView) findViewById(R.id.profile_status);
        profile_friends = (TextView) findViewById(R.id.profile_friends);

        send_request_btn = (Button) findViewById(R.id.send_request_btn);

        dec_request_btn = (Button) findViewById(R.id.dec_request_btn);
        dec_request_btn.setVisibility(View.INVISIBLE);
        dec_request_btn.setEnabled(false);

        pUsersDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(user_id);
        friendRequestDatabase = FirebaseDatabase.getInstance().getReference().child("friend_request");
        currentUserDatabase = FirebaseAuth.getInstance().getCurrentUser();
        currentUserID = currentUserDatabase.getUid();
        friendDatabase = FirebaseDatabase.getInstance().getReference().child("friends");
        notificationDatabase = FirebaseDatabase.getInstance().getReference().child("notifications");
        rootReference = FirebaseDatabase.getInstance().getReference();

        current_state = "not_friends";

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Loading User Data");
        progressDialog.setMessage("Please wait while the profile is being loaded.");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        friendDatabase.child(user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                int numberOfFriends = (int) dataSnapshot.getChildrenCount();
                profile_friends.setText("Total Friends : " + numberOfFriends);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        pUsersDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                String name = dataSnapshot.child("name").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();

                profile_name.setText(name);
                profile_status.setText(status);

                if(!image.equals("default"))
                {
                    Picasso.get().load(image).placeholder(R.drawable.default_avatar).into(profile_image);
                }

                friendRequestDatabase.child(currentUserDatabase.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if(dataSnapshot.hasChild(user_id))
                        {
                            String req_type = dataSnapshot.child(user_id).child("request_type").getValue().toString();
                            if(req_type.equals("received"))
                            {
                                current_state = "req_received";
                                send_request_btn.setText("Accept Friend Request");
                                dec_request_btn.setVisibility(View.VISIBLE);
                                dec_request_btn.setEnabled(true);
                            }
                            else if(req_type.equals("sent"))
                            {
                                current_state = "req_sent";
                                send_request_btn.setText("Cancel Friend Request");
                            }
                        }
                        else
                        {

                            friendDatabase.child(currentUserDatabase.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                    if(dataSnapshot.hasChild(user_id))
                                    {
                                        current_state = "friends";
                                        send_request_btn.setText("Unfriend");
                                    }

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                        }

                        progressDialog.dismiss();

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        send_request_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                send_request_btn.setEnabled(false);

                if(current_state.equals("not_friends"))
                {

                    DatabaseReference notificationReference = rootReference.child("notifications").child(user_id).push();
                    final String notificationID = notificationReference.getKey();

                    final HashMap<String, String> notifications = new HashMap<>();
                    notifications.put("from", currentUserDatabase.getUid());
                    notifications.put("type", "request");

                    final Map requestMap = new HashMap();
                    requestMap.put("friend_request/" + currentUserDatabase.getUid() + "/" + user_id + "/request_type", "sent");
                    requestMap.put("friend_request/" + user_id + "/" + currentUserDatabase.getUid() + "/request_type", "received");
                    requestMap.put("notifications/" + user_id + "/" + notificationID, notifications);

                    rootReference.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                            if(databaseError != null)
                            {

                                current_state = "not_friends";
                                send_request_btn.setText("Send Friend Request");

                                Toast toast = Toast.makeText(ProfileActivity.this, "Friend request not sent.", Toast.LENGTH_SHORT);
                                toast.setGravity(Gravity.CENTER, 0, 0);
                                toast.show();
                            }
                            else
                            {

                                HashMap<String, String> delNotifications = new HashMap<>();
                                delNotifications.put("from", null);
                                delNotifications.put("type", null);

                                Map delMap = new HashMap();
                                delMap.put("notifications/" + user_id + "/" + notificationID, delNotifications);

                                rootReference.updateChildren(delMap);

                                current_state = "req_sent";
                                send_request_btn.setText("Cancel Friend Request");

                                Toast toast = Toast.makeText(ProfileActivity.this, "Friend request sent.", Toast.LENGTH_SHORT);
                                toast.setGravity(Gravity.CENTER, 0, 0);
                                toast.show();

                            }

                        }
                    });

                    send_request_btn.setEnabled(true);

                }
                if(current_state.equals("req_sent"))
                {

                    Map requestMap = new HashMap();
                    requestMap.put("friend_request/" + currentUserDatabase.getUid() + "/" + user_id + "/request_type", null);
                    requestMap.put("friend_request/" + user_id + "/" + currentUserDatabase.getUid() + "/request_type", null);

                    rootReference.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                            if(databaseError != null)
                            {

                                current_state = "req_sent";
                                send_request_btn.setText("Cancel Friend Request");

                                Toast toast = Toast.makeText(ProfileActivity.this, "Friend request not cancelled.", Toast.LENGTH_SHORT);
                                toast.setGravity(Gravity.CENTER, 0, 0);
                                toast.show();
                            }
                            else
                            {

                                friendDatabase.child(currentUserDatabase.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                        if(dataSnapshot.hasChild(user_id))
                                        {
                                            current_state = "friends";
                                            send_request_btn.setText("Unfriend");
                                            Toast toast = Toast.makeText(ProfileActivity.this, "Request already accepted.", Toast.LENGTH_SHORT);
                                            toast.setGravity(Gravity.CENTER, 0, 0);
                                            toast.show();

                                        }
                                        else
                                        {
                                            current_state = "not_friends";
                                            send_request_btn.setText("Send Friend Request");
                                            Toast toast = Toast.makeText(ProfileActivity.this, "Friend request cancelled.", Toast.LENGTH_SHORT);
                                            toast.setGravity(Gravity.CENTER, 0, 0);
                                            toast.show();

                                        }

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });

                            }

                        }
                    });

                    send_request_btn.setEnabled(true);

                }
                if(current_state.equals("req_received"))
                {

                    final String cur_date = DateFormat.getDateTimeInstance().format(new Date());

                    Map requestMap = new HashMap();
                    requestMap.put("friends/" + currentUserDatabase.getUid() + "/" + user_id + "/" + "date", cur_date);
                    requestMap.put("friends/" + user_id + "/" + currentUserDatabase.getUid() + "/" + "date", cur_date);

                    rootReference.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                            Map deleteMap = new HashMap();
                            deleteMap.put("friend_request/" + currentUserDatabase.getUid() + "/" + user_id + "/request_type", null);
                            deleteMap.put("friend_request/" + user_id + "/" + currentUserDatabase.getUid() + "/request_type", null);

                            rootReference.updateChildren(deleteMap, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                                    if(databaseError != null)
                                    {

                                        current_state = "req_received";
                                        send_request_btn.setText("Accept Friend Request");
                                        Toast toast = Toast.makeText(ProfileActivity.this, "Friend request not accepted.", Toast.LENGTH_SHORT);
                                        toast.setGravity(Gravity.CENTER, 0, 0);
                                        toast.show();

                                    }
                                    else
                                    {

                                        current_state = "friends";
                                        send_request_btn.setText("Unfriend");
                                        dec_request_btn.setVisibility(View.INVISIBLE);
                                        dec_request_btn.setEnabled(false);
                                        Toast toast = Toast.makeText(ProfileActivity.this, "Friend request accepted.", Toast.LENGTH_SHORT);
                                        toast.setGravity(Gravity.CENTER, 0, 0);
                                        toast.show();

                                    }

                                }
                            });

                        }
                    });

                    send_request_btn.setEnabled(true);

                }
                if(current_state.equals("friends"))
                {

                    Map requestMap = new HashMap();
                    requestMap.put("friends/" + currentUserDatabase.getUid() + "/" + user_id, null);
                    requestMap.put("friends/" + user_id + "/" + currentUserDatabase.getUid(), null);

                    rootReference.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                            if(databaseError != null)
                            {
                                current_state = "friends";
                                send_request_btn.setText("Unfriend");
                                Toast toast = Toast.makeText(ProfileActivity.this, "Unable to unfriend.", Toast.LENGTH_SHORT);
                                toast.setGravity(Gravity.CENTER, 0, 0);
                                toast.show();

                            }
                            else
                            {

                                Map deleteChat = new HashMap();
                                deleteChat.put("chat/" + currentUserID + "/" + user_id + "/seen", null);
                                deleteChat.put("chat/" + currentUserID + "/" + user_id + "/timestamp", null);
                                deleteChat.put("chat/" + user_id + "/" + currentUserID + "/seen", null);
                                deleteChat.put("chat/" + user_id + "/" + currentUserID + "/timestamp", null);

                                rootReference.updateChildren(deleteChat);

                                current_state = "not_friends";
                                send_request_btn.setText("Send Friend Request");
                                Toast toast = Toast.makeText(ProfileActivity.this, "Unfriended successfully.", Toast.LENGTH_SHORT);
                                toast.setGravity(Gravity.CENTER, 0, 0);
                                toast.show();

                                /*rootReference.child("chat").child(currentUserID).child(user_id).child("seen").removeValue();
                                rootReference.child("chat").child(currentUserID).child(user_id).child("timestamp").removeValue();

                                rootReference.child("chat").child(user_id).child(currentUserID).child("seen").removeValue();
                                rootReference.child("chat").child(user_id).child(currentUserID).child("timestamp").removeValue();*/

                            }

                        }
                    });

                    send_request_btn.setEnabled(true);

                }

            }
        });

        dec_request_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Map deleteMap = new HashMap();
                deleteMap.put("friend_request/" + currentUserDatabase.getUid() + "/" + user_id + "/request_type", null);
                deleteMap.put("friend_request/" + user_id + "/" + currentUserDatabase.getUid() + "/request_type", null);

                rootReference.updateChildren(deleteMap, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                        if(databaseError != null)
                        {

                            current_state = "req_received";
                            send_request_btn.setText("Accept Friend Request");
                            Toast toast = Toast.makeText(ProfileActivity.this, "Friend request not declined.", Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                        }
                        else
                        {

                            current_state = "not_friends";
                            send_request_btn.setText("Send Friend Request");
                            dec_request_btn.setVisibility(View.INVISIBLE);
                            dec_request_btn.setEnabled(false);
                            Toast toast = Toast.makeText(ProfileActivity.this, "Friend request declined.", Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                        }

                    }
                });

            }
        });

    }

}
