package com.example.mousesimulator;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.accessibility.AccessibilityEvent;

public class MouseAccessibilityService extends AccessibilityService {
    private static MouseAccessibilityService instance;
    public static MouseAccessibilityService getInstance() { return instance; }

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        instance = this;
    }
    @Override
    public void onDestroy() { instance = null; super.onDestroy(); }
    @Override
    public void onAccessibilityEvent(AccessibilityEvent e) {}
    @Override
    public void onInterrupt() {}

    public void performClick(float x, float y) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) return;
        Path p = new Path(); p.moveTo(x, y);
        dispatchGesture(new GestureDescription.Builder()
            .addStroke(new GestureDescription.StrokeDescription(p, 0, 100)).build(), null, null);
    }
    public void performDoubleClick(float x, float y) {
        performClick(x, y);
        new Handler(Looper.getMainLooper()).postDelayed(() -> performClick(x, y), 80);
    }
    public void performLongClick(float x, float y) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) return;
        Path p = new Path(); p.moveTo(x, y);
        dispatchGesture(new GestureDescription.Builder()
            .addStroke(new GestureDescription.StrokeDescription(p, 0, 600)).build(), null, null);
    }
}
