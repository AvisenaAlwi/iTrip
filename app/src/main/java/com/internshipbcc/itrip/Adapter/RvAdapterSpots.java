package com.internshipbcc.itrip.Adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.internshipbcc.itrip.R;
import com.internshipbcc.itrip.Util.Spot;

import java.util.List;

/**
 * Created by Sena on 19/03/2018.
 */

public class RvAdapterSpots extends RecyclerView.Adapter<RvAdapterSpots.ViewHolder> {

    List<Spot> data;
    Context context;

    public RvAdapterSpots(Context context, List<Spot> data) {
        this.context = context;
        this.data = data;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View rootView = LayoutInflater.from(context).inflate(R.layout.rv_item_spot, parent, false);
        return new ViewHolder(rootView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Glide.with(context)
                .load(data.get(position).images)
                .thumbnail(0.8f)
                .into(holder.img);
        holder.tvTitle.setText(data.get(position).title);
        holder.tvHtm.setText(data.get(position).htm);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView img;
        TextView tvTitle, tvHtm;

        public ViewHolder(View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.img_spot);
            tvTitle = itemView.findViewById(R.id.tv_spot_title);
            tvHtm = itemView.findViewById(R.id.tv_spot_htm);
        }
    }
}
