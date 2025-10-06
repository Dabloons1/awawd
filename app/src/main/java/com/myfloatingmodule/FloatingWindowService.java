package com.myfloatingmodule;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.core.app.NotificationCompat;

public class FloatingWindowService extends Service {
    
    private WindowManager windowManager;
    private View floatingView;
    private boolean isFloatingWindowVisible = false;
    
    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        startForeground(1, createNotification());
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        android.util.Log.d("FloatingWindow", "Service started, attempting to show floating window");
        showFloatingWindow();
        return START_STICKY;
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                "floating_window_channel",
                "Floating Window Service",
                NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }
    
    private Notification createNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        return new NotificationCompat.Builder(this, "floating_window_channel")
            .setContentTitle("Floating Window Service")
            .setContentText("Floating window is active")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .build();
    }
    
        private void showFloatingWindow() {
            if (isFloatingWindowVisible) return;

            try {
                android.util.Log.d("FloatingWindow", "Attempting to show floating window with anti-cheat bypass");
                windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

                // Create floating view
                floatingView = createFloatingView();

                // Anti-cheat bypass: Use stealthy window types
                int windowType;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    // Try TYPE_TOAST first for better stealth
                    windowType = WindowManager.LayoutParams.TYPE_TOAST;
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    windowType = WindowManager.LayoutParams.TYPE_TOAST;
                } else {
                    windowType = WindowManager.LayoutParams.TYPE_PHONE;
                }

                android.util.Log.d("FloatingWindow", "Using stealthy window type: " + windowType);
            
                WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    windowType,
                    // Anti-cheat bypass: Use stealthy flags
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS |
                    WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED |
                    WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR |
                    WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM |
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                    PixelFormat.TRANSLUCENT
                );
            
            params.gravity = Gravity.TOP | Gravity.START;
            params.x = 50;
            params.y = 200;
            
            // Check overlay permission before trying to add view
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!android.provider.Settings.canDrawOverlays(this)) {
                    android.util.Log.e("FloatingWindow", "Overlay permission not granted - trying alternative approach");
                    // Try with TYPE_TOAST as fallback
                    params.type = WindowManager.LayoutParams.TYPE_TOAST;
                }
            }
            
                // Try multiple approaches to ensure visibility
                try {
                    android.util.Log.d("FloatingWindow", "Attempting to add floating view");
                    windowManager.addView(floatingView, params);
                    isFloatingWindowVisible = true;
                    android.util.Log.d("FloatingWindow", "✓ Floating window added successfully");

                    // Make the window more visible by bringing it to front
                    floatingView.bringToFront();
                    floatingView.invalidate();

                    // Force visibility with multiple attempts
                    new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (floatingView != null) {
                                floatingView.setVisibility(View.VISIBLE);
                                floatingView.bringToFront();
                                floatingView.invalidate();
                                android.util.Log.d("FloatingWindow", "Forced visibility attempt");
                            }
                        }
                    }, 100);

                    // Additional visibility attempt
                    new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (floatingView != null) {
                                floatingView.setVisibility(View.VISIBLE);
                                floatingView.bringToFront();
                                floatingView.invalidate();
                                android.util.Log.d("FloatingWindow", "Second visibility attempt");
                            }
                        }
                    }, 1000);

                } catch (Exception e) {
                    android.util.Log.e("FloatingWindow", "Failed to add view with primary method: " + e.getMessage());
                    // Try alternative window type
                    try {
                        android.util.Log.d("FloatingWindow", "Trying TYPE_TOAST fallback");
                        params.type = WindowManager.LayoutParams.TYPE_TOAST;
                        windowManager.addView(floatingView, params);
                        isFloatingWindowVisible = true;
                        android.util.Log.d("FloatingWindow", "✓ Floating window added with TYPE_TOAST");
                    } catch (Exception e2) {
                        android.util.Log.e("FloatingWindow", "Failed with TYPE_TOAST: " + e2.getMessage());
                        // Try simple fallback
                        try {
                            android.util.Log.d("FloatingWindow", "Trying simple fallback");
                            createSimpleFloatingWindow();
                        } catch (Exception e3) {
                            android.util.Log.e("FloatingWindow", "All methods failed: " + e3.getMessage());
                        }
                    }
                }
            
        } catch (Exception e) {
            // Handle permission or other errors
            android.util.Log.e("FloatingWindow", "Error adding floating window: " + e.getMessage());
            e.printStackTrace();
            
            // Try a simple fallback approach
            try {
                android.util.Log.d("FloatingWindow", "Trying simple fallback approach");
                createSimpleFloatingWindow();
            } catch (Exception e2) {
                android.util.Log.e("FloatingWindow", "Fallback also failed: " + e2.getMessage());
            }
        }
    }
    
    private void createSimpleFloatingWindow() {
        try {
            // Create a very simple, highly visible window
            TextView simpleView = new TextView(this);
            simpleView.setText("MOD MENU ACTIVE");
            simpleView.setTextColor(0xFFFFFFFF);
            simpleView.setTextSize(20);
            simpleView.setBackgroundColor(0xFFFF0000);
            simpleView.setPadding(50, 50, 50, 50);
            
            WindowManager.LayoutParams simpleParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_TOAST,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            );
            
            simpleParams.gravity = Gravity.CENTER;
            simpleParams.x = 0;
            simpleParams.y = 0;
            
            windowManager.addView(simpleView, simpleParams);
            isFloatingWindowVisible = true;
            android.util.Log.d("FloatingWindow", "Simple floating window added successfully");
            
        } catch (Exception e) {
            android.util.Log.e("FloatingWindow", "Simple fallback failed: " + e.getMessage());
        }
    }
    
    private View createFloatingView() {
        // Create a more visible floating window with game-specific buttons
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setBackgroundColor(0xFF000000); // Solid black background for maximum visibility
        layout.setPadding(20, 20, 20, 20);
        
        // Add a bright border to make it more visible
        layout.setBackgroundColor(0xFF000000);
        
        // Title with bright colors
        TextView title = new TextView(this);
        title.setText("SOUL STRIKE MOD MENU");
        title.setTextColor(0xFFFF0000); // Bright red text
        title.setTextSize(16);
        title.setPadding(0, 0, 0, 15);
        title.setBackgroundColor(0xFF00FF00); // Bright green background
        layout.addView(title);
        
        // Test button to verify visibility
        Button testButton = new Button(this);
        testButton.setText("TEST VISIBILITY");
        testButton.setTextColor(0xFFFFFFFF);
        testButton.setBackgroundColor(0xFFFF0000);
        testButton.setTextSize(12);
        testButton.setPadding(10, 10, 10, 10);
        testButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                android.util.Log.d("FloatingWindow", "Test button clicked - window is visible!");
                // Show a toast to confirm visibility
                android.widget.Toast.makeText(FloatingWindowService.this, "Floating window is working!", android.widget.Toast.LENGTH_SHORT).show();
            }
        });
        layout.addView(testButton);
        
        // Game hooking control buttons
        Button godModeButton = new Button(this);
        godModeButton.setText("GOD MODE: OFF");
        godModeButton.setTextColor(0xFFFFFFFF);
        godModeButton.setBackgroundColor(0xFF333333);
        godModeButton.setTextSize(12);
        godModeButton.setPadding(10, 10, 10, 10);
        godModeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean newState = !GameHookManager.isGodModeEnabled();
                GameHookManager.setGodMode(newState);
                godModeButton.setText("GOD MODE: " + (newState ? "ON" : "OFF"));
                godModeButton.setBackgroundColor(newState ? 0xFF00FF00 : 0xFF333333);
                android.widget.Toast.makeText(FloatingWindowService.this, 
                    "God mode " + (newState ? "enabled" : "disabled"), 
                    android.widget.Toast.LENGTH_SHORT).show();
            }
        });
        layout.addView(godModeButton);
        
        Button coinsButton = new Button(this);
        coinsButton.setText("UNLIMITED COINS: OFF");
        coinsButton.setTextColor(0xFFFFFFFF);
        coinsButton.setBackgroundColor(0xFF333333);
        coinsButton.setTextSize(12);
        coinsButton.setPadding(10, 10, 10, 10);
        coinsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean newState = !GameHookManager.isUnlimitedCoinsEnabled();
                GameHookManager.setUnlimitedCoins(newState);
                coinsButton.setText("UNLIMITED COINS: " + (newState ? "ON" : "OFF"));
                coinsButton.setBackgroundColor(newState ? 0xFF00FF00 : 0xFF333333);
                android.widget.Toast.makeText(FloatingWindowService.this, 
                    "Unlimited coins " + (newState ? "enabled" : "disabled"), 
                    android.widget.Toast.LENGTH_SHORT).show();
            }
        });
        layout.addView(coinsButton);
        
        Button speedButton = new Button(this);
        speedButton.setText("SPEED HACK: OFF");
        speedButton.setTextColor(0xFFFFFFFF);
        speedButton.setBackgroundColor(0xFF333333);
        speedButton.setTextSize(12);
        speedButton.setPadding(10, 10, 10, 10);
        speedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean newState = !GameHookManager.isSpeedHackEnabled();
                GameHookManager.setSpeedHack(newState);
                speedButton.setText("SPEED HACK: " + (newState ? "ON" : "OFF"));
                speedButton.setBackgroundColor(newState ? 0xFF00FF00 : 0xFF333333);
                android.widget.Toast.makeText(FloatingWindowService.this, 
                    "Speed hack " + (newState ? "enabled" : "disabled"), 
                    android.widget.Toast.LENGTH_SHORT).show();
            }
        });
        layout.addView(speedButton);
        
        Button closeButton = new Button(this);
        closeButton.setText("Close");
        closeButton.setTextSize(12);
        closeButton.setPadding(5, 5, 5, 5);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideFloatingWindow();
                stopSelf();
            }
        });
        layout.addView(closeButton);
        
        return layout;
    }
    
    private void hideFloatingWindow() {
        if (isFloatingWindowVisible && windowManager != null && floatingView != null) {
            try {
                windowManager.removeView(floatingView);
                isFloatingWindowVisible = false;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        hideFloatingWindow();
    }
}
