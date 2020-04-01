package com.csbteam.khonapp;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.EventDay;
import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CalendarFragment extends Fragment {
    private static final String TAG = "FC";
    //private static final String URL = "http://192.168.1.43:5000/androidEvents";
    //private static final String URL = "http://192.168.64.2/3D/calendar.php";
    //private static final String URL = "https://utg-fansub.me/3D/calendar.php";

    //Real connect
    private static final String URL = "http://khon.itar.site/show";

    private ArrayList<EventDay> events = new ArrayList<>();
    private ArrayList<String> year_month_day = new ArrayList<>();
    private ArrayList<String> location = new ArrayList<>();
    private ArrayList<String> title = new ArrayList<>();
    private ArrayList<String> description = new ArrayList<>();
    private ArrayList<String> img_name = new ArrayList<>();
    private ArrayList<String> years = new ArrayList<>();
    private ArrayList<String> months = new ArrayList<>();
    private ArrayList<String> days = new ArrayList<>();
    private ArrayList<String> link = new ArrayList<>();

    private TextView text_title, text_description, text_location, location_t;
    private ImageView event_img;
    private BottomSheetBehavior mBottomSheetBehavior;
    private CalendarView calendarView;
    private MainActivity mainActivity = new MainActivity();
    private CardView informationHolder;
    private AppCompatActivity appCompatActivity = new AppCompatActivity();
    private Context context;
    private RelativeLayout read_more;
    private String event_list_location;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.calendarfra_layout, container, false);
        context = view.getContext();

        MainActivity activity = (MainActivity) getActivity();
        assert activity != null;
        activity.setToolbarTitle("Even Calendar");

        calendarView = view.findViewById(R.id.calendarView);
        text_title = view.findViewById(R.id.event_title);
        text_description = view.findViewById(R.id.event_description);
        text_location = view.findViewById(R.id.location_r);
        location_t = view.findViewById(R.id.location);
        informationHolder = view.findViewById(R.id.information_holder);
        ImageView popUp_btn = view.findViewById(R.id.event_detail_popup);
        event_img = view.findViewById(R.id.event_img);
        read_more = view.findViewById(R.id.read_more_holder);

        View bottomSheet = view.findViewById(R.id.bottom_sheet);
        mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);

        text_location.setVisibility(View.GONE);
        location_t.setVisibility(View.GONE);

        calendarView.setOnDayClickListener(eventDay -> {
            int i;
            boolean match = false;
            String date = String.valueOf(eventDay.getCalendar().get(Calendar.DATE));
            String month_r = String.valueOf(eventDay.getCalendar().get(Calendar.MONTH));
            String year_r = String.valueOf(eventDay.getCalendar().get(Calendar.YEAR));
            for (i = 0; i < days.size(); i++) {
                if (year_r.equals(years.get(i))) {
                    Log.d(TAG, "onDayClick: Year : " + year_r + " = " + years.get(i));
                    Log.d(TAG, "onDayClick: Month : " + month_r + " = " + months.get(i));
                    Log.d(TAG, "onDayClick: Day   : " + date + " = " + days.get(i));
                    if (month_r.equals(months.get(i))) {
                        Log.d(TAG, "onDayClick: Year : " + year_r + " = " + years.get(i));
                        Log.d(TAG, "onDayClick: Month : " + month_r + " = " + months.get(i));
                        Log.d(TAG, "onDayClick: Day   : " + date + " = " + days.get(i));
                        if (date.equals(days.get(i))) {
                            Log.d(TAG, "onDayClick: Year : " + year_r + " = " + years.get(i));
                            Log.d(TAG, "onDayClick: Month : " + month_r + " = " + months.get(i));
                            Log.d(TAG, "onDayClick: Day   : " + date + " = " + days.get(i));
                            Log.d(TAG, "onDayClick: Called");
                            if (text_description.getVisibility() != View.VISIBLE || event_img.getVisibility() != View.VISIBLE) {
                                informationHolder.setVisibility(View.VISIBLE);
                                read_more.setVisibility(View.VISIBLE);
                                text_description.setVisibility(View.VISIBLE);
                                location_t.setVisibility(View.VISIBLE);
                                text_location.setVisibility(View.VISIBLE);
                                event_img.setVisibility(View.VISIBLE);
                            }
                            if (mBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED || mBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_HIDDEN) {
                                mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                            }
                            text_title.setText(((MyEventDay) eventDay).getNote());
                            text_description.setText(description.get(i));
                            text_location.setText(location.get(i));
                            event_list_location = String.valueOf(i);
                            if (img_name.get(i) != null) {
                                Glide.with(context).load(img_name.get(i)).into(event_img);
                            } else {
                                event_img.setVisibility(View.GONE);
                            }
                            match = true;
                            Log.d(TAG, "onDayClick: Event Day position : " + i);
                            Log.d(TAG, "onDayClick: Event Description  : " + ((MyEventDay) eventDay).getNote());
                            break;
                        }
                    }
                }
            }
            if (!match) {
                informationHolder.setVisibility(View.GONE);
                read_more.setVisibility(View.GONE);
                text_title.setText(getResources().getText(R.string.calendar_noevent));
                text_description.setVisibility(View.GONE);
                text_location.setVisibility(View.GONE);
                location_t.setVisibility(View.GONE);
                event_img.setVisibility(View.GONE);
            }
        });

        read_more.setOnClickListener(View -> {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.addCategory(Intent.CATEGORY_BROWSABLE);
            intent.setData(Uri.parse(link.get(Integer.parseInt(event_list_location))));
            startActivity(intent);
        });

        popUp_btn.setOnClickListener(View -> {
            if (mBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_HIDDEN) {
                mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });

        view.setFocusableInTouchMode(true);
        view.requestFocus();
        view.setOnKeyListener((v, keyCode, event) -> {
            Log.i(TAG, "keyCode: " + keyCode);
            if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                Log.i(TAG, "onKey Back listener is working!!!");
                if (mBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                    mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    return true;
                } else {
                    view.clearFocus();
                }
            }
            return false;
        });

        return view;
    }

    @Override
    public void onStart() {
        connectServer();
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        events.clear();
        year_month_day.clear();
        location.clear();
        title.clear();
        description.clear();
        img_name.clear();
        years.clear();
        months.clear();
        days.clear();
        link.clear();
        Log.d(TAG, "onStop: Called");
    }

    private void connectServer() {

        OkHttpClient client = mainActivity.client.newBuilder().build();

        RequestBody postBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("app_check", "android")
                .build();

        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(URL)
                .post(postBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                call.cancel();
                appCompatActivity.runOnUiThread(() -> {
                    try {
                        Toast.makeText(getContext(), "Fail to connect server", Toast.LENGTH_LONG).show();

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
                        if (res.contains("www.herokucdn.com/error-pages/application-error.html")) {
                            Log.d(TAG, "onResponse: Error appear");
                            //setData_Fail();
                        } else {
                            Log.d(TAG, "onResponse: Success : " + res);
                            initData(res);
                        }
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                });
            }
        });
    }

    private void initData(String res) throws JSONException {
        JSONArray jsonArray = new JSONArray(res);
        //Log.d(TAG, "onResponse: JSON respond : "+response);
        for (int i = 0; i < jsonArray.length(); i++) {                    // Parsing json
            try {
                JSONObject obj = jsonArray.getJSONObject(i);
                String title_a = obj.getString("shows_title");
                String event_link = obj.getString("shows_link");
                String event_date_a = obj.getString("shows_date");
                //String location_a = obj.getString("location");
                String image_a = obj.getString("shows_img");

                title.add(title_a);
                year_month_day.add(event_date_a);
                link.add(event_link);
                getLocation(event_link);
                getDescription(event_link);
                img_name.add(image_a);
                //mImageURL.add(image_url);

                Log.d(TAG, "onResponse: Title   : " + title_a + " Event Date : " + event_date_a);
                Log.d(TAG, "initData: ImagePath : " + event_link);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        initEvent();

    }

    private String correctMonth(String monthIn) {
        String[] monthBe = {"ม.ค.", "ก.พ.", "มี.ค.", "เม.ย.", "พ.ค.", "มิ.ย.", "ก.ค.", "ส.ค.", "ก.ย.", "ต.ค.", "พ.ย.", "ธ.ค."};
        String[] monthAf = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12"};
        String monthOut = null;
        for (int i = 0; i < monthBe.length; i++) {
            if (monthIn.equals(monthBe[i])) {
                monthOut = monthAf[i];
            }
        }
        return monthOut;
    }

    private void getLocation(String thaiTicketURL) {
        OkHttpClient client = mainActivity.client.newBuilder().build();
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(thaiTicketURL)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                call.cancel();
                appCompatActivity.runOnUiThread(() -> {
                    try {
                        Toast.makeText(getContext(), "Fail to connect server", Toast.LENGTH_LONG).show();
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull final Response response) throws IOException {
                assert response.body() != null;
                String res = response.body().string();
                Document document = Jsoup.parse(res);

                //Location
                Element div_detail_item = document.select("div.event-detail-item").first();
                Element box_txt = div_detail_item.select("div.box-txt").first();
                Element a_tag = box_txt.select("a").first();
                String locationHT = a_tag.text();

                location.add(locationHT);
                Log.d(TAG, "onResponse: Location : " + locationHT);
            }
        });

    }

    private void getDescription(String thaiTicketURL) {
        OkHttpClient client = mainActivity.client.newBuilder().build();
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(thaiTicketURL)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                call.cancel();
                appCompatActivity.runOnUiThread(() -> {
                    try {
                        Toast.makeText(getContext(), "Fail to connect server", Toast.LENGTH_LONG).show();
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull final Response response) throws IOException {
                assert response.body() != null;
                String res = response.body().string();
                Document document = Jsoup.parse(res);

                //Description
                Element section_section_detail = document.select("div.body").last();
                String descriptionHT = section_section_detail.text();

                description.add(descriptionHT);

                Log.d(TAG, "onResponse: URL : " + thaiTicketURL);
                Log.d(TAG, "onResponse: Description : " + descriptionHT);
            }
        });
    }

    private void initEvent() {
        for (int i = 0; i < year_month_day.size(); i++) {
            String[] item = year_month_day.get(i).split(" ");
            years.add(String.valueOf(Integer.parseInt(item[2]) - 543));
            months.add(String.valueOf(Integer.parseInt(correctMonth(item[1])) - 1));
            int change = Integer.parseInt(item[0]);
            days.add(String.valueOf(change));
            Log.d(TAG, "initEvent: Data : " + days + " Month : " + months + " Year : " + years);
        }
        for (int j = 0; j < years.size(); j++) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Integer.parseInt(years.get(j)), Integer.parseInt(months.get(j)), Integer.parseInt(days.get(j)));
            events.add(new MyEventDay(calendar, R.drawable.calendar_dot, title.get(j)));
        }
        calendarView.setEvents(events);

        boolean match = false;

        int date = calendarView.getFirstSelectedDate().get(Calendar.DATE);
        int month = calendarView.getFirstSelectedDate().get(Calendar.MONTH);
        int year = calendarView.getFirstSelectedDate().get(Calendar.YEAR);
        for (int i = 0; i < days.size(); i++) {
            if (String.valueOf(year).equals(years.get(i))) {
                if (String.valueOf(month).equals(months.get(i))) {
                    if (String.valueOf(date).equals(days.get(i))) {
                        if (text_description.getVisibility() != View.VISIBLE || event_img.getVisibility() != View.VISIBLE) {
                            informationHolder.setVisibility(View.VISIBLE);
                            read_more.setVisibility(View.VISIBLE);
                            text_description.setVisibility(View.VISIBLE);
                            location_t.setVisibility(View.VISIBLE);
                            text_location.setVisibility(View.VISIBLE);
                            event_img.setVisibility(View.VISIBLE);
                        }
                        if (!description.isEmpty() && !location.isEmpty()) {
                            text_title.setText(title.get(i));
                            text_description.setText(description.get(i));
                            event_list_location = String.valueOf(i);
                            text_location.setText(location.get(i));
                        }
                        if (img_name.get(i) != null) {
                            Glide.with(context).load(img_name.get(i)).into(event_img);
                        } else {
                            event_img.setVisibility(View.GONE);
                        }
                        match = true;
                        break;
                    }
                }
            }
        }
        if (!match) {
            informationHolder.setVisibility(View.GONE);
            read_more.setVisibility(View.GONE);
            text_title.setText(getResources().getText(R.string.calendar_noevent));
            text_description.setVisibility(View.GONE);
            location_t.setVisibility(View.GONE);
            text_location.setVisibility(View.GONE);
            event_img.setVisibility(View.GONE);
        }
    }
}
