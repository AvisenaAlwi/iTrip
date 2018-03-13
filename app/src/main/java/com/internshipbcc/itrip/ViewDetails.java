package com.internshipbcc.itrip;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.ImageView;

import java.io.FileInputStream;

public class ViewDetails extends AppCompatActivity {

    ImageView imgAppBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_details);
        imgAppBar = findViewById(R.id.app_bar_image);
        setSupportActionBar(findViewById(R.id.toolbar));
        if(getIntent()!=null){
            Bitmap bmp = null;
            String filename = getIntent().getStringExtra("image");
            try {
                FileInputStream is = this.openFileInput(filename);
                bmp = BitmapFactory.decodeStream(is);
                imgAppBar.setImageBitmap(bmp);
                is.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            String title = getIntent().getStringExtra("title");
            ((CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar)).setTitle(title);
        }
    }
}
