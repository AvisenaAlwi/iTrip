package com.internshipbcc.itrip;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.internshipbcc.itrip.Util.HTM;
import com.internshipbcc.itrip.Util.Reservasi;

import java.util.Date;

public class ReservasiTiketActivity extends AppCompatActivity implements View.OnClickListener {

    ImageView imgReservasi;
    TextView tvTitle, tvTanggal, tvCheckout;
    Button btnCheckout;

    String title, idWisata;
    Date date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservasi_tiket);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        imgReservasi = findViewById(R.id.img_reservasi);
        tvTitle = findViewById(R.id.tv_title_reservasi);
        tvTanggal = findViewById(R.id.tv_tanggal);
        tvCheckout = findViewById(R.id.tvCheckout);
        btnCheckout = findViewById(R.id.btn_checkout);
        btnCheckout.setOnClickListener(this);
        if (getIntent() != null) {
            String id = getIntent().getStringExtra("id");
            idWisata = id;

            DatabaseReference db = FirebaseDatabase.getInstance().getReference("items").child(id);
            db.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String image = dataSnapshot.child("images").getValue(String.class).split(",")[0];
                    Glide.with(ReservasiTiketActivity.this)
                            .load(image)
                            .thumbnail(0.6f)
                            .into(imgReservasi);
                    title = dataSnapshot.child("title").getValue(String.class);
                    tvTitle.setText(title);
                    int harga = HTM.getHarga(dataSnapshot.child("htm").getValue(String.class));
                    String checkout = "<h4>Checkout<h4/>" +
                            "<div style=\"text-align: justify; text-justify: inter-word;\">" +
                            "Tiket masuk : Rp." + harga + "<br>" +
                            "Biaya admin : Rp.0,-<br>" +
                            "---------------------------- +<br>" +
                            "Total : Rp." + harga +
                            "</div>";

                    String checkout2 = "<body style=\" background:#00000000; text-align:right;\">\n" +
                            "<h4>Checkout<h4/>" +
                            "  <table style=\"width: 100%; text-align:right;\">\n" +
                            "    <tbody style=\"width: 100%text-align:right;\">\n" +
                            "      <tr>\n" +
                            "        <td>Tiket masuk</td>\n" +
                            "        <td>:</td>\n" +
                            "        <td>Rp." + harga + "</td>\n" +
                            "        <td></td>" +
                            "      </tr>\n" +
                            "      <tr>\n" +
                            "        <td>Biaya admin</td>\n" +
                            "        <td>:</td>\n" +
                            "        <td>Rp.0</td>\n" +
                            "        <td></td>" +
                            "      </tr>\n" +
                            "      <tr>\n" +
                            "        <td colspan=\"3\">--------------------------------------------</td>\n" +
                            "        <td>+</td>\n" +
                            "      </tr>\n" +
                            "      <tr>\n" +
                            "        <td>Total</td>\n" +
                            "        <td>:</td>\n" +
                            "        <td>Rp." + harga + "</td>\n" +
                            "        <td></td>" +
                            "      </tr>\n" +
                            "    </tbody>\n" +
                            "  </table>\n" +
                            "</body>";

//                    tvCheckout.setText(Html.fromHtml(checkout2));
                    ((WebView) findViewById(R.id.webview)).setBackgroundColor(Color.TRANSPARENT);
                    ((WebView) findViewById(R.id.webview)).loadDataWithBaseURL(null, checkout2, "text/html", "utf-8", null);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

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

    public void choseTanggal(View v) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_checkout:
                checkout();
                break;
        }
    }

    private void checkout() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        DatabaseReference dbUser = FirebaseDatabase.getInstance().getReference("users").child(user.getUid()).child("reservasi");
        String id = dbUser.push().getKey();
        Reservasi reservasi = new Reservasi(id, user.getUid(), title, idWisata, "Proses Pembayaran");
        dbUser.child(id).setValue(reservasi);
        FirebaseDatabase.getInstance().getReference("reservasi").child(id)
                .setValue(reservasi);

        Toast.makeText(this, "Checkout telah berhasil. Mohon segera transfer uangnya.", Toast.LENGTH_LONG).show();
        finish();
    }
}
