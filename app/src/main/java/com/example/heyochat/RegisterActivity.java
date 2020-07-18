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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class RegisterActivity extends AppCompatActivity {
    private Button CreateAccountButton;
    private EditText UserEmial,UserPassword;
    private TextView AlreadyHaveAccountLink;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;
    private DatabaseReference RootRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth= FirebaseAuth.getInstance();
        RootRef= FirebaseDatabase.getInstance().getReference();
        InitializeFields();



        AlreadyHaveAccountLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendUserToLoginActivity();
            }
        });

        CreateAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CreateNewAccount();
            }
        });

    }

    private void CreateNewAccount()
    {
       String email=UserEmial.getText().toString();
        String password=UserPassword.getText().toString();

        if(TextUtils.isEmpty(email))
       {
        Toast.makeText(this,"Please Enter Email",Toast.LENGTH_SHORT);
       }
        if(TextUtils.isEmpty(password))
        {
            Toast.makeText(this,"Please Enter Password",Toast.LENGTH_SHORT);
        }
        else {  loadingBar.setTitle("Creating New Account");
                loadingBar.setMessage("Please Wait While We Are Creating New Account For You");
                loadingBar.setCanceledOnTouchOutside(true);
                loadingBar.show();

            mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task)
                {
                    if (task.isSuccessful())
                    {
                        String deviceToken= FirebaseInstanceId.getInstance().getToken();//FOR NOTI

                        String currentUserId=mAuth.getCurrentUser().getUid();
                        RootRef.child("Users").child(currentUserId).setValue("null");
                        RootRef.child("Users").child(currentUserId).child("device_token").setValue(deviceToken);



                        SendUserToMainACTIVITY();
                        Toast.makeText(RegisterActivity.this,"Account Created Successfully",Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();

                    }
                    else
                        {
                        String message=task.getException().toString();
                            Toast.makeText(RegisterActivity.this,"ERROR:"+message,Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                        }
                }

            });
        }

    }

    private void InitializeFields() {
        CreateAccountButton=(Button)findViewById(R.id.register_button);

        UserEmial=(EditText)findViewById(R.id.register_email);
        UserPassword=(EditText)findViewById(R.id.register_password);
        AlreadyHaveAccountLink=(TextView)findViewById(R.id.already_have_account_link);
        loadingBar=new ProgressDialog(this);

    }
    private void SendUserToLoginActivity() {
        Intent loginintent=new Intent(RegisterActivity.this,LoginActivity.class);
        startActivity(loginintent);
    }
    private void SendUserToMainACTIVITY() {
        Intent mainintent=new Intent(RegisterActivity.this,MainActivity.class);
         mainintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainintent);
        finish();;
    }
}
