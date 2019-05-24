package com.anull.youchat;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;
import de.hdodenhof.circleimageview.CircleImageView;

public class AllUsers extends AppCompatActivity {

    private Toolbar uToolbar;
    private RecyclerView all_users_list;
    private String current_user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_users);

        uToolbar = (Toolbar) findViewById(R.id.all_users_bar);
        setSupportActionBar(uToolbar);
        getSupportActionBar().setTitle("All Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().show();

        current_user = getIntent().getStringExtra("current_user");

        all_users_list = (RecyclerView) findViewById(R.id.all_users_list);
        all_users_list.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        all_users_list.setLayoutManager(linearLayoutManager);
    }

    @Override
    protected void onStart() {

        super.onStart();
        startListening();

    }

    private void startListening() {

        Query query = FirebaseDatabase.getInstance().getReference().child("users");

        FirebaseRecyclerOptions<Users> options = new FirebaseRecyclerOptions.Builder<Users>().setQuery(query, Users.class).build();

        FirebaseRecyclerAdapter adapter = new FirebaseRecyclerAdapter<Users, UsersViewHolder>(options) {

            @Override
            protected void onBindViewHolder(@NonNull UsersViewHolder holder, int position, @NonNull Users model) {

                holder.setName(model.name);
                holder.setStatus(model.status);
                holder.setThumbImage(model.thumb_image);

                final String userID = getRef(position).getKey();

                holder.uView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if(!userID.equals(current_user))
                        {
                            Intent profileIntent = new Intent(AllUsers.this, ProfileActivity.class);
                            profileIntent.putExtra("user_id", userID);
                            startActivity(profileIntent);
                        }

                    }
                });

            }

            @NonNull
            @Override
            public UsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.users_layout, parent, false);

                return new UsersViewHolder(view);

            }
        };

        all_users_list.setAdapter(adapter);
        adapter.startListening();

    }

    public static class UsersViewHolder extends RecyclerView.ViewHolder {

        View uView;

        public UsersViewHolder(@NonNull View itemView) {
            super(itemView);
            uView = itemView;
        }


        public void setName(String name) {

            TextView user_name = (TextView) uView.findViewById(R.id.user_name);
            user_name.setText(name);
        }



        public void setStatus(String status) {

            TextView user_status = (TextView) uView.findViewById(R.id.user_status);
            user_status.setText(status);
        }

        public void setThumbImage(String thumbImage) {

            CircleImageView user_image = (CircleImageView) uView.findViewById(R.id.user_image);

            if(!thumbImage.equals("default"))
            {
                Picasso.get().load(thumbImage).placeholder(R.drawable.default_avatar).into(user_image);
            }
        }

    }

}
