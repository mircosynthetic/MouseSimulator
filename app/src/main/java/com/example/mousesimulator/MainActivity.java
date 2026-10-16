package com.acantho.remedy;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private static final String PREFS_NAME = "RemedyPrefs";
    private static final String KEY_USER = "username";
    private static final String KEY_PASS = "password";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Layout principale
        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);

        // BARRA SUPERIORE
        LinearLayout topBar = new LinearLayout(this);
        topBar.setOrientation(LinearLayout.HORIZONTAL);
        topBar.setBackgroundColor(Color.parseColor("#1F2937"));
        topBar.setPadding(20, 15, 20, 15);
        topBar.setGravity(Gravity.CENTER_VERTICAL);

        TextView titleView = new TextView(this);
        titleView.setText("ACANTHO REMEDY");
        titleView.setTextColor(Color.WHITE);
        titleView.setTextSize(16);
        titleView.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));

        Button credsButton = new Button(this);
        credsButton.setText("🔑 LOGIN");
        credsButton.setBackgroundColor(Color.parseColor("#2563EB"));
        credsButton.setTextColor(Color.WHITE);
        credsButton.setOnClickListener(v -> showCredentialsDialog());

        topBar.addView(titleView);
        topBar.addView(credsButton);

        webView = new WebView(this);
        webView.setLayoutParams(new LinearLayout.LayoutParams(-1, -1));

        mainLayout.addView(topBar);
        mainLayout.addView(webView);
        setContentView(mainLayout);

        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setBuiltInZoomControls(true);
        s.setDisplayZoomControls(false);
        s.setUserAgentString("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                injectAutoLogin();
            }
        });

        webView.loadUrl("https://acantho.onbmc.com");
        Toast.makeText(this, "Versione 2.2 Avviata", Toast.LENGTH_SHORT).show();
    }

    private void injectAutoLogin() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String u = prefs.getString(KEY_USER, "").replace("'", "\\'");
        String p = prefs.getString(KEY_PASS, "").replace("'", "\\'");

        String js = "(function() {" +
                "  var fix = function() {" +
                "    document.querySelectorAll('*').forEach(function(e) {" +
                "      if(e.getAttribute('ondblclick') && !e.getAttribute('data-fixed')) {" +
                "        e.setAttribute('data-fixed', 'true');" +
                "        e.onclick = function() { eval(this.getAttribute('ondblclick')); };" +
                "        e.style.borderLeft = '4px solid #00FF00';" +
                "      }" +
                "    });" +
                "  };" +
                "  var fill = function() {" +
                "    var ui = document.querySelector('input[name=\"username\"]') || document.getElementById('username');" +
                "    var pi = document.querySelector('input[name=\"password\"]') || document.getElementById('password');" +
                "    if(ui && pi && ui.value === '' && '" + u + "' !== '') {" +
                "      ui.value = '" + u + "';" +
                "      pi.value = '" + p + "';" +
                "      ui.dispatchEvent(new Event('input', { bubbles: true }));" +
                "      pi.dispatchEvent(new Event('input', { bubbles: true }));" +
                "    }" +
                "  };" +
                "  setInterval(fix, 1500);" +
                "  setInterval(fill, 1000);" +
                "})();";
        webView.evaluateJavascript(js, null);
    }

    private void showCredentialsDialog() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Imposta Credenziali");
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);
        final EditText uIn = new EditText(this); uIn.setHint("Username");
        uIn.setText(prefs.getString(KEY_USER, ""));
        final EditText pIn = new EditText(this); pIn.setHint("Password");
        pIn.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        pIn.setText(prefs.getString(KEY_PASS, ""));
        layout.addView(uIn); layout.addView(pIn);
        builder.setView(layout);
        builder.setPositiveButton("Salva", (d, w) -> {
            prefs.edit().putString(KEY_USER, uIn.getText().toString()).putString(KEY_PASS, pIn.getText().toString()).apply();
            webView.reload();
        });
        builder.show();
    }
}
