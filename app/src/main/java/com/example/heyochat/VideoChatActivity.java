package com.example.heyochat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.opengl.GLSurfaceView;
import android.os.Bundle;

import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import android.util.Log;
import com.opentok.android.Session;
import com.opentok.android.Stream;
import com.opentok.android.Publisher;
import com.opentok.android.PublisherKit;
import com.opentok.android.Subscriber;
import com.opentok.android.OpentokError;
import androidx.annotation.NonNull;
import android.Manifest;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;


public class VideoChatActivity extends AppCompatActivity  implements Session.SessionListener,
PublisherKit.PublisherListener
{

    private static String API_KEY="46836044";
    private static String SESSION_ID="1_MX40NjgzNjA0NH5-MTU5NDU4ODIxNDE4MX5Cd3lXL3FTelp3RVczQm1HZGdkU3JFVjZ-fg";
    private static String TOKEN="T1==cGFydG5lcl9pZD00NjgzNjA0NCZzaWc9ZDM2NjhhMWI5ZjE1NmQ3NDZkNzI3NzRhN2JlYzMzZTIwYzgyOTU2YzpzZXNzaW9uX2lkPTFfTVg0ME5qZ3pOakEwTkg1LU1UVTVORFU0T0RJeE5ERTRNWDVDZDNsWEwzRlRlbHAzUlZjelFtMUhaR2RrVTNKRlZqWi1mZyZjcmVhdGVfdGltZT0xNTk0NTg4MzA0Jm5vbmNlPTAuNDg1ODM4OTEzMzYzNTcxNDMmcm9sZT1wdWJsaXNoZXImZXhwaXJlX3RpbWU9MTU5NzE4MDMwMiZpbml0aWFsX2xheW91dF9jbGFzc19saXN0PQ==";
    private static final String LOG_TAG=VideoChatActivity.class.getSimpleName();
    private static final int RC_VIDEO_APP_PERM=124;

    private FrameLayout mPublisherViewController;
    private FrameLayout mSubscriberViewController;
    private ImageView closeVideoChatBtn;
    private DatabaseReference usersRef;
    private String usersID="";
     private com.opentok.android.Session mSession;
     private Publisher mPublisher;
     private Subscriber mSubscriber;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_chat);

        usersID= FirebaseAuth.getInstance().getCurrentUser().getUid();
        usersRef= FirebaseDatabase.getInstance().getReference().child("Users");

        closeVideoChatBtn=findViewById(R.id.close_video_chat_btn);
        closeVideoChatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
             usersRef.addValueEventListener(new ValueEventListener() {
                 @Override
                 public void onDataChange(DataSnapshot dataSnapshot)
                 {
                     if (dataSnapshot.child(usersID).hasChild("Ringing"))//recievr
                     {
                         if (mPublisher!=null)
                     {
                       mPublisher.destroy();

                     }
                         if (mSubscriber!=null)
                         {
                             mSubscriber.destroy();

                         }

                         usersRef.child(usersID).child("Ringing")
                                 .removeValue();
                         startActivity(new Intent(VideoChatActivity.this,MainActivity.class));
                         finish();

                     }
                     if (dataSnapshot.child(usersID).hasChild("Calling"))//sender
                     {
                         usersRef.child(usersID).child("Calling")
                                 .removeValue();
                         if (mPublisher!=null)
                         {
                             mPublisher.destroy();

                         }
                         if (mSubscriber!=null)
                         {
                             mSubscriber.destroy();

                         }
                         startActivity(new Intent(VideoChatActivity.this,MainActivity.class));
                         finish();

                     }
                     else
                     {
                         if (mPublisher!=null)
                         {
                             mPublisher.destroy();

                         }
                         if (mSubscriber!=null)
                         {
                             mSubscriber.destroy();

                         }
                         startActivity(new Intent(VideoChatActivity.this,MainActivity.class));
                         finish();

                     }
                 }

                 @Override
                 public void onCancelled(DatabaseError databaseError) {

                 }
             });

            }
        });
        requestPermissions();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode,permissions,grantResults,VideoChatActivity.this);

    }
    @AfterPermissionGranted(RC_VIDEO_APP_PERM)
    private void requestPermissions() {
        String[] perms = { Manifest.permission.INTERNET, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO };
        if (EasyPermissions.hasPermissions(this, perms))
        {
            mPublisherViewController=findViewById(R.id.publisher_container);
            mSubscriberViewController=findViewById(R.id.subscriber_container);

            mSession = new Session.Builder(this, API_KEY, SESSION_ID).build();
            mSession.setSessionListener(VideoChatActivity.this);
            mSession.connect(TOKEN);

        }
        else {
            EasyPermissions.requestPermissions(this, "This app needs access to your camera and mic to make video calls", RC_VIDEO_APP_PERM, perms);
        }
    }

    @Override
    public void onStreamCreated(PublisherKit publisherKit, Stream stream) {

    }

    @Override
    public void onStreamDestroyed(PublisherKit publisherKit, Stream stream) {

    }

    @Override
    public void onError(PublisherKit publisherKit, OpentokError opentokError) {

    }

    @Override
    public void onConnected(Session session)
    {
        Log.i(LOG_TAG, "Session Connected");

        mPublisher = new Publisher.Builder(this).build();
        mPublisher.setPublisherListener(VideoChatActivity.this);

        mPublisherViewController.addView(mPublisher.getView());

        if (mPublisher.getView() instanceof GLSurfaceView){
            ((GLSurfaceView) mPublisher.getView()).setZOrderOnTop(true);
        }

        mSession.publish(mPublisher);
    }



    @Override
    public void onDisconnected(Session session) {
        Log.i(LOG_TAG, "Session Disconnected");
    }

    @Override
    public void onStreamReceived(Session session, Stream stream)
    {
        Log.i(LOG_TAG, "Stream Received");

        if (mSubscriber == null) {
            mSubscriber = new Subscriber.Builder(this, stream).build();
            mSession.subscribe(mSubscriber);
            mSubscriberViewController.addView(mSubscriber.getView());
        }
    }



    @Override
    public void onStreamDropped(Session session, Stream stream) {
        Log.i(LOG_TAG, "Stream Dropped");

        if (mSubscriber != null) {
            mSubscriber = null;
            mSubscriberViewController.removeAllViews();
        }
    }

    @Override
    public void onError(Session session, OpentokError opentokError) {
        Log.e(LOG_TAG, "Publisher error: " + opentokError.getMessage());
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }


}