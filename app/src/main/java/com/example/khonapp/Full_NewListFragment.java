package com.example.khonapp;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

public class Full_NewListFragment extends Fragment {
    private static final String TAG = "FN";
    //private static final String URL = "http://192.168.64.2/3D/news.php";
    //private static final String URL = "https://utg-fansub.me/3D/news.php";
    //private static final String URL = "http://192.168.1.43:5000/androidNews";
    //Real connection
    private static final String URL = "http://khon.itar.site/androidNews";

    private ArrayList<String> mName = new ArrayList<>();
    private ArrayList<String> mImageURL = new ArrayList<>();
    private ArrayList<String> mDescription = new ArrayList<>();

    LinearLayoutManager layoutManager;
    RecyclerView recyclerView;
    TextView news_toolbars;
    AppCompatActivity news_activity;
    Context context;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: CreateView called");
        final View view = inflater.inflate(R.layout.fragment_full_new, container, false);

        news_activity = (AppCompatActivity) view.getContext();
        Objects.requireNonNull(news_activity.getSupportActionBar()).hide();

        context = view.getContext();
        news_toolbars = view.findViewById(R.id.news_toolbar);
        recyclerView = view.findViewById(R.id.news_fullList_view);

        news_toolbars.setText(getResources().getText(R.string.news_toolbar));
        return view;
    }

    @Override
    public void onStart() {
        Log.d(TAG, "onStart: Initial data");
        initData();
        super.onStart();
    }

    @Override
    public void onStop() {
        mName.clear();
        mImageURL.clear();
        mDescription.clear();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        mName.clear();
        mImageURL.clear();
        mDescription.clear();
        super.onDestroy();
    }

    private void initData() {

        if (!mImageURL.isEmpty() || !mName.isEmpty() || !mDescription.isEmpty()) {
            return;
        }

        RequestQueue requestQueue = Volley.newRequestQueue(context);
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, URL, null,
                response -> {
                    Log.d(TAG, "onResponse: JSON respond : " + response);
                    for (int i = 0; i < response.length(); i++) {                    // Parsing json
                        try {
                            JSONObject obj = response.getJSONObject(i);
                            String title = obj.getString("title");
                            String description = obj.getString("description");
                            String image_url = obj.getString("img_name");
                            //Log.d(TAG, "onResponse: Title : " + title + " Image url : " + image_url);

                            String[] split = image_url.split(",");
                            String real_img_url = split[0];

                            mName.add(title);
                            mDescription.add(description);
                            mImageURL.add(real_img_url);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    initRecycleView();
                }, error -> {
            Log.d(TAG, "onErrorResponse: Error appear");
            Toast.makeText(context, "Please check your internet connection or go to contact us.", Toast.LENGTH_LONG).show();
            error.printStackTrace();
        });
        requestQueue.add(request);
    }

    private void initRecycleView() {
        layoutManager = new LinearLayoutManager(context, RecyclerView.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        Full_NewViewAdapter adapter = new Full_NewViewAdapter(mName, mImageURL, mDescription, context);
        recyclerView.setAdapter(adapter);
    }
}
