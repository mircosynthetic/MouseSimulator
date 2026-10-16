package com.acantho.remedy;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
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

        // Layout principale verticale
        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 
                LinearLayout.LayoutParams.MATCH_PARENT));

        // BARRA SUPERIORE (Forzata altezza 60dp)
        float scale = getResources().getDisplayMetrics().density;
        int heightPixels = (int) (60 * scale + 0.5f);

        LinearLayout topBar = new LinearLayout(this);
        topBar.setOrientation(LinearLayout.HORIZONTAL);
        topBar.setBackgroundColor(Color.parseColor("#111827")); // Grigio quasi nero
        topBar.setGravity(Gravity.CENTER_VERTICAL);
        topBar.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, heightPixels));
        topBar.setPadding(20, 0, 20, 0);

        TextView titleView = new TextView(this);
        titleView.setText("ACANTHO");
        titleView.setTextColor(Color.WHITE);
        titleView.setTextSize(18);
        titleView.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));

        // Tasto LOGIN (Blu brillante)
        Button credsButton = new Button(this);
        credsButton.setText("IMPOSTA DATI");
        credsButton.setBackgroundColor(Color.parseColor("#2563EB"));
        credsButton.setTextColor(Color.WHITE);
        credsButton.setPadding(20, 0, 20, 0);
        credsButton.setOnClickListener(v -> showCredentialsDialog());

        // Tasto X (Rosso)
        Button closeButton = new Button(this);
        closeButton.setText("X");
        closeButton.setBackgroundColor(Color.parseColor("#DC2626"));
        closeButton.setTextColor(Color.WHITE);
        closeButton.setOnClickListener(v -> finish());

        topBar.addView(titleView);
        topBar.addView(credsButton);
        topBar.addView(closeButton);

        // WEBVIEW
        webView = new WebView(this);
        LinearLayout.LayoutParams webViewParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1.0f);
        webView.setLayoutParams(webViewParams);

        mainLayout.addView(topBar);
        mainLayout.addView(webView);
        setContentView(mainLayout);

        configureWebView();
    }

    private void configureWebView() {
        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setSupportZoom(true);
        s.setBuiltInZoomControls(true);
        s.setDisplayZoomControls(false);
        s.setUseWideViewPort(true);
        s.setLoadWithOverviewMode(true);
        
        // Forza User Agent Desktop per evitare layout mobili rotti
        s.setUserAgentString("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");

        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                injectAutoLogin();
            }
        });

        webView.loadUrl("https://acantho.onbmc.com");
    }

    private void injectAutoLogin() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String u = prefs.getString(KEY_USER, "").replace("'", "\\'");
        String p = prefs.getString(KEY_PASS, "").replace("'", "\\'");

        if (u.isEmpty()) return;

        String js = "(function() {" +
                // 1. FIX DOPPIO CLICK (con bordo verde per test)
                "  var fix = function() {" +
                "    document.querySelectorAll('*').forEach(function(e) {" +
                "      if(e.getAttribute('ondblclick') && !e.getAttribute('data-fixed')) {" +
                "        e.setAttribute('data-fixed', 'true');" +
                "        e.onclick = function() { eval(this.getAttribute('ondblclick')); };" +
                "        e.style.borderLeft = '3px solid #00FF00';" +
                "      }" +
                "    });" +
                "  };" +
                // 2. AUTOFILL SPECIFICO PER BMC HELIX SSO (Quello del tuo screenshot)
                "  var fillSSO = function() {" +
                "    var userField = document.getElementById('username') || document.querySelector('input[name=\"username\"]');" +
                "    var passField = document.getElementById('password') || document.querySelector('input[name=\"password\"]');" +
                "    if(userField && passField && userField.value === '') {" +
                "      userField.value = '" + u + "';" +
                "      passField.value = '" + p + "';" +
                "      userField.dispatchEvent(new Event('input', { bubbles: true }));" +
                "      passField.dispatchEvent(new Event('input', { bubbles: true }));" +
                "    }" +
                "  };" +
                "  setInterval(fix, 1500);" +
                "  setInterval(fillSSO, 1000);" +
                "})();";

        webView.evaluateJavascript(js, null);
    }

    private void showCredentialsDialog() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Impostazioni Login");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        final EditText userIn = new EditText(this);
        userIn.setHint("Nome Utente");
        userIn.setText(prefs.getString(KEY_USER, ""));
        layout.addView(userIn);

        final EditText passIn = new EditText(this);
        passIn.setHint("Password");
        passIn.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        passIn.setText(prefs.getString(KEY_PASS, ""));
        layout.addView(passIn);

        builder.setView(layout);
        builder.setPositiveButton("Salva e Accedi", (d, w) -> {
            prefs.edit()
                .putString(KEY_USER, userIn.getText().toString().trim())
                .putString(KEY_PASS, passIn.getText().toString().trim())
                .apply();
            webView.reload();
        });
        builder.setNegativeButton("Annulla", null);
        builder.show();
    }
}
