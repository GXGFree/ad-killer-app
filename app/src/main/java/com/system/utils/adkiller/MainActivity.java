package com.system.utils.adkiller;

import android.accessibility.AccessibilityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private TextView statusText;
    private Button toggleBtn;
    private AccessibilityManager accessibilityManager;

    // Target app package name - CHANGE THIS to the app you want to kill ads for
    private static final String TARGET_APP_PACKAGE = "com.example.adapp";

    // App to switch to for resetting ad timer
    private static final String HELPER_APP_PACKAGE = "com.android.settings";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusText = findViewById(R.id.statusText);
        toggleBtn = findViewById(R.id.toggleBtn);

        accessibilityManager = (AccessibilityManager) getSystemService(Context.ACCESSIBILITY_SERVICE);

        updateStatus();

        toggleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                startActivity(intent);
                Toast.makeText(MainActivity.this, "Find SystemUtils in the list and enable it", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void updateStatus() {
        boolean enabled = isServiceEnabled();
        if (enabled) {
            statusText.setText("Status: Running\nMonitoring: " + TARGET_APP_PACKAGE);
            toggleBtn.setText("Open Accessibility Settings");
        } else {
            statusText.setText("Status: Disabled\nPlease enable the service");
            toggleBtn.setText("Enable Service");
        }
    }

    private boolean isServiceEnabled() {
        String enabledServices = Settings.Secure.getString(
            getContentResolver(),
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        );
        if (enabledServices == null) return false;
        return enabledServices.contains(getPackageName() + "/" + AdKillerService.class.getName());
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateStatus();
    }
}
