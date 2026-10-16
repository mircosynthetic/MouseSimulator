package com.acantho.remedy;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private static final String PREFS_NAME = "RemedyPrefs";
    private static final String KEY_USER = "username";
    private static final String KEY_PASS = "password";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Forza un tema senza barra di sistema per evitare conflitti
        setTheme(androidx.appcompat.R.style.Theme_AppCompat_Light_NoActionBar);
        super.onCreate(savedInstanceState);

        // Contenitore principale (FrameLayout per sovrapporre elementi)
        FrameLayout rootLayout = new FrameLayout(this);
        rootLayout.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        // 1. WEBVIEW (Sotto tutto)
        webView = new WebView(this);
        webView.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        rootLayout.addView(webView);

        // 2. TASTO GALLEGGIANTE "LOGIN" (In basso a destra)
        Button loginFab = new Button(this);
        loginFab.setText("🔑");
        loginFab.setTextSize(24);
        loginFab.setBackgroundColor(Color.parseColor("#FFCC00")); // Giallo acceso
        loginFab.setTextColor(Color.BLACK);
        
        FrameLayout.LayoutParams fabParams = new FrameLayout.LayoutParams(150, 150);
        fabParams.gravity = Gravity.BOTTOM | Gravity.END;
        fabParams.setMargins(0, 0, 50, 200); // Spostato un po' su dal bordo basso
        loginFab.setLayoutParams(fabParams);
        
        loginFab.setOnClickListener(v -> showCredentialsDialog());
        rootLayout.addView(loginFab);

        // 3. TASTO CHIUDI (In alto a destra, piccolo)
        Button closeBtn = new Button(this);
        closeBtn.setText("X");
        closeBtn.setBackgroundColor(Color.RED);
        closeBtn.setTextColor(Color.WHITE);
        FrameLayout.LayoutParams closeParams = new FrameLayout.LayoutParams(100, 100);
        closeParams.gravity = Gravity.TOP | Gravity.END;
        closeParams.setMargins(0, 50, 50, 0);
        closeBtn.setLayoutParams(closeParams);
        closeBtn.setOnClickListener(v -> finish());
        rootLayout.addView(closeBtn);

        setContentView(rootLayout);

        setupWebView();
    }

    private void setupWebView() {
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
                injectScripts();
            }
        });

        webView.loadUrl("https://acantho.onbmc.com");
    }

    private void injectScripts() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String u = prefs.getString(KEY_USER, "").replace("'", "\\'");
        String p = prefs.getString(KEY_PASS, "").replace("'", "\\'");

        // Script specifico per i campi che si vedono nel tuo screenshot
        String js = "(function() {" +
                // FIX DOPPIO CLICK
                "  var fix = function() {" +
                "    document.querySelectorAll('*').forEach(function(e) {" +
                "      if(e.getAttribute('ondblclick') && !e.getAttribute('data-fixed')) {" +
                "        e.setAttribute('data-fixed', 'true');" +
                "        e.onclick = function() { eval(this.getAttribute('ondblclick')); };" +
                "        e.style.borderLeft = '3px solid #00FF00';" +
                "      }" +
                "    });" +
                "  };" +
                // AUTOFILL PER BMC HELIX SSO (Screenshot)
                "  var fill = function() {" +
                "    var uField = document.querySelector('input[name=\"username\"]') || document.getElementById('username');" +
                "    var pField = document.querySelector('input[name=\"password\"]') || document.getElementById('password');" +
                "    if(uField && pField && uField.value === '') {" +
                "      uField.value = '" + u + "';" +
                "      pField.value = '" + p + "';" +
                "      uField.dispatchEvent(new Event('input', { bubbles: true }));" +
                "      pField.dispatchEvent(new Event('input', { bubbles: true }));" +
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
        builder.setTitle("Configura Login");

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(60, 40, 60, 20);

        final EditText uIn = new EditText(this);
        uIn.setHint("Username");
        uIn.setText(prefs.getString(KEY_USER, ""));
        container.addView(uIn);

        final EditText pIn = new EditText(this);
        pIn.setHint("Password");
        pIn.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        pIn.setText(prefs.getString(KEY_PASS, ""));
        container.addView(pIn);

        builder.setView(container);
        builder.setPositiveButton("Salva", (d, w) -> {
            prefs.edit().putString(KEY_USER, uIn.getText().toString()).putString(KEY_PASS, pIn.getText().toString()).apply();
            webView.reload();
        });
        builder.show();
    }
}
