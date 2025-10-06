# My Floating Module

A simple LSposed module that demonstrates how to create a floating window overlay.

## Features

- Floating window overlay that can be displayed over other apps
- Xposed/LSposed integration
- Settings activity for configuration
- Foreground service to maintain the floating window
- Permission handling for overlay access

## Setup Instructions

1. **Install LSposed Framework**: Make sure you have LSposed installed on your device
2. **Build the Module**: Use Android Studio to build the APK
3. **Install the APK**: Install the generated APK on your device
4. **Enable in LSposed**: 
   - Open LSposed Manager
   - Go to Modules
   - Enable "My Floating Module"
   - Reboot your device
5. **Grant Permissions**: 
   - Open the app and grant overlay permission when prompted
   - You can also manually grant it in Settings > Apps > My Floating Module > Display over other apps

## Usage

1. **Manual Start**: Open the app and tap "Start Floating Window"
2. **Automatic Start**: The floating window will automatically appear when the module hooks into system processes
3. **Settings**: Use the Settings button to configure target packages and window position

## Configuration

- **Target Package**: Specify which app package to hook into (leave empty for all apps)
- **Auto Start**: Enable to automatically show floating window when the module loads
- **Window Position**: Set the initial X,Y coordinates for the floating window

## Technical Details

### Key Components

- `XposedInit.java`: Main Xposed hook entry point
- `FloatingWindowService.java`: Service that manages the floating window
- `FloatingWindowManager.java`: Helper class for starting floating windows
- `MainActivity.java`: Main UI for manual control
- `SettingsActivity.java`: Configuration interface

### Permissions Required

- `SYSTEM_ALERT_WINDOW`: For displaying overlay windows
- `FOREGROUND_SERVICE`: For maintaining the floating window service
- `WAKE_LOCK`: For keeping the service active

### Xposed Integration

The module hooks into system processes and can be configured to target specific apps. The floating window will appear when the hooked process loads.

## Building

1. Open the project in Android Studio
2. Sync Gradle files
3. Build the APK (Build > Build Bundle(s) / APK(s) > Build APK(s))
4. Install the generated APK

## Troubleshooting

- **Floating window not appearing**: Check if overlay permission is granted
- **Module not loading**: Ensure LSposed is properly installed and the module is enabled
- **Service crashes**: Check device logs for error messages

## License

This is a demonstration project. Use responsibly and in accordance with your device's terms of service.
