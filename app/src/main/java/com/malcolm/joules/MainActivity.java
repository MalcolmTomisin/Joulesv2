package com.malcolm.joules;

import android.os.Bundle;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.malcolm.joules.databinding.ActivityMainBinding;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding mainBinding;
    ActionBarDrawerToggle toggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainBinding = DataBindingUtil.setContentView(this,R.layout.activity_main);
        toggle = new ActionBarDrawerToggle(this,mainBinding.drawerLayout,
                R.string.nav_app_bar_open_drawer_description,R.string.nav_close);
        mainBinding.drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        mainBinding.designNavigationView.inflateHeaderView(R.layout.navigation_header);
        mainBinding.designNavigationView.setNavigationItemSelectedListener(item -> false);
    }

}
