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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Full_NewListFragment extends Fragment {
    private static final String TAG = "FN";
    //private static final String URL = "http://192.168.64.2/3D/news.php";
    //private static final String URL = "https://utg-fansub.me/3D/news.php";
    //private static final String URL = "http://192.168.1.43:5000/androidNews";
    //Real connection
    private static final String URL = "http://khon.itar.site";

    private ArrayList<String> news_date = new ArrayList<>();
    private ArrayList<String> news_img = new ArrayList<>();
    private ArrayList<String> news_link = new ArrayList<>();
    private ArrayList<String> news_title = new ArrayList<>();

    LinearLayoutManager layoutManager;
    RecyclerView recyclerView;
    TextView news_toolbars;
    AppCompatActivity news_activity;
    Context context;
    MainActivity mainActivity = new MainActivity();

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
        connectServer();
        super.onStart();
    }

    @Override
    public void onStop() {
        news_date.clear();
        news_img.clear();
        news_link.clear();
        news_title.clear();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        news_date.clear();
        news_img.clear();
        news_link.clear();
        news_title.clear();
        super.onDestroy();
    }

    public void connectServer() {

        if (!news_title.isEmpty() || !news_img.isEmpty() || !news_date.isEmpty() || !news_link.isEmpty()) {
            return;
        }

        RequestBody postBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("app_check", "android")
                .build();
        AppCompatActivity appCompatActivity = new AppCompatActivity();

        OkHttpClient cus_client = mainActivity.client.newBuilder().build();
        okhttp3.Request request = new Request.Builder()
                .url(URL)
                .post(postBody)
                .build();

        cus_client.newCall(request).enqueue(new Callback() {
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
                        String res = response.body().string().trim();
                        Log.d(TAG, "onResponse: " + res.trim());
                        //setData();
                        if (res.contains("www.herokucdn.com/error-pages/application-error.html")) {
                            Log.d(TAG, "onResponse: Error appear");
                            //setData_Fail();
                        } else {
                            initData(res);
                        }
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                });
            }
        });
    }

    public void initData(String response) throws JSONException {
        //Log.d(TAG, "initData: Response JSON : "+response.toString());
        JSONArray jsonArray = new JSONArray(response);
        try {
            //JSONObject obj = new JSONObject(response);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);

                String news_dateJ = obj.getString("news_date");
                String news_imgJ = obj.getString("news_img");
                String news_linkJ = obj.getString("news_link");
                String news_titleJ = obj.getString("news_title");

                news_date.add(news_dateJ);
                news_img.add(news_imgJ);
                news_link.add(news_linkJ);
                news_title.add(news_titleJ);
            }

            initRecycleView();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void initRecycleView() {
        layoutManager = new LinearLayoutManager(context, RecyclerView.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        Full_NewViewAdapter adapter = new Full_NewViewAdapter(news_date, news_img, news_link, news_title, context);
        recyclerView.setAdapter(adapter);
    }
}
