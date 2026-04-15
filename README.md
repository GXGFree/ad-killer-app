# SystemUtils - Ad Blocker App

A stealth Android app that automatically skips ads in apps. Disguised as a "System Utility" to avoid detection by ad-funded apps.

## Features

- **Stealth Mode**: App name shows as "SystemUtils" to avoid detection
- **Auto-Skip Ads**: Automatically detects and clicks skip buttons
- **Timer Reset**: Switches to helper app and back to reset ad countdown timers
- **No Root Required**: Uses Android's Accessibility Service

## How to Build

### Method A: GitHub Codespaces (Recommended - No Installation Needed)

1. Go to: https://github.com/new
2. Create a new repository named "ad-killer-app"
3. Upload all files from this folder to the repository
4. Click "Code" → "Create codespace on main"
5. Wait for the environment to load
6. In the terminal, run:
   ```bash
   ./gradlew assembleDebug
   ```
7. Download the APK from: `app/build/outputs/apk/debug/app-debug.apk`

### Method B: Local Build

1. Install Android Studio
2. Open this folder as a project
3. Click "Build" → "Build Bundle(s) / APK(s)" → "Build APK"

## How to Use

1. Install the APK on your Android phone
2. Open the app (it will show as "SystemUtils")
3. Tap "Enable Service" button
4. Find "SystemUtils" in the accessibility settings list
5. Enable it
6. The app will now run in the background

## Configuration

Edit these constants in `AdKillerService.java` to customize:

```java
// Target apps to monitor
private static final String[] TARGET_APPS = {
    "com.example.adapp",      // Change to your target app
    "com.another.targetapp"
};

// Helper app to switch to (use a lightweight app)
private static final String HELPER_APP = "com.android.settings";
```

## How It Works

1. The app uses Android's **Accessibility Service** to monitor screen content
2. When you open a target app, it watches for ad windows
3. If a skip button appears, it automatically clicks it
4. If an ad has a countdown timer, it switches to another app and back to reset the timer

## Disclaimer

This app is for educational purposes. Using automation against app ToS may result in account restrictions. Use at your own risk.
