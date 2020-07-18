package com.example.heyochat;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


public class RequestsFragment extends Fragment {
    private View RequestFragmentView;
    private RecyclerView myRequestsList;
    private DatabaseReference ChatRequestRef,UsersRef,ContactsRef;
    private FirebaseAuth mAuth;
    private String currentUserID;

    public RequestsFragment()
    {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        RequestFragmentView= inflater.inflate(R.layout.fragment_requests, container, false);

        mAuth=FirebaseAuth.getInstance();
        currentUserID=mAuth.getCurrentUser().getUid();
        ChatRequestRef= FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        UsersRef=FirebaseDatabase.getInstance().getReference().child("Users");
        ContactsRef=FirebaseDatabase.getInstance().getReference().child("Contacts");


        myRequestsList=(RecyclerView) RequestFragmentView.findViewById(R.id.chat_requests_list);
        myRequestsList.setLayoutManager(new LinearLayoutManager(getContext()));


    return RequestFragmentView;
    }

    @Override
    public void onStart()
    {
        super.onStart();
        FirebaseRecyclerOptions<Contacts> options=new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(ChatRequestRef.child(currentUserID),Contacts.class).build();

        FirebaseRecyclerAdapter<Contacts,RequestViewHolder> adapter=new
                FirebaseRecyclerAdapter<Contacts, RequestViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final RequestViewHolder holder, int position, @NonNull Contacts contacts)
                    {
                        holder.itemView.findViewById(R.id.request_accept_button).setVisibility(View.VISIBLE);
                        holder.itemView.findViewById(R.id.request_cancel_button).setVisibility(View.VISIBLE);

                       final String list_user_id=getRef(position).getKey();////id of of users in loop of current used id of chat request
                        DatabaseReference getTypeRef=getRef(position).child("request_type").getRef();///reuqest type dekhega of tat psition user in loop
                        getTypeRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot)
                            {
                             if (dataSnapshot.exists())
                             {
                                 String type=dataSnapshot.getValue().toString();   //request type dekhega of current user k node mai sender ki id se
                                 if (type.equals("recieved"))
                                 {
                                     UsersRef.child(list_user_id).addValueEventListener(new ValueEventListener() {//list user ka users node mai search krega
                                         @Override
                                         public void onDataChange(DataSnapshot dataSnapshot)
                                         {
                                             if (dataSnapshot.hasChild("image"))
                                             {
                                                 final String requestProfileImage=dataSnapshot.child("image").getValue().toString();

                                                 Picasso.get().load(requestProfileImage).placeholder(R.drawable.profile_image).into(holder.profileImage);


                                             }
                                             final String requestUserName=dataSnapshot.child("name").getValue().toString();
                                                 final String requestUserStatus=dataSnapshot.child("status").getValue().toString();
                                                 holder.userName.setText(requestUserName);
                                                 holder.userStatus.setText(requestUserStatus);


                                             holder.AcceptButton.setOnClickListener(new View.OnClickListener()
                                             {//clicl on accept button
                                                 @Override
                                                 public void onClick(View view)
                                                 {
                                                    ContactsRef.child(currentUserID).child(list_user_id).child("Contacts")
                                                    .setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override//saving that user in contacts in currentuid
                                                        public void onComplete(@NonNull Task<Void> task)
                                                        {
                                                            if (task.isSuccessful())
                                                            {
                                                                ContactsRef.child(list_user_id).child(currentUserID).child("Contacts")
                                                                        .setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override//saving that user in contacts in crrtuid
                                                                    public void onComplete(@NonNull Task<Void> task)
                                                                    {
                                                                        if (task.isSuccessful())
                                                                        {
                                                                            ChatRequestRef.child(currentUserID).child(list_user_id)
                                                                                    .removeValue()
                                                                                    .addOnCompleteListener(new OnCompleteListener<Void>()
                                                                                    {
                                                                                        @Override//removing that from request node
                                                                                        public void onComplete(@NonNull Task<Void> task)
                                                                                        {
                                                                                            if (task.isSuccessful()) {
                                                                                                ChatRequestRef.child(list_user_id).child(currentUserID)
                                                                                                        .removeValue()
                                                                                                        .addOnCompleteListener(new OnCompleteListener<Void>()
                                                                                                        {
                                                                                                            @Override//remove from request node
                                                                                                            public void onComplete(@NonNull Task<Void> task)
                                                                                                            {if (task.isSuccessful())
                                                                                                            {
                                                                                                                Toast.makeText(getContext(),"Accepted",Toast.LENGTH_SHORT).show();

                                                                                                            }
                                                                                                            }
                                                                                                        });
                                                                                            }


                                                                                        }
                                                                                    });

                                                                        }
                                                                    }
                                                                }) ;

                                                            }
                                                        }
                                                    }) ;

                                                 }
                                             });
                                             holder.CancelButton.setOnClickListener(new View.OnClickListener() {
                                                 @Override
                                                 public void onClick(View view)
                                                 {
                                                     ChatRequestRef.child(currentUserID).child(list_user_id)
                                                             .removeValue()
                                                             .addOnCompleteListener(new OnCompleteListener<Void>()
                                                             {
                                                                 @Override//removing that from request node
                                                                 public void onComplete(@NonNull Task<Void> task)
                                                                 {
                                                                     if (task.isSuccessful()) {
                                                                         ChatRequestRef.child(list_user_id).child(currentUserID)
                                                                                 .removeValue()
                                                                                 .addOnCompleteListener(new OnCompleteListener<Void>()
                                                                                 {
                                                                                     @Override//remove from request node
                                                                                     public void onComplete(@NonNull Task<Void> task)
                                                                                     {if (task.isSuccessful())
                                                                                     {
                                                                                         Toast.makeText(getContext(),"REMOVED",Toast.LENGTH_SHORT).show();

                                                                                     }
                                                                                     }
                                                                                 });
                                                                     }


                                                                 }
                                                             });

                                                 }
                                             });

                                         }

                                         @Override
                                         public void onCancelled(DatabaseError databaseError)
                                         {

                                         }
                                     });
                                 }
                                 else if (type.equals("sent"))
                                 {
                                     Button request_sent_btn=holder.itemView.findViewById(R.id.request_accept_button);
                                     request_sent_btn.setText("Request Sent");


                                     UsersRef.child(list_user_id).addValueEventListener(new ValueEventListener() {//list user ka users node mai search krega
                                         @Override
                                         public void onDataChange(DataSnapshot dataSnapshot)
                                         {
                                             if (dataSnapshot.hasChild("image"))
                                             {
                                                 final String requestProfileImage=dataSnapshot.child("image").getValue().toString();

                                                 Picasso.get().load(requestProfileImage).placeholder(R.drawable.profile_image).into(holder.profileImage);


                                             }
                                             final String requestUserName=dataSnapshot.child("name").getValue().toString();
                                             final String requestUserStatus=dataSnapshot.child("status").getValue().toString();
                                             holder.userName.setText(requestUserName);
                                             holder.userStatus.setText(requestUserStatus);


                                             holder.CancelButton.setOnClickListener(new View.OnClickListener() {
                                                 @Override
                                                 public void onClick(View view)
                                                 {
                                                     ChatRequestRef.child(currentUserID).child(list_user_id)
                                                             .removeValue()
                                                             .addOnCompleteListener(new OnCompleteListener<Void>()
                                                             {
                                                                 @Override//removing that from request node
                                                                 public void onComplete(@NonNull Task<Void> task)
                                                                 {
                                                                     if (task.isSuccessful()) {
                                                                         ChatRequestRef.child(list_user_id).child(currentUserID)
                                                                                 .removeValue()
                                                                                 .addOnCompleteListener(new OnCompleteListener<Void>()
                                                                                 {
                                                                                     @Override//remove from request node
                                                                                     public void onComplete(@NonNull Task<Void> task)
                                                                                     {if (task.isSuccessful())
                                                                                     {
                                                                                         Toast.makeText(getContext(),"You Have Canceled The Request",Toast.LENGTH_SHORT).show();

                                                                                     }
                                                                                     }
                                                                                 });
                                                                     }


                                                                 }
                                                             });

                                                 }
                                             });

                                         }

                                         @Override
                                         public void onCancelled(DatabaseError databaseError)
                                         {

                                         }
                                     });

                                 }


                             }

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError)
                            {

                            }
                        });

                    }

                    @NonNull
                    @Override
                    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType)
                    {
                        View view=LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_display_layout,viewGroup,false);
                        RequestViewHolder viewHolder=new RequestViewHolder(view);
                        return viewHolder;
                    }
                };
            myRequestsList.setAdapter(adapter);
            adapter.startListening();



    }
public static class RequestViewHolder extends RecyclerView.ViewHolder
{
    TextView userName,userStatus;
    CircleImageView profileImage;
     Button AcceptButton,CancelButton;

    public RequestViewHolder(@NonNull View itemView) {
        super(itemView);
        userName=itemView.findViewById(R.id.user_profile_name);
        userStatus=itemView.findViewById(R.id.user_status);
        profileImage=itemView.findViewById(R.id.users_profile_image);
        AcceptButton=itemView.findViewById(R.id.request_accept_button);
        CancelButton=itemView.findViewById(R.id.request_cancel_button);
    }
}

}