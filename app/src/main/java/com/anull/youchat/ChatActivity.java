package com.anull.youchat;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import de.hdodenhof.circleimageview.CircleImageView;


public class ChatActivity extends AppCompatActivity {

    private String chatUser;
    private String chatName;
    private Toolbar chatToolbar;
    private ImageView addButton, sendButton;
    private EditText chatMessage;
    private TextView title, lastSeen;
    private CircleImageView userImage;
    private DatabaseReference RootRef;
    private FirebaseAuth chatAuth;
    private String currentUserID;
    private RecyclerView messagesList;
    private final List<Messages> MessagesList = new ArrayList<>();
    private LinearLayoutManager linearLayout;
    private MessageAdapter Adapter;
    private StorageReference imageStorage;
    private ProgressDialog cProgress;

    private static final int GALLERY_PICK = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        addButton = (ImageView) findViewById(R.id.add_image);
        sendButton = (ImageView) findViewById(R.id.send_image);

        RootRef = FirebaseDatabase.getInstance().getReference();
        chatAuth = FirebaseAuth.getInstance();
        currentUserID = chatAuth.getCurrentUser().getUid();
        imageStorage = FirebaseStorage.getInstance().getReference();

        cProgress = new ProgressDialog(this);

        chatMessage = (EditText) findViewById(R.id.message_content);

        chatUser = getIntent().getStringExtra("user_id");
        chatName = getIntent().getStringExtra("user_name");

        chatToolbar = (Toolbar) findViewById(R.id.chat_app_bar);
        setSupportActionBar(chatToolbar);

        final ActionBar actionBar = getSupportActionBar();

        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = inflater.inflate(R.layout.chat_custom_bar, null);

        actionBar.setCustomView(actionBarView);

        title = (TextView) findViewById(R.id.custom_bar_title);
        title.setText(chatName);
        lastSeen = (TextView) findViewById(R.id.custom_bar_seen);

        userImage = (CircleImageView) findViewById(R.id.custom_bar_image);

        Adapter = new MessageAdapter(MessagesList);

        messagesList = (RecyclerView) findViewById(R.id.messages_list);
        linearLayout = new LinearLayoutManager(this);
        messagesList.setHasFixedSize(true);
        messagesList.setLayoutManager(linearLayout);
        messagesList.setAdapter(Adapter);

        RootRef.child("chat").child(currentUserID).child(chatUser).child("seen").setValue(true);
        /*RootRef.child("chat").child(currentUserID).child(chatUser).child("timestamp").setValue(ServerValue.TIMESTAMP);*/

        loadMessages();

