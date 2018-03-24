package com.internshipbcc.itrip.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import com.internshipbcc.itrip.R;
import com.internshipbcc.itrip.Util.Review;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by Sena on 20/03/2018.
 */

public class RvAdapterReview extends RecyclerView.Adapter<RvAdapterReview.ViewHolder> {

    Context context;
    List<Review> data;

    public RvAdapterReview(Context context, List<Review> data) {
        this.context = context;
        this.data = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View rootView = LayoutInflater.from(context).inflate(R.layout.rv_item_review, parent, false);
        return new ViewHolder(rootView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm\nEEE, dd/MM/YYYY");
        holder.tvUser.setText(data.get(position).user);
        holder.tvBody.setText(data.get(position).body);
        holder.ratingBar.setRating(data.get(position).star);
        holder.tvDate.setText(sdf.format(data.get(position).date));
        if (position % 2 == 0)
            holder.itemView.setBackgroundColor(Color.parseColor("#f5f5f5"));
        else holder.itemView.setBackgroundColor(Color.parseColor("#ffffff"));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvUser, tvBody, tvDate;
        RatingBar ratingBar;

        public ViewHolder(View itemView) {
            super(itemView);
            tvUser = itemView.findViewById(R.id.tv_review_user);
            tvBody = itemView.findViewById(R.id.tv_review_body);
            ratingBar = itemView.findViewById(R.id.ratingBar_review);
            tvDate = itemView.findViewById(R.id.tv_review_date);
            ratingBar.setMax(5);
        }
    }
}
