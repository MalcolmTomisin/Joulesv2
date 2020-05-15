package com.malcolm.joules;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.malcolm.joules.databinding.ActivitySplashBinding;


public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_TIME = 7000;
    private ActivitySplashBinding binding;
    private AnimationDrawable logoStart;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        binding.imageView6.setBackgroundResource(R.drawable.logo);
        logoStart = (AnimationDrawable) binding.imageView6.getBackground();
    }

    @Override
    protected void onStart() {
        super.onStart();
        logoStart.start();
        new Handler().postDelayed(
                () -> {
                    Intent decideIntent = new Intent(SplashActivity.this,MainActivity.class);
                    SplashActivity.this.startActivity(decideIntent);
                    SplashActivity.this.finish();
                },
                SPLASH_TIME
        );
    }
}
