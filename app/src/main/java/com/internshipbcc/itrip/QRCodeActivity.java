package com.internshipbcc.itrip;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ImageView;

import net.glxn.qrgen.android.QRCode;

public class QRCodeActivity extends AppCompatActivity {

    String id;

    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        String title = "QR Code - ";
        if (getIntent() != null) {
            id = getIntent().getStringExtra("id");
            title += getIntent().getStringExtra("title");
            Bitmap bitmap = QRCode.from(id).withSize(400, 400).bitmap();
            ImageView myImage = findViewById(R.id.img_qr_code);
            myImage.setImageBitmap(bitmap);
        }
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            getSupportActionBar().setTitle(title);
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
