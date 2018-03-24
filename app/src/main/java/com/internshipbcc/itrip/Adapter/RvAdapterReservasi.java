package com.internshipbcc.itrip.Adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.internshipbcc.itrip.QRCodeActivity;
import com.internshipbcc.itrip.R;
import com.internshipbcc.itrip.Util.Reservasi;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sena on 24/03/2018.
 */

public class RvAdapterReservasi extends RecyclerView.Adapter<RvAdapterReservasi.ViewHolder> {

    Context context;
    List<Reservasi> data = new ArrayList<>();

    public RvAdapterReservasi(Context context, List<Reservasi> data) {
        this.context = context;
        this.data = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.rv_item_reservasi, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.tvTitle.setText(data.get(position).titleWisata);
        holder.tvStatus.setText(data.get(position).status);
        holder.btnOpenQRCode.setOnClickListener(v -> {
            Intent i = new Intent(context, QRCodeActivity.class);
            i.putExtra("id", data.get(position).id);
            i.putExtra("title", data.get(position).titleWisata);
            context.startActivity(i);
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvStatus;
        Button btnOpenQRCode;

        public ViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_title_reservasi);
            tvStatus = itemView.findViewById(R.id.tv_status_reservasi);
            btnOpenQRCode = itemView.findViewById(R.id.btn_buka_qrcode);
        }
    }
}
