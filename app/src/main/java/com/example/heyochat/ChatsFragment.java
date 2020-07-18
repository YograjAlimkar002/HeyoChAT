package com.example.heyochat;

import android.content.Intent;
import android.icu.util.Calendar;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment {

    private View PrivateChatsView;
    private RecyclerView chatsList;
   private DatabaseReference ChatsRef,UsersRef,RootRef;
   private FirebaseAuth mAuth;
   private String currentUserID,calledBy="";


    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,

                             Bundle savedInstanceState) {
        PrivateChatsView= inflater.inflate(R.layout.fragment_chats, container, false);

        mAuth=  FirebaseAuth.getInstance();
        currentUserID=mAuth.getCurrentUser().getUid();
        RootRef= FirebaseDatabase.getInstance().getReference();

   ChatsRef= FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserID);
   UsersRef=FirebaseDatabase.getInstance().getReference().child("Users");// Inflate the layout for this fragment
        chatsList=(RecyclerView) PrivateChatsView.findViewById(R.id.chats_list);
        chatsList.setLayoutManager(new LinearLayoutManager(getContext()));



    return PrivateChatsView;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onStart()
    {
        super.onStart();
//calling code mehod



        FirebaseRecyclerOptions<Contacts> options=new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(ChatsRef,Contacts.class).build();

        FirebaseRecyclerAdapter<Contacts,ChatsViewHolder> adapter=new FirebaseRecyclerAdapter<Contacts, ChatsViewHolder>(options)
        {
            @Override
            protected void onBindViewHolder(@NonNull final ChatsViewHolder holder, int position, @NonNull Contacts contacts)
            {
               final String usersIDs=getRef(position).getKey();
                final String[] userImage = {"default_image"};
                UsersRef.child(usersIDs).addValueEventListener(new ValueEventListener() { ////get the deatil of user of tat id
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        if (dataSnapshot.exists())
                        {
                            if (dataSnapshot.hasChild("image"))
                            {
                                userImage[0] =dataSnapshot.child("image").getValue().toString();
                                Picasso.get().load(userImage[0]).placeholder(R.drawable.profile_image).into(holder.profileImage);



                            }

                            final String profileName=dataSnapshot.child("name").getValue().toString();
                            String profileStatus=dataSnapshot.child("status").getValue().toString();

                            holder.userName.setText(profileName);


                            if (dataSnapshot.child("userState").hasChild("state"))
                            {
                                String state=dataSnapshot.child("userState").child("state").getValue().toString();
                                String date=dataSnapshot.child("userState").child("date").getValue().toString();
                                String time=dataSnapshot.child("userState").child("time").getValue().toString();
                                if (state.equals("online"))
                                {
                                    holder.userStatus.setText("online");

                                }else if (state.equals("offline"))
                                {
                                    holder.userStatus.setText("Last Seen:"+date+""+time);

                                }
                            }
                            else
                                {
                                holder.userStatus.setText("offline");

                                }

                            ////////////VIDEO CALL KA CODE
                            holder.fragmentVideoCallButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view)
                                {

                                    Intent caliingIntent=new Intent(getContext(),CallingActivity.class);
                                    caliingIntent.putExtra("visit_user_id",usersIDs);

                                    startActivity(caliingIntent);
                                }
                            });




                            holder.itemView.setOnClickListener(new View.OnClickListener() {//WHEN CURRENT USER CLCIK ON ANY CONTACT
                                @Override
                                public void onClick(View view)
                                {



                                    Intent chatIntent=new Intent(getContext(),ChatActivity.class);
                                    chatIntent.putExtra("visit_user_id",usersIDs); //send id of clicked contact
                                    chatIntent.putExtra("visit_user_name",profileName);
                                    chatIntent.putExtra("visit_user_image", userImage[0]);
                                    //send name of clicked contact

                                    startActivity(chatIntent);




                                }
                            });

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
            public ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
            {
                View view= LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_display_layout,viewGroup,false);
                 return new ChatsViewHolder(view);
            }
        };
        chatsList.setAdapter(adapter);
        adapter.startListening();
    }



    public static class ChatsViewHolder extends RecyclerView.ViewHolder
{
    TextView userName,userStatus;
    CircleImageView profileImage;
    Button fragmentVideoCallButton;

    public ChatsViewHolder(@NonNull View itemView)
    {
        super(itemView);

        userName=itemView.findViewById(R.id.user_profile_name);
        userStatus=itemView.findViewById(R.id.user_status);
        profileImage=itemView.findViewById(R.id.users_profile_image);
        fragmentVideoCallButton=itemView.findViewById(R.id.fragment_video_call_btn);


    }
}
//calling code

}
