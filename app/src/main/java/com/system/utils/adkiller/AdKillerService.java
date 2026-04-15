package com.system.utils.adkiller;

import android.accessibility.AccessibilityEvent;
import android.accessibility.AccessibilityNodeInfo;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.provider.Settings;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AdKillerService extends AccessibilityService {

    private static final String TAG = "AdKillerService";
    
    // Target apps to monitor (change these to your target apps)
    private static final String[] TARGET_APPS = {
        "com.example.adapp",
        "com.another.targetapp"
    };

    // Helper app package (a simple app to switch to - use Settings or any lightweight app)
    private static final String HELPER_APP = "com.android.settings";

    // Keywords for skip button
    private static final String[] SKIP_KEYWORDS = {
        "skip", "跳过", "skip ad", "skip advertisement",
        "close", "关闭", "x", "✕", "×", "×"
    };

    // Keywords that indicate an ad is playing
    private static final String[] AD_KEYWORDS = {
        "ad", "advertisement", "ads", "广告", "advertising"
    };

    private boolean isWatchingTargetApp = false;
    private boolean isAdPlaying = false;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Set<String> processedSkipButtons = new HashSet<>();

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "AdKillerService created");
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        String packageName = event.getPackageName() != null ? event.getPackageName().toString() : "";

        // Check if we're in a target app
        isWatchingTargetApp = isTargetApp(packageName);

        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            if (isWatchingTargetApp) {
                Log.i(TAG, "In target app: " + packageName);
                isAdPlaying = false;
                processedSkipButtons.clear();
                
                // Delay to let the page load, then check for ads
                handler.postDelayed(this::checkForAdAndSkip, 2000);
            }
        }

        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED && isWatchingTargetApp) {
            checkForSkipButton();
        }
    }

    private boolean isTargetApp(String packageName) {
        for (String app : TARGET_APPS) {
            if (app.equals(packageName)) return true;
        }
        return false;
    }

    private void checkForAdAndSkip() {
        if (!isWatchingTargetApp) return;
        
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        if (rootNode == null) return;

        // Look for skip button
        if (findAndClickSkipButton(rootNode)) {
            Log.i(TAG, "Ad skipped!");
            isAdPlaying = false;
        }

        // If ad is playing and we need to reset timer by switching apps
        if (isAdPlaying && shouldResetAdTimer()) {
            resetAdTimerBySwitching();
        }

        rootNode.recycle();
    }

    private void checkForSkipButton() {
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        if (rootNode == null) return;
        findAndClickSkipButton(rootNode);
        rootNode.recycle();
    }

    private boolean findAndClickSkipButton(AccessibilityNodeInfo node) {
        if (node == null) return false;

        CharSequence text = node.getText();
        CharSequence desc = node.getContentDescription();
        String nodeText = (text != null ? text.toString().toLowerCase() : "") + 
                          (desc != null ? desc.toString().toLowerCase() : "");

        // Check if this is a skip button
        for (String keyword : SKIP_KEYWORDS) {
            if (nodeText.contains(keyword)) {
                // Check if already processed
                String viewId = node.getViewIdResourceName();
                if (viewId != null && processedSkipButtons.contains(viewId)) {
                    return false;
                }

                if (node.isClickable()) {
                    Log.i(TAG, "Found skip button: " + nodeText);
                    processedSkipButtons.add(viewId != null ? viewId : "");
                    node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    return true;
                }
            }
        }

        // Check if this is an ad indicator
        for (String keyword : AD_KEYWORDS) {
            if (nodeText.contains(keyword)) {
                isAdPlaying = true;
            }
        }

        // Recursively check children
        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            if (child != null) {
                if (findAndClickSkipButton(child)) {
                    child.recycle();
                    return true;
                }
                child.recycle();
            }
        }

        return false;
    }

    private boolean shouldResetAdTimer() {
        // Check if there's a countdown timer visible
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        if (rootNode == null) return false;

        // Look for timer elements
        String pageText = getAllText(rootNode);
        rootNode.recycle();

        // If we see numbers like "5", "4", "3" that look like countdown timers
        return pageText.matches(".*[5-1]\\s*[\u79d2秒].*");
    }

    private String getAllText(AccessibilityNodeInfo node) {
        StringBuilder sb = new StringBuilder();
        if (node == null) return "";
        
        CharSequence text = node.getText();
        if (text != null) sb.append(text).append(" ");
        
        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            if (child != null) {
                sb.append(getAllText(child));
                child.recycle();
            }
        }
        return sb.toString();
    }

    private void resetAdTimerBySwitching() {
        Log.i(TAG, "Resetting ad timer by switching apps...");
        
        // Launch helper app
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Failed to launch helper app: " + e.getMessage());
        }

        // Come back after 1 second
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                goBackToTargetApp();
            }
        }, 1000);
    }

    private void goBackToTargetApp() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        for (String pkg : TARGET_APPS) {
            try {
                intent.setPackage(pkg);
                startActivity(intent);
                Log.i(TAG, "Returned to: " + pkg);
                break;
            } catch (Exception e) {
                // Try next package
            }
        }
    }

    @Override
    public void onInterrupt() {
        Log.i(TAG, "AdKillerService interrupted");
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.i(TAG, "AdKillerService connected");
        
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED | 
                          AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        info.notificationTimeout = 100;
        info.description = "System utility service";
        setServiceInfo(info);
    }
}
