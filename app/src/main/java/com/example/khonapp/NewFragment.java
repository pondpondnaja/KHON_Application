package com.example.khonapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class NewFragment extends Fragment {
    private static final String TAG = "newAc";

    private MainActivity activity;
    private AppCompatActivity news_activity;
    private String news_link, news_title, news_desc, news_desc_2, news_img_link, news_img_link_2;
    private String check_where;
    private Context context;
    private ProgressBar progressBar_1, progressBar_2;
    private WebView webView;
    private ImageView imageView, imageView_2;
    private TextView textView, textView_2, text_news_title;
    private CardView cardView;
    private MainActivity mainActivity = new MainActivity();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.activity_news, container, false);

        context = view.getContext();
        cardView = view.findViewById(R.id.img_new_2_holder);
        imageView = view.findViewById(R.id.img_news);
        imageView_2 = view.findViewById(R.id.img_news_2);
        textView = view.findViewById(R.id.text_news);
        textView_2 = view.findViewById(R.id.text_news_2);
        text_news_title = view.findViewById(R.id.text_news_title);
        progressBar_1 = view.findViewById(R.id.progressBar_1);
        progressBar_2 = view.findViewById(R.id.progressBar_2);
        activity = (MainActivity) getActivity();
        news_activity = (AppCompatActivity) view.getContext();

        Objects.requireNonNull(news_activity.getSupportActionBar()).hide();

        Bundle bundle = getArguments();
        if (bundle != null) {
            news_link = bundle.getString("news_link");
            news_img_link = bundle.getString("news_img_link");
            news_title = bundle.getString("news_title");
            check_where = bundle.getString("from");
        }

        getNewsData();
        Log.d(TAG, "onCreateView: Link : " + news_title);
        return view;
    }

    private void getNewsData() {

        AppCompatActivity appCompatActivity = new AppCompatActivity();
        OkHttpClient client = mainActivity.client.newBuilder().build();
        okhttp3.Request request = new Request.Builder()
                .url(news_link)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                call.cancel();
                appCompatActivity.runOnUiThread(() -> {
                    try {
                        Toast.makeText(context, "Fail to connect server", Toast.LENGTH_LONG).show();

                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                });
            }

            @Override
            public void onResponse(Call call, final Response response) {
                appCompatActivity.runOnUiThread(() -> {
                    try {
                        assert response.body() != null;
                        String res = response.body().string();
                        Document document = Jsoup.parse(res);

                        Element div_detail_item = document.select("div.css-1bjep61").first();
                        Element box_txt = div_detail_item.select("div.css-1mq01hs").first();

                        if (document.select("img.img-responsive").first() != null) {
                            Element img_link_2 = document.select("img.img-responsive").first();
                            news_img_link_2 = img_link_2.attr("src");
                        }

                        news_desc = div_detail_item.text();
                        news_desc_2 = box_txt.text();

                        Log.d(TAG, "onResponse: Text_2 :" + news_img_link_2);

                        setData();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
        });
    }

    private void setData() {
        if (cardView.getVisibility() == View.GONE && !news_img_link_2.isEmpty()) {
            cardView.setVisibility(View.VISIBLE);
            textView_2.setVisibility(View.VISIBLE);
        }

        if (news_img_link_2 != null) {
            //img_1
            Glide.with(context)
                    .asBitmap()
                    .load(news_img_link)
                    .listener(new RequestListener<Bitmap>() {
                        @Override
                        public boolean onLoadFailed(GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                            Toast.makeText(context, "Can't load image.", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "onLoadFailed: Message : " + e);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                            progressBar_1.setVisibility(View.GONE);
                            return false;
                        }
                    })
                    .into(imageView);
            //img_2
            Glide.with(context)
                    .asBitmap()
                    .load(news_img_link_2)
                    .listener(new RequestListener<Bitmap>() {
                        @Override
                        public boolean onLoadFailed(GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                            Toast.makeText(context, "Can't load image.", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "onLoadFailed: Message : " + e);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                            progressBar_2.setVisibility(View.GONE);
                            return false;
                        }
                    })
                    .into(imageView_2);

            text_news_title.setText(news_title);
            textView.setText(news_desc_2);
            textView_2.setText(news_desc.replace(news_desc_2, ""));
        } else {
            cardView.setVisibility(View.GONE);
            textView_2.setVisibility(View.GONE);
            //img_1
            Glide.with(context)
                    .asBitmap()
                    .load(news_img_link)
                    .listener(new RequestListener<Bitmap>() {
                        @Override
                        public boolean onLoadFailed(GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                            Toast.makeText(context, "Can't load image.", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "onLoadFailed: Message : " + e);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                            progressBar_1.setVisibility(View.GONE);
                            return false;
                        }
                    })
                    .into(imageView);

            text_news_title.setText(news_title);
            textView.setText(news_desc);
        }
    }

    @Override
    public void onStop() {
        Log.d(TAG, "onStop: Stop");
        activity.toolbar_text.setSelected(false);
        if (check_where.equals("list")) {
            Objects.requireNonNull(news_activity.getSupportActionBar()).hide();
        } else {
            Objects.requireNonNull(news_activity.getSupportActionBar()).show();
        }
        super.onStop();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: Destroy");
        activity.toolbar_text.setSelected(false);
        if (check_where.equals("list")) {
            Objects.requireNonNull(news_activity.getSupportActionBar()).hide();
        } else {
            Objects.requireNonNull(news_activity.getSupportActionBar()).show();
        }
        super.onDestroy();
    }
}
