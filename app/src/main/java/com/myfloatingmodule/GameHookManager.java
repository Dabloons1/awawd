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
            // Hook class loading process to catch classes as they're loaded
            hookClassLoadingProcess(lpparam);
            
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
            
            // Try to find classes with delays to allow them to load
            for (int attempt = 0; attempt < 3; attempt++) {
                XposedBridge.log("MyFloatingModule: Class search attempt " + (attempt + 1) + "/3");
                
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
                
                // Wait before next attempt
                if (attempt < 2) {
                    try {
                        Thread.sleep(2000); // 2 second delay
                    } catch (InterruptedException e) {
                        // Continue
                    }
                }
            }
        } catch (Exception e) {
            XposedBridge.log("MyFloatingModule: Game pattern hook failed: " + e.getMessage());
        }
    }
    
    private static void hookClassLoadingProcess(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            // Hook ClassLoader.loadClass to catch classes as they're loaded
            Class<?> classLoaderClass = XposedHelpers.findClass("java.lang.ClassLoader", lpparam.classLoader);
            if (classLoaderClass != null) {
                XposedHelpers.findAndHookMethod(classLoaderClass, "loadClass", String.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        String className = (String) param.args[0];
                        
                        // Log interesting classes
                        if (className.contains("Data_") || 
                            className.contains("Player") || 
                            className.contains("Mission") ||
                            className.contains("Currency") ||
                            className.contains("Manager")) {
                            XposedBridge.log("MyFloatingModule: Class being loaded: " + className);
                        }
                    }
                });
            }
        } catch (Exception e) {
            XposedBridge.log("MyFloatingModule: Class loading hook failed: " + e.getMessage());
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
            
            // Hook Unity's IL2CPP runtime directly
            hookUnityIL2CPPClasses(lpparam);
            
            // Hook IL2CPP native functions
            hookIL2CPPNativeFunctions(lpparam);
            
        } catch (Exception e) {
            XposedBridge.log("MyFloatingModule: IL2CPP runtime hook failed: " + e.getMessage());
        }
    }
    
    private static void hookUnityIL2CPPClasses(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            // Hook Unity's IL2CPP classes directly
            String[] unityIL2CPPClasses = {
                "UnityEngine.Object",
                "UnityEngine.MonoBehaviour", 
                "UnityEngine.GameObject",
                "UnityEngine.Component",
                "UnityEngine.Transform",
                "UnityEngine.RectTransform",
                "UnityEngine.Canvas",
                "UnityEngine.UI.Button",
                "UnityEngine.UI.Text",
                "UnityEngine.UI.Image"
            };
            
            for (String className : unityIL2CPPClasses) {
                try {
                    XposedBridge.log("MyFloatingModule: Attempting to find Unity class: " + className);
                    Class<?> unityClass = XposedHelpers.findClass(className, lpparam.classLoader);
                    if (unityClass != null) {
                        XposedBridge.log("MyFloatingModule: ✓ Found Unity class: " + className);
                        hookUnityClassMethods(unityClass, className);
                    } else {
                        XposedBridge.log("MyFloatingModule: ✗ Unity class not found: " + className);
                    }
                } catch (Exception e) {
                    XposedBridge.log("MyFloatingModule: ✗ Error finding Unity class " + className + ": " + e.getMessage());
                }
            }
            
        } catch (Exception e) {
            XposedBridge.log("MyFloatingModule: Unity IL2CPP classes hook failed: " + e.getMessage());
        }
    }
    
    private static void hookUnityClassMethods(Class<?> unityClass, String className) {
        try {
            // Hook common Unity methods
            String[] unityMethods = {"Update", "Start", "Awake", "OnEnable", "OnDisable", "FixedUpdate", "LateUpdate"};
            
            for (String methodName : unityMethods) {
                try {
                    XposedHelpers.findAndHookMethod(unityClass, methodName, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            XposedBridge.log("MyFloatingModule: Unity method called: " + className + "." + methodName);
                        }
                    });
                } catch (Exception e) {
                    // Method not found, continue
                }
            }
            
        } catch (Exception e) {
            XposedBridge.log("MyFloatingModule: Error hooking Unity class methods: " + e.getMessage());
        }
    }
    
    private static void hookIL2CPPNativeFunctions(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            // Hook IL2CPP native functions for memory manipulation
            XposedBridge.log("MyFloatingModule: Attempting to hook IL2CPP native functions");
            
            // Try to hook IL2CPP runtime functions
            try {
                Class<?> il2cppRuntimeClass = XposedHelpers.findClass("il2cpp.IL2CPP", lpparam.classLoader);
                if (il2cppRuntimeClass != null) {
                    XposedBridge.log("MyFloatingModule: Found IL2CPP runtime class");
                }
            } catch (Exception e) {
                XposedBridge.log("MyFloatingModule: IL2CPP runtime class not found: " + e.getMessage());
            }
            
            // Hook Unity's native IL2CPP functions
            try {
                Class<?> unityPlayerClass = XposedHelpers.findClass("com.unity3d.player.UnityPlayer", lpparam.classLoader);
                if (unityPlayerClass != null) {
                    XposedBridge.log("MyFloatingModule: Found UnityPlayer class, hooking IL2CPP functions");
                    
                    // Hook Unity's IL2CPP method calls
                    XposedHelpers.findAndHookMethod(unityPlayerClass, "UnitySendMessage", 
                        String.class, String.class, String.class, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            String gameObject = (String) param.args[0];
                            String method = (String) param.args[1];
                            String message = (String) param.args[2];
                            
                            XposedBridge.log("MyFloatingModule: Unity IL2CPP Message - " + gameObject + "." + method + "(" + message + ")");
                            
                            // Try to intercept game data modifications
                            if (method.contains("Mission") || method.contains("Data") || method.contains("Player")) {
                                XposedBridge.log("MyFloatingModule: Game data modification detected: " + method);
                            }
                        }
                    });
                }
            } catch (Exception e) {
                XposedBridge.log("MyFloatingModule: Unity IL2CPP hook failed: " + e.getMessage());
            }
            
        } catch (Exception e) {
            XposedBridge.log("MyFloatingModule: IL2CPP native functions hook failed: " + e.getMessage());
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
