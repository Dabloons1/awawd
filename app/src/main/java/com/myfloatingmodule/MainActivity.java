package com.myfloatingmodule;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    
    private static final int REQUEST_OVERLAY_PERMISSION = 1001;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Create a simple layout programmatically
        setContentView(createLayout());
        
        // Check and request overlay permission
        if (!Settings.canDrawOverlays(this)) {
            requestOverlayPermission();
        }
    }
    
    private android.widget.LinearLayout createLayout() {
        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(50, 50, 50, 50);
        
        TextView title = new TextView(this);
        title.setText("My Floating Module");
        title.setTextSize(24);
        title.setPadding(0, 0, 0, 30);
        layout.addView(title);
        
        TextView description = new TextView(this);
        description.setText("This is an LSposed module with floating window functionality.");
        description.setTextSize(16);
        description.setPadding(0, 0, 0, 30);
        layout.addView(description);
        
        Button startButton = new Button(this);
        startButton.setText("Start Floating Window");
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startFloatingWindow();
            }
        });
        layout.addView(startButton);
        
        Button stopButton = new Button(this);
        stopButton.setText("Stop Floating Window");
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopFloatingWindow();
            }
        });
        layout.addView(stopButton);
        
        Button settingsButton = new Button(this);
        settingsButton.setText("Open Settings");
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSettings();
            }
        });
        layout.addView(settingsButton);
        
        return layout;
    }
    
    private void requestOverlayPermission() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent, REQUEST_OVERLAY_PERMISSION);
    }
    
    private void startFloatingWindow() {
        if (!Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "Please grant overlay permission first", Toast.LENGTH_SHORT).show();
            requestOverlayPermission();
            return;
        }
        
        Intent serviceIntent = new Intent(this, FloatingWindowService.class);
        startForegroundService(serviceIntent);
        Toast.makeText(this, "Floating window started", Toast.LENGTH_SHORT).show();
    }
    
    private void stopFloatingWindow() {
        Intent serviceIntent = new Intent(this, FloatingWindowService.class);
        stopService(serviceIntent);
        Toast.makeText(this, "Floating window stopped", Toast.LENGTH_SHORT).show();
    }
    
    private void openSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_OVERLAY_PERMISSION) {
            if (Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "Overlay permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Overlay permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
