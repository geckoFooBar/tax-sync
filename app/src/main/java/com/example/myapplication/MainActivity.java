package com.example.myapplication;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    TextView txtMessage;
    Button btnClick;
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        txtMessage = findViewById(R.id.txtMessage);
        btnClick = findViewById(R.id.btnClick);

        btnClick.setOnClickListener(view -> txtMessage.setText("Hello to TYBBA CA students!!!"));
    }
}
