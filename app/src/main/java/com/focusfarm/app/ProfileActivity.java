package com.focusfarm.app;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.focusfarm.app.data.SharedPrefsManager;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        findViewById(R.id.btn_profile_back).setOnClickListener(v -> finish());

        TextView stats = findViewById(R.id.tv_profile_stats);
        stats.setText(getString(
                R.string.profile_stats_summary,
                SharedPrefsManager.getStreak(this),
                SharedPrefsManager.getFarmHealth(this),
                SharedPrefsManager.getTreeLevel(this),
                SharedPrefsManager.getAvgSleepHours(this),
                SharedPrefsManager.getSleepGoalHours(this)));
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}