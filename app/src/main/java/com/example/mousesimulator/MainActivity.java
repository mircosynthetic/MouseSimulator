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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // --- TEST VISIVO ---
        Toast.makeText(this, "!!! VERSIONE 2.0 CARICATA !!!", Toast.LENGTH_LONG).show();

        // Layout Super Semplice
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.DKGRAY);

        // Barra con tasti giganti per non sbagliare
        LinearLayout bar = new LinearLayout(this);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setPadding(20, 20, 20, 20);
        bar.setGravity(Gravity.CENTER);

        Button btnLogin = new Button(this);
        btnLogin.setText("SALVA LOGIN");
        btnLogin.setBackgroundColor(Color.BLUE);
        btnLogin.setTextColor(Color.WHITE);
        btnLogin.setOnClickListener(v -> apriDialogo());

        Button btnReset = new Button(this);
        btnReset.setText("PULISCI");
        btnReset.setOnClickListener(v -> {
            getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit().clear().apply();
            webView.reload();
        });

        bar.addView(btnLogin);
        bar.addView(btnReset);
        root.addView(bar);

        webView = new WebView(this);
        webView.setLayoutParams(new LinearLayout.LayoutParams(-1, -1));
        root.addView(webView);

        setContentView(root);

        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setUserAgentString("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                // Inietta il trucco per compilare i campi
                SharedPreferences p = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                String user = p.getString("u", "");
                String pass = p.getString("p", "");
                
                String js = "javascript:(function() {" +
                    "var u='" + user + "'; var p='" + pass + "';" +
                    "setInterval(function() {" +
                    "  var ui = document.getElementById('username') || document.getElementsByName('username')[0];" +
                    "  var pi = document.getElementById('password') || document.getElementsByName('password')[0];" +
                    "  if(ui && pi && ui.value=='') {" +
                    "    ui.value=u; pi.value=p;" +
                    "    ui.dispatchEvent(new Event('input')); pi.dispatchEvent(new Event('input'));" +
                    "  }" +
                    "  document.querySelectorAll('*').forEach(e => {" +
                    "    if(e.getAttribute('ondblclick') && !e.onclick) {" +
                    "      e.onclick = function() { eval(this.getAttribute('ondblclick')); };" +
                    "      e.style.borderLeft = '5px solid red';" + // Bordo rosso per vedere cosa puoi cliccare
                    "    }" +
                    "  });" +
                    "}, 1000);})()";
                webView.loadUrl(js);
            }
        });

        webView.loadUrl("https://acantho.onbmc.com");
    }

    private void apriDialogo() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Inserisci Dati");
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        final EditText u = new EditText(this); u.setHint("User");
        final EditText p = new EditText(this); p.setHint("Pass");
        p.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        l.addView(u); l.addView(p);
        builder.setView(l);
        builder.setPositiveButton("SALVA", (d, w) -> {
            getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit()
                .putString("u", u.getText().toString())
                .putString("p", p.getText().toString()).apply();
            webView.reload();
        });
        builder.show();
    }
}
