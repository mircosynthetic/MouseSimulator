package com.acantho.remedy;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.util.Base64;
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

    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable runnableCode;

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
        topBar.setPadding(15, 12, 15, 12);
        topBar.setGravity(Gravity.CENTER_VERTICAL);

        TextView titleView = new TextView(this);
        titleView.setText("ACANTHO REMEDY");
        titleView.setTextColor(Color.WHITE);
        titleView.setTextSize(15);
        titleView.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));

        Button credsButton = new Button(this);
        credsButton.setText("🔑 Credenziali");
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
        startContinuousInjection();
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
        s.setJavaScriptCanOpenWindowsAutomatically(true);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        s.setUserAgentString("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");

        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                injectMasterScript();
            }
        });

        webView.loadUrl("https://acantho.onbmc.com");
    }

    private void startContinuousInjection() {
        runnableCode = new Runnable() {
            @Override
            public void run() {
                injectMasterScript();
                handler.postDelayed(this, 1500); // Esegue la scansione ogni 1.5 secondi
            }
        };
        handler.post(runnableCode);
    }

    private void injectMasterScript() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String u = prefs.getString(KEY_USER, "");
        String p = prefs.getString(KEY_PASS, "");

        // Codifica in Base64 per evitare qualsiasi errore con caratteri speciali
        String uBase64 = Base64.encodeToString(u.getBytes(), Base64.NO_WRAP);
        String pBase64 = Base64.encodeToString(p.getBytes(), Base64.NO_WRAP);

        String js = "(function() {" +
                "  function getDocs() {" +
                "    var docs = [document];" +
                "    var iframes = document.querySelectorAll('iframe, frame');" +
                "    for(var i=0; i<iframes.length; i++) {" +
                "      try { if(iframes[i].contentDocument) docs.push(iframes[i].contentDocument); } catch(e){}" +
                "    }" +
                "    return docs;" +
                "  }" +
                // 1. FIX DOPPIO CLICK PER TICKET (Cerca anche negli iFrame!)
                "  try {" +
                "    getDocs().forEach(function(doc) {" +
                "      var elems = doc.querySelectorAll('*');" +
                "      for(var i=0; i<elems.length; i++) {" +
                "        var el = elems[i];" +
                "        if(el.getAttribute && el.getAttribute('ondblclick') && !el.getAttribute('data-tap-fixed')) {" +
                "          el.setAttribute('data-tap-fixed', 'true');" +
                "          el.addEventListener('click', function(evt) {" +
                "            var code = this.getAttribute('ondblclick');" +
                "            if(code) { eval(code); }" +
                "          }, false);" +
                "          el.style.borderLeft = '4px solid #00FF00';" +
                "        }" +
                "      }" +
                "    });" +
                "  } catch(e) {}" +

                // 2. AUTO-LOGIN PER BMC HELIX SSO
                "  try {" +
                "    var uVal = decodeURIComponent(escape(atob('" + uBase64 + "')));" +
                "    var pVal = decodeURIComponent(escape(atob('" + pBase64 + "')));" +
                "    if (uVal !== '') {" +
                "      getDocs().forEach(function(doc) {" +
                "        var userInput = doc.querySelector('input[name=\"username\"]') || doc.getElementById('username') || doc.querySelector('input[type=\"text\"]');" +
                "        var passInput = doc.querySelector('input[name=\"password\"]') || doc.getElementById('password') || doc.querySelector('input[type=\"password\"]');" +
                "        if(userInput && userInput.value !== uVal) {" +
                "          userInput.value = uVal;" +
                "          userInput.dispatchEvent(new Event('input', { bubbles: true }));" +
                "          userInput.dispatchEvent(new Event('change', { bubbles: true }));" +
                "          userInput.style.border = '2px solid #00FF00';" +
                "        }" +
                "        if(passInput && pVal !== '' && passInput.value !== pVal) {" +
                "          passInput.value = pVal;" +
                "          passInput.dispatchEvent(new Event('input', { bubbles: true }));" +
                "          passInput.dispatchEvent(new Event('change', { bubbles: true }));" +
                "          passInput.style.border = '2px solid #00FF00';" +
                "        }" +
                "      });" +
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
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null && runnableCode != null) {
            handler.removeCallbacks(runnableCode);
        }
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
