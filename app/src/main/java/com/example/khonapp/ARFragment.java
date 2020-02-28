package com.example.khonapp;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ARFragment extends Fragment {

    private static final String TAG = "MainActivityAR";
    private ArrayList<String> ARName = new ArrayList<>();
    private ArrayList<String> FolderName = new ArrayList<>();
    LinearLayoutManager layoutManager;
    RecyclerView recyclerView;

    TextView textView;
    Context context;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        final View view =  inflater.inflate(R.layout.fragment_ar,container,false);
        context = view.getContext();

        //initdata();
        textView      = view.findViewById(R.id.ar_toolbar);
        recyclerView  = view.findViewById(R.id.ar_recycleview);
        layoutManager = new LinearLayoutManager(context,RecyclerView.VERTICAL,false);
        recyclerView.setLayoutManager(layoutManager);
        ARListRecycleViewAdapter adapter = new ARListRecycleViewAdapter(getActivity(),context,ARName,FolderName);
        recyclerView.setAdapter(adapter);

        textView.setText(getResources().getString(R.string.ar_toolbar));
        return view;
    }

    @Override
    public void onStart(){
        Log.d(TAG, "onStart: Initial data");
        initdata();
        super.onStart();
    }

    @Override
    public void onStop() {
        Log.d(TAG, "onPause: Clear data");
        ARName.clear();
        FolderName.clear();
        super.onStop();
    }

    public void initdata() {
        Log.d(TAG, "initdata: addData");
        ARName.add("ท่าที่ 1 : ฉัน");
        ARName.add("ท่าที่ 2 : โกรธ");
        ARName.add("ท่าที่ 3 : ร้องไห้");
        ARName.add("ท่าที่ 4 : อาย");
        ARName.add("ท่าที่ 5 : ยิ้ม");

        FolderName.add("Am");
        FolderName.add("Angry");
        FolderName.add("Cry");
        FolderName.add("Shy");
        FolderName.add("Smile");
    }

}
