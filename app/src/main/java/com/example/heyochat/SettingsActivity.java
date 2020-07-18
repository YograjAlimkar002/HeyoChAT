package com.example.heyochat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {
 private Button UpdateAccountSetting;
 private EditText userName,userStatus;
 private CircleImageView userProfileImage;
 private String currentUserID;
 private FirebaseAuth mAuth;
 private DatabaseReference RootRef;
 private static final int GalleryPick=1;
 private StorageReference UserProfileImagesRef;
 private ProgressDialog loadingBar;
    private Toolbar mToolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mToolbar=(Toolbar)findViewById(R.id.settings_toolbar);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle("Settings");

        mAuth=FirebaseAuth.getInstance();
        currentUserID=mAuth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();
        UserProfileImagesRef= FirebaseStorage.getInstance().getReference().child("Profile Images");//CREATE FOLDER IN DATABSE
        Initializefields();


        UpdateAccountSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                UpdateSettings();
            }
        });

        RetrieveUserInfo();

        userProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Intent galleryIntent=new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,GalleryPick);

            }
        });
    }




    private void Initializefields() {
        UpdateAccountSetting=(Button)findViewById(R.id.update_settings_button);
        userName=(EditText)findViewById(R.id.set_user_name);
       userStatus =(EditText)findViewById(R.id.set_profile_status);
       userProfileImage=(CircleImageView)findViewById(R.id.profile_image);
       loadingBar=new ProgressDialog(this);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
         if (requestCode==GalleryPick && resultCode==RESULT_OK && data!=null)
         {
             Uri imageUri=data.getData();
             CropImage.activity()
                     .setGuidelines(CropImageView.Guidelines.ON)
                     .setAspectRatio(1,1)
                     .start(this);

         }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode==RESULT_OK)
            {    loadingBar.setTitle("Set Profile Image");
                 loadingBar.setMessage("Please Wait ,Your Profile Image Is Updating..");
                 loadingBar.setCanceledOnTouchOutside(false);
                 loadingBar.show();
                Uri resultUri=result.getUri();
                StorageReference filepath=UserProfileImagesRef.child(currentUserID+".jpg");
                filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                   if (task.isSuccessful())
                   {
                       Toast.makeText(SettingsActivity.this,"Profile Image Uploaded Successfully",Toast.LENGTH_SHORT).show();
                       final String downloadUrl=task.getResult().getDownloadUrl().toString();
                       RootRef.child("Users").child(currentUserID).child("image").setValue(downloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                           @Override
                           public void onComplete(@NonNull Task<Void> task) {
                          if (task.isSuccessful())
                          {
                              Toast.makeText(SettingsActivity.this,"Image Saved in Database",Toast.LENGTH_SHORT).show();
                               loadingBar.dismiss();
                          }
                          else {
                              String messsage=task.getException().toString();
                              Toast.makeText(SettingsActivity.this,"ERROR:"+messsage,Toast.LENGTH_SHORT).show();
                              loadingBar.dismiss();

                          }
                           }
                       });
                   }
                   else {
                       String messsage=task.getException().toString();
                       Toast.makeText(SettingsActivity.this,"ERROR:"+messsage,Toast.LENGTH_SHORT).show();
                       loadingBar.dismiss();

                   }
                    }
                });
            }
        }
    }

    private void UpdateSettings()
    {
        String setUserName=userName.getText().toString();
        String setStatus=userStatus.getText().toString();
        if (TextUtils.isEmpty(setUserName))
        {
            Toast.makeText(SettingsActivity.this,"Please Write Your Name",Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(setStatus))
        {
            Toast.makeText(SettingsActivity.this,"Please Write Your Status",Toast.LENGTH_SHORT).show();
        }
        else
        {
            HashMap<String,Object> profilemap=new HashMap<>();
            profilemap.put("uid",currentUserID);
            profilemap.put("name",setUserName);
            profilemap.put("status",setStatus);

            RootRef.child("Users").child(currentUserID).updateChildren(profilemap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                   if (task.isSuccessful())
                   {   SendUserToMainACTIVITY();
                       Toast.makeText(SettingsActivity.this,"Profile Updated Successfully",Toast.LENGTH_SHORT).show();
                   }
                   else
                       {
                       String message=task.getException().toString();
                           Toast.makeText(SettingsActivity.this,"ERROR:"+ message,Toast.LENGTH_SHORT).show();

                       }
                }
            });

        }

    }
    private void RetrieveUserInfo()
    {
    RootRef.child("Users").child(currentUserID).addValueEventListener(new ValueEventListener() {     //datasnapshot is RootRef value listened
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            if( (dataSnapshot.exists())&& (dataSnapshot.hasChild("name")&&(dataSnapshot.hasChild("image"))))
            {
              String retrieveUserName=dataSnapshot.child("name").getValue().toString();
              String retrieveStatus=dataSnapshot.child("status").getValue().toString();
              String retrieveProfileImage=dataSnapshot.child("image").getValue().toString();

                userName.setText(retrieveUserName);
                userStatus.setText(retrieveStatus);
                Picasso.get().load(retrieveProfileImage).into(userProfileImage);

            }
            else if( (dataSnapshot.exists())&& (dataSnapshot.hasChild("name")))

            {
                String retrieveUserName=dataSnapshot.child("name").getValue().toString();
                String retrieveStatus=dataSnapshot.child("status").getValue().toString();


                userName.setText(retrieveUserName);
                userStatus.setText(retrieveStatus);

            }
            else
            {
                Toast.makeText(SettingsActivity.this,"Please set and update profile information",Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    });

    }

    private void SendUserToMainACTIVITY() {
        Intent mainintent=new Intent(SettingsActivity.this,MainActivity.class);
        mainintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainintent);
        finish();;
    }
}