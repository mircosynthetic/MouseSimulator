package com.example.mousesimulator;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private boolean isRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnStart = findViewById(R.id.btnStart);
        Button btnStop = findViewById(R.id.btnStop);
        TextView tvStatus = findViewById(R.id.tvStatus);

        btnStart.setOnClickListener(v -> {
            if (!checkPermissions()) return;
            startService(new Intent(this, MouseOverlayService.class));
            isRunning = true;
            tvStatus.setText("✅ Mouse attivo");
            btnStart.setEnabled(false);
            btnStop.setEnabled(true);
            Toast.makeText(this, "Mouse avviato! Usa il trackpad.", Toast.LENGTH_LONG).show();
        });

        btnStop.setOnClickListener(v -> {
            stopService(new Intent(this, MouseOverlayService.class));
            isRunning = false;
            tvStatus.setText("Mouse fermo");
            btnStart.setEnabled(true);
            btnStop.setEnabled(false);
        });
    }

    private boolean checkPermissions() {
        // 1. Controllo permesso overlay
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && !Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "Concedi il permesso di overlay", Toast.LENGTH_LONG).show();
            startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName())));
            return false;
        }

        // 2. Controllo servizio accessibilità
        if (MouseAccessibilityService.getInstance() == null) {
            Toast.makeText(this, "Attiva il servizio di accessibilità!", Toast.LENGTH_LONG).show();
            startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
            return false;
        }

        return true;
    }
}
