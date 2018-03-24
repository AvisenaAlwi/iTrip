package com.internshipbcc.itrip;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.internshipbcc.itrip.Adapter.RvAdapterReservasi;
import com.internshipbcc.itrip.Util.Reservasi;

import java.util.ArrayList;
import java.util.List;

public class MyReservasiActivity extends AppCompatActivity {

    RecyclerView rv;

    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_reservasi);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Reservasi");
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        String uId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        rv = findViewById(R.id.rv_my_reservasi);
        rv.setLayoutManager(new LinearLayoutManager(this));

        List<Reservasi> data = new ArrayList<>();

        DatabaseReference db = FirebaseDatabase.getInstance().getReference("users").child(uId).child("reservasi");
        db.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot item : dataSnapshot.getChildren()) {
                    String id = item.getKey();
                    String titleWisata = item.child("titleWisata").getValue(String.class);
                    String idWisata = item.child("idWisata").getValue(String.class);
                    String status = item.child("status").getValue(String.class);
                    data.add(new Reservasi(id, uId, titleWisata, idWisata, status));
                }

                rv.setAdapter(new RvAdapterReservasi(MyReservasiActivity.this, data));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
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
