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
        mainLayout.setBackgroundColor(Color.BLACK);

        // Barra Superiore
        LinearLayout topBar = new LinearLayout(this);
        topBar.setOrientation(LinearLayout.HORIZONTAL);
        topBar.setBackgroundColor(Color.parseColor("#2C3E50"));
        topBar.setPadding(10, 10, 10, 10);
        topBar.setGravity(Gravity.CENTER_VERTICAL);

        TextView titleView = new TextView(this);
        titleView.setText("ACANTHO");
        titleView.setTextColor(Color.WHITE);
        titleView.setTextSize(14);
        titleView.setPadding(20, 0, 20, 0);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
        titleView.setLayoutParams(titleParams);

        // Tasto Credenziali (Blu)
        Button credsButton = new Button(this);
        credsButton.setText("🔑 LOGIN");
        credsButton.setBackgroundColor(Color.parseColor("#2980B9"));
        credsButton.setTextColor(Color.WHITE);
        credsButton.setOnClickListener(v -> showCredentialsDialog());

        // Tasto Chiudi
        Button closeButton = new Button(this);
        closeButton.setText("X");
        closeButton.setBackgroundColor(Color.parseColor("#C0392B"));
        closeButton.setTextColor(Color.WHITE);
        closeButton.setOnClickListener(v -> finish());

        topBar.addView(titleView);
        topBar.addView(credsButton);
        topBar.addView(closeButton);

        // WebView
        webView = new WebView(this);
        webView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        
        mainLayout.addView(topBar);
        mainLayout.addView(webView);
        setContentView(mainLayout);

        setupWebView();
    }

    private void setupWebView() {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        settings.setUserAgentString("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");

        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                injectScripts();
            }
        });

        webView.loadUrl("https://acantho.onbmc.com");
    }

    private void injectScripts() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String u = prefs.getString(KEY_USER, "").replace("'", "\\'");
        String p = prefs.getString(KEY_PASS, "").replace("'", "\\'");

        // Script che gestisce sia il doppioclick che l'autocompletamento avanzato
        String js = "(function() {" +
                // 1. FIX DOPPIO CLICK
                "  var fixClicks = function() {" +
                "    document.querySelectorAll('*').forEach(function(e) {" +
                "      if(e.getAttribute('ondblclick') && !e.getAttribute('data-fixed')) {" +
                "        e.setAttribute('data-fixed', 'true');" +
                "        e.onclick = function() { eval(this.getAttribute('ondblclick')); };" +
                "        e.style.borderLeft = '4px solid #00FF00';" +
                "      }" +
                "    });" +
                "  };" +
                // 2. AUTO-LOGIN REMEDY
                "  var fillLogin = function() {" +
                "    var userField = document.getElementById('username-id') || document.querySelector('input[name=\"username\"]') || document.querySelector('input[type=\"text\"]');" +
                "    var passField = document.getElementById('pwd-id') || document.querySelector('input[type=\"password\"]');" +
                "    if(userField && passField && '" + u + "' !== '') {" +
                "      if(userField.value === '') {" +
                "        userField.value = '" + u + "';" +
                "        passField.value = '" + p + "';" +
                "        userField.dispatchEvent(new Event('change'));" +
                "        passField.dispatchEvent(new Event('change'));" +
                "      }" +
                "    }" +
                "  };" +
                "  setInterval(fixClicks, 1500);" +
                "  setInterval(fillLogin, 1000);" +
                "})();";

        webView.evaluateJavascript(js, null);
    }

    private void showCredentialsDialog() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Imposta Login Automatico");

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(40, 20, 40, 20);

        final EditText userIn = new EditText(this);
        userIn.setHint("Username (es. mario.rossi)");
        userIn.setText(prefs.getString(KEY_USER, ""));
        
        final EditText passIn = new EditText(this);
        passIn.setHint("Password");
        passIn.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        passIn.setText(prefs.getString(KEY_PASS, ""));

        container.addView(userIn);
        container.addView(passIn);
        builder.setView(container);

        builder.setPositiveButton("Salva e Ricarica", (d, w) -> {
            prefs.edit()
                .putString(KEY_USER, userIn.getText().toString().trim())
                .putString(KEY_PASS, passIn.getText().toString().trim())
                .apply();
            webView.reload();
            Toast.makeText(this, "Credenziali salvate. Ricarico pagina...", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Annulla", null);
        builder.show();
    }
}
