package com.example.myapplication.activity;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;

public class TaxDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tax_detail);

        TextView tvName = findViewById(R.id.tvTaxName);
        TextView tvDate = findViewById(R.id.tvTaxDate);
        TextView tvStatus = findViewById(R.id.tvTaxStatus);

        tvName.setText(getIntent().getStringExtra("name"));
        tvDate.setText(getIntent().getStringExtra("date"));
        tvStatus.setText(getIntent().getStringExtra("status"));
    }
}
