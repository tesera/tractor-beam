package com.tesera.tractorbeam.parsers;

import android.content.Context;

import com.google.gson.Gson;
import com.tesera.andbtiles.Andbtiles;
import com.tesera.andbtiles.callbacks.AndbtilesCallback;
import com.tesera.andbtiles.exceptions.AndbtilesException;
import com.tesera.andbtiles.utils.Consts;
import com.tesera.tractorbeam.callbacks.OnConfigParsed;
import com.tesera.tractorbeam.pojos.ConfigJson;
import com.tesera.tractorbeam.pojos.Map;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ConfigParser {

    private int mapCounter;

    public void parseConfigJsonFile(final Context context, final Andbtiles andbtiles, final String url, final OnConfigParsed callback) {

        final String configUrl = url + com.tesera.tractorbeam.utils.Consts.CONFIG_FILE;
        new Thread(new Runnable() {
            @Override
            public void run() {
                InputStream inputStream;
                try {
                    URL configFileUrl = new URL(configUrl);
                    HttpURLConnection urlConnection = (HttpURLConnection) configFileUrl.openConnection();
                    inputStream = urlConnection.getInputStream();
                } catch (Exception e) {
                    callback.onError(e);
                    return;
                }

                final ConfigJson configJson;
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                try {
                    configJson = new Gson().fromJson(reader, ConfigJson.class);
                    inputStream.close();
                    reader.close();
                } catch (Exception e) {
                    callback.onError(e);
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
                                            callback.onSuccess();
                                    }

                                    @Override
                                    public void onError(Exception e) {
                                        callback.onError(e);
                                    }
                                });
                            else {
                                andbtiles.addRemoteJsonTileProvider(map.getEndpoint(), null, getCacheMode(map.getCacheMode()),
                                        map.getExtents().get(0).getBoundingBox(), map.getExtents().get(0).getMinZoom(), map.getExtents().get(0).getMaxZoom(), new AndbtilesCallback() {
                                    @Override
                                    public void onSuccess() {
                                        // this is an async task, only load when all maps are loaded
                                        mapCounter++;
                                        if (mapCounter == configJson.getMaps().size())
                                            callback.onSuccess();
                                    }

                                    @Override
                                    public void onError(Exception e) {
                                        callback.onError(e);
                                    }
                                });
                            }
                            break;
                        case "local":
                            try {
                                andbtiles.addLocalMbTilesProvider(map.getEndpoint(), map.getGeoJsonEndpoint());
                                // this is an async task, only load when all maps are loaded
                                mapCounter++;
                                if (mapCounter == configJson.getMaps().size())
                                    callback.onSuccess();
                            } catch (AndbtilesException e) {
                                callback.onError(e);
                            }
                            break;
                    }
                }
            }
        }).start();
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
