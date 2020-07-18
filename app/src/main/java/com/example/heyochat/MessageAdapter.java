package com.example.heyochat;

import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder>
{
    private List<Messages> userMessagesList ;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    public MessageAdapter(List<Messages> userMessagesList)
    {
        this.userMessagesList=userMessagesList;

    }

    public class MessageViewHolder extends RecyclerView.ViewHolder
    {
        public TextView senderMessageText,recieverMessageText   ;
        public CircleImageView recieverProfileImage;
        public MessageViewHolder(@NonNull View itemView)
        {
            super(itemView);
            senderMessageText=(TextView)itemView.findViewById(R.id.sender_message_text);
            recieverMessageText=(TextView)itemView.findViewById(R.id.reciever_message_text);
            recieverProfileImage=(CircleImageView)itemView.findViewById(R.id.message_profile_image);


        }
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view= LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.custom_messages_layout,viewGroup,false);
        mAuth=FirebaseAuth.getInstance();


        return new MessageViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, final int i)
    {
    String messageSenderID=mAuth.getCurrentUser().getUid();
        Messages messages=userMessagesList.get(i);
        String fromUserID=messages.getFrom();
        String fromMessageType=messages.getType();
        usersRef=FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserID);
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.hasChild("image"))
                {
                    String recieverImage=dataSnapshot.child("image").getValue().toString();
                    Picasso.get().load(recieverImage).placeholder(R.drawable.profile_image).into(holder.recieverProfileImage);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
        if (fromMessageType.equals("text"))//sender
        {
            holder.recieverMessageText.setVisibility(View.INVISIBLE);
            holder.recieverProfileImage.setVisibility(View.INVISIBLE);
            holder.senderMessageText.setVisibility(View.INVISIBLE);

            if (fromUserID.equals(messageSenderID))
            {                holder.senderMessageText.setVisibility(View.VISIBLE);

                holder.senderMessageText.setBackgroundResource(R.drawable.sender_message_layout);
                holder.senderMessageText.setText(messages.getMessage()+"\n \n"+ messages.getTime()+"-"+messages.getDate());

            }
            else {
                holder.recieverMessageText.setVisibility(View.VISIBLE);
                holder.recieverProfileImage.setVisibility(View.VISIBLE);

                holder.recieverMessageText.setBackgroundResource(R.drawable.receiver_messages_layout);
                holder.recieverMessageText.setText(messages.getMessage()+"\n \n"+ messages.getTime()+"-"+messages.getDate());


            }
        }

    }

    @Override
    public int getItemCount() {
        return userMessagesList.size();
    }


}
