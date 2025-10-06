package com.myfloatingmodule;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;

public class XposedInit implements IXposedHookLoadPackage {
    
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        XposedBridge.log("MyFloatingModule: Package loaded: " + lpparam.packageName);
        XposedBridge.log("MyFloatingModule: Process name: " + lpparam.processName);
        
            // Hook into the specific game package
            if (lpparam.packageName.equals("com.com2usholdings.soulstrike.android.google.global.normal")) {

                XposedBridge.log("MyFloatingModule: *** TARGET GAME DETECTED ***");
                XposedBridge.log("MyFloatingModule: Hooking into Soul Strike game: " + lpparam.packageName);
                XposedBridge.log("MyFloatingModule: ClassLoader: " + lpparam.classLoader);

                // IMMEDIATE floating window start - don't wait for conditions
                XposedBridge.log("MyFloatingModule: Starting floating window immediately");
                FloatingWindowManager.startFloatingWindow(lpparam.classLoader);

                // Try multiple approaches to get context and start floating window
                try {
                    hookUnityActivity(lpparam);
                } catch (Exception e) {
                    XposedBridge.log("MyFloatingModule: Error hooking Unity activity: " + e.getMessage());
                }

                // Multiple fallback attempts with different delays
                new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        XposedBridge.log("MyFloatingModule: 2 second fallback - trying to start floating window");
                        FloatingWindowManager.startFloatingWindow(lpparam.classLoader);
                    }
                }, 2000);

                new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        XposedBridge.log("MyFloatingModule: 5 second fallback - trying to start floating window");
                        FloatingWindowManager.startFloatingWindow(lpparam.classLoader);
                    }
                }, 5000);

                new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        XposedBridge.log("MyFloatingModule: 10 second fallback - trying to start floating window");
                        FloatingWindowManager.startFloatingWindow(lpparam.classLoader);
                    }
                }, 10000);
            }
        // Also hook into system processes for broader compatibility
        else if (lpparam.packageName.equals("com.android.systemui") || 
                 lpparam.packageName.equals("com.android.launcher3") ||
                 lpparam.packageName.equals("android")) {
            
            XposedBridge.log("MyFloatingModule: Hooking into system process: " + lpparam.packageName);
            
            // Start floating window service
            FloatingWindowManager.startFloatingWindow(lpparam.classLoader);
        }
        else {
            XposedBridge.log("MyFloatingModule: Ignoring package: " + lpparam.packageName);
        }
    }
    
    private void hookUnityActivity(XC_LoadPackage.LoadPackageParam lpparam) {
        // Hook Unity IL2CPP runtime for game memory manipulation
        try {
            hookUnityIL2CPP(lpparam);
        } catch (Exception e) {
            XposedBridge.log("MyFloatingModule: IL2CPP hook failed: " + e.getMessage());
        }
        
        // Try multiple approaches to get the activity context
        try {
            // Method 1: Hook Activity.onCreate for any activity in the game
            Class<?> activityClass = XposedHelpers.findClass("android.app.Activity", lpparam.classLoader);
            XposedHelpers.findAndHookMethod(activityClass, "onCreate", android.os.Bundle.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    android.app.Activity activity = (android.app.Activity) param.thisObject;
                    String activityName = activity.getClass().getName();
                    XposedBridge.log("MyFloatingModule: Activity created: " + activityName);
                    
                    // Check if it's the main game activity
                    if (activityName.contains("Unity") || 
                        activityName.contains("Main") || 
                        activityName.contains("Game") ||
                        activityName.contains("SoulStrike")) {
                        XposedBridge.log("MyFloatingModule: Main game activity detected: " + activityName);
                        FloatingWindowManager.startFloatingWindowWithContext(activity);
                    }
                }
            });
        } catch (Exception e) {
            XposedBridge.log("MyFloatingModule: Activity hook failed: " + e.getMessage());
        }
        
        // Method 2: Try to hook Application.onCreate
        try {
            Class<?> applicationClass = XposedHelpers.findClass("android.app.Application", lpparam.classLoader);
            XposedHelpers.findAndHookMethod(applicationClass, "onCreate", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    android.app.Application app = (android.app.Application) param.thisObject;
                    XposedBridge.log("MyFloatingModule: Application onCreate detected");
                    // Use application context as fallback
                    FloatingWindowManager.startFloatingWindowWithApplicationContext(app);
                }
            });
        } catch (Exception e) {
            XposedBridge.log("MyFloatingModule: Application hook failed: " + e.getMessage());
        }
        
        // Method 3: Try to get context from ActivityThread
        try {
            Class<?> activityThreadClass = XposedHelpers.findClass("android.app.ActivityThread", lpparam.classLoader);
            XposedHelpers.findAndHookMethod(activityThreadClass, "performLaunchActivity", 
                XposedHelpers.findClass("android.app.ActivityThread$ActivityClientRecord", lpparam.classLoader),
                android.content.Intent.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    XposedBridge.log("MyFloatingModule: Activity launch detected");
                    // Try to get the activity from the record
                    Object record = param.args[0];
                    if (record != null) {
                        try {
                            Object activity = XposedHelpers.getObjectField(record, "activity");
                            if (activity instanceof android.app.Activity) {
                                XposedBridge.log("MyFloatingModule: Got activity from launch record");
                                FloatingWindowManager.startFloatingWindowWithContext((android.app.Activity) activity);
                            }
                        } catch (Exception e) {
                            XposedBridge.log("MyFloatingModule: Error getting activity from record: " + e.getMessage());
                        }
                    }
                }
            });
        } catch (Exception e) {
            XposedBridge.log("MyFloatingModule: ActivityThread hook failed: " + e.getMessage());
        }
    }
    
    private void hookUnityIL2CPP(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedBridge.log("MyFloatingModule: Attempting to hook Unity IL2CPP runtime");
        
        try {
            // Hook IL2CPP runtime directly
            hookIL2CPPRuntimeDirect(lpparam);
        } catch (Exception e) {
            XposedBridge.log("MyFloatingModule: Direct IL2CPP hook failed: " + e.getMessage());
        }
        
        try {
            // Initialize comprehensive game hooks
            GameHookManager.initializeGameHooks(lpparam);
        } catch (Exception e) {
            XposedBridge.log("MyFloatingModule: Game hook manager failed: " + e.getMessage());
        }
        
        try {
            // Hook Unity's IL2CPP runtime functions for memory manipulation
            hookUnityMemoryFunctions(lpparam);
        } catch (Exception e) {
            XposedBridge.log("MyFloatingModule: Unity memory hook failed: " + e.getMessage());
        }
        
        try {
            // Hook Unity's MonoBehaviour methods for game logic manipulation
            hookUnityGameLogic(lpparam);
        } catch (Exception e) {
            XposedBridge.log("MyFloatingModule: Unity game logic hook failed: " + e.getMessage());
        }
    }
    
    private void hookIL2CPPRuntimeDirect(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            XposedBridge.log("MyFloatingModule: Hooking IL2CPP runtime directly");
            
            // Hook IL2CPP runtime functions
            String[] il2cppClasses = {
                "il2cpp.IL2CPP",
                "il2cpp.Il2CppObject",
                "il2cpp.Il2CppClass",
                "il2cpp.Il2CppMethod",
                "il2cpp.Il2CppType"
            };
            
            for (String className : il2cppClasses) {
                try {
                    XposedBridge.log("MyFloatingModule: Attempting to find IL2CPP class: " + className);
                    Class<?> il2cppClass = XposedHelpers.findClass(className, lpparam.classLoader);
                    if (il2cppClass != null) {
                        XposedBridge.log("MyFloatingModule: ✓ Found IL2CPP class: " + className);
                    } else {
                        XposedBridge.log("MyFloatingModule: ✗ IL2CPP class not found: " + className);
                    }
                } catch (Exception e) {
                    XposedBridge.log("MyFloatingModule: ✗ Error finding IL2CPP class " + className + ": " + e.getMessage());
                }
            }
            
            // Hook Unity's native IL2CPP functions
            try {
                Class<?> unityPlayerClass = XposedHelpers.findClass("com.unity3d.player.UnityPlayer", lpparam.classLoader);
                if (unityPlayerClass != null) {
                    XposedBridge.log("MyFloatingModule: Found UnityPlayer, hooking IL2CPP methods");
                    
                    // Hook Unity's native methods
                    XposedHelpers.findAndHookMethod(unityPlayerClass, "UnitySendMessage", 
                        String.class, String.class, String.class, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            String gameObject = (String) param.args[0];
                            String method = (String) param.args[1];
                            String message = (String) param.args[2];
                            
                            XposedBridge.log("MyFloatingModule: Unity IL2CPP Call - " + gameObject + "." + method + "(" + message + ")");
                            
                            // Look for game data modifications
                            if (method.contains("Data_") || method.contains("Mission") || method.contains("Player")) {
                                XposedBridge.log("MyFloatingModule: *** GAME DATA DETECTED *** - " + method);
                            }
                        }
                    });
                }
            } catch (Exception e) {
                XposedBridge.log("MyFloatingModule: Unity IL2CPP hook failed: " + e.getMessage());
            }
            
        } catch (Exception e) {
            XposedBridge.log("MyFloatingModule: Direct IL2CPP runtime hook failed: " + e.getMessage());
        }
    }
    
    private void hookUnityMemoryFunctions(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            // Hook Unity's memory allocation functions
            Class<?> unityClass = XposedHelpers.findClass("com.unity3d.player.UnityPlayer", lpparam.classLoader);
            if (unityClass != null) {
                XposedBridge.log("MyFloatingModule: Found UnityPlayer class, hooking memory functions");
                
                // Hook Unity's native memory functions
                XposedHelpers.findAndHookMethod(unityClass, "UnitySendMessage", 
                    String.class, String.class, String.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        String gameObject = (String) param.args[0];
                        String method = (String) param.args[1];
                        String message = (String) param.args[2];
                        
                        XposedBridge.log("MyFloatingModule: Unity message intercepted - GameObject: " + gameObject + 
                                       ", Method: " + method + ", Message: " + message);
                        
                        // Intercept and modify game messages
                        if (method.contains("SetHealth") || method.contains("SetCoins") || method.contains("SetScore")) {
                            XposedBridge.log("MyFloatingModule: Game value modification detected: " + method);
                            // Here we can modify the message to change game values
                        }
                    }
                });
            }
        } catch (Exception e) {
            XposedBridge.log("MyFloatingModule: Unity memory hook error: " + e.getMessage());
        }
    }
    
    private void hookUnityGameLogic(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            // Hook common Unity game classes for value manipulation
            String[] gameClasses = {
                "PlayerController",
                "GameManager", 
                "PlayerStats",
                "CurrencyManager",
                "HealthManager",
                "ScoreManager"
            };
            
            for (String className : gameClasses) {
                try {
                    Class<?> gameClass = XposedHelpers.findClass(className, lpparam.classLoader);
                    if (gameClass != null) {
                        XposedBridge.log("MyFloatingModule: Found game class: " + className);
                        hookGameClassMethods(gameClass, className);
                    }
                } catch (Exception e) {
                    // Class not found, continue with next
                }
            }
        } catch (Exception e) {
            XposedBridge.log("MyFloatingModule: Game logic hook error: " + e.getMessage());
        }
    }
    
    private void hookGameClassMethods(Class<?> gameClass, String className) {
        try {
            // Hook common game methods
            String[] methods = {"Update", "Start", "Awake", "OnEnable", "OnDisable"};
            
            for (String methodName : methods) {
                try {
                    XposedHelpers.findAndHookMethod(gameClass, methodName, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            XposedBridge.log("MyFloatingModule: Game method called: " + className + "." + methodName);
                        }
                    });
                } catch (Exception e) {
                    // Method not found, continue
                }
            }
            
            // Hook value modification methods
            String[] valueMethods = {"SetHealth", "SetCoins", "SetScore", "AddCoins", "TakeDamage", "Heal"};
            
            for (String methodName : valueMethods) {
                try {
                    XposedHelpers.findAndHookMethod(gameClass, methodName, int.class, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            int originalValue = (Integer) param.args[0];
                            XposedBridge.log("MyFloatingModule: " + className + "." + methodName + " called with value: " + originalValue);
                            
                            // Modify the value for game hacking
                            if (methodName.contains("Health") || methodName.contains("Heal")) {
                                param.args[0] = 999999; // Max health
                                XposedBridge.log("MyFloatingModule: Modified health to 999999");
                            } else if (methodName.contains("Coin") || methodName.contains("Currency")) {
                                param.args[0] = 999999; // Max coins
                                XposedBridge.log("MyFloatingModule: Modified coins to 999999");
                            }
                        }
                    });
                } catch (Exception e) {
                    // Method not found, continue
                }
            }
            
        } catch (Exception e) {
            XposedBridge.log("MyFloatingModule: Error hooking " + className + ": " + e.getMessage());
        }
    }
}
