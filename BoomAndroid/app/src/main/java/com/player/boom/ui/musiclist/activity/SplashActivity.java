package com.player.boom.ui.musiclist.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.player.boom.R;
import com.player.boom.task.PlayerService;


/**
 * Created by Rahul Agarwal on 12-10-16.
 */

public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_TIME_OUT = 2000;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startService(new Intent(this, PlayerService.class));
        new Handler().postDelayed(new Runnable() {

            /*
             * Showing splash screen with a timer. This will be useful when you
             * want to show case your app logo / company
             */

            @Override
            public void run() {
                // This method will be executed once the timer is over
                // Start your app main activity
                Intent i = new Intent(SplashActivity.this, BoomPlayerActivity.class);
                startActivity(i);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                // close this activity
                finish();
            }
        }, SPLASH_TIME_OUT);
    }
}
