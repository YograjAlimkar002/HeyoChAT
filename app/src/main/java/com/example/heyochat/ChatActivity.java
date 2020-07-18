package com.example.heyochat;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.icu.util.Calendar;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private String messageRecieverId,messageRecieverName,MessageRecieverImage,messageSenderID;
    private TextView userName,userLastSeen;
    private CircleImageView userImage;
    private Toolbar ChatToolbar;
    private ImageButton SendMessageButton;
    private EditText MessageInputText;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;
    private final List<Messages> messagesList=new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;
    private RecyclerView userMessagesList;
    private String saveCurrentTime,saveCurrentDate;
    private String currentUserID;
    private Button VideoCallButton;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_chat);
         mAuth=FirebaseAuth.getInstance();
         messageSenderID=mAuth.getCurrentUser().getUid();
         RootRef= FirebaseDatabase.getInstance().getReference();


        messageRecieverId=getIntent().getExtras().get("visit_user_id").toString();
        messageRecieverName=getIntent().getExtras().get("visit_user_name").toString();
        MessageRecieverImage=getIntent().getExtras().get("visit_user_image").toString();

        IntitalizeControllers();
        userName.setText(messageRecieverName);
        Picasso.get().load(MessageRecieverImage).placeholder(R.drawable.profile_image).into(userImage);

        DisplayLastSeen();


        userImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent chatIntent=new Intent(ChatActivity.this,ViewImageProfilePic.class);

                chatIntent.putExtra("visit_user_image", MessageRecieverImage);
                chatIntent.putExtra("visit_user_name", messageRecieverName);
                //send name of clicked contact

                startActivity(chatIntent);

            }
        });

        SendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                 SendMessage();

            }
        });
    VideoCallButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view)
        {
            Intent caliingIntent=new Intent(ChatActivity.this,CallingActivity.class);
            caliingIntent.putExtra("visit_user_id",messageRecieverId);

            startActivity(caliingIntent);

        }
    });
    }



    
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void IntitalizeControllers()
    {
        ChatToolbar=(Toolbar)findViewById(R.id.chat_toolbar);
       setSupportActionBar(ChatToolbar);
        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        LayoutInflater layoutInflater=(LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView= layoutInflater.inflate(R.layout.custom_chat_bar,null);
        actionBar.setCustomView(actionBarView);
        userImage=(CircleImageView)findViewById(R.id.custom_profile_image);
        userName=(TextView)findViewById(R.id.custom_profile_name);
        userLastSeen=(TextView)findViewById(R.id.custom_user_last_seen);
        VideoCallButton=(Button)findViewById(R.id.video_call_btn);
        SendMessageButton=(ImageButton)findViewById(R.id.send_message_btn);
        MessageInputText=(EditText)findViewById(R.id.input_message);
        messageAdapter=new MessageAdapter(messagesList);
        userMessagesList=(RecyclerView)findViewById(R.id.private_messages_list_of_users);
        linearLayoutManager=new LinearLayoutManager(this);
        userMessagesList.setLayoutManager(linearLayoutManager);
        userMessagesList.setAdapter(messageAdapter);

        Calendar calendar=Calendar.getInstance();
        SimpleDateFormat currentDate=new SimpleDateFormat("MM dd,yyyy");
        saveCurrentDate=currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime=new SimpleDateFormat("hh:mm a");
        saveCurrentTime=currentTime.format(calendar.getTime());

    }

    private  void DisplayLastSeen()
    {
        RootRef.child("Users").child(messageRecieverId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.child("userState").hasChild("state"))
                {
                    String state=dataSnapshot.child("userState").child("state").getValue().toString();
                    String date=dataSnapshot.child("userState").child("date").getValue().toString();
                    String time=dataSnapshot.child("userState").child("time").getValue().toString();
                    if (state.equals("online"))
                    {
                        userLastSeen.setText("online");

                    }else if (state.equals("offline"))
                    {
                        userLastSeen.setText("Last Seen:" + date + "" + time);

                    }
                }
                else
                {
                    userLastSeen.setText("offline");

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onStart() {
        super.onStart();



        RootRef.child("Messages").child(messageSenderID).child(messageRecieverId).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s)
            {
             Messages messages=dataSnapshot.getValue(Messages.class);

             messagesList.add(messages);
             messageAdapter.notifyDataSetChanged();
             userMessagesList.smoothScrollToPosition(userMessagesList.getAdapter().getItemCount());


            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }








    private void SendMessage()
    {

        String messageText=MessageInputText.getText().toString();
        if (TextUtils.isEmpty(messageText))
        {
            Toast.makeText(ChatActivity.this,"Write Your Message FIrst",Toast.LENGTH_SHORT).show();

        }
        else
        {
            String messageSenderRef="Messages/"+ messageSenderID + "/"+messageRecieverId;
            String  messageRecieverRef="Messages/"+ messageRecieverId + "/"+messageSenderID;

            DatabaseReference userMessageKeyRef=RootRef.child("Messages")
                    .child(messageSenderID).child(messageRecieverId).push();
            String messagePushID=userMessageKeyRef.getKey();

            Map messageTextBody=new HashMap();
            messageTextBody.put("message",messageText);
            messageTextBody.put("type","text");
            messageTextBody.put("from",messageSenderID);
            messageTextBody.put("to",messageRecieverId);
            messageTextBody.put("messageID",messagePushID);
            messageTextBody.put("time",saveCurrentTime);
            messageTextBody.put("date",saveCurrentDate);

            Map messageBodyDetails=new HashMap();
            messageBodyDetails.put(messageSenderRef+"/"+messagePushID,messageTextBody);
            messageBodyDetails.put(messageRecieverRef+"/"+messagePushID,messageTextBody);

            RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
               if (task.isSuccessful())
               {

               }

               MessageInputText.setText("");
                }
            });
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void updateUserStatus(String state)
    {
        String saveCurrentTime, saveCurrentDate;

        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calendar.getTime());

        HashMap<String, Object> onlineStateMap = new HashMap<>();
        onlineStateMap.put("time", saveCurrentTime);
        onlineStateMap.put("date", saveCurrentDate);
        onlineStateMap.put("state", state);

        RootRef.child("Users").child(currentUserID).child("userState")
                .updateChildren(onlineStateMap);

    }



}