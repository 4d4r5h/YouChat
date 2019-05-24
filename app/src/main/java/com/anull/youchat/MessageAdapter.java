package com.anull.youchat;

import android.content.Intent;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder>{

    private List<Messages> MessageList;
    private FirebaseAuth MAuth;
    private DatabaseReference userDatabase;

    public MessageAdapter(List<Messages> MessageList) {

        this.MessageList = MessageList;

    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_single_layout, parent, false);

        return new MessageViewHolder(view);

    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {

        private TextView myMessageText, othersMessageText;
        private TextView myMessageTime, othersMessageTime;
        private TextView seenText;

        public MessageViewHolder(View view) {

            super(view);

            myMessageText = (TextView) view.findViewById(R.id.my_message_text_layout);
            myMessageTime = (TextView) view.findViewById(R.id.my_time_text_layout);
            othersMessageText = (TextView) view.findViewById(R.id.others_message_text_layout);
            othersMessageTime = (TextView) view.findViewById(R.id.others_time_text_layout);
            seenText = (TextView) view.findViewById(R.id.seen_text);

        }

    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder viewHolder, int i) {

        MAuth = FirebaseAuth.getInstance();
        String currentUser = MAuth.getCurrentUser().getUid();

        final Messages message = MessageList.get(i);
        String from_user = message.getFrom();
        String message_type = message.getType();
        long mTime = message.getTime();

        GetTimeAgo obj = new GetTimeAgo();
        String sentTime = obj.getTimeAgo(mTime, null);

        if(from_user.equals(currentUser))
        {
            if(message.getSeen())
                viewHolder.seenText.setVisibility(View.VISIBLE);
            else
                viewHolder.seenText.setVisibility(View.GONE);

            viewHolder.othersMessageText.setVisibility(View.GONE);
            viewHolder.othersMessageTime.setVisibility(View.GONE);
            viewHolder.myMessageText.setVisibility(View.VISIBLE);
            viewHolder.myMessageTime.setVisibility(View.VISIBLE);
            viewHolder.myMessageTime.setText(sentTime);

            if (message_type.equals("text"))
            {
                viewHolder.myMessageText.setTypeface(null, Typeface.NORMAL);
                viewHolder.myMessageText.setText(message.getMessage());
                viewHolder.myMessageText.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });
            }
            else
            {
                viewHolder.myMessageText.setTypeface(null, Typeface.BOLD);
                viewHolder.myMessageText.setText("You have sent an image. Tap to open.");
                viewHolder.myMessageText.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent showImageIntent = new Intent(v.getContext(), ShowingImage.class);
                        showImageIntent.putExtra("imageURL", message.getMessage());
                        v.getContext().startActivity(showImageIntent);

                    }
                });
            }
        }
        else
        {
            viewHolder.seenText.setVisibility(View.GONE);
            viewHolder.myMessageText.setVisibility(View.GONE);
            viewHolder.myMessageTime.setVisibility(View.GONE);
            viewHolder.othersMessageText.setVisibility(View.VISIBLE);
            viewHolder.othersMessageTime.setVisibility(View.VISIBLE);
            viewHolder.othersMessageTime.setText(sentTime);

            if(message_type.equals("text"))
            {
                viewHolder.othersMessageText.setTypeface(null, Typeface.NORMAL);
                viewHolder.othersMessageText.setText(message.getMessage());
                viewHolder.othersMessageText.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });
            }
            else
            {
                viewHolder.othersMessageText.setTypeface(null, Typeface.BOLD);
                viewHolder.othersMessageText.setText("You have received an image. Tap to open.");
                viewHolder.othersMessageText.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent showImageIntent = new Intent(v.getContext(), ShowingImage.class);
                        showImageIntent.putExtra("imageURL", message.getMessage());
                        v.getContext().startActivity(showImageIntent);

                    }
                });
            }
        }
    }

    @Override
    public int getItemCount() {
        return MessageList.size();
    }

}
