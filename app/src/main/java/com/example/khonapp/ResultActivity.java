package com.example.khonapp;

import android.Manifest;
import android.content.ContentUris;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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
    private ArrayList<String> model_id = new ArrayList<>();

    private String img_path, img_real_path, previewPath, out_image;
    private int Bitmap_width, Bitmap_height;

    private ImageView imageView;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;

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

        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);

        if (savedInstanceState == null) {
            progressBar.setVisibility(View.VISIBLE);
            progressBar.bringToFront();
            imageView.setVisibility(View.VISIBLE);

            Bundle extra = getIntent().getExtras();
            if (extra == null) {
                img_path = null;
            } else {
                img_path = extra.getString("img_path");
                img_address = Uri.parse(img_path);
                //img_real_path = getPath(ResultActivity.this, img_address);
                //Log.d(TAG, "onCreate: IMG_Add : " + img_real_path);
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

        postRequest(URL, postBodyImage);
    }

    private void postRequest(String postUrl, RequestBody postBody) {

        OkHttpClient cus_client = mainActivity.client.newBuilder()
                .connectTimeout(1, TimeUnit.MINUTES)
                .readTimeout(1, TimeUnit.MINUTES)
                .build();

        AppCompatActivity appCompatActivity = new AppCompatActivity();
        Request request = new Request.Builder()
                .url(postUrl)
                .post(postBody)
                .build();

        cus_client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                call.cancel();
                appCompatActivity.runOnUiThread(() -> {
                    try {
                        Toast.makeText(getApplicationContext(), "Fail to connect server", Toast.LENGTH_LONG).show();

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
                String model_idJ = obj.getString("gID");

                name.add(nameJ);
                desc.add(descJ);
                score.add(scoreJ);
                gesture_name.add(gesture_nameJ);
                gesture_score.add(gesture_scoreJ);
                gestureDesc.add(gestureDescJ);
                out_img.add(out_imageJ);
                model_id.add(model_idJ);
            }

            initRecycleView();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void initRecycleView() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext(), RecyclerView.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        ResultAdapter adapter = new ResultAdapter(name, desc, score, gesture_name, gesture_score, gestureDesc, out_img, model_id, ResultActivity.this, Bitmap_width, Bitmap_height);
        recyclerView.setAdapter(adapter);
    }

    public void setData_Fail() {
        progressBar.setVisibility(View.GONE);
        //progressBar_cha.setVisibility(View.GONE);
        //progressBar_gesture.setVisibility(View.GONE);

        //mTItle.setText("Memory leak please contact supporter.");
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

    private static String getPath(final Context context, final Uri uri) {
        // DocumentProvider
        if (DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    Log.d(TAG, "getPath: Primary : " + Environment.getExternalStorageDirectory() + "/" + split[1]);
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                Log.d(TAG, "getPath: isDownloadsDocument(uri) : " + getDataColumn(context, contentUri, null, null));
                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    private static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {

        final String column = "_data";
        final String[] projection = {column};
        try (Cursor cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        }
        return null;
    }

    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //this method is called, when user presses Allow or Deny from Permission Request Popup
        switch (requestCode) {
            case PERMISSION_CODE: {
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
}
