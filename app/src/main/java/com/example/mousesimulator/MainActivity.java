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

        // Layout principale verticale
        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);

        // Barra Superiore (Toolbar)
        LinearLayout topBar = new LinearLayout(this);
        topBar.setOrientation(LinearLayout.HORIZONTAL);
        topBar.setBackgroundColor(Color.parseColor("#1F2937")); // Grigio scuro
        topBar.setPadding(16, 16, 16, 16);
        topBar.setGravity(Gravity.CENTER_VERTICAL);

        TextView titleView = new TextView(this);
        titleView.setText("Acantho Remedy");
        titleView.setTextColor(Color.WHITE);
        titleView.setTextSize(16);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
        titleView.setLayoutParams(titleParams);

        // Pulsante Gestione Credenziali
        Button credsButton = new Button(this);
        credsButton.setText("🔑 Credenziali");
        credsButton.setOnClickListener(v -> showCredentialsDialog());

        // Pulsante Chiudi App
        Button closeButton = new Button(this);
        closeButton.setText("❌ Chiudi");
        closeButton.setOnClickListener(v -> finish());

        topBar.addView(titleView);
        topBar.addView(credsButton);
        topBar.addView(closeButton);

        // WebView
        webView = new WebView(this);
        LinearLayout.LayoutParams webViewParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        webView.setLayoutParams(webViewParams);

        mainLayout.addView(topBar);
        mainLayout.addView(webView);

        setContentView(mainLayout);

        // Impostazioni WebView
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);

        // User-Agent Desktop
        String desktopUA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";
        settings.setUserAgentString(desktopUA);

        // Gestione Cookie persistenti
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.setAcceptThirdPartyCookies(webView, true);

        // WebViewClient con auto-compilazione e fix doppioclick
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                CookieManager.getInstance().flush();
                injectJavaScript();
            }
        });

        webView.loadUrl("https://acantho.onbmc.com");
    }

    private void injectJavaScript() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String savedUser = prefs.getString(KEY_USER, "");
        String savedPass = prefs.getString(KEY_PASS, "");

        String safeUser = savedUser.replace("'", "\\'");
        String safePass = savedPass.replace("'", "\\'");

        String js = "(function() {" +
                // 1. Convertitore da Doppio Click a Singolo Tap
                "  var fixDblClick = function() {" +
                "    document.querySelectorAll('*').forEach(function(e) {" +
                "      if(e.getAttribute('ondblclick') && !e.getAttribute('data-fixed')) {" +
                "        e.setAttribute('data-fixed', 'true');" +
                "        e.onclick = function() { eval(this.getAttribute('ondblclick')); };" +
                "        e.style.borderLeft = '4px solid #00FF00';" +
                "      }" +
                "    });" +
                "  };" +
                "  setInterval(fixDblClick, 1500);" +

                // 2. Autocompilazione Credenziali
                "  var fillCredentials = function() {" +
                "    var u = '" + safeUser + "';" +
                "    var p = '" + safePass + "';" +
                "    if (!u || !p) return;" +
                "    var passInput = document.querySelector('input[type=\"password\"]');" +
                "    if (passInput) {" +
                "      var inputs = document.querySelectorAll('input');" +
                "      var userInput = null;" +
                "      inputs.forEach(function(i) {" +
                "        if (i.type === 'text' || i.type === 'email' || !i.type) {" +
                "          if (i !== passInput && !userInput) userInput = i;" +
                "        }" +
                "      });" +
                "      if (userInput && passInput) {" +
                "        if (userInput.value !== u) {" +
                "          userInput.value = u;" +
                "          userInput.dispatchEvent(new Event('input', { bubbles: true }));" +
                "          userInput.dispatchEvent(new Event('change', { bubbles: true }));" +
                "        }" +
                "        if (passInput.value !== p) {" +
                "          passInput.value = p;" +
                "          passInput.dispatchEvent(new Event('input', { bubbles: true }));" +
                "          passInput.dispatchEvent(new Event('change', { bubbles: true }));" +
                "        }" +
                "      }" +
                "    }" +
                "  };" +
                "  setInterval(fillCredentials, 1000);" +
                "})();";

        webView.evaluateJavascript(js, null);
    }

    private void showCredentialsDialog() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String currentUsers = prefs.getString(KEY_USER, "");
        String currentPass = prefs.getString(KEY_PASS, "");

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Salva Credenziali BMC Remedy");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        final EditText userInput = new EditText(this);
        userInput.setHint("Nome Utente / Username");
        userInput.setText(currentUsers);
        layout.addView(userInput);

        final EditText passInput = new EditText(this);
        passInput.setHint("Password");
        passInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        passInput.setText(currentPass);
        layout.addView(passInput);

        builder.setView(layout);

        builder.setPositiveButton("Salva", (dialog, which) -> {
            String u = userInput.getText().toString().trim();
            String p = passInput.getText().toString().trim();

            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(KEY_USER, u);
            editor.putString(KEY_PASS, p);
            editor.apply();

            Toast.makeText(MainActivity.this, "Credenziali salvate!", Toast.LENGTH_SHORT).show();
            injectJavaScript();
        });

        builder.setNegativeButton("Annulla", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
