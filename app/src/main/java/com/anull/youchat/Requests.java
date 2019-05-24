package com.anull.youchat;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import de.hdodenhof.circleimageview.CircleImageView;

public class Requests extends Fragment {

    private View rView;
    private RecyclerView requests_list;
    private FirebaseAuth rAuth;
    private String currentUserID;
    private DatabaseReference UserDatabase;
    private DatabaseReference FriendRequestDatabase;
    private TextView ifNoRequests;

    public Requests() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rView = inflater.inflate(R.layout.fragment_requests, container, false);

        ifNoRequests = (TextView) rView.findViewById(R.id.if_no_requests);

        requests_list = (RecyclerView) rView.findViewById(R.id.requests_list);

        rAuth = FirebaseAuth.getInstance();
        currentUserID = rAuth.getCurrentUser().getUid();

        UserDatabase = FirebaseDatabase.getInstance().getReference().child("users");
        UserDatabase.keepSynced(true);
        FriendRequestDatabase = FirebaseDatabase.getInstance().getReference().child("friend_request").child(currentUserID);
        FriendRequestDatabase.keepSynced(true);

        requests_list.setHasFixedSize(true);
        requests_list.setLayoutManager(new LinearLayoutManager(getContext()));
        requests_list.setAlpha(0);

        return rView;

    }

    public void onStart() {

        super.onStart();

        FirebaseRecyclerOptions<RequestsGetSet> rOptions = new FirebaseRecyclerOptions.Builder<RequestsGetSet>().setQuery(FriendRequestDatabase, RequestsGetSet.class).build();

        FirebaseRecyclerAdapter<RequestsGetSet, RequestsViewHolder> adapter = new FirebaseRecyclerAdapter<RequestsGetSet, RequestsViewHolder>(rOptions) {

            @Override
            protected void onBindViewHolder(@NonNull final RequestsViewHolder holder, int position, @NonNull final RequestsGetSet model) {

                final String userID = getRef(position).getKey();

                if(model.request_type.equals("received"))
                {

                    ifNoRequests.setVisibility(View.GONE);
                    requests_list.setAlpha(1);

                    UserDatabase.child(userID).addValueEventListener(new ValueEventListener() {

                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            String name = dataSnapshot.child("name").getValue().toString();
                            String userImage = dataSnapshot.child("thumb_image").getValue().toString();
                            String status = dataSnapshot.child("status").getValue().toString();

                            holder.setName(name);
                            holder.setThumbImage(userImage);
                            holder.setStatus(status);

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                    holder.RView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            Intent profileIntent = new Intent(getContext(), ProfileActivity.class);
                            profileIntent.putExtra("user_id", userID);
                            startActivity(profileIntent);

                        }
                    });

                }

            }

            @NonNull
            @Override
            public RequestsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {

                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.users_layout, parent, false);

                return new RequestsViewHolder(view);

            }

        };

        requests_list.setAdapter(adapter);
        adapter.startListening();

    }

    public static class RequestsViewHolder extends RecyclerView.ViewHolder {

        View RView;

        public RequestsViewHolder(@NonNull View itemView) {
            super(itemView);
            RView = itemView;
        }

        public void setName(String name)
        {
            TextView user_name = (TextView) RView.findViewById(R.id.user_name);
            user_name.setText(name);
        }

        public void setThumbImage(String thumbImage)
        {
            CircleImageView user_image = (CircleImageView) RView.findViewById(R.id.user_image);

            if (!thumbImage.equals("default"))
            {
                Picasso.get().load(thumbImage).placeholder(R.drawable.default_avatar).into(user_image);
            }
        }

        public  void setStatus(String status)
        {
            TextView user_status = (TextView) RView.findViewById(R.id.user_status);
            user_status.setText(status);
        }

    }

}
