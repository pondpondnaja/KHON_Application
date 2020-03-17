package com.example.khonapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.util.ArrayList;

public class ResultAdapter extends RecyclerView.Adapter<ResultAdapter.ViewHolder> {

    private static final String TAG = "ResultAP";
    private static final String URL = "http://khon.itar.site";

    private ArrayList<String> name = new ArrayList<>();
    private ArrayList<String> desc = new ArrayList<>();
    private ArrayList<String> score = new ArrayList<>();
    private ArrayList<String> gesture_name = new ArrayList<>();
    private ArrayList<String> gesture_score = new ArrayList<>();
    private ArrayList<String> gestureDesc = new ArrayList<>();
    private ArrayList<String> out_img = new ArrayList<>();
    private ArrayList<String> model_id = new ArrayList<>();
    private Context context;
    private int Bitmap_width, Bitmap_height;

    public ResultAdapter(ArrayList<String> name, ArrayList<String> desc, ArrayList<String> score, ArrayList<String> gesture_name, ArrayList<String> gesture_score, ArrayList<String> gestureDesc, ArrayList<String> out_img, ArrayList<String> model_id, Context context, int Bitmap_width, int Bitmap_height) {
        this.name = name;
        this.desc = desc;
        this.score = score;
        this.gesture_name = gesture_name;
        this.gesture_score = gesture_score;
        this.gestureDesc = gestureDesc;
        this.out_img = out_img;
        this.model_id = model_id;
        this.context = context;
        this.Bitmap_width = Bitmap_width;
        this.Bitmap_height = Bitmap_height;
    }

    @NonNull
    @Override
    public ResultAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_result_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ResultAdapter.ViewHolder holder, int position) {

        String Uri_builder = URL + out_img.get(position);
        String string_builder_character = name.get(position) + " " + score.get(position) + " %";
        String string_builder_gesture = gesture_name.get(position) + " " + gesture_score.get(position) + " %";

        holder.progressBar_cha.setVisibility(View.GONE);
        holder.progressBar_gesture.setVisibility(View.GONE);
        holder.mTItle.setText(string_builder_character);
        holder.mDescription.setText(desc.get(position));
        holder.mGesture.setText(string_builder_gesture);
        holder.mGestureDescription.setText(gestureDesc.get(position));

        String orientation = getOrientation(Bitmap_width, Bitmap_height);

        Log.d(TAG, "onBindViewHolder: Orientation : " + orientation);

        if (orientation.equals("landscape")) {
            FrameLayout.LayoutParams imageViewParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, 700);
            RelativeLayout.LayoutParams cardLayout = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, 700);
            cardLayout.setMarginStart(25);
            cardLayout.setMarginEnd(25);
            holder.cardView.setLayoutParams(cardLayout);
            holder.mImage.setLayoutParams(imageViewParams);
        } else if (orientation.equals("portrait")) {
            FrameLayout.LayoutParams imageViewParams = new FrameLayout.LayoutParams(1000, 1500);
            holder.mImage.setLayoutParams(imageViewParams);
        }

        String out_img_URL_Builder = URL + out_img.get(position).replace("\\/", "/");

        Glide.with(context)
                .asBitmap()
                .load(out_img_URL_Builder)
                .listener(new RequestListener<Bitmap>() {
                    @Override
                    public boolean onLoadFailed(GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                        Toast.makeText(context, "Can't load image.", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "onLoadFailed: Message : " + e);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                        holder.mImage.setVisibility(View.VISIBLE);
                        holder.progressBar.setVisibility(View.GONE);
                        return false;
                    }
                })
                .into(holder.mImage);
    }

    private String getOrientation(int Width, int Height) {

        String orientation = "landscape";
        try {
            Log.d(TAG, "getOrientation: H : " + Height + " W : " + Width);
            if (Width < Height) {
                orientation = "portrait";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return orientation;
    }

    @Override
    public int getItemCount() {
        return name.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView mTItle, mDescription, mGesture, mGestureDescription;
        private ImageView mImage;
        private CardView cardView;
        private ProgressBar progressBar, progressBar_cha, progressBar_gesture;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            progressBar = itemView.findViewById(R.id.progressBar);
            progressBar_cha = itemView.findViewById(R.id.progressBar_cha);
            progressBar_gesture = itemView.findViewById(R.id.progressBar_gesture);
            mTItle = itemView.findViewById(R.id.result_title);
            mDescription = itemView.findViewById(R.id.result_description);
            mImage = itemView.findViewById(R.id.preview_img);
            mGesture = itemView.findViewById(R.id.resultGesture_title);
            mGestureDescription = itemView.findViewById(R.id.resultGesture_description);
            cardView = itemView.findViewById(R.id.preview_img_container);
        }
    }
}