        RootRef.child("users").child(chatUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                String online = dataSnapshot.child("online").getValue().toString();
                String image = dataSnapshot.child("thumb_image").getValue().toString();

                if(online.equals("true"))
                {
                    lastSeen.setText("Online");
                }
                else
                {
                    GetTimeAgo obj = new GetTimeAgo();
                    long lastTime  = Long.parseLong(online);
                    String lastSeenTime = obj.getTimeAgo(lastTime, getApplicationContext());

                    lastSeen.setText(lastSeenTime);
                }
                if (!image.equals("default"))
                {
                    Picasso.get().load(image).placeholder(R.drawable.default_avatar).into(userImage);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        /*RootRef.child("chat").child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(!dataSnapshot.hasChild(chatUser))
                {
                    Map chatAddMap = new HashMap();
                    chatAddMap.put("seen", false);
                    chatAddMap.put("timestamp", ServerValue.TIMESTAMP);

                    Map chatUserMap = new HashMap();
                    chatUserMap.put("chat/" + currentUserID + "/" + chatUser, chatAddMap);
                    chatUserMap.put("chat/" + chatUser + "/" + currentUserID, chatAddMap);

                    RootRef.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                        }
                    });
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });*/

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                sendMessage();

            }
        });

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(galleryIntent, "SELECT IMAGE"), GALLERY_PICK);

                cProgress.setTitle("Sending Image");
                cProgress.setMessage("Please wait while the image is being sent.");
                cProgress.setCanceledOnTouchOutside(false);
                cProgress.show();
            }
        });

    }

    private void loadMessages() {

        DatabaseReference messageRef = RootRef.child("messages").child(currentUserID).child(chatUser);

        Query messageQuery = messageRef;

        messageQuery.addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                Messages message = dataSnapshot.getValue(Messages.class);

                MessagesList.add(message);
                Adapter.notifyDataSetChanged();

                messagesList.scrollToPosition(MessagesList.size() - 1);

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void sendMessage()
    {

        String message = chatMessage.getText().toString().trim();

        if(!TextUtils.isEmpty(message))
        {

            String current_user_ref = "messages/" + currentUserID + "/" + chatUser;
            String chat_user_ref = "messages/" + chatUser + "/" + currentUserID;

            DatabaseReference user_message_push = RootRef.child("messages").child(currentUserID)
                    .child(chatUser).push();
            String push_id = user_message_push.getKey();

            Map messageMap = new HashMap();
            messageMap.put("message", message);
            messageMap.put("seen", false);
            messageMap.put("type", "text");
            messageMap.put("time", ServerValue.TIMESTAMP);
            messageMap.put("from", currentUserID);

            Map messageUserMap = new HashMap();
            messageUserMap.put(current_user_ref + "/" + push_id,messageMap);
            messageUserMap.put(chat_user_ref + "/" + push_id, messageMap);

            chatMessage.setText("");

            RootRef.child("chat").child(currentUserID).child(chatUser).child("seen").setValue(true);
            RootRef.child("chat").child(currentUserID).child(chatUser).child("timestamp").setValue(ServerValue.TIMESTAMP);

            RootRef.child("chat").child(chatUser).child(currentUserID).child("seen").setValue(false);
            RootRef.child("chat").child(chatUser).child(currentUserID).child("timestamp").setValue(ServerValue.TIMESTAMP);

            RootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                    if(databaseError != null)
                    {
                        Toast toast = Toast.makeText(getApplicationContext(), "There was some error. Please retry.", Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                    }

                }
            });
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_PICK)
        {

            if (resultCode == RESULT_OK)
            {

                Uri resultUri = data.getData();

                final String current_user_ref = "messages/" + currentUserID + "/" + chatUser;
                final String chat_user_ref = "messages/" + chatUser + "/" + currentUserID;

                DatabaseReference user_message_push = RootRef.child("messages").child(currentUserID)
                        .child(chatUser).push();
                final String push_id = user_message_push.getKey();

                final StorageReference filePath = imageStorage.child("message_images").child(push_id + ".jpg");

                filePath.putFile(resultUri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {

                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {

                        if (!task.isSuccessful())
                        {
                            throw Objects.requireNonNull(task.getException());
                        }

                        return filePath.getDownloadUrl();
                    }

                }).addOnCompleteListener(new OnCompleteListener<Uri>() {

                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {

                        if (task.isSuccessful())
                        {

                            String currentImageURL = task.getResult().toString();

                            Map messageMap = new HashMap();
                            messageMap.put("message", currentImageURL);
                            messageMap.put("seen", false);
                            messageMap.put("type", "image");
                            messageMap.put("time", ServerValue.TIMESTAMP);
                            messageMap.put("from", currentUserID);

                            Map messageUserMap = new HashMap();
                            messageUserMap.put(current_user_ref + "/" + push_id,messageMap);
                            messageUserMap.put(chat_user_ref + "/" + push_id, messageMap);

                            chatMessage.setText("");

                            RootRef.child("chat").child(currentUserID).child(chatUser).child("seen").setValue(true);
                            RootRef.child("chat").child(currentUserID).child(chatUser).child("timestamp").setValue(ServerValue.TIMESTAMP);

                            RootRef.child("chat").child(chatUser).child(currentUserID).child("seen").setValue(false);
                            RootRef.child("chat").child(chatUser).child(currentUserID).child("timestamp").setValue(ServerValue.TIMESTAMP);

                            RootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                                    cProgress.dismiss();

                                }
                            });

                        }

                    }
                });

            }
            else
            {
                Toast toast = Toast.makeText(getApplicationContext(), "There was some error. Please retry.", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
        }

    }

}
