package com.example.heyochat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;

import android.text.TextUtils;
import android.view.View;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.security.acl.Group;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupChatActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private ImageButton SendMessageButton;
    private EditText userMessageInputs;
    private ScrollView mScrollView;
    private TextView displayTextMessages;
    private String currentGroupName,currentUserID,currentUserName,currentDate,currentTime;

    private FirebaseAuth mAuth;
    private DatabaseReference UserRef,GroupNameRef,GroupMessageKeyRef;
    public TextView senderMessageText,recieverMessageText   ;
  private CircleImageView recieverProfileImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        currentGroupName=getIntent().getExtras().get("groupName").toString();
        mAuth=FirebaseAuth.getInstance();
        currentUserID=mAuth.getCurrentUser().getUid();
        UserRef= FirebaseDatabase.getInstance().getReference().child("Users");
        GroupNameRef=FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupName).child("messages");

        currentGroupName=getIntent().getExtras().get("groupName").toString();
        InitializeFields();
        GetUserInfo();
        SendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SaveMessageToDatabase();
                userMessageInputs.setText("");

                mScrollView.fullScroll(ScrollView.FOCUS_DOWN);

            }
        });




    }

    @Override
    protected void onStart()
    {
        super.onStart();
        GroupNameRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
            {
                if(dataSnapshot.exists())

                {
                    DisplayMessages(dataSnapshot);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
            {
                if(dataSnapshot.exists())

                {
                    DisplayMessages(dataSnapshot);
                }
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


    private void InitializeFields() {
        mToolbar=(Toolbar)findViewById(R.id.group_chat_bar_layout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(currentGroupName);
        SendMessageButton= (ImageButton) findViewById(R.id.send_message_button);
        userMessageInputs=(EditText)findViewById(R.id.input_group_message);
        senderMessageText=(TextView)findViewById(R.id.sender_message_text);
        recieverMessageText=(TextView)findViewById(R.id.reciever_message_text);
        recieverProfileImage=(CircleImageView)findViewById(R.id.message_profile_image);
        displayTextMessages=(TextView)findViewById(R.id.group_chat_text_display);
        mScrollView=(ScrollView)findViewById(R.id.my_scroll_view);

    }
    private void GetUserInfo()
    {
        UserRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if ((dataSnapshot.exists()))

                {
                    currentUserName=dataSnapshot.child("name").getValue().toString();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private void SaveMessageToDatabase()
    {
        String message=userMessageInputs.getText().toString();
        String messageKEY=GroupNameRef.push().getKey();

        if (TextUtils.isEmpty(message))

        {
            Toast.makeText(GroupChatActivity.this,"Please Write Message First",Toast.LENGTH_SHORT);

        }
        else
        {
            Calendar calForDate= Calendar.getInstance();
            SimpleDateFormat currentDateFormat=new SimpleDateFormat("MMM dd,yyyy");
            currentDate=currentDateFormat.format(calForDate.getTime());

            Calendar calForTime= Calendar.getInstance();
            SimpleDateFormat currentTimeFormat=new SimpleDateFormat("hh:mm a");
            currentTime=currentTimeFormat.format(calForTime.getTime());

            HashMap<String,Object> groupMessageKey=new HashMap<>();

            GroupNameRef.updateChildren(groupMessageKey);
            GroupMessageKeyRef=GroupNameRef.child(messageKEY);   //STORING IT IN GMKREF THE GROUP KEY

            HashMap<String ,Object> messageInfoMap=new HashMap<>() ;
            messageInfoMap.put("name",currentUserName);
            messageInfoMap.put("message",message);
            messageInfoMap.put("date",currentDate);
            messageInfoMap.put("time",currentTime);
            messageInfoMap.put("senderID",currentUserID);

            GroupMessageKeyRef.updateChildren(messageInfoMap);


        }
    }

    private void DisplayMessages(DataSnapshot dataSnapshot)
    {
        Iterator iterator=dataSnapshot.getChildren().iterator();
        while (iterator.hasNext())

        {
            String chatDate=(String)((DataSnapshot)iterator.next()).getValue();
            String chatMessage=(String)((DataSnapshot)iterator.next()).getValue();
            String chatName=(String)((DataSnapshot)iterator.next()).getValue();
            String senderID=(String)((DataSnapshot)iterator.next()).getValue();
            String chatTime=(String)((DataSnapshot)iterator.next()).getValue();


            displayTextMessages.append(chatName+":\n"+chatMessage+"\n"+chatTime+"     "+chatDate+"\n\n\n");

            mScrollView.fullScroll(ScrollView.FOCUS_DOWN);  mScrollView.fullScroll(ScrollView.FOCUS_DOWN);

        }
    }

}