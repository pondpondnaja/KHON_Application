package com.csbteam.khonapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.util.ArrayList;

public class Full_NewViewAdapter extends RecyclerView.Adapter<Full_NewViewAdapter.ViewHolder> {
    private static final String TAG = "NewAC";
    private ArrayList<String> news_img;
    private ArrayList<String> news_link;
    private ArrayList<String> news_title;
    private Context mContext;
    private Bundle bundle;

    Full_NewViewAdapter(ArrayList<String> news_img, ArrayList<String> news_link, ArrayList<String> news_title, Context mContext) {
        this.news_img = news_img;
        this.news_link = news_link;
        this.news_title = news_title;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public Full_NewViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.new_list_full,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        holder.new_title.setText(news_title.get(position).trim());
        holder.new_title.setSelected(true);

        Glide.with(mContext)
                .asBitmap()
                .load(news_img.get(position))
                .listener(new RequestListener<Bitmap>() {
                    @Override
                    public boolean onLoadFailed(GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                        Toast.makeText(mContext, "Can't load image.", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "onLoadFailed: Message : " + e);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                        holder.progressBar.setVisibility(View.GONE);
                        return false;
                    }
                })
                .into(holder.news_full_img);

        holder.new_parent.setOnClickListener(view -> {

            Log.d(TAG, "onClick: click on image : " + news_title.get(position));
            Toast.makeText(mContext, news_title.get(position), Toast.LENGTH_SHORT).show();

            bundle = new Bundle();
            bundle.putString("news_link", news_link.get(position));
            bundle.putString("news_img_link", news_img.get(position));
            bundle.putString("news_title", news_title.get(position));
            bundle.putString("from", "list");

            Log.d(TAG, "onClick: IMG_Link : " + news_link.get(position));

            NewFragment newFragment = new NewFragment();
            newFragment.setArguments(bundle);

            AppCompatActivity activity = (AppCompatActivity) view.getContext();
            activity.getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right, R.anim.slide_in_right, R.anim.slide_out_right)
                    .add(R.id.fragment_container, newFragment, "news_full_item").addToBackStack("news_full_item").commit();
        });
    }

    @Override
    public int getItemCount() {
        return news_title.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView new_title;
        CardView new_parent;
        ImageView news_full_img;
        ProgressBar progressBar;

        private ViewHolder(View itemView) {
            super(itemView);
            new_title = itemView.findViewById(R.id.new_title_full);
            new_parent = itemView.findViewById(R.id.new_parent);
            progressBar = itemView.findViewById(R.id.progressBar);
            news_full_img = itemView.findViewById(R.id.news_img_fullList);
        }
    }
}
