package com.tesera.tractorbeam;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.tesera.andbtiles.Andbtiles;
import com.tesera.andbtiles.exceptions.AndbtilesException;
import com.tesera.andbtiles.pojos.MapItem;
import com.tesera.andbtiles.utils.Consts;
import com.tesera.tractorbeam.callbacks.OnConfigParsed;
import com.tesera.tractorbeam.utils.ConfigParser;
import com.tesera.tractorbeam.utils.Utils;

import java.io.ByteArrayInputStream;
import java.util.List;

public class MainActivity extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // initialize the andbtiles library
        final Andbtiles andbtiles = new Andbtiles(this);
        // enable setup the webview
        final WebView webView = new WebView(this);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        webSettings.setAppCacheEnabled(true);
        webSettings.setAppCachePath(getCacheDir().getAbsolutePath());
        // enable chromium debugging for KitKat
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            WebView.setWebContentsDebuggingEnabled(true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
                
                Uri urlUri = Uri.parse(url);
                List<String> urlSegments = urlUri.getPathSegments();

                // intercept the geo json request
                if (url.endsWith(Consts.EXTENSION_GEO_JSON)) {
                    String mapId = urlSegments.get(urlSegments.size() - 1).replace("." + Consts.EXTENSION_GEO_JSON, "");
                    MapItem mapItem = andbtiles.getMapById(mapId);
                    if (mapItem == null)
                        return null;
                    if (mapItem.getGeoJsonString() == null || mapItem.getGeoJsonString().length() == 0)
                        return null;
                    return new WebResourceResponse("text/plain", "UTF-8", new ByteArrayInputStream(mapItem.getGeoJsonString().getBytes()));
                }

                // the tile request should matches this format
                // http://b.tiles.mapbox.com/v3/<account>.<map_id>/<z>/<x>/<y>.png
                if (!url.matches(".*/[0-9]+/[0-9]+/[0-9]+.png"))
                    return super.shouldInterceptRequest(view, url);

                // get the zoom, x and y tile coordinate from the url
                String y = urlSegments.get(urlSegments.size() - 1).replace(".png", "");
                String x = urlSegments.get(urlSegments.size() - 2);
                String z = urlSegments.get(urlSegments.size() - 3);
                String mapId = urlSegments.get(urlSegments.size() - 4);

                try {
                    // try to find the tile in the database, otherwise use the web request
                    byte[] tileBytes = andbtiles.getTile(mapId, Integer.valueOf(z), Integer.valueOf(x), Integer.valueOf(y));
                    return tileBytes == null ? null : new WebResourceResponse("image/png", "UTF-8", new ByteArrayInputStream(tileBytes));
                } catch (AndbtilesException e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    return null;
                }
            }
        });

        // if no URL is saved, ask the user to enter one
        String url = Utils.getStringFromPrefs(this, Consts.EXTRA_JSON);
        if (url == null) {
            setContentView(R.layout.activity_main);
            final EditText mUrl = (EditText) findViewById(R.id.edit_url);
            final Button mConfirm = (Button) findViewById(R.id.btn_confirmn);
            mConfirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mConfirm.setEnabled(false);
                    final String url = mUrl.getText().toString();
                    if (!url.matches(Patterns.WEB_URL.pattern())) {
                        mUrl.setError(getString(R.string.error_url));
                        mUrl.requestFocus();
                        return;
                    }

                    // hide the keyboard
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(mUrl.getWindowToken(), 0);

                    // create handler for UI update
                    final Handler handler = new Handler() {
                        @Override
                        public void handleMessage(Message msg) {
                            loadUrl(webView, url);
                            super.handleMessage(msg);
                        }
                    };

                    // parse the config dile
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
            loadUrl(webView, url);
    }

    private void loadUrl(WebView webView, String url) {
        setContentView(webView);
        webView.loadUrl(url);
    }
}
