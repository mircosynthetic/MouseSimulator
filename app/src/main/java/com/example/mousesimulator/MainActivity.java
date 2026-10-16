package com.acantho.remedy;

import android.app.Activity;
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

public class MainActivity extends Activity {

    private WebView webView;
    private static final String PREFS_NAME = "RemedyPrefs";
    private static final String KEY_USER = "username";
    private static final String KEY_PASS = "password";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Layout Principale Verticale
        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);

        // BARRA SUPERIORE
        LinearLayout topBar = new LinearLayout(this);
        topBar.setOrientation(LinearLayout.HORIZONTAL);
        topBar.setBackgroundColor(Color.parseColor("#1F2937"));
        topBar.setPadding(15, 10, 15, 10);
        topBar.setGravity(Gravity.CENTER_VERTICAL);

        TextView titleView = new TextView(this);
        titleView.setText("ACANTHO");
        titleView.setTextColor(Color.WHITE);
        titleView.setTextSize(15);
        titleView.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));

        // Tasto Gestione Credenziali
        Button credsButton = new Button(this);
        credsButton.setText("🔑 Credenziali");
        credsButton.setBackgroundColor(Color.parseColor("#2563EB"));
        credsButton.setTextColor(Color.WHITE);
        credsButton.setOnClickListener(v -> showCredentialsDialog());

        // Tasto Compilazione Manuale/Forzata
        Button fillButton = new Button(this);
        fillButton.setText("⚡ Compila");
        fillButton.setBackgroundColor(Color.parseColor("#10B981"));
        fillButton.setTextColor(Color.WHITE);
        fillButton.setOnClickListener(v -> injectScript());

        // Tasto Chiudi App
        Button closeButton = new Button(this);
        closeButton.setText("❌");
        closeButton.setBackgroundColor(Color.parseColor("#DC2626"));
        closeButton.setTextColor(Color.WHITE);
        closeButton.setOnClickListener(v -> finish());

        topBar.addView(titleView);
        topBar.addView(credsButton);
        topBar.addView(fillButton);
        topBar.addView(closeButton);

        // WEBVIEW
        webView = new WebView(this);
        webView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 
                LinearLayout.LayoutParams.MATCH_PARENT));

        mainLayout.addView(topBar);
        mainLayout.addView(webView);
        setContentView(mainLayout);

        setupWebView();
    }

    private void setupWebView() {
        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setBuiltInZoomControls(true);
        s.setDisplayZoomControls(false);
        s.setUseWideViewPort(true);
        s.setLoadWithOverviewMode(true);
        s.setUserAgentString("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");

        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                injectScript();
            }
        });

        webView.loadUrl("https://acantho.onbmc.com");
    }

    private void injectScript() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String u = prefs.getString(KEY_USER, "").replace("'", "\\'").replace("\n", "");
        String p = prefs.getString(KEY_PASS, "").replace("'", "\\'").replace("\n", "");

        if (u.isEmpty() || p.isEmpty()) {
            Toast.makeText(this, "Salva prima le credenziali dal tasto 🔑!", Toast.LENGTH_SHORT).show();
            return;
        }

        String js = "(function() {" +
                // Funzione per forzare l'inserimento su framework Angular / React
                "  function setInputValue(element, value) {" +
                "    if (!element) return;" +
                "    element.focus();" +
                "    var lastValue = element.value;" +
                "    element.value = value;" +
                "    var event = new Event('input', { bubbles: true });" +
                "    var tracker = element._valueTracker;" +
                "    if (tracker) { tracker.setValue(lastValue); }" +
                "    element.dispatchEvent(event);" +
                "    element.dispatchEvent(new Event('change', { bubbles: true }));" +
                "    element.blur();" +
                "  }" +
                // Cerca ed inietta i campi
                "  var autoFill = function() {" +
                "    var uVal = '" + u + "';" +
                "    var pVal = '" + p + "';" +
                "    var passInput = document.querySelector('input[type=\"password\"]') || document.getElementById('password') || document.querySelector('input[name=\"password\"]');" +
                "    var userInput = document.getElementById('username') || document.querySelector('input[name=\"username\"]') || document.querySelector('input[placeholder*=\"utente\"]') || document.querySelector('input[type=\"text\"]');" +
                "    if (userInput && userInput.value !== uVal) {" +
                "      setInputValue(userInput, uVal);" +
                "      userInput.style.border = '2px solid #00FF00';" +
                "    }" +
                "    if (passInput && passInput.value !== pVal) {" +
                "      setInputValue(passInput, pVal);" +
                "      passInput.style.border = '2px solid #00FF00';" +
                "    }" +
                "  };" +
                // Convertitore Doppio Click -> Tap Singolo
                "  var fixClicks = function() {" +
                "    document.querySelectorAll('*').forEach(function(e) {" +
                "      if(e.getAttribute('ondblclick') && !e.getAttribute('data-fixed')) {" +
                "        e.setAttribute('data-fixed', 'true');" +
                "        e.onclick = function() { eval(this.getAttribute('ondblclick')); };" +
                "        e.style.borderLeft = '4px solid #00FF00';" +
                "      }" +
                "    });" +
                "  };" +
                "  autoFill();" +
                "  setInterval(autoFill, 1000);" +
                "  setInterval(fixClicks, 1500);" +
                "})();";

        webView.evaluateJavascript(js, null);
    }

    private void showCredentialsDialog() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Salva Credenziali Remedy");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 30, 50, 10);

        final EditText uInput = new EditText(this);
        uInput.setHint("Nome utente");
        uInput.setText(prefs.getString(KEY_USER, ""));
        layout.addView(uInput);

        final EditText pInput = new EditText(this);
        pInput.setHint("Password");
        pInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        pInput.setText(prefs.getString(KEY_PASS, ""));
        layout.addView(pInput);

        builder.setView(layout);

        builder.setPositiveButton("Salva e Ricarica", (dialog, which) -> {
            prefs.edit()
                .putString(KEY_USER, uInput.getText().toString().trim())
                .putString(KEY_PASS, pInput.getText().toString().trim())
                .apply();
            Toast.makeText(this, "Credenziali salvate!", Toast.LENGTH_SHORT).show();
            webView.reload();
        });

        builder.setNegativeButton("Annulla", null);
        builder.show();
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
