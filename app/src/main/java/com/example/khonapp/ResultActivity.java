package com.example.khonapp;

import android.Manifest;
import android.content.ContentUris;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

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

    private TextView mTItle, mDescription, mGesture, mGestureDescription;
    private ImageView mImage;
    private ProgressBar progressBar, progressBar_cha, progressBar_gesture;

    Uri img_address;
    MainActivity mainActivity = new MainActivity();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        if (Build.VERSION.SDK_INT >= 23) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);
        }

        progressBar = findViewById(R.id.progressBar);
        progressBar_cha = findViewById(R.id.progressBar_cha);
        progressBar_gesture = findViewById(R.id.progressBar_gesture);
        mTItle = findViewById(R.id.result_title);
        mDescription = findViewById(R.id.result_description);
        mImage = findViewById(R.id.preview_img);
        mGesture = findViewById(R.id.resultGesture_title);
        mGestureDescription = findViewById(R.id.resultGesture_description);

        if (savedInstanceState == null) {
            progressBar.setVisibility(View.VISIBLE);
            progressBar_cha.setVisibility(View.VISIBLE);
            progressBar_gesture.setVisibility(View.VISIBLE);

            mTItle.setText("");
            mDescription.setText("");
            mGesture.setText("");
            mGestureDescription.setText("");
            progressBar.bringToFront();
            progressBar_cha.bringToFront();
            progressBar_gesture.bringToFront();

            Bundle extra = getIntent().getExtras();
            if (extra == null) {
                img_path = null;
            } else {
                img_path = extra.getString("img_path");
                img_address = Uri.parse(img_path);
                img_real_path = getPath(ResultActivity.this, img_address);
                //progressBar.setVisibility(View.GONE);
                //setData();

                createBody();
            }
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
    }

    public void createBody() {

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;

        Bitmap bitmap = BitmapFactory.decodeFile(img_real_path, options);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] byteArray = stream.toByteArray();

        RequestBody postBodyImage = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file_input", "androidFlask.jpg",
                        RequestBody.create(MediaType.parse("image/*jpg"), byteArray))
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
                        if (res.contains("www.herokucdn.com/error-pages/application-error.html")) {
                            Log.d(TAG, "onResponse: Error appear");
                            setData_Fail();
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

            setData();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setData() {
        //Text
        String string_builder_character = name + " " + score + " %";
        String string_builder_gesture = gesture_name + " " + gesture_score + " %";

        progressBar_cha.setVisibility(View.GONE);
        progressBar_gesture.setVisibility(View.GONE);
        mTItle.setText(string_builder_character);
        //mDescription.setText(desc);
        mGesture.setText(string_builder_gesture);
        //mGestureDescription.setText(gestureDesc);
        //Image
        Log.d(TAG, "setPreviewImage: Image Path : " + img_real_path);
        Bitmap bMap = BitmapFactory.decodeFile(img_real_path);
        Log.d(TAG, "setData: Orientation : " + getOrientation(img_address));
        Uri detect_img = Uri.parse(out_image);
        String orientation = getOrientation(detect_img);

        if (orientation.equals("landscape")) {
            FrameLayout.LayoutParams imageViewParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
            mImage.setLayoutParams(imageViewParams);
        } else if (orientation.equals("portrait")) {
            FrameLayout.LayoutParams imageViewParams = new FrameLayout.LayoutParams(1000, 1500);
            mImage.setLayoutParams(imageViewParams);
        }

        String out_img_URL_Builder = URL.replace("/application", out_image.replace("\\/", "/"));

        Glide.with(ResultActivity.this)
                .asBitmap()
                .load(out_img_URL_Builder)
                .listener(new RequestListener<Bitmap>() {
                    @Override
                    public boolean onLoadFailed(GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                        Toast.makeText(ResultActivity.this, "Can't load image.", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "onLoadFailed: Message : " + e);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                        progressBar.setVisibility(View.GONE);
                        return false;
                    }
                })
                .into(mImage);

        //mImage.setImageURI(Uri.parse(out_image));
    }

    public void setData_Fail() {
        progressBar.setVisibility(View.GONE);
        progressBar_cha.setVisibility(View.GONE);
        progressBar_gesture.setVisibility(View.GONE);

        mTItle.setText("Memory leak please contact supporter.");
    }

    private String getOrientation(Uri uri) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        String orientation = "landscape";
        try {
            BitmapFactory.decodeFile(img_real_path, options);
            int imageHeight = options.outHeight;
            int imageWidth = options.outWidth;
            Log.d(TAG, "getOrientation: H : " + imageHeight + " W : " + imageWidth);
            if (imageHeight > imageWidth) {
                orientation = "portrait";
            }
        } catch (Exception e) {
            //Do nothing
        }
        return orientation;
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
                    createBody();
                } else {
                    //permission from popup was denied
                    Toast.makeText(this, "Permission denied...", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
