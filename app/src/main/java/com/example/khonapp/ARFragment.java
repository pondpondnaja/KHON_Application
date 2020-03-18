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

        //initData();
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
        initData();
        super.onStart();
    }

    @Override
    public void onStop() {
        Log.d(TAG, "onPause: Clear data");
        ARName.clear();
        FolderName.clear();
        super.onStop();
    }

    private void initData() {
        Log.d(TAG, "initData: addData");
        ARName.add("ท่าที่ 1 : ฉัน,ตัวเรา");
        ARName.add("ท่าที่ 2 : โกรธ,โมโห");
        ARName.add("ท่าที่ 3 : ร้องไห้โศกา");
        ARName.add("ท่าที่ 4 : เขิน,อาย");
        ARName.add("ท่าที่ 5 : ยิ้ม,ดีใจ");
        ARName.add("ท่าที่ 6 : รัก");
        ARName.add("ท่าที่ 7 : ปฏิเสธ,ไม่");
        ARName.add("ท่าที่ 8 : สูญสิ้น,ตาย");
        ARName.add("ท่าที่ 9 : ยิ่งใหญ่,เกรียงไกร");
        ARName.add("ท่าที่ 10 : ที่นั่น,ที่นี่,ชี้ไห้เห็น");

        FolderName.add("Am");
        FolderName.add("Angry");
        FolderName.add("Cry");
        FolderName.add("Shy");
        FolderName.add("Smile");

        FolderName.add("Love");
        FolderName.add("Ignore");
        FolderName.add("Dead");
        FolderName.add("Mighty");
        FolderName.add("There");
    }

}
