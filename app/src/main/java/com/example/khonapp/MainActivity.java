package com.example.khonapp;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {//implements NavigationView.OnNavigationItemSelectedListener
    private static final String TAG = "mainAc";

    private static final int WRITE_PERMISSION_CODE = 100;
    private static final int CAMERA_CODE = 101;
    private static final int AR_CODE = 102;
    private static final int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 124;
    private static final int Limit = 4;
    private static final String URL = "http://192.168.64.2/3D/news.php";
    //private static final String URL = "https://utg-fansub.me/3D/news.php";

    //private DrawerLayout drawer;
    private Toast backToast;
    private int Destination;

    boolean doubleBackToExitPressedOnce = false;
    boolean isRunning = false;

    private FragmentManager fragmentManager;
    private RecyclerView recyclerView;
    //private NavigationView navigationView;
    //private Runnable runtoLeft;
    //private Handler handler;

    public Toolbar toolbar;
    public TextView toolbar_text;
    public CardView ar_card, detect_card, os_1, os_2;

    //Recycle vars.
    private ArrayList<String> mName = new ArrayList<>();
    private ArrayList<String> mImageURL = new ArrayList<>();
    private ArrayList<String> mDescription = new ArrayList<>();

    //Layout
    LinearLayoutManager layoutManager;
    SlideRecycleViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: MainScreen");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fragmentManager = getSupportFragmentManager();
        toolbar = findViewById(R.id.toolbar);
        //drawer          = findViewById(R.id.drawer_layout);
        ar_card = findViewById(R.id.card_1);
        detect_card = findViewById(R.id.card_2);
        os_1 = findViewById(R.id.os_1);
        os_2 = findViewById(R.id.os_2);
        toolbar_text = toolbar.findViewById(R.id.text_toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar_text.setText(toolbar.getTitle());
        /*
        navigationView  = findViewById(R.id.navigationView);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(MainActivity.this, drawer, toolbar,
                R.string.navigation_draw_open, R.string.navigation_draw_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        */

        if (savedInstanceState == null) {
            toolbar_text.setText(getResources().getString(R.string.sp_text1));
            //navigationView.setCheckedItem(R.id.home_section);
        }

        ar_card.setOnClickListener(view -> {
            checkPermission(AR_CODE);
        });

        detect_card.setOnClickListener(view -> {
            checkPermission(CAMERA_CODE);
        });

        os_1.setOnClickListener(view -> {
            String mailto = "mailto:supporter@gmail.com" +
                    "?subject=" + Uri.encode("Provide support");
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
            emailIntent.setData(Uri.parse(mailto));
            startActivity(emailIntent);
        });
        //initImageBitmap();
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "onStart: AutoScroll Enable");
        initImageBitmap();
        super.onStart();
    }

    @Override
    protected void onResume() {
        if (fragmentManager.getBackStackEntryCount() == 0) {
            Log.d(TAG, "onResume: RecycleView AutoScroll Resume BackStack = " + fragmentManager.getBackStackEntryCount());
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause: RecycleView AutoScroll Pause and Remove Callback");
        recyclerView.clearFocus();
        recyclerView.clearOnScrollListeners();
        Log.d(TAG, "onPause: mName     : " + mName.size());
        Log.d(TAG, "onPause: mImageURL : " + mImageURL.size());
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: recycle destroy");
        //handler.removeCallbacks(runtoLeft);
        recyclerView.clearFocus();
        recyclerView.stopScroll();
        recyclerView.clearOnScrollListeners();
        super.onDestroy();
    }

/*Side Menu.
    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem){
        switch (menuItem.getItemId()) {

            case R.id.home_section:
                toolbar.setTitle(getResources().getString(R.string.app_name));
                int backStackEntry = getSupportFragmentManager().getBackStackEntryCount();
                if (backStackEntry > 0) {
                    for (int i = 0; i < backStackEntry; i++) {
                        fragmentManager.popBackStackImmediate();
                        onResume();
                    }
                }
                Log.d(TAG, "onNavigationItemSelected: call on resume : backstack "+fragmentManager.getBackStackEntryCount());
                break;

            case R.id.pic_detect:
                getSupportFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(R.anim.slide_in_right,R.anim.slide_out_right,R.anim.slide_in_right,R.anim.slide_out_right)
                        .replace(R.id.fragment_container, new CameraFragment(),"camera").addToBackStack("pic_detect").commit();
                onPause();
                getSupportActionBar().hide();
                break;

            case R.id.ar_model:
                toolbar.setTitle(menuItem.getTitle().toString());
                getSupportFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(R.anim.slide_in_right,R.anim.slide_out_right,R.anim.slide_in_right,R.anim.slide_out_right)
                        .replace(R.id.fragment_container, new ARFragment(),"ar_model").addToBackStack("ar").commit();
                Log.d(TAG, "onNavigationItemSelected: Back stack AR = "+getSupportFragmentManager().getBackStackEntryCount());
                //getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ARFragment(),"ar_model").addToBackStack(null).commit();
                onPause();
                break;

            case R.id.message:
                Intent emailIntent = new Intent(android.content.Intent.ACTION_VIEW);
                Uri data = Uri.parse("mailto:giggabome@gmail.com?subject=" + "Provide support");
                emailIntent.setData(data);
                startActivity(emailIntent);
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_right);
                break;
        }

        drawer.closeDrawer(GravityCompat.START);

        return true;
    }
*/

    public void initImageBitmap() {

        if (!mImageURL.isEmpty() || !mName.isEmpty() || !mDescription.isEmpty()) {
            return;
        }

        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, URL, null,
                response -> {
                    Log.d(TAG, "onResponse: JSON respond : " + response);
                    for (int i = 0; i < Limit; i++) {                    // Parsing json
                        try {
                            JSONObject obj = response.getJSONObject(i);
                            String title = obj.getString("title");
                            String description = obj.getString("description");
                            String image_url = obj.getString("img_url");
                            Log.d(TAG, "onResponse: Title : " + title + " Image url : " + image_url);

                            mName.add(title);
                            mDescription.add(description);
                            mImageURL.add(image_url);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    mName.add("Read more");
                    mDescription.add("Read more");
                    mImageURL.add("Read more");
                    initRecycleView();
                    //scrollable();
                    //autoScrolltoLeft();
                }, error -> {
            Log.d(TAG, "onErrorResponse: Error appear");
            Toast.makeText(MainActivity.this, "Please check your internet connection or go to contact us.", Toast.LENGTH_LONG).show();
            error.printStackTrace();
        });
        requestQueue.add(request);
    }

    private void initRecycleView() {
        Log.d(TAG, "initRecycleView: init RecycleView");
        layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView = findViewById(R.id.recycleview);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new SlideRecycleViewAdapter(this, this, mName, mImageURL, mDescription);
        recyclerView.setHasFixedSize(false);
        recyclerView.setAdapter(adapter);
        //scrollable();
    }

    /*public void scrollable() {
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int last = layoutManager.findLastCompletelyVisibleItemPosition();
                int finalSize = adapter.getItemCount() - 1;
                if (last == finalSize){
                    layoutManager.scrollToPosition(0);
                }
            }
        });
    }

    public void autoScrolltoLeft() {
        handler   = new Handler();
        runtoLeft = new Runnable() {
            @Override
            public void run() {
                isRunning = true;
                recyclerView.scrollBy(2, 0);
                handler.postDelayed(this, 0);
            }
        };
        handler.postDelayed(runtoLeft, 0);
    }*/

    public void ARClick() {
        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right, R.anim.slide_in_right, R.anim.slide_out_right)
                .replace(R.id.fragment_container, new ARFragment(), "ar_model").addToBackStack("ar").commit();
        Log.d(TAG, "onNavigationItemSelected: Back stack AR = " + getSupportFragmentManager().getBackStackEntryCount());
        //getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ARFragment(),"ar_model").addToBackStack(null).commit();
        onPause();
        getSupportActionBar().hide();
    }

    public void CameraClick() {
        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right, R.anim.slide_in_right, R.anim.slide_out_right)
                .replace(R.id.fragment_container, new CameraFragment(), "camera").addToBackStack("pic_detect").commit();
        onPause();
        getSupportActionBar().hide();
    }

    public void eventClick(View view) {
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

        getSupportActionBar().show();

        /*
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
            Log.d(TAG, "onBackPressed: Backstack 1 = "+fragmentManager.getBackStackEntryCount());

        }else
        */

        if (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStackImmediate();
        } else if (!doubleBackToExitPressedOnce) {
            this.doubleBackToExitPressedOnce = true;
            backToast = Toast.makeText(this, "BACK again to exit.", Toast.LENGTH_SHORT);
            backToast.show();
            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    doubleBackToExitPressedOnce = false;
                }

            }, 2000);
        } else {
            backToast.cancel();
            super.onBackPressed();
            return;
        }

        if (fragmentManager.getBackStackEntryCount() == 0) {
            Log.d(TAG, "onBackPressed: Backstack = " + fragmentManager.getBackStackEntryCount());

            toolbar_text.setText(getResources().getString(R.string.sp_text1));
            getSupportActionBar().show();
            Log.d(TAG, "onBackPressed: Runable status: " + isRunning);
            if (!isRunning) {
                //scrollable();
                //autoScrolltoLeft();
                Log.d(TAG, "onBackPressed: Resume!!");
                Log.d(TAG, "onBackPressed: Runable status: " + isRunning);
            }
        }
    }

    //PERMISSION ------------------------------------------------------------------------------------------------------------

    // Function to check and request permission

    public void checkPermission(int WhereTogo) {
        Destination = WhereTogo;
        ArrayList<String> permissionNeeded = new ArrayList<>();
        final ArrayList<String> permissionList = new ArrayList<>();

        if (!addPermission(permissionList, Manifest.permission.CAMERA)) {
            permissionNeeded.add("Camera");
        }
        if (!addPermission(permissionList, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            permissionNeeded.add("Storage (Write)");
        }
        if (!addPermission(permissionList, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            permissionNeeded.add("Storage (Read)");
        }

        if (permissionList.size() > 0) {
            if (permissionNeeded.size() > 0) {
                // Need Rationale
                String message = "You need to grant access to " + permissionNeeded.get(0);
                for (int i = 1; i < permissionNeeded.size(); i++) {
                    message = message + ", " + permissionNeeded.get(i);
                }
                showMessageOKCancel(message,
                        (dialog, which) -> requestPermissions(permissionList.toArray(new String[permissionList.size()]),
                                REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS));
                return;
            }
            requestPermissions(permissionList.toArray(new String[permissionList.size()]), REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
            return;
        } else {
            switch (WhereTogo) {
                case 101:
                    CameraClick();
                    break;

                case 102:
                    ARClick();
                    break;
            }
        }
    }

    private boolean addPermission(ArrayList<String> permissionsList, String permission) {
        if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(permission);
            // Check for Rationale Option
            if (!shouldShowRequestPermissionRationale(permission))
                return false;
        }
        return true;
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    /*public boolean checkPermission(String permission, int requestCode) {
        // Checking if permission is not granted
        if (ContextCompat.checkSelfPermission(MainActivity.this, permission) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{permission}, requestCode);
            return false;
        } else {
            Toast.makeText(MainActivity.this, "Permission already granted", Toast.LENGTH_SHORT).show();
            return true;
        }
    }*/

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS) {
            Map<String, Integer> perms = new HashMap<String, Integer>();
            // Initial
            perms.put(Manifest.permission.CAMERA, PackageManager.PERMISSION_GRANTED);
            perms.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
            perms.put(Manifest.permission.READ_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
            // Fill with results
            for (int i = 0; i < permissions.length; i++)
                perms.put(permissions[i], grantResults[i]);
            // Check for ACCESS_FINE_LOCATION
            if (perms.get(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                    && perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                    && perms.get(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // All Permissions Granted
                switch (Destination) {
                    case AR_CODE:
                        ARClick();
                        break;

                    case CAMERA_CODE:
                        CameraClick();
                        break;
                }
            } else {
                // Permission Denied
                Toast.makeText(MainActivity.this, "Some Permission is Denied", Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
