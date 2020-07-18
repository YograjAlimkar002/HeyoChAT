package com.example.heyochat;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ViewImageProfilePic extends AppCompatActivity {
    private Toolbar mToolbar;

    private String MessageRecieverImage;
    private ImageView userImage;
    private String messageRecievername;
     private ActionBar actionBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_image_profile_pic);
        messageRecievername=getIntent().getExtras().get("visit_user_name").toString();

        mToolbar = (Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(messageRecievername);
        actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);


        userImage=(ImageView) findViewById(R.id.imageview_dp);

        MessageRecieverImage=getIntent().getExtras().get("visit_user_image").toString();
        Picasso.get().load(MessageRecieverImage).placeholder(R.drawable.profile_image).into(userImage);


    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}