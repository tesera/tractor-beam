package com.tesera.tractorbeam;

import android.app.Activity;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;

import com.tesera.tractorbeam.utils.Consts;
import com.tesera.tractorbeam.utils.Utils;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final WebView webView = new WebView(this);
        // Enable Javascript
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient());

        String url = Utils.getStringFromPrefs(this, Consts.PREF_KEY_URL);
        if (url == null) {
            setContentView(R.layout.activity_main);
            final EditText mUrl = (EditText) findViewById(R.id.edit_url);

            Button mConfirm = (Button) findViewById(R.id.btn_confirmn);
            mConfirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String url = mUrl.getText().toString();
                    if (!url.matches(Patterns.WEB_URL.pattern())) {
                        mUrl.setError(getString(R.string.error_url));
                        mUrl.requestFocus();
                        return;
                    }

                    Utils.setStringToPrefs(MainActivity.this, Consts.PREF_KEY_URL, url);
                    setContentView(webView);
                    webView.loadUrl(url);
                }
            });
        } else {
            setContentView(webView);
            webView.loadUrl(url);
        }
    }

}
