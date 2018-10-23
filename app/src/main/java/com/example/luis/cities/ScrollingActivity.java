package com.example.luis.cities;


import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.luis.cities.model.Coord;
import com.example.luis.cities.model.Data;
import com.example.luis.cities.ui.FragmentOne;
import com.example.luis.cities.ui.FragmentTwo;

public class ScrollingActivity extends BaseActivity implements FragmentOne.IlistenerFrag{


   private FragmentOne fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.toolbar_title);


        Log.d("act","create");
        showFirstFragment();

    }

    @Override
    public void onBackPressed() {


        super.onBackPressed();

    }



    private void showFirstFragment() {
        FragmentOne f1=new FragmentOne();
        fragment=f1;
        fragmentTransaction(FragmentOne.class.getSimpleName(),f1,false,1);
    }

    @Override
    public void onSelected(Data city) {

        Toast.makeText(this, "Selected: " + city.getName() + ", " + city.getCountry(), Toast.LENGTH_LONG).show();

        FragmentTwo fragment2 = new FragmentTwo();

        Bundle bundle = new Bundle();
        Coord obj = city.getCoord();
        bundle.putSerializable("coord", obj);
        fragment2.setArguments(bundle);

        fragmentTransaction(FragmentTwo.class.getSimpleName(), fragment2, true, 1);
    }
}
