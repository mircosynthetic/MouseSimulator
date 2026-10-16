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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);

        // BARRA SUPERIORE
        LinearLayout topBar = new LinearLayout(this);
        topBar.setOrientation(LinearLayout.HORIZONTAL);
        topBar.setBackgroundColor(Color.parseColor("#111827"));
        topBar.setPadding(20, 20, 20, 20);
        topBar.setGravity(Gravity.CENTER_VERTICAL);

        TextView title = new TextView(this);
        title.setText("ACANTHO");
        title.setTextColor(Color.WHITE);
        title.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1f));

        Button btnLogin = new Button(this);
        btnLogin.setText("🔑 LOGIN");
        btnLogin.setOnClickListener(v -> showLoginDialog());

        topBar.addView(title);
        topBar.addView(btnLogin);
        mainLayout.addView(topBar);

        webView = new WebView(this);
        webView.setLayoutParams(new LinearLayout.LayoutParams(-1, -1));
        mainLayout.addView(webView);
        setContentView(mainLayout);

        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
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
        SharedPreferences p = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String user = p.getString("u", "").replace("'", "\\'");
        String pass = p.getString("p", "").replace("'", "\\'");

        String js = "(function() {" +
                "  var fill = function() {" +
                "    var uField = document.querySelector('input[name=\"username\"]') || document.getElementById('username');" +
                "    var pField = document.querySelector('input[name=\"password\"]') || document.getElementById('password');" +
                "    if(uField && pField && uField.value === '' && '" + user + "' !== '') {" +
                "      uField.value = '" + user + "';" +
                "      pField.value = '" + pass + "';" +
                "      uField.dispatchEvent(new Event('input', {bubbles:true}));" +
                "      pField.dispatchEvent(new Event('input', {bubbles:true}));" +
                "    }" +
                "  };" +
                "  var fix = function() {" +
                "    document.querySelectorAll('*').forEach(e => {" +
                "      if(e.getAttribute('ondblclick') && !e.getAttribute('data-fix')) {" +
                "        e.setAttribute('data-fix', '1');" +
                "        e.onclick = function() { eval(this.getAttribute('ondblclick')); };" +
                "        e.style.borderLeft = '4px solid #00FF00';" +
                "      }" +
                "    });" +
                "  };" +
                "  setInterval(fill, 1000);" +
                "  setInterval(fix, 1500);" +
                "})();";
        webView.evaluateJavascript(js, null);
    }

    private void showLoginDialog() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle("Dati Accesso");
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        l.setPadding(40, 20, 40, 20);
        final EditText u = new EditText(this); u.setHint("User"); u.setText(prefs.getString("u", ""));
        final EditText p = new EditText(this); p.setHint("Pass"); p.setInputType(129); p.setText(prefs.getString("p", ""));
        l.addView(u); l.addView(p);
        b.setView(l);
        b.setPositiveButton("Salva", (d, w) -> {
            prefs.edit().putString("u", u.getText().toString()).putString("p", p.getText().toString()).apply();
            webView.reload();
        });
        b.show();
    }
}
