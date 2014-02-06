package com.tesera.tractorbeam;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
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

import com.tesera.tractorbeam.utils.Consts;
import com.tesera.tractorbeam.utils.TilesContract;
import com.tesera.tractorbeam.utils.Utils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.List;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final WebView webView = new WebView(this);
        // enable javascript and caching
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        webSettings.setAppCacheEnabled(true);
        webSettings.setAppCachePath(getCacheDir().getAbsolutePath());

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
                String mapName = urlSegments.get(urlSegments.size() - 4);

                // query the provider for a tile specified by x, y and z coordinates
                String[] projection = new String[]{TilesContract.COLUMN_TILE_DATA};
                String selection = TilesContract.COLUMN_ZOOM_LEVEL + " = ? AND "
                        + TilesContract.COLUMN_TILE_COLUMN + "= ? AND "
                        + TilesContract.COLUMN_TILE_ROW + "= ?";
                String[] selectionArgs = new String[]{z, x, String.valueOf((int) (Math.pow(2, Integer.valueOf(z)) - Integer.valueOf(y) - 1))};
                // .mbtiles database use TSM coordinates
                // we need to switch to Google compatible coordinates
                // https://gist.github.com/tmcw/4954720
                // query the content resolver for tiles
                Uri contentProviderUri = Uri.parse(TilesContract.CONTENT_URI + mapName + File.separator + TilesContract.TABLE_TILES);
                Cursor cursor = getContentResolver().query(contentProviderUri, projection, selection, selectionArgs, null);
                if (cursor == null || !cursor.moveToFirst())
                    return super.shouldInterceptRequest(view, url);

                // return web resource response as data from the content resolver
                byte[] tileBytes = cursor.getBlob(cursor.getColumnIndex(TilesContract.COLUMN_TILE_DATA));
                return new WebResourceResponse("image/png", "UTF-8", new ByteArrayInputStream(tileBytes));
            }

        });

        // if no URL is saved, ask the user to enter one
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

                    // save the url to preferences
                    Utils.setStringToPrefs(MainActivity.this, Consts.PREF_KEY_URL, url);
                    setContentView(webView);
                    webView.loadUrl(url);

                    // hide the keyboard
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(mUrl.getWindowToken(), 0);
                }
            });
            // load the saved URL
        } else {
            setContentView(webView);
            webView.loadUrl(url);
        }
    }
}
