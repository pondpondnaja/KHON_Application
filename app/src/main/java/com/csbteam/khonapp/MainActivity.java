package com.csbteam.khonapp;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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

public class MainActivity extends AppCompatActivity {//implements NavigationView.OnNavigationItemSelectedListener{
    private static final String TAG = "mainAc";

    private static final int PERMISSION_CODE = 1000;
    private static final int PERMISSION_CODE_2 = 1001;
    private static final int IMAGE_CAPTURE_CODE = 1001;
    private static final int GALLERY_REQUEST_CODE = 1002;
    //private static final String URL = "http://192.168.64.2/3D/news.php";
    //private static final String URL = "https://utg-fansub.me/3D/news.php";
    //private static final String URL = "http://192.168.1.43:5000/androidNews";
    //Real connection
    private static final String URL = "http://khon.itar.site";
    //private DrawerLayout drawer;
    private Toast backToast;

    boolean doubleBackToExitPressedOnce = false;
    boolean isRunning = false;
    boolean reload = false;
    Uri image_uri;

    private FragmentManager fragmentManager;
    private RecyclerView recyclerView;
    private ScrollView scrollView;

    public Toolbar toolbar;
    public ImageView overlay;
    public TextView toolbar_text;
    public CardView ar_card, detect_card, os_1, os_2, optional, camera_holder, gallery_holder;

    //Recycle vars.
    private ArrayList<String> news_date = new ArrayList<>();
    private ArrayList<String> news_desc = new ArrayList<>();
    private ArrayList<String> news_img = new ArrayList<>();
    private ArrayList<String> news_link = new ArrayList<>();
    private ArrayList<String> news_title = new ArrayList<>();

    //Layout
    LinearLayoutManager layoutManager;
    SlideRecycleViewAdapter adapter;
    SwipeRefreshLayout mSwipeRefreshLayout;
    //Connection
    public OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: MainScreen");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);

        fragmentManager = getSupportFragmentManager();
        toolbar = findViewById(R.id.toolbar);
        ar_card = findViewById(R.id.card_1);
        detect_card = findViewById(R.id.card_2);
        os_1 = findViewById(R.id.os_1);
        os_2 = findViewById(R.id.os_2);
        overlay = findViewById(R.id.img_overlay_optional);
        optional = findViewById(R.id.option_holder);
        camera_holder = findViewById(R.id.camera_optional_holder);
        gallery_holder = findViewById(R.id.gallery_holder);
        toolbar_text = toolbar.findViewById(R.id.text_toolbar);
        mSwipeRefreshLayout = findViewById(R.id.drawer_layout);
        scrollView = findViewById(R.id.service);

        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        toolbar_text.setText(toolbar.getTitle());

        if (savedInstanceState == null) {
            toolbar_text.setText(getResources().getString(R.string.sp_text1));
        }

        ar_card.setOnClickListener(view -> ARClick());

        detect_card.setOnClickListener(view -> {
            mSwipeRefreshLayout.setEnabled(false);
            optional.setVisibility(View.VISIBLE);
            overlay.setVisibility(View.VISIBLE);
        });

        overlay.setOnClickListener(view -> {
            if (optional.getVisibility() == View.VISIBLE) {
                mSwipeRefreshLayout.setEnabled(true);
                optional.setVisibility(View.GONE);
                overlay.setVisibility(View.GONE);
            }
        });

        camera_holder.setOnClickListener(view -> CameraClick());
        gallery_holder.setOnClickListener(view -> GalleryClick());

        os_1.setOnClickListener(view -> {
            String mailto = "mailto:supporter@gmail.com" +
                    "?subject=" + Uri.encode("Provide support");
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
            emailIntent.setData(Uri.parse(mailto));
            startActivity(emailIntent);
        });

        scrollView.getViewTreeObserver().addOnScrollChangedListener(() -> {
            if (scrollView != null) {
                if (scrollView.getScrollY() == 0) {
                    mSwipeRefreshLayout.setEnabled(true);
                } else {
                    mSwipeRefreshLayout.setEnabled(false);
                }
            }
        });

