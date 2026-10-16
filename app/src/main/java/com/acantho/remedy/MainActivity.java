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
        fillButton.setOnClickListener(v -> injectFill());

        Button spaceButton = new Button(this);
        spaceButton.setText("⎵ SPAZIO");
        spaceButton.setBackgroundColor(Color.parseColor("#F59E0B"));
        spaceButton.setTextColor(Color.WHITE);
        spaceButton.setOnClickListener(v -> triggerSpacebar());

        Button closeButton = new Button(this);
        closeButton.setText("❌");
        closeButton.setBackgroundColor(Color.parseColor("#DC2626"));
        closeButton.setTextColor(Color.WHITE);
        closeButton.setOnClickListener(v -> finish());

        topBar.addView(titleView);
        topBar.addView(credsButton);
        topBar.addView(fillButton);
        topBar.addView(spaceButton);
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
                return false;
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed();
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                CookieManager.getInstance().flush();
                injectFill();
                initSpacebarAutomation();
            }
        });

        webView.loadUrl("https://acantho.onbmc.com");
    }

    private void injectFill() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String u = prefs.getString(KEY_USER, "").trim();
        String p = prefs.getString(KEY_PASS, "").trim();

        if (u.isEmpty()) return;

        String safeUser = JSONObject.quote(u);
        String safePass = JSONObject.quote(p);

        String js = "(function() {" +
                "  var userVal = " + safeUser + ";" +
                "  var passVal = " + safePass + ";" +
                "  function processDoc(doc) {" +
                "    try {" +
                "      var userInput = doc.getElementById('username') || doc.querySelector('input[name=\"username\"]') || doc.querySelector('input[name*=\"user\"]') || doc.querySelector('input[type=\"text\"]');" +
                "      var passInput = doc.getElementById('password') || doc.querySelector('input[name=\"password\"]') || doc.querySelector('input[type=\"password\"]');" +
                "      if (userInput && userInput.value !== userVal) {" +
                "        userInput.value = userVal;" +
                "        userInput.dispatchEvent(new Event('input', { bubbles: true }));" +
                "        userInput.dispatchEvent(new Event('change', { bubbles: true }));" +
                "        userInput.style.border = '2px solid #10B981';" +
                "      }" +
                "      if (passInput && passVal !== '' && passInput.value !== passVal) {" +
                "        passInput.value = passVal;" +
                "        passInput.dispatchEvent(new Event('input', { bubbles: true }));" +
                "        passInput.dispatchEvent(new Event('change', { bubbles: true }));" +
                "        passInput.style.border = '2px solid #10B981';" +
                "      }" +
                "    } catch(e) {}" +
                "  }" +
                "  processDoc(document);" +
                "  try {" +
                "    var frames = document.querySelectorAll('iframe, frame');" +
                "    for (var f = 0; f < frames.length; f++) {" +
                "      if (frames[f].contentDocument) processDoc(frames[f].contentDocument);" +
                "    }" +
                "  } catch(e) {}" +
                "})();";

        if (webView != null) {
            webView.evaluateJavascript(js, null);
        }
    }

    private void initSpacebarAutomation() {
        // AUTOMAZIONE: Invia la Barra Spaziatrice al tocco di qualsiasi riga/cella della tabella Remedy
        String js = "(function() {" +
                "  if (window.__spaceAutoInit) return;" +
                "  window.__spaceAutoInit = true;" +

                "  function sendSpace(targetDoc, el) {" +
                "    var target = el || targetDoc.activeElement || targetDoc.body;" +
                "    var down = new KeyboardEvent('keydown', { key: ' ', code: 'Space', keyCode: 32, which: 32, bubbles: true, cancelable: true });" +
                "    var up = new KeyboardEvent('keyup', { key: ' ', code: 'Space', keyCode: 32, which: 32, bubbles: true, cancelable: true });" +
                "    target.dispatchEvent(down);" +
                "    target.dispatchEvent(up);" +
                "  }" +

                "  function attachListener(doc) {" +
                "    if (doc.__spaceAttached) return;" +
                "    doc.__spaceAttached = true;" +
                "    doc.addEventListener('click', function(e) {" +
                "      var t = e.target;" +
                "      if (!t) return;" +
                "      var row = t.closest ? t.closest('tr, td, [role=\"row\"], [role=\"gridcell\"], .Table, .TableData, .selection') : null;" +
                "      if (row) {" +
                "        setTimeout(function() { sendSpace(doc, t); }, 200);" +
                "      }" +
                "    }, true);" +
                "  }" +

                "  attachListener(document);" +
                "  setInterval(function() {" +
                "    try {" +
                "      var frames = document.querySelectorAll('iframe, frame');" +
                "      for (var f = 0; f < frames.length; f++) {" +
                "        if (frames[f].contentDocument) attachListener(frames[f].contentDocument);" +
                "      }" +
                "    } catch(e) {}" +
                "  }, 1500);" +
                "})();";

        if (webView != null) {
            webView.evaluateJavascript(js, null);
        }
    }

    private void triggerSpacebar() {
        String js = "(function() {" +
                "  function fireSpace(doc) {" +
                "    var el = doc.activeElement || doc.body;" +
                "    var down = new KeyboardEvent('keydown', { key: ' ', code: 'Space', keyCode: 32, which: 32, bubbles: true, cancelable: true });" +
                "    var up = new KeyboardEvent('keyup', { key: ' ', code: 'Space', keyCode: 32, which: 32, bubbles: true, cancelable: true });" +
                "    el.dispatchEvent(down);" +
                "    el.dispatchEvent(up);" +
                "  }" +
                "  fireSpace(document);" +
                "  try {" +
                "    var frames = document.querySelectorAll('iframe, frame');" +
                "    for (var f = 0; f < frames.length; f++) {" +
                "      if (frames[f].contentDocument) fireSpace(frames[f].contentDocument);" +
                "    }" +
                "  } catch(e) {}" +
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
