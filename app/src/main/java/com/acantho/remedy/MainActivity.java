package com.acantho.remedy;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.http.SslError;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.webkit.CookieManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

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
        topBar.setBackgroundColor(Color.parseColor("#111827"));
        topBar.setPadding(12, 10, 12, 10);
        topBar.setGravity(Gravity.CENTER_VERTICAL);

        TextView titleView = new TextView(this);
        titleView.setText("ACANTHO");
        titleView.setTextColor(Color.WHITE);
        titleView.setTextSize(14);
        titleView.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));

        Button credsButton = new Button(this);
        credsButton.setText("🔑 LOGIN");
        credsButton.setBackgroundColor(Color.parseColor("#2563EB"));
        credsButton.setTextColor(Color.WHITE);
        credsButton.setOnClickListener(v -> showCredentialsDialog());

        Button fillButton = new Button(this);
        fillButton.setText("⚡ COMPILA");
        fillButton.setBackgroundColor(Color.parseColor("#10B981"));
        fillButton.setTextColor(Color.WHITE);
        fillButton.setOnClickListener(v -> injectFillOnce());

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
        s.setAllowFileAccess(true);
        s.setAllowContentAccess(true);
        s.setBuiltInZoomControls(true);
        s.setDisplayZoomControls(false);
        s.setUseWideViewPort(true);
        s.setLoadWithOverviewMode(true);
        s.setJavaScriptCanOpenWindowsAutomatically(true);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        s.setUserAgentString("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");

        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);

        webView.setWebChromeClient(new WebChromeClient());

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false; // Lascia che WebView gestisca i reindirizzamenti nativamente
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed();
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                CookieManager.getInstance().flush();
                
                // Compila i dati una volta sola dopo il caricamento della pagina
                injectFillOnce();
                
                // Attiva la conversione dei doppi click con bordo verde
                startDblClickFixLoop();
            }
        });

        webView.loadUrl("https://acantho.onbmc.com");
    }

    private void injectFillOnce() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String u = prefs.getString(KEY_USER, "").trim();
        String p = prefs.getString(KEY_PASS, "").trim();

        if (u.isEmpty()) return;

        String safeUser = JSONObject.quote(u);
        String safePass = JSONObject.quote(p);

        String js = "(function() {" +
                "  var userVal = " + safeUser + ";" +
                "  var passVal = " + safePass + ";" +
                "  function doFill(doc) {" +
                "    try {" +
                "      var uEl = doc.getElementById('username') || doc.querySelector('input[name=\"username\"]') || doc.querySelector('input[type=\"text\"]');" +
                "      var pEl = doc.getElementById('password') || doc.querySelector('input[name=\"password\"]') || doc.querySelector('input[type=\"password\"]');" +
                "      if (uEl) {" +
                "        uEl.value = userVal;" +
                "        uEl.dispatchEvent(new Event('input', { bubbles: true }));" +
                "        uEl.dispatchEvent(new Event('change', { bubbles: true }));" +
                "        uEl.style.border = '2px solid #10B981';" +
                "      }" +
                "      if (pEl && passVal !== '') {" +
                "        pEl.value = passVal;" +
                "        pEl.dispatchEvent(new Event('input', { bubbles: true }));" +
                "        pEl.dispatchEvent(new Event('change', { bubbles: true }));" +
                "        pEl.style.border = '2px solid #10B981';" +
                "      }" +
                "    } catch(e) {}" +
                "  }" +
                "  doFill(document);" +
                "  try {" +
                "    var frames = document.querySelectorAll('iframe, frame');" +
                "    for (var f = 0; f < frames.length; f++) {" +
                "      if (frames[f].contentDocument) doFill(frames[f].contentDocument);" +
                "    }" +
                "  } catch(e) {}" +
                "})();";

        if (webView != null) {
            webView.evaluateJavascript(js, null);
        }
    }

    private void startDblClickFixLoop() {
        // CONVERTITORE ORIGINALE ONDBLCLICK -> ONCLICK (Quello che funzionava con il bordo verde)
        String js = "(function() {" +
                "  if (window.__dblClickLoopStarted) return;" +
                "  window.__dblClickLoopStarted = true;" +
                "  var fix = function() {" +
                "    try {" +
                "      var docs = [document];" +
                "      var frames = document.querySelectorAll('iframe, frame');" +
                "      for (var f = 0; f < frames.length; f++) {" +
                "        try { if (frames[f].contentDocument) docs.push(frames[f].contentDocument); } catch(e){}" +
                "      }" +
                "      docs.forEach(function(doc) {" +
                "        doc.querySelectorAll('*').forEach(function(e) {" +
                "          if (e.getAttribute('ondblclick') && !e.getAttribute('data-fixed')) {" +
                "            e.setAttribute('data-fixed', 'true');" +
                "            e.onclick = function() { eval(this.getAttribute('ondblclick')); };" +
                "            e.style.borderLeft = '4px solid #00FF00';" +
                "          }" +
                "        });" +
                "      });" +
                "    } catch(err) {}" +
                "  };" +
                "  setInterval(fix, 1500);" +
                "  fix();" +
                "})();";

        if (webView != null) {
            webView.evaluateJavascript(js, null);
        }
    }

    private void showCredentialsDialog() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Salva Credenziali Remedy");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 30, 50, 10);

        final EditText uInput = new EditText(this);
        uInput.setHint("Nome utente / Username");
        uInput.setText(prefs.getString(KEY_USER, ""));
        layout.addView(uInput);

        final EditText pInput = new EditText(this);
        pInput.setHint("Password");
        pInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        pInput.setText(prefs.getString(KEY_PASS, ""));
        layout.addView(pInput);

        builder.setView(layout);

        builder.setPositiveButton("Salva e Accedi", (dialog, which) -> {
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
