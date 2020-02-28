package com.example.khonapp;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ARFragment extends Fragment {

    private static final String TAG = "MainActivity";
    private ArrayList<String> ARName = new ArrayList<>();
    private ArrayList<String> FolderName = new ArrayList<>();
    LinearLayoutManager layoutManager;
    RecyclerView recyclerView;

    Context context;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        final View view =  inflater.inflate(R.layout.fragment_ar,container,false);
        context = view.getContext();

        inidata();

        recyclerView = view.findViewById(R.id.ar_recycleview);
        layoutManager = new LinearLayoutManager(context,RecyclerView.VERTICAL,false);
        recyclerView.setLayoutManager(layoutManager);
        ARListRecycleViewAdapter adapter = new ARListRecycleViewAdapter(context,ARName,FolderName);
        recyclerView.setAdapter(adapter);

        return view;
    }

    public void inidata(){
        ARName.add("ท่าที่ 1");
        ARName.add("ท่าที่ 2");
        ARName.add("ท่าที่ 3");
        ARName.add("ท่าที่ 4");
        ARName.add("ท่าที่ 5");
        ARName.add("ท่าที่ 6");
        ARName.add("ท่าที่ 7");
        ARName.add("ท่าที่ 8");
        ARName.add("ท่าที่ 9");
        ARName.add("ท่าที่ 10");

        FolderName.add("Earth");
        FolderName.add("Elf");
        FolderName.add("Megumin");
        FolderName.add("Elf");
        FolderName.add("Earth");
        FolderName.add("Elf");
        FolderName.add("Earth");
        FolderName.add("Elf");
        FolderName.add("Earth");
        FolderName.add("Elf");
    }

}
