package com.acantho.remedy;

import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.graphics.Color;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // TEST INFALLIBILE: Sfondo Verde e Messaggio
        LinearLayout layout = new LinearLayout(this);
        layout.setBackgroundColor(Color.GREEN); 
        
        WebView web = new WebView(this);
        web.setWebViewClient(new WebViewClient());
        web.getSettings().setJavaScriptEnabled(true);
        
        layout.addView(web);
        setContentView(layout);
        
        Toast.makeText(this, "SE VEDI VERDE, L'APP È NUOVA", Toast.LENGTH_LONG).show();
        web.loadUrl("https://acantho.onbmc.com");
    }
}
