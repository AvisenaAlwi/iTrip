package com.internshipbcc.itrip.Util;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.internshipbcc.itrip.R;

/**
 * Created by Sena on 18/03/2018.
 */

public class CustomWindowInfoAdapter implements GoogleMap.InfoWindowAdapter {

    View view;
    Context context;

    public CustomWindowInfoAdapter(Context context) {
        this.context = context;
        this.view = LayoutInflater.from(context).inflate(R.layout.custom_window_info, null);
    }

    private void reDrawInfo(Marker marker, View mview) {
        TextView title = mview.findViewById(R.id.txtJudul);
        title.setText(marker.getTitle());
    }

    @Override
    public View getInfoWindow(Marker marker) {
        if (!marker.getTitle().equalsIgnoreCase("Posisi Anda Sekarang")) {
            reDrawInfo(marker, view);
            return view;
        } else {
            TextView textView = new TextView(context);
            textView.setText(marker.getTitle());
            textView.setBackgroundColor(Color.WHITE);
            textView.setPadding(15, 15, 15, 15);
            return textView;
        }
    }

    @Override
    public View getInfoContents(Marker marker) {
        if (!marker.getTitle().equalsIgnoreCase("Posisi Anda Sekarang")) {
            reDrawInfo(marker, view);
            return view;
        } else {
            TextView textView = new TextView(context);
            textView.setText(marker.getTitle());
            textView.setBackgroundColor(Color.WHITE);
            textView.setPadding(20, 20, 20, 20);
            return textView;
        }
    }
}
