package com.example.filiw.activities;

import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.filiw.R;

public class Activity_FirstPage extends AppCompatActivity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EditText puser   = (EditText)findViewById(R.id.editTextTextPersonName);
        Button contiinue =findViewById(R.id.buttoncontinuefromFrontPage);
        contiinue .setOnClickListener(v -> {
            Intent temp=new Intent(Activity_FirstPage.this,activity_show_topics.class);
            if (puser.getText().toString().trim().length()!=0){
                temp.putExtra("nameofperson",puser.getText().toString().trim());
            }else{
                temp.putExtra("nameofperson","Guest");
            }
            startActivity(temp);
        });
    }
}
