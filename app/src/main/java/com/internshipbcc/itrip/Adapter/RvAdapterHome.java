package com.internshipbcc.itrip.Adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.internshipbcc.itrip.R;
import com.internshipbcc.itrip.Util.ItemHome;
import com.internshipbcc.itrip.ViewDetails;
import com.joooonho.SelectableRoundedImageView;

import java.io.FileOutputStream;
import java.util.List;

/**
 * Created by Sena on 12/03/2018.
 */

public class RvAdapterHome extends RecyclerView.Adapter<RvAdapterHome.ViewHolder> {

    Context context;
    List<ItemHome> data;

    public RvAdapterHome(Context context, List<ItemHome> data) {
        this.context = context;
        this.data = data;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View root = LayoutInflater.from(context).inflate(R.layout.rv_item_home, parent, false);
        return new ViewHolder(root);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Bitmap[] bmp = new Bitmap[1];
        Glide.with(context)
                .asBitmap()
                .load(data.get(position).image)
                .thumbnail(0.3f)
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
        int endIndex = data.get(position).des.length() > 101 ? 100 : data.get(position).des.length() - 1;
        String shortDes = data.get(position).des.substring(0, endIndex) + "... <b>Selengkapnya</b>";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            holder.tvDes.setText(Html.fromHtml(shortDes, Html.FROM_HTML_MODE_COMPACT));
        else holder.tvDes.setText(Html.fromHtml(shortDes));
        holder.tvTitle.setText(data.get(position).title);

        if (data.get(position).isWisata) {
            holder.tvBadge.setText("Wisata");
            holder.cvBadge.setCardBackgroundColor(context.getResources().getColor(R.color.colorPrimary));
        } else {
            holder.tvBadge.setText("Event");
            holder.cvBadge.setCardBackgroundColor(context.getResources().getColor(R.color.merah));
        }

        //Item clicked
        holder.root.setOnClickListener(v -> {
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
            intent.putExtra("imageLink", data.get(position).image);
            context.startActivity(intent, options.toBundle());
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        RelativeLayout root;
        SelectableRoundedImageView image;
        TextView tvTitle, tvDes, tvBadge;
        CardView cvBadge;

        public ViewHolder(View itemView) {
            super(itemView);
            root = itemView.findViewById(R.id.root);
            image = itemView.findViewById(R.id.img_rv_item_home);
            tvTitle = itemView.findViewById(R.id.tv_rv_item_home_title);
            tvDes = itemView.findViewById(R.id.tv_rv_item_home_des);
            cvBadge = itemView.findViewById(R.id.cv_item_rv_home_badge);
            tvBadge = itemView.findViewById(R.id.tv_item_rv_home_badge);
        }
    }
}
