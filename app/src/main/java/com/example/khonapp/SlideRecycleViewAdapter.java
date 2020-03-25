package com.example.khonapp;

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
import java.util.Objects;

public class SlideRecycleViewAdapter extends RecyclerView.Adapter<SlideRecycleViewAdapter.ViewHolder> {

    private static final String TAG = "SlideRecycleView";
    private static final int Slide_size = 4;
    //vars
    private ArrayList<String> news_date;
    private ArrayList<String> news_img;
    private ArrayList<String> news_link;
    private ArrayList<String> news_title;

    private Context mContext;
    private Bundle bundle;

    SlideRecycleViewAdapter(ArrayList<String> news_date, ArrayList<String> news_img, ArrayList<String> news_link, ArrayList<String> news_title, Context mContext) {
        this.news_date = news_date;
        this.news_img = news_img;
        this.news_link = news_link;
        this.news_title = news_title;
        this.mContext = mContext;
    }

    @Override
    public int getItemViewType(int position) {
        Log.d(TAG, "getItemViewType: TypeView : " + position + " mNameSize = " + news_date.size());
        if (position == Slide_size) {
            return 2;
        } else {
            return 1;
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder:  called View Type : " + viewType);
        View view;
        if (viewType == 2) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_list_lastitem, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_list, parent, false);
        }
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        Log.d(TAG, "onBindViewHolder: called");
        Log.d(TAG, "onBindViewHolder: ImgURL : " + news_img.get(position));
        //Set image
        if (position == getItemCount() - 1) {
            Glide.with(mContext)
                    .load(R.drawable.ic_play_circle_outline_black_24dp)
                    .into(holder.img);
        } else {
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
                    .into(holder.img);
        }

        holder.parentLayout.setOnClickListener(view -> {

            Log.d(TAG, "onClick: click on image : " + news_title.get(position));

            if (position == Slide_size) {
                AppCompatActivity news_activity = (AppCompatActivity) view.getContext();
                Objects.requireNonNull(news_activity.getSupportActionBar()).hide();
                bundle = new Bundle();
                bundle.putStringArrayList("news_date", news_date);
                bundle.putStringArrayList("news_img", news_img);
                bundle.putStringArrayList("news_link", news_link);
                bundle.putStringArrayList("news_title", news_title);

                Full_NewListFragment fullNewListFragment = new Full_NewListFragment();
                fullNewListFragment.setArguments(bundle);

                news_activity.getSupportFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right, R.anim.slide_in_right, R.anim.slide_out_right)
                        .replace(R.id.fragment_container, fullNewListFragment, "full_news").addToBackStack("full_news").commit();
            } else {
                bundle = new Bundle();
                bundle.putString("news_link", news_link.get(position));
                bundle.putString("news_img_link", news_img.get(position));
                bundle.putString("news_title", news_title.get(position));
                bundle.putString("from", "main");

                NewFragment newFragment = new NewFragment();
                newFragment.setArguments(bundle);

                AppCompatActivity activity = (AppCompatActivity) view.getContext();
                activity.getSupportFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right, R.anim.slide_in_right, R.anim.slide_out_right)
                        .replace(R.id.fragment_container, newFragment, "news").addToBackStack("news").commit();
            }
        });
    }

    @Override
    public int getItemCount() {
        return Slide_size + 1;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        CardView parentLayout;
        ImageView img;
        TextView t_news;
        ProgressBar progressBar;

        private ViewHolder(View itemView) {
            super(itemView);
            parentLayout = itemView.findViewById(R.id.parentLayout);
            img = itemView.findViewById(R.id.image_view);
            progressBar = itemView.findViewById(R.id.progressBar);
            t_news = itemView.findViewById(R.id.text_news);
        }
    }
}