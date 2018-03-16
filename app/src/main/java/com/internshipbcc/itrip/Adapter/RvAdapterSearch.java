package com.internshipbcc.itrip.Adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.internshipbcc.itrip.R;
import com.internshipbcc.itrip.Util.ItemHome;
import com.internshipbcc.itrip.ViewDetails;

import java.io.FileOutputStream;
import java.util.List;

/**
 * Created by Sena on 16/03/2018.
 */

public class RvAdapterSearch extends RecyclerView.Adapter<RvAdapterSearch.ViewHolder> {

    Context context;
    List<ItemHome> data;

    public RvAdapterSearch(Context context, List<ItemHome> data) {
        this.context = context;
        this.data = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View rootView = LayoutInflater.from(context).inflate(R.layout.rv_item_home_search, parent, false);
        return new ViewHolder(rootView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Bitmap[] bmp = new Bitmap[1];
        Glide.with(context)
                .asBitmap()
                .load(data.get(position).image)
                .listener(new RequestListener<Bitmap>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                        bmp[0] = resource;
                        return false;
                    }
                })
                .into(holder.image);
        holder.tvTitle.setText(data.get(position).title);
        String desc = data.get(position).des;
        desc = desc.substring(0, desc.length() > 50 ? 49 : desc.length()).concat(" ... ")
                .concat("<b>Selengkapnya</b>");
        holder.tvDesc.setText(Html.fromHtml(desc));

        holder.itemView.setOnClickListener(v -> {
            //make file
            String filename = "bitmap.png";
            try {
                //Write file
                FileOutputStream stream = context.openFileOutput(filename, Context.MODE_PRIVATE);
                bmp[0].compress(Bitmap.CompressFormat.PNG, 100, stream);
                //Cleanup
                stream.close();
                //bmp[0].recycle();
            } catch (Exception e) {
                e.printStackTrace();
            }
            Intent intent = new Intent(context, ViewDetails.class);
            Pair<View, String> p1 = Pair.create(holder.image, "image");
            Pair<View, String> p2 = Pair.create(holder.tvTitle, "title");
            ActivityOptionsCompat options = ActivityOptionsCompat.
                    makeSceneTransitionAnimation((Activity) context, p1);
            intent.putExtra("id", data.get(position).id);
            intent.putExtra("image", filename);
            intent.putExtra("title", holder.tvTitle.getText());
            context.startActivity(intent, options.toBundle());
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        ImageView image;
        TextView tvTitle, tvDesc;

        public ViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.img_item_search);
            tvTitle = itemView.findViewById(R.id.tv_item_search_title);
            tvDesc = itemView.findViewById(R.id.tv_item_search_desc);
        }
    }
}
