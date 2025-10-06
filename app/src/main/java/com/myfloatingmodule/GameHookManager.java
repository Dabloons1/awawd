package com.myfloatingmodule;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class GameHookManager {
    
    private static boolean godModeEnabled = false;
    private static boolean unlimitedCoinsEnabled = false;
    private static boolean speedHackEnabled = false;
    
    public static void initializeGameHooks(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedBridge.log("MyFloatingModule: Initializing game hooks for Soul Strike");
        
        try {
            // Hook Unity's native functions
            hookUnityNativeFunctions(lpparam);
            
            // Hook common game patterns
            hookGamePatterns(lpparam);
            
            // Hook IL2CPP runtime
            hookIL2CPPRuntime(lpparam);
            
        } catch (Exception e) {
            XposedBridge.log("MyFloatingModule: Game hook initialization failed: " + e.getMessage());
        }
    }
    
    private static void hookUnityNativeFunctions(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            // Hook Unity's JNI functions for native code manipulation
            Class<?> unityPlayerClass = XposedHelpers.findClass("com.unity3d.player.UnityPlayer", lpparam.classLoader);
            if (unityPlayerClass != null) {
                XposedBridge.log("MyFloatingModule: Hooking Unity native functions");
                
                // Hook Unity's native method calls
                XposedHelpers.findAndHookMethod(unityPlayerClass, "UnitySendMessage", 
                    String.class, String.class, String.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        String gameObject = (String) param.args[0];
                        String method = (String) param.args[1];
                        String message = (String) param.args[2];
                        
                        // Log all Unity messages for analysis
                        XposedBridge.log("MyFloatingModule: Unity Message - " + gameObject + "." + method + "(" + message + ")");
                        
                        // Intercept and modify game values
                        if (method.contains("Health") || method.contains("HP")) {
                            if (godModeEnabled) {
                                param.args[2] = "999999";
                                XposedBridge.log("MyFloatingModule: God mode - Health set to 999999");
                            }
                        } else if (method.contains("Coin") || method.contains("Currency") || method.contains("Gold")) {
                            if (unlimitedCoinsEnabled) {
                                param.args[2] = "999999";
                                XposedBridge.log("MyFloatingModule: Unlimited coins - Currency set to 999999");
                            }
                        } else if (method.contains("Speed") || method.contains("Move")) {
                            if (speedHackEnabled) {
                                param.args[2] = "10.0";
                                XposedBridge.log("MyFloatingModule: Speed hack - Speed set to 10.0");
                            }
                        }
                    }
                });
            }
        } catch (Exception e) {
            XposedBridge.log("MyFloatingModule: Unity native hook failed: " + e.getMessage());
        }
    }
    
    private static void hookGamePatterns(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            // Hook actual Soul Strike game classes found in the dump
            String[] targetClasses = {
                "Data_Mission",              // Mission data (confirmed to exist)
                "PlayerStats",               // Player statistics
                "PlayerData",                // Player data management
                "Data_Companions_PlayerPower", // Player power data
                "Charac_PlayerStat",         // Character player stats
                "BT_Player__Manager",        // Player behavior tree manager
                "Alch_Currency",             // Currency system
                "Currency_Info_Popup",       // Currency UI
                "EventManager",              // Event system
                "RuntimeManager",            // Runtime management
                "LocalSave__Manager",        // Save system
                "Reddot__Manager"            // Notification system
            };
            
            for (String className : targetClasses) {
                try {
                    XposedBridge.log("MyFloatingModule: Attempting to find class: " + className);
                    Class<?> gameClass = XposedHelpers.findClass(className, lpparam.classLoader);
                    if (gameClass != null) {
                        XposedBridge.log("MyFloatingModule: ✓ Found game class: " + className);
                        hookGameClass(gameClass, className);
                    } else {
                        XposedBridge.log("MyFloatingModule: ✗ Class not found: " + className);
                    }
                } catch (Exception e) {
                    XposedBridge.log("MyFloatingModule: ✗ Error finding class " + className + ": " + e.getMessage());
                    // Continue with next class instead of crashing
                }
            }
        } catch (Exception e) {
            XposedBridge.log("MyFloatingModule: Game pattern hook failed: " + e.getMessage());
        }
    }
    
    private static void hookGameClass(Class<?> gameClass, String className) {
        try {
            // Hook common game methods
            String[] commonMethods = {
                "Update", "Start", "Awake", "OnEnable", "OnDisable",
                "FixedUpdate", "LateUpdate", "OnGUI"
            };
            
            for (String methodName : commonMethods) {
                try {
                    XposedHelpers.findAndHookMethod(gameClass, methodName, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            XposedBridge.log("MyFloatingModule: " + className + "." + methodName + " called");
                        }
                    });
                } catch (Exception e) {
                    // Method not found, continue
                }
            }
            
            // Hook value modification methods
            hookValueMethods(gameClass, className);
            
        } catch (Exception e) {
            XposedBridge.log("MyFloatingModule: Error hooking " + className + ": " + e.getMessage());
        }
    }
    
    private static void hookValueMethods(Class<?> gameClass, String className) {
        try {
            // Hook common Unity lifecycle methods first
            String[] lifecycleMethods = {"Update", "Start", "Awake", "OnEnable", "OnDisable", "FixedUpdate", "LateUpdate"};
            for (String methodName : lifecycleMethods) {
                try {
                    XposedHelpers.findAndHookMethod(gameClass, methodName, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            XposedBridge.log("MyFloatingModule: " + className + "." + methodName + " called");
                        }
                    });
                } catch (Exception e) {
                    // Method not found, continue
                }
            }
            
            // Hook all public methods with common parameter types
            try {
                java.lang.reflect.Method[] methods = gameClass.getDeclaredMethods();
                for (java.lang.reflect.Method method : methods) {
                    if (method.getParameterCount() == 1) {
                        Class<?> paramType = method.getParameterTypes()[0];
                        String methodName = method.getName();
                        
                        // Hook methods that might modify values
                        if (methodName.toLowerCase().contains("set") || 
                            methodName.toLowerCase().contains("add") || 
                            methodName.toLowerCase().contains("take") ||
                            methodName.toLowerCase().contains("damage") ||
                            methodName.toLowerCase().contains("heal") ||
                            methodName.toLowerCase().contains("coin") ||
                            methodName.toLowerCase().contains("currency") ||
                            methodName.toLowerCase().contains("speed") ||
                            methodName.toLowerCase().contains("health")) {
                            
                            try {
                                if (paramType == int.class) {
                                    XposedHelpers.findAndHookMethod(gameClass, methodName, int.class, new XC_MethodHook() {
                                        @Override
                                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                                            int originalValue = (Integer) param.args[0];
                                            XposedBridge.log("MyFloatingModule: " + className + "." + methodName + " called with: " + originalValue);
                                            
                                            // Apply hacks based on method name
                                            if (methodName.toLowerCase().contains("damage") || methodName.toLowerCase().contains("take")) {
                                                if (godModeEnabled) {
                                                    param.args[0] = 0; // No damage
                                                    XposedBridge.log("MyFloatingModule: God mode - Damage blocked");
                                                }
                                            } else if (methodName.toLowerCase().contains("heal") || methodName.toLowerCase().contains("health")) {
                                                if (godModeEnabled) {
                                                    param.args[0] = 999999; // Max health
                                                    XposedBridge.log("MyFloatingModule: God mode - Health set to max");
                                                }
                                            } else if (methodName.toLowerCase().contains("coin") || methodName.toLowerCase().contains("currency")) {
                                                if (unlimitedCoinsEnabled) {
                                                    if (methodName.toLowerCase().contains("add") || methodName.toLowerCase().contains("set")) {
                                                        param.args[0] = 999999; // Max coins
                                                        XposedBridge.log("MyFloatingModule: Unlimited coins - Currency set to max");
                                                    } else if (methodName.toLowerCase().contains("spend")) {
                                                        param.args[0] = 0; // No cost
                                                        XposedBridge.log("MyFloatingModule: Unlimited coins - Cost set to 0");
                                                    }
                                                }
                                            }
                                        }
                                    });
                                } else if (paramType == float.class) {
                                    XposedHelpers.findAndHookMethod(gameClass, methodName, float.class, new XC_MethodHook() {
                                        @Override
                                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                                            float originalValue = (Float) param.args[0];
                                            XposedBridge.log("MyFloatingModule: " + className + "." + methodName + " called with: " + originalValue);
                                            
                                            if (methodName.toLowerCase().contains("speed") && speedHackEnabled) {
                                                param.args[0] = 10.0f; // 10x speed
                                                XposedBridge.log("MyFloatingModule: Speed hack - Speed set to 10x");
                                            }
                                        }
                                    });
                                }
                            } catch (Exception e) {
                                // Method hook failed, continue
                            }
                        }
                    }
                }
            } catch (Exception e) {
                XposedBridge.log("MyFloatingModule: Error getting methods from " + className + ": " + e.getMessage());
            }
            
        } catch (Exception e) {
            XposedBridge.log("MyFloatingModule: Error hooking value methods in " + className + ": " + e.getMessage());
        }
    }
    
    private static void hookIL2CPPRuntime(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            // Hook IL2CPP runtime functions for low-level memory manipulation
            XposedBridge.log("MyFloatingModule: Attempting to hook IL2CPP runtime");
            
            // Hook common IL2CPP classes
            String[] il2cppClasses = {
                "Il2CppObject",
                "Il2CppClass", 
                "Il2CppMethod",
                "Il2CppType"
            };
            
            for (String className : il2cppClasses) {
                try {
                    Class<?> il2cppClass = XposedHelpers.findClass(className, lpparam.classLoader);
                    if (il2cppClass != null) {
                        XposedBridge.log("MyFloatingModule: Found IL2CPP class: " + className);
                    }
                } catch (Exception e) {
                    // Class not found, continue
                }
            }
            
        } catch (Exception e) {
            XposedBridge.log("MyFloatingModule: IL2CPP runtime hook failed: " + e.getMessage());
        }
    }
    
    // Public methods to control game hacks
    public static void setGodMode(boolean enabled) {
        godModeEnabled = enabled;
        XposedBridge.log("MyFloatingModule: God mode " + (enabled ? "enabled" : "disabled"));
    }
    
    public static void setUnlimitedCoins(boolean enabled) {
        unlimitedCoinsEnabled = enabled;
        XposedBridge.log("MyFloatingModule: Unlimited coins " + (enabled ? "enabled" : "disabled"));
    }
    
    public static void setSpeedHack(boolean enabled) {
        speedHackEnabled = enabled;
        XposedBridge.log("MyFloatingModule: Speed hack " + (enabled ? "enabled" : "disabled"));
    }
    
    public static boolean isGodModeEnabled() {
        return godModeEnabled;
    }
    
    public static boolean isUnlimitedCoinsEnabled() {
        return unlimitedCoinsEnabled;
    }
    
    public static boolean isSpeedHackEnabled() {
        return speedHackEnabled;
    }
}
