package com.example.heyochat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity {

    private Button LoginButton,PhoneLoginButton;
    private EditText UserEmial,UserPassword;
    private TextView NeeedNewAccountLink,ForgrtPasswordLink;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;
    private DatabaseReference UsersRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth=FirebaseAuth.getInstance();
        UsersRef= FirebaseDatabase.getInstance().getReference().child("Users");

        InitializeFields();

         NeeedNewAccountLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               SendUserToRegisterActivity();
            }
        });

         LoginButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 AllowUserToLogin();
             }
         });
         PhoneLoginButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 Intent phoneLoginIntent=new Intent(LoginActivity.this,PhoneLoginActivity.class);
                 startActivity(phoneLoginIntent);
             }
         });

    }

    private void AllowUserToLogin()
    {
        String email=UserEmial.getText().toString();
        String password=UserPassword.getText().toString();

        if(TextUtils.isEmpty(email))
        {
            Toast.makeText(this,"Please Enter Email",Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(password))
        {
            Toast.makeText(this,"Please Enter Password",Toast.LENGTH_SHORT).show();
        }
        else
            {
                loadingBar.setTitle("Sign In ");
                loadingBar.setMessage("Please Wait....");
                loadingBar.setCanceledOnTouchOutside(true);
                loadingBar.show();

                mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
               if ((task.isSuccessful()))
               {
                   String currentUserId=mAuth.getCurrentUser().getUid();///FOR NOTI
                   String deviceToken= FirebaseInstanceId.getInstance().getToken();//FOR NOTI
                   UsersRef.child(currentUserId).child("device_token")
                           .setValue(deviceToken).addOnCompleteListener(new OnCompleteListener<Void>() {
                       @Override
                       public void onComplete(@NonNull Task<Void> task)
                       {
                           if (task.isSuccessful())
                           {
                               SendUserToMainACTIVITY();
                               Toast.makeText(LoginActivity.this,"Logged In Successful",Toast.LENGTH_SHORT).show();
                               loadingBar.dismiss();

                           }

                       }
                   });


               }
               else
               {
                   String message=task.getException().toString();
                   Toast.makeText(LoginActivity.this,"ERROR:"+message,Toast.LENGTH_SHORT).show();
                   loadingBar.dismiss();
               }
                }
            });
        }
    }

    private void InitializeFields() {
        LoginButton=(Button)findViewById(R.id.login_button);
        PhoneLoginButton=(Button)findViewById(R.id.phone_login_button);
        UserEmial=(EditText)findViewById(R.id.login_email);
        UserPassword=(EditText)findViewById(R.id.login_password);
        NeeedNewAccountLink=(TextView)findViewById(R.id.need_new_account_link);
        ForgrtPasswordLink=(TextView)findViewById(R.id.forgot_password_link);
       loadingBar=new ProgressDialog(this);


    }



    private void SendUserToMainACTIVITY() {
        Intent mainintent=new Intent(LoginActivity.this,MainActivity.class);
        mainintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainintent);
        finish();;
    }
    private void SendUserToRegisterActivity() {
        Intent registerintent=new Intent(LoginActivity.this,RegisterActivity.class);
        startActivity(registerintent);
    }


}
