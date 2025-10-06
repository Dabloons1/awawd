package com.myfloatingmodule;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
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
        
        Button testButton = new Button(this);
        testButton.setText("Test Direct Window");
        testButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createDirectFloatingWindow();
            }
        });
        layout.addView(testButton);
        
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
    
    private void createDirectFloatingWindow() {
        try {
            android.util.Log.d("MainActivity", "Creating direct floating window test");
            
            WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
            
            // Create a simple test window
            TextView testView = new TextView(this);
            testView.setText("DIRECT MOD MENU TEST");
            testView.setTextColor(0xFFFFFFFF);
            testView.setTextSize(20);
            testView.setBackgroundColor(0xFFFF0000);
            testView.setPadding(50, 50, 50, 50);
            
            WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_TOAST,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                android.graphics.PixelFormat.TRANSLUCENT
            );
            
            params.gravity = Gravity.CENTER;
            params.x = 0;
            params.y = 0;
            
            windowManager.addView(testView, params);
            android.util.Log.d("MainActivity", "✓ Direct floating window created successfully");
            Toast.makeText(this, "Direct floating window created!", Toast.LENGTH_LONG).show();
            
        } catch (Exception e) {
            android.util.Log.e("MainActivity", "Direct floating window creation failed: " + e.getMessage());
            Toast.makeText(this, "Direct floating window failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
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
