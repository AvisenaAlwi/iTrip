package com.internshipbcc.itrip;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.LayoutInflaterCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.borjabravo.readmoretextview.ReadMoreTextView;
import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.internshipbcc.itrip.Adapter.RvAdapterSpots;
import com.internshipbcc.itrip.Util.GravitySnapHelper;
import com.internshipbcc.itrip.Util.Spot;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.context.IconicsContextWrapper;
import com.mikepenz.iconics.context.IconicsLayoutInflater2;
import com.mikepenz.iconics.view.IconicsTextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class ViewDetails extends AppCompatActivity {

    String idItem;
    ImageView imgAppBar;
    RelativeLayout loading_dim_layout;
    TextView tvTitle;
    IconicsTextView tvLocation;
    IconicsTextView tvAccess[] = new IconicsTextView[3];
    ReadMoreTextView rmtvDesc;
    RecyclerView rvSpots;
    private boolean dataIsLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LayoutInflaterCompat.setFactory2(getLayoutInflater(), new IconicsLayoutInflater2(getDelegate()));
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_details);
        imgAppBar = findViewById(R.id.app_bar_image);

        loading_dim_layout = findViewById(R.id.loading_dim_layout);
        tvTitle = findViewById(R.id.tv_vd_title);
        tvLocation = findViewById(R.id.tv_vd_location);
        tvAccess[0] = findViewById(R.id.tv_vd_access_plane);
        tvAccess[1] = findViewById(R.id.tv_vd_access_train);
        tvAccess[2] = findViewById(R.id.tv_vd_access_bus);
        rmtvDesc = findViewById(R.id.rmtv_desc);

        rvSpots = findViewById(R.id.rv_spots);
        rvSpots.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        SnapHelper sh = new GravitySnapHelper();
        sh.attachToRecyclerView(rvSpots);

        setSupportActionBar(findViewById(R.id.toolbar));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        if(getIntent()!=null){
            idItem = getIntent().getStringExtra("id");
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
        }

        //Fetch data
        DatabaseReference db = FirebaseDatabase.getInstance().getReference("/items/" + idItem);
        db.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                dataIsLoaded = true;
                String title = dataSnapshot.child("title").getValue(String.class);
                String location = dataSnapshot.child("location").getValue(String.class);
                String desc = dataSnapshot.child("desc").getValue(String.class);
                tvTitle.setText(title);
                tvLocation.setText("{gmd-place} " + location);
                setAccess(title);
                rmtvDesc.setText(desc, TextView.BufferType.SPANNABLE);
//                rmtvDesc.setTrimCollapsedText("Selengkapnya");
//                rmtvDesc.setTrimExpandedText("Sembunyikan");
//                rmtvDesc.setTrimLines(10);
                rmtvDesc.setTrimLength(100);
                rmtvDesc.setTrimMode(0);

                DataSnapshot spots = dataSnapshot.child("spots");
                List<Spot> dataSpots = new ArrayList<>();
                if (spots.exists()) {
                    for (DataSnapshot spot : spots.getChildren()) {
                        String[] dt = spot.getValue(String.class).split(",");
                        String titleSpot = dt[0];
                        String imageSpot = dt[1];
                        String htmSpot = "Rp." + dt[2];
                        dataSpots.add(new Spot(titleSpot, imageSpot, htmSpot));
                    }
                }
                if (!dataSpots.isEmpty()) {
                    rvSpots.setAdapter(new RvAdapterSpots(ViewDetails.this, dataSpots));
                }
                new Handler().postDelayed(() -> loading_dim_layout.setVisibility(View.GONE), 1000);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

                loading_dim_layout.findViewById(R.id.pg_loading).setVisibility(View.GONE);
                loading_dim_layout.findViewById(R.id.error_message).setVisibility(View.VISIBLE);
            }
        });

        new Handler().postDelayed(() -> Glide.with(ViewDetails.this)
                .load(getIntent().getStringExtra("imageLink"))
                .thumbnail(.8f)
                .into(imgAppBar), 1000);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (dataIsLoaded)
            loading_dim_layout.setVisibility(View.GONE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_view_detail, menu);
        MenuItem addToWishList = menu.getItem(0);
        addToWishList.setIcon(
                new IconicsDrawable(this, "gmd-favorite-border")
                        .color(Color.WHITE)
                        .sizeDp(24));
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.menu_item_wishlist:
                Toast.makeText(this, "Add to wishlist", Toast.LENGTH_LONG).show();
                break;
        }
        return true;
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(IconicsContextWrapper.wrap(newBase));
    }

    public void openInMaps(View v) {
        String uri = "geo:0,0?q=" + tvTitle.getText().toString();
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Google Maps tidak terinstall.", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private void setAccess(String destination) {
        String key = "AIzaSyC1PkLZ1n3rm9OHcrHSZBpq9qj8_ZcznZE";
        String linkBandara = "https://maps.googleapis.com/maps/api/directions/json?origin=Bandara%20Abdul%20Rachman%20Saleh&destination=" + destination + "&key=" + key;
        String linkStasiun = "https://maps.googleapis.com/maps/api/directions/json?origin=stasiun%20kota%20baru%20malang&destination=" + destination + "&key=" + key;
        String linkTerminal = "https://maps.googleapis.com/maps/api/directions/json?origin=terminal%20arjosari%20malang&destination=" + destination + "&key=" + key;

        AsyncHttpClient http = new AsyncHttpClient();
        http.setConnectTimeout(1000);
        http.get(linkBandara, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try {
                    JSONObject response = new JSONObject(new String(responseBody));
                    JSONObject rute = response
                            .getJSONArray("routes")
                            .getJSONObject(0)
                            .getJSONArray("legs")
                            .getJSONObject(0);
                    String waktu = rute.getJSONObject("duration").getString("text");
                    waktu = waktu.replace("hours", "Jam");
                    waktu = waktu.replace("mins", "Menit");
                    String jarak = rute.getJSONObject("distance").getString("text");
                    tvAccess[0].setText("{gmd-flight} Bandara Abdurrahman Saleh : " + waktu + " (" + jarak + ")");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                http.get(linkBandara, this);
            }

        });
        http.get(linkStasiun, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try {
                    JSONObject response = new JSONObject(new String(responseBody));
                    JSONObject rute = response
                            .getJSONArray("routes")
                            .getJSONObject(0)
                            .getJSONArray("legs")
                            .getJSONObject(0);
                    String waktu = rute.getJSONObject("duration").getString("text");
                    waktu = waktu.replace("hours", "Jam");
                    waktu = waktu.replace("mins", "Menit");
                    String jarak = rute.getJSONObject("distance").getString("text");
                    tvAccess[1].setText("{gmd-train} Stasiun Malang Kota Baru : " + waktu + " (" + jarak + ")");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                http.get(linkStasiun, this);
            }
        });
        http.get(linkTerminal, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try {
                    JSONObject response = new JSONObject(new String(responseBody));
                    JSONObject rute = response
                            .getJSONArray("routes")
                            .getJSONObject(0)
                            .getJSONArray("legs")
                            .getJSONObject(0);
                    String waktu = rute.getJSONObject("duration").getString("text");
                    waktu = waktu.replace("hours", "Jam");
                    waktu = waktu.replace("mins", "Menit");
                    String jarak = rute.getJSONObject("distance").getString("text");
                    tvAccess[2].setText("{gmd-directions-bus} Terminal Arjosari : " + waktu + " (" + jarak + ")");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                http.get(linkTerminal, this);
            }

        });
    }
}
