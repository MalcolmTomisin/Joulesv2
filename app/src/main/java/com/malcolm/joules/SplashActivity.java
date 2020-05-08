package com.malcolm.joules;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.malcolm.joules.databinding.ActivitySplashBinding;


public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_TIME = 3000;
    private ActivitySplashBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
    }

    @Override
    protected void onStart() {
        super.onStart();
        new Handler().postDelayed(
                new Runnable() {
                    @Override
                    public void run() {
                        Intent decideIntent = new Intent(SplashActivity.this,MainActivity.class);
                        SplashActivity.this.startActivity(decideIntent);
                        SplashActivity.this.finish();
                    }
                },
                SPLASH_TIME
        );
    }
}
