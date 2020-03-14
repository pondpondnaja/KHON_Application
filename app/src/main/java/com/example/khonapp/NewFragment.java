package com.example.khonapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import java.util.Objects;

public class NewFragment extends Fragment {
    private static final String TAG = "newAc";

    private MainActivity activity;
    private AppCompatActivity news_activity;
    private String news_link;
    private String check_where;
    private Context context;
    private ProgressBar progressBar;
    private WebView webView;
    private ImageView imageView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.activity_news, container, false);


        context = view.getContext();
        imageView = view.findViewById(R.id.img_overlay_news);
        webView = view.findViewById(R.id.new_viewer);
        progressBar = view.findViewById(R.id.progressBar);
        activity = (MainActivity) getActivity();
        news_activity = (AppCompatActivity) view.getContext();

        Objects.requireNonNull(news_activity.getSupportActionBar()).hide();

        Bundle bundle = getArguments();
        if (bundle != null) {
            news_link = bundle.getString("news_link");
            check_where = bundle.getString("from");
        }

        Log.d(TAG, "onCreateView: Link : " + news_link);

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.loadUrl(news_link);

        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                progressBar.setVisibility(View.VISIBLE);
                imageView.setVisibility(View.VISIBLE);
                progressBar.bringToFront();
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                progressBar.setVisibility(View.GONE);
                imageView.setVisibility(View.GONE);
                Log.d(TAG, "onPageFinished: Finish");
                super.onPageFinished(view, url);
            }
        });

        if (savedInstanceState != null)
            webView.restoreState(savedInstanceState);
        else {
            webView.loadUrl(news_link);
        }

        return view;
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
