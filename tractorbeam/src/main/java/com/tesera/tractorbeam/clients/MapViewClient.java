package com.tesera.tractorbeam.clients;


import android.net.Uri;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.tesera.andbtiles.Andbtiles;
import com.tesera.andbtiles.pojos.MapItem;
import com.tesera.andbtiles.utils.Consts;

import java.io.ByteArrayInputStream;
import java.util.List;

public class MapViewClient extends WebViewClient {

    private Andbtiles mAndbtiles;

    public MapViewClient(Andbtiles andbtiles) {
        mAndbtiles = andbtiles;
    }

    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
        Uri urlUri = Uri.parse(url);
        List<String> urlSegments = urlUri.getPathSegments();

        // intercept the geo json request
        if (url.endsWith(Consts.EXTENSION_GEO_JSON)) {
            String mapId = urlSegments.get(urlSegments.size() - 1).replace("." + Consts.EXTENSION_GEO_JSON, "");
            MapItem mapItem = mAndbtiles.getMapById(mapId);
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

        // try to find the tile in the database, otherwise use the web request
        byte[] tileBytes = mAndbtiles.getTile(mapId, Integer.valueOf(z), Integer.valueOf(x), Integer.valueOf(y));
        return new WebResourceResponse("image/png", "UTF-8", new ByteArrayInputStream(tileBytes));
    }
}
