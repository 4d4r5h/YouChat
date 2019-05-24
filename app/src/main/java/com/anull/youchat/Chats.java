package com.anull.youchat;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import de.hdodenhof.circleimageview.CircleImageView;

public class Chats extends Fragment {

    private RecyclerView chatsList;
    private View mainView;
    private DatabaseReference convDatabase;
    private DatabaseReference messageDatabase;
    private DatabaseReference usersDatabase;
    private FirebaseAuth Auth;
    private String currentUserID;
    private String UserStatus;
    private TextView ifNoChats;
    private DatabaseReference friendsDatabase;
    private FirebaseRecyclerAdapter<Conv, ConvViewHolder> adapter;

    public Chats() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mainView = inflater.inflate(R.layout.fragment_chats, container, false);

        chatsList = (RecyclerView) mainView.findViewById(R.id.chats_list);

        ifNoChats = (TextView) mainView.findViewById(R.id.if_no_chats);

        Auth = FirebaseAuth.getInstance();
        currentUserID = Auth.getCurrentUser().getUid();

        convDatabase = FirebaseDatabase.getInstance().getReference().child("chat").child(currentUserID);
        convDatabase.keepSynced(true);

        usersDatabase = FirebaseDatabase.getInstance().getReference().child("users");
        usersDatabase.keepSynced(true);

        friendsDatabase = FirebaseDatabase.getInstance().getReference().child("friends").child(currentUserID);

        messageDatabase = FirebaseDatabase.getInstance().getReference().child("messages").child(currentUserID);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        chatsList.setHasFixedSize(true);
        chatsList.setLayoutManager(linearLayoutManager);

        return mainView;

    }

    public void onStart() {

        super.onStart();

        convDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if((int) dataSnapshot.getChildrenCount() == 0)
                {
                    ifNoChats.setVisibility(View.VISIBLE);
                }
                else
                {
                    ifNoChats.setVisibility(View.GONE);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        Query conversationQuery = convDatabase.orderByChild("timestamp");
        FirebaseRecyclerOptions<Conv> cOptions = new FirebaseRecyclerOptions.Builder<Conv>().setQuery(conversationQuery, Conv.class).build();

        adapter = new FirebaseRecyclerAdapter<Conv, ConvViewHolder>(cOptions) {

            @Override
            protected void onBindViewHolder(@NonNull final ConvViewHolder holder, int position, @NonNull final Conv model) {

                final String listUserID = getRef(position).getKey();

                /*friendsDatabase.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.hasChild(listUserID))
                        {
                            holder.CView.setVisibility(View.VISIBLE);
                        }
                        else
                        {
                            holder.CView.setVisibility(View.GONE);
                        }

                        if((int) dataSnapshot.getChildrenCount() == 0)
                        {
                            ifNoChats.setVisibility(View.VISIBLE);
                        }
                        else
                        {
                            ifNoChats.setVisibility(View.GONE);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });*/

                messageDatabase.addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if(dataSnapshot.hasChild(listUserID))
                        {

                            Query lastMessageQuery = messageDatabase.child(listUserID).limitToLast(1);

                            lastMessageQuery.addChildEventListener(new ChildEventListener() {
                                @Override
                                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                                    String data = dataSnapshot.child("message").getValue().toString();

                                    if(dataSnapshot.child("type").getValue().toString().equals("image"))
                                    {
                                        if(dataSnapshot.child("from").getValue().toString().equals(listUserID))
                                        {
                                            holder.setMessage("You have received an image.", model.isSeen());
                                        }
                                        else
                                            holder.setMessage("You have sent an image.", model.isSeen());
                                    }
                                    else
                                        holder.setMessage(data, model.isSeen());

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
                        else
                        {
                            holder.setMessage(UserStatus, model.isSeen());
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                usersDatabase.child(listUserID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        final String userName = dataSnapshot.child("name").getValue().toString();
                        String userImage = dataSnapshot.child("thumb_image").getValue().toString();
                        UserStatus = dataSnapshot.child("status").getValue().toString();

                        if(dataSnapshot.hasChild("online"))
                        {
                            String userOnline = dataSnapshot.child("online").getValue().toString();
                            holder.setUserOnline(userOnline);
                        }

                        holder.setName(userName);

                        if(!userImage.equals("default"))
                            holder.setUserImage(userImage);

                        holder.CView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                chatIntent.putExtra("user_id", listUserID);
                                chatIntent.putExtra("user_name", userName);
                                startActivity(chatIntent);

                            }
                        });

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }

            @NonNull
            @Override
            public ConvViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {

                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.users_layout, parent, false);

                return new ConvViewHolder(view);

            }
        };

            chatsList.setAdapter(adapter);
            adapter.startListening();

    }

    public static class ConvViewHolder extends RecyclerView.ViewHolder {

        View CView;

        public ConvViewHolder(View itemView) {

            super(itemView);
            CView = itemView;

        }

        public void setMessage(String message, boolean isSeen) {

            TextView userStatus = (TextView) CView.findViewById(R.id.user_status);
            userStatus.setText(message);

            if (!isSeen)
            {
                userStatus.setTypeface(userStatus.getTypeface(), Typeface.BOLD);
            }
            else
            {
                userStatus.setTypeface(userStatus.getTypeface(), Typeface.NORMAL);
            }

        }

        public void setName(String name) {

            TextView userName = (TextView) CView.findViewById(R.id.user_name);
            userName.setText(name);

        }

        public void setUserImage(String thumb_image) {

            CircleImageView userImage = (CircleImageView) CView.findViewById(R.id.user_image);
            Picasso.get().load(thumb_image).placeholder(R.drawable.default_avatar).into(userImage);

        }

        public void setUserOnline(String online_status) {

            ImageView userOnline = (ImageView) CView.findViewById(R.id.online_icon);

            if(online_status.equals("true"))
            {
                userOnline.setVisibility(View.VISIBLE);
            }
            else
            {
                userOnline.setVisibility(View.INVISIBLE);
            }

        }

    }

}
