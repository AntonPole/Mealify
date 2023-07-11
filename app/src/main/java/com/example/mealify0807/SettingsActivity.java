package com.example.mealify0807;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

public class SettingsActivity extends AppCompatActivity {

    private Switch nightModeSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (isNightModeEnabled()) {
            setTheme(R.style.Theme_Mealify0807_Dark);
        } else {
            setTheme(R.style.Theme_Mealify0807);
        }

        setContentView(R.layout.activity_settings);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        nightModeSwitch = findViewById(R.id.night_mode_switch);
        nightModeSwitch.setChecked(isNightModeEnabled());
        nightModeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setNightModeEnabled(isChecked);
                Toast.makeText(SettingsActivity.this, "Night mode " + (isChecked ? "enabled" : "disabled"), Toast.LENGTH_SHORT).show();
                recreate();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean isNightModeEnabled() {
        int nightModeFlags = getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK;
        return nightModeFlags == android.content.res.Configuration.UI_MODE_NIGHT_YES;
    }

    private void setNightModeEnabled(boolean enabled) {
        int nightMode = enabled ? android.content.res.Configuration.UI_MODE_NIGHT_YES : android.content.res.Configuration.UI_MODE_NIGHT_NO;
        getApplicationContext().getResources().getConfiguration().uiMode &= ~android.content.res.Configuration.UI_MODE_NIGHT_MASK;
        getApplicationContext().getResources().getConfiguration().uiMode |= nightMode;
    }
}