        mSwipeRefreshLayout.setOnRefreshListener(() -> {
            if (fragmentManager.getBackStackEntryCount() == 0) {
                Log.d(TAG, "onCreate: Called refresh");
                reload = true;
                connectServer();
            } else {
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });

        mSwipeRefreshLayout.setColorSchemeResources(R.color.Main_color_1, R.color.Main_color_2, R.color.Main_color_4);

        mSwipeRefreshLayout.post(() -> {
            if (mSwipeRefreshLayout != null) {
                Log.d(TAG, "onCreate: BackStack : " + fragmentManager.getBackStackEntryCount());
                mSwipeRefreshLayout.setRefreshing(true);
            }
            // Fetching data from server
            reload = true;
            connectServer();
        });

        Log.d(TAG, "onCreate: Y : " + scrollView.getScaleY());
    }

    public void disableRefresh() {
        mSwipeRefreshLayout.setEnabled(false);
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "onStart: AutoScroll Enable");
        //connectServer();
        super.onStart();
    }

    @Override
    protected void onResume() {
        if (optional.getVisibility() == View.VISIBLE) {
            optional.setVisibility(View.GONE);
            overlay.setVisibility(View.GONE);
        }
        if (fragmentManager.getBackStackEntryCount() == 0) {
            Log.d(TAG, "onResume: RecycleView AutoScroll Resume BackStack = " + fragmentManager.getBackStackEntryCount());
            Objects.requireNonNull(getSupportActionBar()).show();
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause: RecycleView AutoScroll Pause and Remove Callback");
        scrollView.setScrollY(0);
        //recyclerView.clearFocus();
        //recyclerView.clearOnScrollListeners();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: recycle destroy");
        recyclerView.clearFocus();
        recyclerView.stopScroll();
        recyclerView.clearOnScrollListeners();
        super.onDestroy();
    }

    public void connectServer() {

        if (!news_date.isEmpty() && !reload) {
            return;
        }

        news_date.clear();
        news_desc.clear();
        news_img.clear();
        news_title.clear();

        RequestBody postBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("app_check", "android")
                .build();

        OkHttpClient cus_client = client.newBuilder().build();
        Request request = new Request.Builder()
                .url(URL)
                .post(postBody)
                .build();

        cus_client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                call.cancel();
                runOnUiThread(() -> {
                    try {
                        Toast.makeText(getApplicationContext(), "Fail to connect server", Toast.LENGTH_LONG).show();

                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                    mSwipeRefreshLayout.setRefreshing(false);
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull final Response response) {
                runOnUiThread(() -> {
                    try {
                        assert response.body() != null;
                        String res = response.body().string().trim();
                        Log.d(TAG, "onResponse: " + res.trim());
                        //setData();
                        if (res.contains("www.herokucdn.com/error-pages/application-error.html")) {
                            Log.d(TAG, "onResponse: Error appear");
                            reload = false;
                            //setData_Fail();
                        } else {
                            reload = false;
                            initData(res);
                        }
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                    mSwipeRefreshLayout.setRefreshing(false);
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
        Log.d(TAG, "initRecycleView: init RecycleView");
        layoutManager = new LinearLayoutManager(MainActivity.this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView = findViewById(R.id.recycleview);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new SlideRecycleViewAdapter(news_date, news_img, news_link, news_title, MainActivity.this);
        adapter.notifyDataSetChanged();
        recyclerView.setHasFixedSize(false);
        recyclerView.setAdapter(adapter);
        //scrollable();
    }

    public void ARClick() {
        mSwipeRefreshLayout.setEnabled(false);
        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right, R.anim.slide_in_right, R.anim.slide_out_right)
                .replace(R.id.fragment_container, new ARFragment(), "ar_model").addToBackStack("ar").commit();
        Log.d(TAG, "onNavigationItemSelected: Back stack AR = " + getSupportFragmentManager().getBackStackEntryCount());
        //getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ARFragment(),"ar_model").addToBackStack(null).commit();
        onPause();
        Objects.requireNonNull(getSupportActionBar()).hide();
    }

    public void GalleryClick() {
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED ||
                checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            //permission not enabled, request it
            String[] permission = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
            //show popup to request permissions
            requestPermissions(permission, PERMISSION_CODE_2);
        } else {
            //permission already granted
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            String[] mimeTypes = {"image/jpeg", "image/png"};
            mSwipeRefreshLayout.setEnabled(false);
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
            startActivityForResult(intent, GALLERY_REQUEST_CODE);
        }
    }

    public void CameraClick() {
        if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED ||
                checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            //permission not enabled, request it
            String[] permission = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
            //show popup to request permissions
            requestPermissions(permission, PERMISSION_CODE);
        } else {
            //permission already granted
            openCamera();
        }
    }

    private void openCamera() {
        mSwipeRefreshLayout.setEnabled(false);
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera");
        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        //Camera intent
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(cameraIntent, IMAGE_CAPTURE_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //called when image was captured from camera
        if (resultCode == RESULT_OK) {
            switch (requestCode) {

                case IMAGE_CAPTURE_CODE:
                    //set the image captured to our ImageView
                    Intent previewIn = new Intent(MainActivity.this, ResultActivity.class);
                    previewIn.putExtra("img_path", image_uri.toString());
                    mSwipeRefreshLayout.setEnabled(false);
                    startActivity(previewIn);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    break;

                case GALLERY_REQUEST_CODE:
                    Uri selectedImage = data.getData();
                    Intent galleryIn = new Intent(MainActivity.this, ResultActivity.class);
                    assert selectedImage != null;
                    galleryIn.putExtra("img_path", selectedImage.toString());
                    mSwipeRefreshLayout.setEnabled(false);
                    startActivity(galleryIn);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void eventClick(View view) {
        mSwipeRefreshLayout.setEnabled(false);
        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right, R.anim.slide_in_right, R.anim.slide_out_right)
                .replace(R.id.fragment_container, new CalendarFragment(), "event").addToBackStack("event").commit();
        onPause();
    }

    public void setToolbarTitle(String text) {
        toolbar_text.setText(text);
    }

    @Override
    public void onBackPressed() {

        Objects.requireNonNull(getSupportActionBar()).show();

        if (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStackImmediate();
            mSwipeRefreshLayout.setEnabled(true);
        } else if (optional.getVisibility() == View.VISIBLE) {
            optional.setVisibility(View.GONE);
            overlay.setVisibility(View.GONE);
        } else if (!doubleBackToExitPressedOnce) {
            this.doubleBackToExitPressedOnce = true;
            backToast = Toast.makeText(this, "BACK again to exit.", Toast.LENGTH_SHORT);
            backToast.show();
            new Handler().postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
        } else {
            backToast.cancel();
            super.onBackPressed();
            return;
        }

        if (fragmentManager.getBackStackEntryCount() == 0) {
            Log.d(TAG, "onBackPressed: BackStack = " + fragmentManager.getBackStackEntryCount());

            toolbar_text.setText(getResources().getString(R.string.sp_text1));
            getSupportActionBar().show();
            Log.d(TAG, "onBackPressed: RunAble status: " + isRunning);
            if (!isRunning) {
                Log.d(TAG, "onBackPressed: Resume!!");
                Log.d(TAG, "onBackPressed: RunAble status: " + isRunning);
            }
        }
    }

    /*--------------------------------------------PERMISSION CHECK--------------------------------------------*/

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //this method is called, when user presses Allow or Deny from Permission Request Popup
        if (requestCode == PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //permission from popup was granted
                openCamera();
            } else {
                //permission from popup was denied
                Toast.makeText(this, "Permission denied...", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
