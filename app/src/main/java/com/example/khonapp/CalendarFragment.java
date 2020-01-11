package com.example.khonapp;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.EventDay;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;

public class CalendarFragment extends Fragment {
    private static final String TAG = "FC";
    //private static final String URL = "http://192.168.1.43:5000/androidNews";
    private static final String URL = "http://192.168.64.2/3D/calendar.php";
    //private static final String URL = "https://utg-fansub.me/3D/calendar.php";

    private ArrayList<EventDay> events = new ArrayList<>();
    private ArrayList<String> year_month_day = new ArrayList<>();
    private ArrayList<String> location = new ArrayList<>();
    private ArrayList<String> title = new ArrayList<>();
    private ArrayList<String> description = new ArrayList<>();
    private ArrayList<String> years = new ArrayList<>();
    private ArrayList<String> months = new ArrayList<>();
    private ArrayList<String> days = new ArrayList<>();

    private TextView text_title, text_description, text_location, location_t;
    private CalendarView calendarView;
    private Context context;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.calendarfra_layout, container, false);
        context = view.getContext();

        MainActivity activity = (MainActivity) getActivity();
        assert activity != null;
        activity.settoolbarTitle("Even Calendar");

        calendarView = view.findViewById(R.id.calendarView);
        text_title = view.findViewById(R.id.event_title);
        text_description = view.findViewById(R.id.event_description);
        text_location = view.findViewById(R.id.location_r);
        location_t = view.findViewById(R.id.location);

        //String[] arr = {"2019-11-16", "2019-11-23", "2019-11-28", "2019-11-31", "2020-0-1", "2020-0-2"};
        //String[] des = {"description 1", "description 2", "description 3", "description 4", "description 5", "description 6"};
        //String[] tit = {"Title 1", "Title 2", "Title 3", "Title 4", "Title 5", "Title 6"};

        /*for (int i = 0; i < title.size(); i++) {
            String[] item = year_month_day.get(i).split("-");
            years.add(item[0]);
            months.add(item[1]);
            days.add(item[2]);
        }*/

        calendarView.setOnDayClickListener(eventDay -> {
            //Log.d(TAG, "onDayClick: " + eventDay.getCalendar().get(Calendar.DATE));
            int i;
            boolean match = false;
            String date = String.valueOf(eventDay.getCalendar().get(Calendar.DATE));
            String month_r = String.valueOf(eventDay.getCalendar().get(Calendar.MONTH));
            for (i = 0; i < days.size(); i++) {
                if (month_r.equals(months.get(i))) {
                    Log.d(TAG, "onDayClick: Month : " + month_r + " = " + months.get(i));
                    Log.d(TAG, "onDayClick: Day   : " + date + " = " + days.get(i));
                    if (date.equals(days.get(i))) {
                        Log.d(TAG, "onDayClick: Called");
                        if (text_description.getVisibility() != View.VISIBLE) {
                            text_description.setVisibility(View.VISIBLE);
                            location_t.setVisibility(View.VISIBLE);
                            text_location.setVisibility(View.VISIBLE);
                        }
                        text_title.setText(((MyEventDay) eventDay).getNote());
                        text_description.setText(description.get(i));
                        text_location.setText(location.get(i));
                        match = true;
                        Log.d(TAG, "onDayClick: Event Day position : " + i);
                        Log.d(TAG, "onDayClick: Event Description  : " + ((MyEventDay) eventDay).getNote());
                        break;
                    }
                }
            }
            if (!match) {
                text_title.setText(getResources().getText(R.string.calendar_noevent));
                text_description.setVisibility(View.GONE);
                text_location.setVisibility(View.GONE);
                location_t.setVisibility(View.GONE);
            }
        });
        return view;
    }

    @Override
    public void onStart() {
        initData();
        super.onStart();
    }

    private void initData() {
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.POST, URL, null,
                response -> {
                    //Log.d(TAG, "onResponse: JSON respond : "+response);
                    for (int i = 0; i < response.length(); i++) {                    // Parsing json
                        try {
                            JSONObject obj = response.getJSONObject(i);
                            String title_a = obj.getString("title");
                            String description_a = obj.getString("description");
                            String event_date_a = obj.getString("event_date");
                            String location_a = obj.getString("location");
                            Log.d(TAG, "onResponse: Title : " + title + " Event Date : " + year_month_day);

                            title.add(title_a);
                            description.add(description_a);
                            year_month_day.add(event_date_a);
                            location.add(location_a);
                            //mImageURL.add(image_url);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    initEvent();
                }, error -> {
            Log.d(TAG, "onErrorResponse: Error appear");
            Toast.makeText(getContext(), "Please check your internet connection or go to contact us.", Toast.LENGTH_LONG).show();
            error.printStackTrace();
        });
        requestQueue.add(request);
    }

    private void initEvent() {
        for (int i = 0; i < year_month_day.size(); i++) {
            String[] item = year_month_day.get(i).split("-");
            years.add(item[0]);
            months.add(String.valueOf(Integer.valueOf(item[1]) - 1));
            int change = Integer.valueOf(item[2]);
            days.add(String.valueOf(change));
        }
        for (int j = 0; j < years.size(); j++) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Integer.valueOf(years.get(j)), Integer.valueOf(months.get(j)), Integer.valueOf(days.get(j)));
            events.add(new MyEventDay(calendar, R.drawable.calendar_dot, title.get(j)));
        }
        calendarView.setEvents(events);

        boolean match = false;
        int date = calendarView.getFirstSelectedDate().get(Calendar.DATE);
        for (int i = 0; i < days.size(); i++) {
            if (String.valueOf(date).equals(days.get(i))) {
                if (text_description.getVisibility() != View.VISIBLE) {
                    text_description.setVisibility(View.VISIBLE);
                    location_t.setVisibility(View.VISIBLE);
                    text_location.setVisibility(View.VISIBLE);
                }
                text_title.setText(title.get(i));
                text_description.setText(description.get(i));
                text_location.setText(location.get(i));
                match = true;
                break;
            }
        }
        if (!match) {
            text_title.setText(getResources().getText(R.string.calendar_noevent));
            text_description.setVisibility(View.GONE);
            location_t.setVisibility(View.GONE);
            text_location.setVisibility(View.GONE);
        }
    }
}
