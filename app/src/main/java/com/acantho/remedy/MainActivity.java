package com.acantho.remedy;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.util.Base64;
import android.view.Gravity;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingEncodingException;

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

        // BARRA SUPERIORE DEDICATA
        LinearLayout topBar = new LinearLayout(this);
        topBar.setOrientation(LinearLayout.HORIZONTAL);
        topBar.setBackgroundColor(Color.parseColor("#111827"));
        topBar.setPadding(16, 12, 16, 12);
        topBar.setGravity(Gravity.CENTER_VERTICAL);

        TextView titleView = new TextView(this);
        titleView.setText("ACANTHO REMEDY");
        titleView.setTextColor(Color.WHITE);
        titleView.setTextSize(15);
        titleView.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));

        Button credsButton = new Button(this);
        credsButton.setText("🔑 LOGIN");
        credsButton.setBackgroundColor(Color.parseColor("#2563EB"));
        credsButton.setTextColor(Color.WHITE);
        credsButton.setOnClickListener(v -> showCredentialsDialog());

        Button closeButton = new Button(this);
        closeButton.setText("❌");
        closeButton.setBackgroundColor(Color.parseColor("#DC2626"));
        closeButton.setTextColor(Color.WHITE);
        closeButton.setOnClickListener(v -> finish());

        topBar.addView(titleView);
        topBar.addView(credsButton);
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
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                CookieManager.getInstance().flush();
                injectCoreEngine();
            }
        });

        webView.loadUrl("https://acantho.onbmc.com");
    }

    private void injectCoreEngine() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String u = prefs.getString(KEY_USER, "");
        String p = prefs.getString(KEY_PASS, "");

        String uB64 = "";
        String pB64 = "";
        try {
            uB64 = Base64.encodeToString(u.getBytes("UTF-8"), Base64.NO_WRAP);
            pB64 = Base64.encodeToString(p.getBytes("UTF-8"), Base64.NO_WRAP);
        } catch (Exception e) {
            uB64 = "";
            pB64 = "";
        }

        String js = "(function() {" +
                "  var uVal = ''; var pVal = '';" +
                "  try { uVal = decodeURIComponent(escape(atob('" + uB64 + "'))); } catch(e){}" +
                "  try { pVal = decodeURIComponent(escape(atob('" + pB64 + "'))); } catch(e){}" +

                "  function getDocs() {" +
                "    var docs = [document];" +
                "    try {" +
                "      var iframes = document.querySelectorAll('iframe, frame');" +
                "      for(var i=0; i<iframes.length; i++) {" +
                "        try { if(iframes[i].contentDocument) docs.push(iframes[i].contentDocument); } catch(e){}" +
                "      }" +
                "    } catch(e){}" +
                "    return docs;" +
                "  }" +

                // 1. MOTORE UNIVERSALE SINGLE-TAP -> DOUBLE-CLICK (Sia inline che JS)
                "  function applyTapEngine(doc) {" +
                "    if (doc.__tapEngineActive) return;" +
                "    doc.__tapEngineActive = true;" +
                "    doc.addEventListener('click', function(e) {" +
                "      if (e.__isSyntheticDbl) return;" +
                "      var target = e.target;" +
                "      if (!target) return;" +
                "      try {" +
                "        var dblEvt = new MouseEvent('dblclick', {" +
                "          bubbles: true," +
                "          cancelable: true," +
                "          view: doc.defaultView || window" +
                "        });" +
                "        dblEvt.__isSyntheticDbl = true;" +
                "        target.dispatchEvent(dblEvt);" +
                "      } catch(err){}" +
                "      var curr = target;" +
                "      while (curr && curr !== doc && curr !== doc.body) {" +
                "        var ondbl = curr.getAttribute && curr.getAttribute('ondblclick');" +
                "        if (ondbl) {" +
                "          try { eval(ondbl); } catch(err){}" +
                "          break;" +
                "        }" +
                "        curr = curr.parentElement;" +
                "      }" +
                "    }, true);" +
                "  }" +

                // 2. MOTORE DI AUTOCOMPILAZIONE NATIVA (Bypassa Angular/React)
                "  function setNativeValue(el, val) {" +
                "    if (!el || !val) return;" +
                "    try {" +
                "      var proto = Object.getPrototypeOf(el);" +
                "      var setter = Object.getOwnPropertyDescriptor(proto, 'value').set;" +
                "      setter.call(el, val);" +
                "    } catch(e) {" +
                "      el.value = val;" +
                "    }" +
                "    try {" +
                "      el.dispatchEvent(new Event('input', { bubbles: true }));" +
                "      el.dispatchEvent(new Event('change', { bubbles: true }));" +
                "      el.dispatchEvent(new Event('blur', { bubbles: true }));" +
                "    } catch(e){}" +
                "  }" +

                "  function runCycle() {" +
                "    getDocs().forEach(function(doc) {" +
                "      applyTapEngine(doc);" +
                "      if (uVal !== '') {" +
                "        var passInput = doc.querySelector('input[type=\"password\"]');" +
                "        var userInput = doc.querySelector('input[name=\"username\"]') || " +
                "                        doc.getElementById('username') || " +
                "                        doc.querySelector('input[name*=\"user\"]') || " +
                "                        doc.querySelector('input[id*=\"user\"]') || " +
                "                        (passInput ? doc.querySelector('input:not([type=\"password\"]):not([type=\"hidden\"]):not([type=\"submit\"])') : null);" +
                "        if (userInput && userInput.value !== uVal) {" +
                "          setNativeValue(userInput, uVal);" +
                "          userInput.style.border = '2px solid #10B981';" +
                "        }" +
                "        if (passInput && pVal !== '' && passInput.value !== pVal) {" +
                "          setNativeValue(passInput, pVal);" +
                "          passInput.style.border = '2px solid #10B981';" +
                "        }" +
                "      }" +
                "    });" +
                "  }" +

                "  if (!window.__remedyInterval) {" +
                "    window.__remedyInterval = setInterval(runCycle, 600);" +
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
