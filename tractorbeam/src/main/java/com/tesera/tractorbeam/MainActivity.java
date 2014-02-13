package com.tesera.tractorbeam;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
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

import com.google.gson.Gson;
import com.tesera.andbtiles.Andbtiles;
import com.tesera.andbtiles.callbacks.AndbtilesCallback;
import com.tesera.andbtiles.exceptions.AndbtilesException;
import com.tesera.andbtiles.utils.Consts;
import com.tesera.tractorbeam.pojos.ConfigJson;
import com.tesera.tractorbeam.pojos.Map;
import com.tesera.tractorbeam.utils.Utils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

public class MainActivity extends Activity {

    private int mapCounter;

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
                // the tile request should matches this format
                // http://b.tiles.mapbox.com/v3/tesera.CuraH20_Phase2/9/166/183.png
                if (!url.matches(".*/[0-9]+/[0-9]+/[0-9]+.png"))
                    return super.shouldInterceptRequest(view, url);

                // get the zoom, x and y tile coordinate from the url
                Uri urlUri = Uri.parse(url);
                List<String> urlSegments = urlUri.getPathSegments();
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
                    String url = mUrl.getText().toString();
                    if (!url.matches(Patterns.WEB_URL.pattern())) {
                        mUrl.setError(getString(R.string.error_url));
                        mUrl.requestFocus();
                        return;
                    }

                    // hide the keyboard
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(mUrl.getWindowToken(), 0);

                    // parse the tractor-beam-config.json
                    InputStream inputStream;
                    try {
                        inputStream = getAssets().open("tractor-beam-config.json");
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        mConfirm.setEnabled(true);
                        return;
                    }

                    final ConfigJson configJson;
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    try {
                        configJson = new Gson().fromJson(reader, ConfigJson.class);
                        inputStream.close();
                        reader.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        mConfirm.setEnabled(false);
                        return;
                    }

                    // add the maps into andbtiles
                    for (Map map : configJson.getMaps()) {
                        switch (map.getType()) {
                            case "internet":
                                if (map.getEndpoint().endsWith(Consts.EXTENSION_MBTILES))
                                    andbtiles.addRemoteMbilesProvider(map.getEndpoint(), new AndbtilesCallback() {
                                        @Override
                                        public void onSuccess() {
                                            // this is an async task, only load when all maps are loaded
                                            mapCounter++;
                                            if (mapCounter == configJson.getMaps().size())
                                                loadUrl(webView, configJson.getUrl());
                                        }

                                        @Override
                                        public void onError(Exception e) {
                                            mConfirm.setEnabled(false);
                                            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                else {
                                    andbtiles.addRemoteJsonTileProvider(map.getEndpoint(), null, getCacheMode(map.getCacheMode()), new AndbtilesCallback() {
                                        @Override
                                        public void onSuccess() {
                                            // this is an async task, only load when all maps are loaded
                                            mapCounter++;
                                            if (mapCounter == configJson.getMaps().size())
                                                loadUrl(webView, configJson.getUrl());
                                        }

                                        @Override
                                        public void onError(Exception e) {
                                            mConfirm.setEnabled(false);
                                            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                                break;
                            case "local":
                                try {
                                    andbtiles.addLocalMbTilesProvider(map.getEndpoint());
                                    // this is an async task, only load when all maps are loaded
                                    mapCounter++;
                                    if (mapCounter == configJson.getMaps().size())
                                        loadUrl(webView, configJson.getUrl());
                                } catch (AndbtilesException e) {
                                    mConfirm.setEnabled(false);
                                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                                break;
                        }
                    }
                    Utils.setStringToPrefs(MainActivity.this, Consts.EXTRA_JSON, url);
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

    private int getCacheMode(String cacheMode) {
        switch (cacheMode) {
            case "no-cache":
                return Consts.CACHE_NO;
            case "on-demand":
                return Consts.CACHE_ON_DEMAND;
            case "full":
                return Consts.CACHE_FULL;
            case "data-only":
                return Consts.CACHE_DATA;
            default:
                return Consts.CACHE_NO;
        }
    }
}
