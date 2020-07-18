package com.example.heyochat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity
{
    private String recieverUserID,senderUserID,Current_State;
    private CircleImageView userProfileImage;
    private TextView userProfileName,userProfileStatus;
    private Button SendMessageRequestButton,DeclineMessageRequestButton;
    private DatabaseReference UserRef,ChatRequestRef,ContactsRef,NotificationRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth=FirebaseAuth.getInstance();
        UserRef= FirebaseDatabase.getInstance().getReference().child("Users");
        ChatRequestRef= FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        ContactsRef= FirebaseDatabase.getInstance().getReference().child("Contacts");
        NotificationRef= FirebaseDatabase.getInstance().getReference().child("Notifications");


        recieverUserID=getIntent().getExtras().get("visit_user_id").toString();//clicked user

        senderUserID=mAuth.getCurrentUser().getUid();//curent user


        userProfileImage=(CircleImageView)findViewById(R.id.visit_profile_image);
        userProfileName=(TextView)findViewById(R.id.visit_user_name);
        userProfileStatus=(TextView)findViewById(R.id.visit_profile_status);
        SendMessageRequestButton=(Button)findViewById(R.id.send_message_request_button);
        DeclineMessageRequestButton=(Button)findViewById(R.id.decline_message_request_button);
        Current_State="new";
        RetrieveUserInfo();
    }

    private void RetrieveUserInfo()
    {
 UserRef.child(recieverUserID).addValueEventListener(new ValueEventListener() {
     @Override
     public void onDataChange(DataSnapshot dataSnapshot)
     {
      if ((dataSnapshot.exists())&&(dataSnapshot.hasChild("image")))
      {
          String userImage=dataSnapshot.child("image").getValue().toString();
          String userName=dataSnapshot.child("name").getValue().toString();
          String userStatus=dataSnapshot.child("status").getValue().toString();

          Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(userProfileImage);
          userProfileName.setText(userName);
          userProfileStatus.setText(userStatus);

           ManageChatRequests();

      }
      else {
          String userName=dataSnapshot.child("name").getValue().toString();
          String userStatus=dataSnapshot.child("status").getValue().toString();

          userProfileName.setText(userName);
          userProfileStatus.setText(userStatus);
          ManageChatRequests();

      }


     }

     @Override
     public void onCancelled(DatabaseError databaseError) {

     }
 });

    }

    private void ManageChatRequests()
    {////chcks curent user to show button type
         ChatRequestRef.child(senderUserID).addValueEventListener(new ValueEventListener()
         {
             @Override
             public void onDataChange(DataSnapshot dataSnapshot)
             {
                 if (dataSnapshot.hasChild(recieverUserID))
               {
                 String request_type=dataSnapshot.child(recieverUserID).child("request_type").getValue().toString();
                 if (request_type.equals("sent"))
                 {
                     Current_State="request_sent";
                     SendMessageRequestButton.setText("Cancel Chat Request");
                 }
                 else if (request_type.equals("recieved"))
                 {
                     Current_State="request_recieved";
                     SendMessageRequestButton.setText("Accept Chat Request");
                     DeclineMessageRequestButton.setVisibility(View.VISIBLE);
                     DeclineMessageRequestButton.setEnabled(true);
                     DeclineMessageRequestButton.setOnClickListener(new View.OnClickListener()
                     {
                         @Override
                         public void onClick(View view) {
                             CancelChatRequest();
                         }
                     });


                 }

              }else
                 {//to display current user the remove the contact button
                     ContactsRef.child(senderUserID).addValueEventListener(new ValueEventListener() {
                         @Override
                         public void onDataChange(DataSnapshot dataSnapshot)
                         {
                             if (dataSnapshot.hasChild(recieverUserID))
                             {
                                 Current_State="friends";
                                 SendMessageRequestButton.setText("Remove This Contact");
                             }

                         }

                         @Override
                         public void onCancelled(DatabaseError databaseError)
                         {

                         }
                     });
                 }

             }

             @Override
             public void onCancelled(DatabaseError databaseError) {

             }
         });

        if(!senderUserID.equals(recieverUserID))
        {//current user click on other contact
            SendMessageRequestButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view)
                {
                    SendMessageRequestButton.setEnabled(false);
                    if (Current_State.equals("new"))////two persons r new to each othjr
                    {
                        SendChatRequest();
                    }
                    if (Current_State.equals("request_sent"))
                    {
                        CancelChatRequest();
                    }
                    if (Current_State.equals("request_recieved"))
                    {
                        AcceptChatRequest();
                    }
                    if (Current_State.equals("friends"))/////////if both are friends nd click on remove contact...il remove from contacts node
                    {
                        RemoveSpecificContact();
                    }
                }
            });
        }
        else
            {    //current user click on his own contact
                SendMessageRequestButton.setVisibility(View.INVISIBLE);

            }
    }

    private void RemoveSpecificContact()
    {

        ContactsRef.child(senderUserID).child(recieverUserID)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {if (task.isSuccessful())
            {
                ContactsRef.child(recieverUserID).child(senderUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful())
                        {
                            SendMessageRequestButton.setEnabled(true);
                            Current_State="new";

                            SendMessageRequestButton.setText("Send Message");
                            DeclineMessageRequestButton.setVisibility(View.INVISIBLE);
                            DeclineMessageRequestButton.setEnabled(false);


                        }

                    }
                });
            }

            }
        });
    }

    private void AcceptChatRequest()
    {///////////saving data in contacts for sender
        ContactsRef.child(senderUserID).child(recieverUserID)
                .child("Contacts").setValue("Saved")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if (task.isSuccessful())
                {///////////saving data in contacts for reciever
                    ContactsRef.child(recieverUserID).child(senderUserID)
                            .child("Contacts").setValue("Saved")
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            if (task.isSuccessful())
                            {   ///////////deleting data from chat request
                                ChatRequestRef.child(senderUserID).child(recieverUserID)
                                        .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task)
                                    {
                                        if (task.isSuccessful())
                                    {/////////deleting data from chat request
                                        ChatRequestRef.child(recieverUserID).child(senderUserID)
                                                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task)
                                            {SendMessageRequestButton.setEnabled(true);
                                            Current_State="friends";
                                                SendMessageRequestButton.setText("Remove This Contact");
                                                DeclineMessageRequestButton.setVisibility(View.INVISIBLE);
                                                DeclineMessageRequestButton.setEnabled(false);

                                            }
                                        });


                                    }
                                    }
                                });

                            }
                        }
                    });
                }
            }
        });
    }

    private void CancelChatRequest()
    {
        ChatRequestRef.child(senderUserID).child(recieverUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {if (task.isSuccessful())
            {
                ChatRequestRef.child(recieverUserID).child(senderUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful())
                        {
                            SendMessageRequestButton.setEnabled(true);
                            Current_State="new";

                            SendMessageRequestButton.setText("Send Message");
                            DeclineMessageRequestButton.setVisibility(View.INVISIBLE);
                            DeclineMessageRequestButton.setEnabled(false);


                              }

                    }
                });
            }

            }
        });
    }

    private void SendChatRequest()
    {
        ChatRequestRef.child(senderUserID).child(recieverUserID).child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
           if (task.isSuccessful())
           {
               ChatRequestRef.child(recieverUserID).child(senderUserID).child("request_type").setValue("recieved")
                       .addOnCompleteListener(new OnCompleteListener<Void>() {
                   @Override
                   public void onComplete(@NonNull Task<Void> task)
                   {
                  if (task.isSuccessful())
                  {
                      HashMap<String,String> chatNotificationMap=new HashMap<>();
                      chatNotificationMap.put("from",senderUserID);
                      chatNotificationMap.put("type","request");
                      NotificationRef.child(recieverUserID).push().setValue(chatNotificationMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                          @Override//FOR NOTI
                          public void onComplete(@NonNull Task<Void> task)
                          {
                              if (task.isSuccessful())
                              {
                                  SendMessageRequestButton.setEnabled(true);
                                  Current_State="request sent";
                                  SendMessageRequestButton.setText("Cancel Chat Request");

                              }

                          }
                      });


                  }
                   }
               });
           }
            }
        });

    }
}