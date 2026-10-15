package com.example.mousesimulator;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

public class MouseOverlayService extends Service {

    private WindowManager windowManager;
    private View overlayView;
    private WindowManager.LayoutParams params;

    // Posizione corrente del "cursore" (centro schermo di default)
    private float cursorX = 540f;
    private float cursorY = 960f;

    // Sensibilità del trackpad (più alto = cursore più veloce)
    private final float SENSITIVITY = 3.0f;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        // Crea l'overlay
        overlayView = LayoutInflater.from(this).inflate(R.layout.overlay_layout, null);

        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );
        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 50;
        params.y = 200;

        windowManager.addView(overlayView, params);

        setupDragBar();
        setupTrackpad();
        setupButtons();
    }

    // === Trascinamento del pannello ===
    private void setupDragBar() {
        View dragBar = overlayView.findViewById(R.id.tvDragBar);
        dragBar.setOnTouchListener(new View.OnTouchListener() {
            private int initialX, initialY;
            private float initialTouchX, initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);
                        windowManager.updateViewLayout(overlayView, params);
                        return true;
                }
                return false;
            }
        });
    }

    // === Trackpad: muove il cursore virtuale ===
    private void setupTrackpad() {
        View trackpad = overlayView.findViewById(R.id.trackpad);
        trackpad.setOnTouchListener(new View.OnTouchListener() {
            private float lastX, lastY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        lastX = event.getRawX();
                        lastY = event.getRawY();
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        float deltaX = (event.getRawX() - lastX) * SENSITIVITY;
                        float deltaY = (event.getRawY() - lastY) * SENSITIVITY;
                        cursorX += deltaX;
                        cursorY += deltaY;

                        // Limita ai bordi dello schermo
                        int screenWidth = getResources().getDisplayMetrics().widthPixels;
                        int screenHeight = getResources().getDisplayMetrics().heightPixels;
                        cursorX = Math.max(0, Math.min(cursorX, screenWidth));
                        cursorY = Math.max(0, Math.min(cursorY, screenHeight));

                        lastX = event.getRawX();
                        lastY = event.getRawY();
                        return true;
                }
                return false;
            }
        });
    }

    // === Pulsanti Click ===
    private void setupButtons() {
        MouseAccessibilityService svc = MouseAccessibilityService.getInstance();

        Button btnLeft = overlayView.findViewById(R.id.btnLeftClick);
        btnLeft.setOnClickListener(v -> {
            if (svc != null) svc.performClick(cursorX, cursorY);
        });

        Button btnDouble = overlayView.findViewById(R.id.btnDoubleClick);
        btnDouble.setOnClickListener(v -> {
            if (svc != null) svc.performDoubleClick(cursorX, cursorY);
        });

        Button btnLong = overlayView.findViewById(R.id.btnLongClick);
        btnLong.setOnClickListener(v -> {
            if (svc != null) svc.performLongClick(cursorX, cursorY);
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (overlayView != null) {
            windowManager.removeView(overlayView);
        }
    }
}
