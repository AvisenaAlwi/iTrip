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
import android.support.constraint.ConstraintLayout;
import android.support.v4.view.LayoutInflaterCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.borjabravo.readmoretextview.ReadMoreTextView;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.internshipbcc.itrip.Adapter.RvAdapterReview;
import com.internshipbcc.itrip.Adapter.RvAdapterSpots;
import com.internshipbcc.itrip.Util.GravitySnapHelper;
import com.internshipbcc.itrip.Util.ItemHome;
import com.internshipbcc.itrip.Util.Review;
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
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class ViewDetails extends AppCompatActivity {

    String titleWisata, descWisata, imageWisata;
    boolean isWisata;

    TextView titleToolbar;
    String idItem;
    ImageView imgAppBar;
    RelativeLayout loading_dim_layout;
    TextView tvTitle;
    IconicsTextView tvLocation;
    IconicsTextView tvAccess[] = new IconicsTextView[3];
    ReadMoreTextView rmtvDesc;
    RecyclerView rvSpots, rvReview;

    EditText etBodyReview;
    RatingBar rbNewReview;

    ConstraintLayout layoutReview;
    Button btnReservasi;
    MenuItem addToWishList;
    boolean isWishlist = false;
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
        etBodyReview = findViewById(R.id.input_review);
        rvSpots = findViewById(R.id.rv_spots);
        rvReview = findViewById(R.id.rv_review);
        rbNewReview = findViewById(R.id.rating_bar_new_review);
        layoutReview = findViewById(R.id.layout_review);
        btnReservasi = findViewById(R.id.btn_reservasi);


        rvSpots.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        SnapHelper sh = new GravitySnapHelper();
        sh.attachToRecyclerView(rvSpots);

        rvReview.setLayoutManager(new LinearLayoutManager(this));

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            String title = getIntent().getStringExtra("title");
            getSupportActionBar().setTitle(title);
            for (int i = 0; i < toolbar.getChildCount(); i++) {
                if (toolbar.getChildAt(i) instanceof TextView)
                    titleToolbar = (TextView) toolbar.getChildAt(i);
            }
            if (titleToolbar != null)
                titleToolbar.setVisibility(View.INVISIBLE);
        }
        NestedScrollView ss = findViewById(R.id.nsv_vd);
        ss.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            try {
                if (scrollY < tvTitle.getTop())
                    titleToolbar.setVisibility(View.INVISIBLE);
                else titleToolbar.setVisibility(View.VISIBLE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        if (getIntent() != null) {
            idItem = getIntent().getStringExtra("id");
            Bitmap bmp = null;
            String filename = getIntent().getStringExtra("image");
            if (filename != null && !filename.equalsIgnoreCase("")) {
                try {
                    FileInputStream is = this.openFileInput(filename);
                    bmp = BitmapFactory.decodeStream(is);
                    imgAppBar.setImageBitmap(bmp);
                    is.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                DatabaseReference db = FirebaseDatabase.getInstance().getReference("/items/" + idItem);
                db.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Glide.with(ViewDetails.this)
                                .load(dataSnapshot.child("images").getValue(String.class).split(",")[0])
                                .into(imgAppBar);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });

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
                rmtvDesc.setTrimLength(100);
                rmtvDesc.setTrimMode(0);

                //set global variable
                ViewDetails.this.titleWisata = title;
                ViewDetails.this.descWisata = desc;
                ViewDetails.this.imageWisata = dataSnapshot.child("images").getValue(String.class).split(",")[0];
                ViewDetails.this.isWisata = dataSnapshot.child("type").getValue(Double.class) == 0 ? true : false;

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
                    findViewById(R.id.tv_tempat_wisata_menarik).setVisibility(View.VISIBLE);
                }

                showAllReview(dataSnapshot.child("reviews"));

                new Handler().postDelayed(() -> loading_dim_layout.setVisibility(View.GONE), 1000);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

                loading_dim_layout.findViewById(R.id.pg_loading).setVisibility(View.GONE);
                loading_dim_layout.findViewById(R.id.error_message).setVisibility(View.VISIBLE);
            }
        });

        if (getIntent().getStringExtra("linkImage") != null)
            new Handler().postDelayed(() -> Glide.with(ViewDetails.this)
                    .load(getIntent().getStringExtra("linkImage"))
                    .thumbnail(.8f)
                    .into(imgAppBar), 1000);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            layoutReview.setVisibility(View.GONE);
            btnReservasi.setVisibility(View.GONE);
            ss.setPadding(0, 0, 0, 0);
        } else {
            btnReservasi.setOnClickListener(v -> {
                Intent i = new Intent(this, ReservasiTiketActivity.class);
                i.putExtra("id", idItem);
                startActivity(i);
            });
        }
    }

    private void checkIsWishlisted() {
        DatabaseReference db = FirebaseDatabase.getInstance().getReference("users/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + "/wishlist/");
        db.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    if (data.getKey().equalsIgnoreCase(idItem)) {
                        isWishlist = true;
                        break;
                    } else isWishlist = false;
                }

                if (isWishlist) {
                    addToWishList.setIcon(
                            new IconicsDrawable(ViewDetails.this, "gmd-favorite")
                                    .sizeDp(24)
                                    .color(Color.parseColor("#e74c3c"))
                    );
                } else {
                    addToWishList.setIcon(
                            new IconicsDrawable(ViewDetails.this, "gmd-favorite-border")
                                    .color(Color.WHITE)
                                    .sizeDp(24)
                    );
                }

                db.removeEventListener(this);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void showAllReview(DataSnapshot reviews) {
        float avgStar = 0;
        if (reviews.exists()) {
            List<Review> dataReview = new ArrayList<>();
            for (DataSnapshot review : reviews.getChildren()) {
                try {
                    String id = review.getKey();
                    String user = review.child("user").getValue(String.class);
                    String body = review.child("body").getValue(String.class);
                    int star = review.child("star").getValue(Integer.class);
                    Date date = review.child("date").getValue(Date.class);

                    dataReview.add(new Review(id, user, body, star, date));
                    avgStar += star;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (!dataReview.isEmpty()) {
                Collections.reverse(dataReview);
                rvReview.setAdapter(new RvAdapterReview(this, dataReview));
            }
            avgStar /= reviews.getChildrenCount();
        }

        ((RatingBar) findViewById(R.id.ratingBar)).setRating(avgStar);
        if (avgStar != 0)
            ((TextView) findViewById(R.id.tv_rating)).setText(String.format("%.1f", (double) avgStar));
        else
            ((TextView) findViewById(R.id.tv_rating)).setText("0.0");
        String frasa = "";
        if (avgStar >= 5)
            frasa = "Sangat bagus";
        else if (avgStar >= 4)
            frasa = "Bagus";
        else if (avgStar >= 3)
            frasa = "Cukup";
        else if (avgStar >= 2)
            frasa = "Kurang";
        else frasa = "Tidak bagus";
        ((TextView) findViewById(R.id.tv_rating_frasa)).setText(frasa);
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
        addToWishList = menu.getItem(0);
        addToWishList.setIcon(
                new IconicsDrawable(this, "gmd-favorite-border")
                        .color(Color.WHITE)
                        .sizeDp(24));
        if (FirebaseAuth.getInstance().getCurrentUser() != null)
            checkIsWishlisted();
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.menu_item_wishlist:
                if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                    if (isWishlist)
                        removeFromWishList();
                    else
                        addToWishList();
                } else {
                    new MaterialDialog.Builder(this)
                            .title("Login")
                            .content("Mohon login sebagai pengguna terlebih dahulu.")
                            .positiveText("OK")
                            .onPositive((dialog, which) -> {
                                dialog.dismiss();
                            }).show();
                }
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

    public void postReview(View v) {
        if (etBodyReview.getText().length() == 0) {
            etBodyReview.setError("Mohon kasih pendapat...");
            return;
        }
        if (rbNewReview.getRating() == 0) {
            Toast.makeText(this, "Mohon beri bintang.", Toast.LENGTH_LONG).show();
            return;
        }
        DatabaseReference dbRefReview = FirebaseDatabase.getInstance().getReference("/items/" + idItem);
        String id = dbRefReview.child("/reviews/").push().getKey();
//        Review review = new Review(id,"Avissena", etBodyReview.getText().toString(), (int)rbNewReview.getRating());

        dbRefReview.child("/reviews/" + id).child("body").setValue(etBodyReview.getText().toString());
        dbRefReview.child("/reviews/" + id).child("star").setValue((int) rbNewReview.getRating());
        dbRefReview.child("/reviews/" + id).child("user").setValue(FirebaseAuth.getInstance().getCurrentUser().getEmail());
        dbRefReview.child("/reviews/" + id).child("date").setValue(Calendar.getInstance().getTime());

        etBodyReview.setText("");
        rbNewReview.setRating(0);
        etBodyReview.clearFocus();

    }

    private void addToWishList() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            addToWishList.setIcon(
                    new IconicsDrawable(ViewDetails.this, "gmd-favorite")
                            .sizeDp(24)
                            .color(Color.parseColor("#e74c3c"))
            );
            DatabaseReference db = FirebaseDatabase.getInstance().getReference("users/" + user.getUid() + "/wishlist");
            ItemHome itemWishlist = new ItemHome(idItem, titleWisata, descWisata, imageWisata, isWisata);
            db.child(idItem).setValue(itemWishlist);
            isWishlist = true;
//            checkIsWishlisted();
        }
    }

    private void removeFromWishList() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            addToWishList.setIcon(
                    new IconicsDrawable(ViewDetails.this, "gmd-favorite-border")
                            .color(Color.WHITE)
                            .sizeDp(24)
            );
            DatabaseReference db = FirebaseDatabase.getInstance().getReference("users/" + user.getUid() + "/wishlist/" + idItem);
            if (db != null) {
                db.removeValue();
                isWishlist = false;
            }
//            checkIsWishlisted();
        }
    }
}
