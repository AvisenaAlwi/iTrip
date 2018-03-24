package com.internshipbcc.itrip;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.internshipbcc.itrip.Adapter.RvAdapterHome;
import com.internshipbcc.itrip.Util.ItemHome;

import java.util.ArrayList;
import java.util.List;

public class WishlistActivity extends AppCompatActivity {

    Toolbar toolbar;
    RecyclerView rvWishlist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wishlist);
        toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Wishlist");
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
        }
        rvWishlist = findViewById(R.id.rv_wishlist);
        rvWishlist.setLayoutManager(new LinearLayoutManager(this));
        if (getIntent() != null) {
            String uid = getIntent().getStringExtra("uid");
            //Fetch data
            DatabaseReference db = FirebaseDatabase.getInstance().getReference("users/" + uid + "/wishlist/");
            if (db != null) {
                List<ItemHome> data = new ArrayList<>();
                db.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot item : dataSnapshot.getChildren()) {
                            String idWisata = item.child("id").getValue(String.class);
                            String title = item.child("title").getValue(String.class);
                            String desc = item.child("des").getValue(String.class);
                            String image = item.child("image").getValue(String.class);
                            boolean isWisata = item.child("isWisata").getValue(Boolean.class);
                            data.add(new ItemHome(idWisata, title, desc, image, isWisata));
                        }
                        rvWishlist.setAdapter(new RvAdapterHome(WishlistActivity.this, data));
                        findViewById(R.id.rl_loading).setVisibility(View.GONE);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });

            }
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
