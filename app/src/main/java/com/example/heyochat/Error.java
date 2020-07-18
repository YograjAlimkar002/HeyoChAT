package com.example.heyochat;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.widget.Toolbar;

public class Error extends AppCompatActivity {
    private Button WayToHome;
    private Toolbar mToolBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_error);
        mToolBar=(Toolbar)findViewById(R.id.errortoolbar);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setTitle("Heyo Chat");

        WayToHome=(Button)findViewById(R.id.buttontohome);
        WayToHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(Error.this,MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}