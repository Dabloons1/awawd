package com.myfloatingmodule;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import de.robv.android.xposed.XposedBridge;

public class FloatingWindowManager {
    
    public static void startFloatingWindow(ClassLoader classLoader) {
        try {
            XposedBridge.log("MyFloatingModule: Attempting to start floating window");
            
            // Try multiple context acquisition methods
            Context context = null;
            
            // Method 1: Try system context
            try {
                context = getSystemContext();
                if (context != null) {
                    XposedBridge.log("MyFloatingModule: Got system context");
                }
            } catch (Exception e) {
                XposedBridge.log("MyFloatingModule: System context failed: " + e.getMessage());
            }
            
            // Method 2: Try to get context from ActivityThread
            if (context == null) {
                try {
                    Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
                    Object activityThread = activityThreadClass.getMethod("currentActivityThread").invoke(null);
                    if (activityThread != null) {
                        context = (Context) activityThreadClass.getMethod("getSystemContext").invoke(activityThread);
                        XposedBridge.log("MyFloatingModule: Got ActivityThread context");
                    }
                } catch (Exception e) {
                    XposedBridge.log("MyFloatingModule: ActivityThread context failed: " + e.getMessage());
                }
            }
            
            // Method 3: Try to get context from current application
            if (context == null) {
                try {
                    Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
                    Object activityThread = activityThreadClass.getMethod("currentActivityThread").invoke(null);
                    if (activityThread != null) {
                        Object app = activityThreadClass.getMethod("getApplication").invoke(activityThread);
                        if (app instanceof Context) {
                            context = (Context) app;
                            XposedBridge.log("MyFloatingModule: Got application context");
                        }
                    }
                } catch (Exception e) {
                    XposedBridge.log("MyFloatingModule: Application context failed: " + e.getMessage());
                }
            }
            
            if (context != null) {
                XposedBridge.log("MyFloatingModule: Starting floating window service with context");
                
                // Make context final for use in inner classes
                final Context finalContext = context;
                
                // Start immediately and also with delays
                startFloatingWindowService(finalContext);
                
                // Also try with delays for better compatibility
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startFloatingWindowService(finalContext);
                    }
                }, 1000);
                
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startFloatingWindowService(finalContext);
                    }
                }, 3000);
                
            } else {
                XposedBridge.log("MyFloatingModule: Could not get any context - trying alternative approach");
                // Try to start without context (this might fail but worth trying)
                try {
                    // This is a last resort - might not work but worth attempting
                    XposedBridge.log("MyFloatingModule: Attempting to start service without context");
                } catch (Exception e) {
                    XposedBridge.log("MyFloatingModule: Alternative approach failed: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            XposedBridge.log("MyFloatingModule: Error starting floating window: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void startFloatingWindowService(Context context) {
        try {
            Intent serviceIntent = new Intent(context, FloatingWindowService.class);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent);
            } else {
                context.startService(serviceIntent);
            }
            XposedBridge.log("MyFloatingModule: Floating window service started successfully");
        } catch (Exception e) {
            XposedBridge.log("MyFloatingModule: Error starting service: " + e.getMessage());
        }
    }
    
    public static void startFloatingWindowWithContext(android.app.Activity activity) {
        try {
            XposedBridge.log("MyFloatingModule: Starting floating window with activity context");
            
            // Add a small delay to ensure the activity is fully initialized
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        Intent serviceIntent = new Intent(activity, FloatingWindowService.class);
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                            activity.startForegroundService(serviceIntent);
                        } else {
                            activity.startService(serviceIntent);
                        }
                        XposedBridge.log("MyFloatingModule: Floating window service started with activity context");
                    } catch (Exception e) {
                        XposedBridge.log("MyFloatingModule: Error starting with activity context: " + e.getMessage());
                    }
                }
            }, 2000); // 2 second delay for activity initialization
            
        } catch (Exception e) {
            XposedBridge.log("MyFloatingModule: Error starting floating window with context: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static void startFloatingWindowWithApplicationContext(android.app.Application app) {
        try {
            XposedBridge.log("MyFloatingModule: Starting floating window with application context");
            
            // Add a small delay to ensure the application is fully initialized
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        Intent serviceIntent = new Intent(app, FloatingWindowService.class);
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                            app.startForegroundService(serviceIntent);
                        } else {
                            app.startService(serviceIntent);
                        }
                        XposedBridge.log("MyFloatingModule: Floating window service started with application context");
                    } catch (Exception e) {
                        XposedBridge.log("MyFloatingModule: Error starting with application context: " + e.getMessage());
                    }
                }
            }, 3000); // 3 second delay for application initialization
            
        } catch (Exception e) {
            XposedBridge.log("MyFloatingModule: Error starting floating window with application context: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static Context getSystemContext() {
        try {
            // Try to get system context through reflection
            Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
            Object activityThread = activityThreadClass.getMethod("currentActivityThread").invoke(null);
            if (activityThread != null) {
                return (Context) activityThreadClass.getMethod("getSystemContext").invoke(activityThread);
            }
        } catch (Exception e) {
            XposedBridge.log("MyFloatingModule: Could not get system context: " + e.getMessage());
        }
        return null;
    }
}
