package com.anull.youchat;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import de.hdodenhof.circleimageview.CircleImageView;

public class Friends extends Fragment {

    private RecyclerView friends_list;
    private FirebaseAuth fAuth;
    private String currentUserID;
    private View fView;
    private DatabaseReference UserDatabase;
    private DatabaseReference FriendsDatabase;
    private TextView ifNoFriends;

    public Friends() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        fView = inflater.inflate(R.layout.fragment_friends, container, false);

        friends_list = (RecyclerView) fView.findViewById(R.id.friends_list);

        ifNoFriends = (TextView) fView.findViewById(R.id.if_no_friends);

        fAuth = FirebaseAuth.getInstance();
        currentUserID = fAuth.getCurrentUser().getUid();

        UserDatabase = FirebaseDatabase.getInstance().getReference().child("users");
        UserDatabase.keepSynced(true);
        FriendsDatabase = FirebaseDatabase.getInstance().getReference().child("friends").child(currentUserID);
        FriendsDatabase.keepSynced(true);

        friends_list.setHasFixedSize(true);
        friends_list.setLayoutManager(new LinearLayoutManager(getContext()));

        return fView;
    }

    public void onStart() {
        super.onStart();

        FriendsDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if((int) dataSnapshot.getChildrenCount() == 0)
                {
                    ifNoFriends.setVisibility(View.VISIBLE);
                }
                else
                {
                    ifNoFriends.setVisibility(View.GONE);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        FirebaseRecyclerOptions<FriendsGetSet> fOptions = new FirebaseRecyclerOptions.Builder<FriendsGetSet>().setQuery(FriendsDatabase, FriendsGetSet.class).build();

        FirebaseRecyclerAdapter<FriendsGetSet, FriendsViewHolder> adapter = new FirebaseRecyclerAdapter<FriendsGetSet, FriendsViewHolder>(fOptions) {

            @Override
            protected void onBindViewHolder(@NonNull final FriendsViewHolder holder, int position, @NonNull final FriendsGetSet model) {

                final String userID = getRef(position).getKey();

                UserDatabase.child(userID).addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        final String name = dataSnapshot.child("name").getValue().toString();
                        String userImage = dataSnapshot.child("thumb_image").getValue().toString();

                        holder.setName(name);
                        holder.setThumbImage(userImage);
                        holder.setDate(model.date);
                        if(dataSnapshot.hasChild("online"))
                        {
                            String userOnline = dataSnapshot.child("online").getValue().toString();
                            holder.setOnline(userOnline);
                        }

                        holder.FView.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View v) {

                              CharSequence options[] = new CharSequence[]{"Open Profile", "Send Message"};

                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                builder.setTitle("Select Options");
                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        if(which == 0)
                                        {
                                            Intent profileIntent = new Intent(getContext(), ProfileActivity.class);
                                            profileIntent.putExtra("user_id", userID);
                                            startActivity(profileIntent);
                                        }
                                        if(which == 1)
                                        {
                                            Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                            chatIntent.putExtra("user_id", userID);
                                            chatIntent.putExtra("user_name", name);
                                            startActivity(chatIntent);
                                        }

                                    }
                                });

                                builder.show();

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
            public FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.users_layout, parent, false);

                return new FriendsViewHolder(view);

            }

        };

        friends_list.setAdapter(adapter);
        adapter.startListening();

    }

    public static class FriendsViewHolder extends RecyclerView.ViewHolder {

        View FView;

        public FriendsViewHolder(@NonNull View itemView)
        {
            super(itemView);
            FView = itemView;
        }


        public void setName(String name)
        {
            TextView user_name = (TextView) FView.findViewById(R.id.user_name);
            user_name.setText(name);
        }

        public void setThumbImage(String thumbImage)
        {
            CircleImageView user_image = (CircleImageView) FView.findViewById(R.id.user_image);

            if (!thumbImage.equals("default")) {
                Picasso.get().load(thumbImage).placeholder(R.drawable.default_avatar).into(user_image);
            }
        }

        public  void setDate(String date)
        {
            TextView user_status = (TextView) FView.findViewById(R.id.user_status);
            user_status.setText(date);
        }

        public void setOnline(String isOnline)
        {
            ImageView online_icon = (ImageView) FView.findViewById(R.id.online_icon);

            if(isOnline.equals("true"))
            {
                online_icon.setVisibility(View.VISIBLE);
            }
            else
            {
                online_icon.setVisibility(View.INVISIBLE);
            }
        }

    }

}
