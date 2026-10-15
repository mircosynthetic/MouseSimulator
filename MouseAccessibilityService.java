package com.example.mousesimulator;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.os.Build;
import android.view.accessibility.AccessibilityEvent;

public class MouseAccessibilityService extends AccessibilityService {

    private static MouseAccessibilityService instance;

    public static MouseAccessibilityService getInstance() {
        return instance;
    }

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        instance = this;
    }

    @Override
    public void onDestroy() {
        instance = null;
        super.onDestroy();
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // Non ci serve intercettare eventi
    }

    @Override
    public void onInterrupt() {
    }

    // === CLICK SINGOLO ===
    public void performClick(float x, float y) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) return;

        Path path = new Path();
        path.moveTo(x, y);

        GestureDescription.Builder builder = new GestureDescription.Builder();
        builder.addStroke(new GestureDescription.StrokeDescription(path, 0, 100));

        dispatchGesture(builder.build(), null, null);
    }

    // === DOPPIO CLICK ===
    public void performDoubleClick(float x, float y) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) return;

        // Primo click
        performClick(x, y);

        // Secondo click dopo 80ms
        android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());
        handler.postDelayed(() -> performClick(x, y), 80);
    }

    // === CLICK LUNGO (Long Press) ===
    public void performLongClick(float x, float y) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) return;

        Path path = new Path();
        path.moveTo(x, y);

        GestureDescription.Builder builder = new GestureDescription.Builder();
        // Durata 600ms = long press
        builder.addStroke(new GestureDescription.StrokeDescription(path, 0, 600));

        dispatchGesture(builder.build(), null, null);
    }
}
