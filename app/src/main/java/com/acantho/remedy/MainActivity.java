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
import android.webkit.RenderProcessGoneDetail;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
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

        // Layout Principale
        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);

        // BARRA SUPERIORE
        LinearLayout topBar = new LinearLayout(this);
        topBar.setOrientation(LinearLayout.HORIZONTAL);
        topBar.setBackgroundColor(Color.parseColor("#111827"));
        topBar.setPadding(10, 8, 10, 8);
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
        fillButton.setOnClickListener(v -> injectScript());

        Button reloadButton = new Button(this);
        reloadButton.setText("🔄");
        reloadButton.setBackgroundColor(Color.parseColor("#4B5563"));
        reloadButton.setTextColor(Color.WHITE);
        reloadButton.setOnClickListener(v -> webView.reload());

        Button closeButton = new Button(this);
        closeButton.setText("❌");
        closeButton.setBackgroundColor(Color.parseColor("#DC2626"));
        closeButton.setTextColor(Color.WHITE);
        closeButton.setOnClickListener(v -> finish());

        topBar.addView(titleView);
        topBar.addView(credsButton);
        topBar.addView(fillButton);
        topBar.addView(reloadButton);
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
        s.setSupportMultipleWindows(false); // Mantiene tutto nella stessa finestra principale
        s.setJavaScriptCanOpenWindowsAutomatically(true);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        s.setUserAgentString("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");

        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onCloseWindow(WebView window) {
                // Impedisce la chiusura dell'app
            }
        });

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return false;
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed();
            }

            @Override
            public boolean onRenderProcessGone(WebView view, RenderProcessGoneDetail detail) {
                view.loadUrl("https://acantho.onbmc.com");
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                CookieManager.getInstance().flush();
                injectScript();
            }
        });

        webView.loadUrl("https://acantho.onbmc.com");
    }

    private void injectScript() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String u = prefs.getString(KEY_USER, "").trim();
        String p = prefs.getString(KEY_PASS, "").trim();

        String safeUser = JSONObject.quote(u);
        String safePass = JSONObject.quote(p);

        String js = "(function() {" +
                "  var userVal = " + safeUser + ";" +
                "  var passVal = " + safePass + ";" +

                // 1. FORZA I POPUP / NUOVE FINESTRE AD APRIRSI NELLA STESSA PAGINA (Evita lo schermo bianco dopo il login)
                "  if (!window.__windowOpenOverridden) {" +
                "    window.__windowOpenOverridden = true;" +
                "    window.open = function(url) {" +
                "      if (url) { window.location.href = url; }" +
                "      return window;" +
                "    };" +
                "  }" +

                // 2. AUTOCOMPILAZIONE NOME UTENTE E PASSWORD
                "  function setInputValue(el, val) {" +
                "    if (!el || !val) return;" +
                "    if (el.value === val && el.getAttribute('data-autofilled') === 'true') return;" +
                "    try {" +
                "      var setter = Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype, 'value').set;" +
                "      if (setter) { setter.call(el, val); } else { el.value = val; }" +
                "      el.setAttribute('data-autofilled', 'true');" +
                "      ['input', 'change', 'keydown', 'keyup', 'blur'].forEach(function(evt) {" +
                "        try { el.dispatchEvent(new Event(evt, { bubbles: true })); } catch(e){}" +
                "      });" +
                "      el.style.border = '2px solid #10B981';" +
                "    } catch(e) { el.value = val; }" +
                "  }" +

                "  function processDoc(doc) {" +
                "    try {" +
                "      if (userVal !== '') {" +
                "        var userInput = doc.querySelector('#username input') || " +
                "                        doc.querySelector('form#login_form #username input') || " +
                "                        doc.querySelector('form#login_form input[name=\"username\"]') || " +
                "                        doc.getElementById('username-id');" +

                "        var passInput = doc.querySelector('#password input') || " +
                "                        doc.querySelector('form#login_form #password input') || " +
                "                        doc.querySelector('form#login_form input[type=\"password\"]') || " +
                "                        doc.getElementById('pwd-id');" +

                "        if (userInput) setInputValue(userInput, userVal);" +
                "        if (passInput && passVal !== '') setInputValue(passInput, passVal);" +
                "      }" +

                // 3. AUTOMAZIONE BARRA SPAZIATRICE AL TAP SUI TICKET
                "      if (!doc.__spaceAttached) {" +
                "        doc.__spaceAttached = true;" +
                "        doc.addEventListener('click', function(e) {" +
                "          var t = e.target;" +
                "          if (!t) return;" +
                "          var row = t.closest ? t.closest('tr, td, [role=\"row\"], [role=\"gridcell\"], .Table, .TableData') : null;" +
                "          if (row) {" +
                "            setTimeout(function() {" +
                "              var target = doc.activeElement || t;" +
                "              var down = new KeyboardEvent('keydown', { key: ' ', code: 'Space', keyCode: 32, which: 32, bubbles: true, cancelable: true });" +
                "              var up = new KeyboardEvent('keyup', { key: ' ', code: 'Space', keyCode: 32, which: 32, bubbles: true, cancelable: true });" +
                "              target.dispatchEvent(down);" +
                "              target.dispatchEvent(up);" +
                "            }, 200);" +
                "          }" +
                "        }, true);" +
                "      }" +
                "    } catch(e) {}" +
                "  }" +

                "  function runCycle() {" +
                "    processDoc(document);" +
                "    try {" +
                "      var frames = document.querySelectorAll('iframe, frame');" +
                "      for (var f = 0; f < frames.length; f++) {" +
                "        if (frames[f].contentDocument) processDoc(frames[f].contentDocument);" +
                "      }" +
                "    } catch(e) {}" +
                "  }" +

                "  if (!window.__remedyLoop) {" +
                "    window.__remedyLoop = setInterval(runCycle, 600);" +
                "  }" +
                "  runCycle();" +
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
