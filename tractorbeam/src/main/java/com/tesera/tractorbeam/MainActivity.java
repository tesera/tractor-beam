package com.tesera.tractorbeam;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;

import com.tesera.andbtiles.Andbtiles;
import com.tesera.andbtiles.utils.Consts;
import com.tesera.tractorbeam.callbacks.OnConfigParsed;
import com.tesera.tractorbeam.clients.MapViewClient;
import com.tesera.tractorbeam.parsers.ConfigParser;
import com.tesera.tractorbeam.utils.Utils;

public class MainActivity extends Activity {
    private static final String TAG = "Tractor Beam WebView";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // initialize the andbtiles library
        final Andbtiles andbtiles = new Andbtiles(this);

        // enable setup the webview
        final WebView webView = new WebView(this);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAppCacheEnabled(true);
        webSettings.setAppCachePath(getCacheDir().getAbsolutePath());
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE); // disable simple cache index... we use appcache

        //enable chromium debugging for KitKat
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            WebView.setWebContentsDebuggingEnabled(true);

        webView.setWebViewClient(new MapViewClient(andbtiles));

        // if no URL is saved, ask the user to enter one
        String url = Utils.getStringFromPrefs(this, Consts.EXTRA_JSON);
        if (url == null) {
            setContentView(R.layout.activity_main);
            final EditText mUrl = (EditText) findViewById(R.id.edit_url);
            final Button mConfirm = (Button) findViewById(R.id.btn_confirmn);
            mConfirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final String url = mUrl.getText().toString();
                    if (!url.matches(Patterns.WEB_URL.pattern())) {
                        mUrl.setError(getString(R.string.error_url));
                        mUrl.requestFocus();
                        return;
                    }
                    // hide the keyboard
                    mConfirm.setEnabled(false);
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(mUrl.getWindowToken(), 0);

                    // create handler for UI update
                    final Handler handler = new Handler() {
                        @Override
                        public void handleMessage(Message msg) {
                            loadUrl(webView, url, true);
                            super.handleMessage(msg);
                        }
                    };

                    //save the url
                    Utils.setStringToPrefs(MainActivity.this, Consts.EXTRA_JSON, url);
                    // parse the config file
                    ConfigParser configParser = new ConfigParser();
                    configParser.parseConfigJsonFile(MainActivity.this, andbtiles, url, new OnConfigParsed() {
                        @Override
                        public void onSuccess() {
                            handler.sendEmptyMessage(0);
                        }

                        @Override
                        public void onError(Exception e) {
                            // no config file available, just load the url
                            e.printStackTrace();
                            handler.sendEmptyMessage(0);
                        }
                    });

                }
            });
            // load the saved URL
        } else
            loadUrl(webView, url, false);
    }

    private void loadUrl(WebView webView, String url, Boolean cache) {
        setContentView(webView);
        webView.loadUrl(url);
    }

    @Override
    protected void onStop() {
        super.onStop();
        this.finish();
    }
}
