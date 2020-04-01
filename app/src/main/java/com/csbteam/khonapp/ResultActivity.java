package com.csbteam.khonapp;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ResultActivity extends AppCompatActivity {

    private static final int PERMISSION_CODE = 1000;
    private static final String TAG = "cameraResult";
    //private static final String URL = "http://10.70.1.61:5000/connectFromAndroid";
    //private static final String URL = "http://192.168.64.2/3D/testscript.php";
    //Real connection
    private static final String URL = "http://khon.itar.site/application";

    //ArrayList
    private ArrayList<String> name = new ArrayList<>();
    private ArrayList<String> desc = new ArrayList<>();
    private ArrayList<String> score = new ArrayList<>();
    private ArrayList<String> gesture_name = new ArrayList<>();
    private ArrayList<String> gesture_score = new ArrayList<>();
    private ArrayList<String> gestureDesc = new ArrayList<>();
    private ArrayList<String> out_img = new ArrayList<>();

    private int Bitmap_width, Bitmap_height;

    private ImageView imageView;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private CardView error;

    Uri img_address;
    MainActivity mainActivity = new MainActivity();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        recyclerView = findViewById(R.id.detect_result);
        progressBar = findViewById(R.id.progressBar);
        imageView = findViewById(R.id.img_overlay_result);
        error = findViewById(R.id.error_container);

        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);

        if (savedInstanceState == null) {
            progressBar.setVisibility(View.VISIBLE);
            progressBar.bringToFront();
            imageView.setVisibility(View.VISIBLE);
            error.setVisibility(View.GONE);

            Bundle extra = getIntent().getExtras();
            String img_path;
            if (extra != null) {
                img_path = extra.getString("img_path");
                img_address = Uri.parse(img_path);
                createDetectionBody();
            }
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
    }

    public Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    public void createDetectionBody() {

        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), img_address);
        } catch (IOException e) {
            e.printStackTrace();
        }

        int orientation = getOrientation(ResultActivity.this, img_address);
        Matrix matrix = new Matrix();
        matrix.postRotate(90);

        assert bitmap != null;
        Bitmap convertedImage = getResizedBitmap(bitmap, 500);

        if (orientation == 90) {
            bitmap = Bitmap.createBitmap(convertedImage, 0, 0, convertedImage.getWidth(), convertedImage.getHeight(), matrix, true);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            Log.d(TAG, "createBody: Converted and ReCorrect orientation");
        } else {
            convertedImage.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            Log.d(TAG, "createBody: Converted");
        }

        Bitmap_width = bitmap.getWidth();
        Bitmap_height = bitmap.getHeight();

        Log.d(TAG, "createBody: Orientation : " + orientation);
        Log.d(TAG, "createBody: W : " + Bitmap_width + " H : " + Bitmap_height);
        byte[] byteArray = stream.toByteArray();

        RequestBody postBodyImage = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file_input", "androidFlask.jpg", RequestBody.create(MediaType.parse("image/*jpg"), byteArray))
                .addFormDataPart("app_check", "android")
                .build();

        postRequest(postBodyImage);
    }

    private void postRequest(RequestBody postBody) {

        OkHttpClient cus_client = mainActivity.client.newBuilder()
                .connectTimeout(1, TimeUnit.MINUTES)
                .readTimeout(1, TimeUnit.MINUTES)
                .build();

        AppCompatActivity appCompatActivity = new AppCompatActivity();
        Request request = new Request.Builder()
                .url(ResultActivity.URL)
                .post(postBody)
                .build();

        cus_client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                call.cancel();
                appCompatActivity.runOnUiThread(() -> {
                    try {
                        Toast.makeText(getApplicationContext(), "Fail to connect server", Toast.LENGTH_LONG).show();
                        error.setVisibility(View.VISIBLE);
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull final Response response) {
                appCompatActivity.runOnUiThread(() -> {
                    try {
                        assert response.body() != null;
                        String res = response.body().string().trim();
                        Log.d(TAG, "onResponse: " + res.trim());
                        Log.d(TAG, "onResponse: PATH : " + img_address);
                        //setData();
                        if (res.contains("www.herokucdn.com/error-pages/application-error.html") || res.contains("500")) {
                            Log.d(TAG, "onResponse: Error appear");
                            setData_Fail();
                        } else {
                            progressBar.setVisibility(View.GONE);
                            imageView.setVisibility(View.GONE);
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

                String nameJ = obj.getString("character_thai");
                String descJ = obj.getString("character_desc");
                String scoreJ = obj.getString("score");
                String gesture_nameJ = obj.getString("gesture_name");
                String gesture_scoreJ = obj.getString("gesture_score");
                String gestureDescJ = obj.getString("desc");
                String out_imageJ = obj.getString("out_image");

                name.add(nameJ);
                desc.add(descJ);
                score.add(scoreJ);
                gesture_name.add(gesture_nameJ);
                gesture_score.add(gesture_scoreJ);
                gestureDesc.add(gestureDescJ);
                out_img.add(out_imageJ);
            }

            initRecycleView();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void initRecycleView() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext(), RecyclerView.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        ResultAdapter adapter = new ResultAdapter(name, desc, score, gesture_name, gesture_score, gestureDesc, out_img, ResultActivity.this, Bitmap_width, Bitmap_height);
        recyclerView.setAdapter(adapter);
    }

    public void setData_Fail() {
        progressBar.setVisibility(View.GONE);
        imageView.setVisibility(View.GONE);
        error.setVisibility(View.VISIBLE);
    }

    public static int getOrientation(Context context, Uri photoUri) {
        /* it's on the external media. */
        Cursor cursor = context.getContentResolver().query(photoUri,
                new String[]{MediaStore.Images.ImageColumns.ORIENTATION}, null, null, null);

        assert cursor != null;
        if (cursor.getCount() != 1) {
            return -1;
        }

        cursor.moveToFirst();
        return cursor.getInt(0);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //this method is called, when user presses Allow or Deny from Permission Request Popup
        if (requestCode == PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //permission from popup was granted
                createDetectionBody();
            } else {
                //permission from popup was denied
                Toast.makeText(this, "Permission denied...", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
