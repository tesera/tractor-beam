package com.tesera.tractorbeam.pojos;

public class Map {
    private String type;
    private String endpoint;
    private String geoJsonEndpoint;
    private String cacheMode;
    private String boundingBox;
    private int minZoom;
    private int maxZoom;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getGeoJsonEndpoint() {
        return geoJsonEndpoint;
    }

    public void setGeoJsonEndpoint(String geoJsonEndpoint) {
        this.geoJsonEndpoint = geoJsonEndpoint;
    }

    public String getCacheMode() {
        return cacheMode;
    }

    public void setCacheMode(String cacheMode) {
        this.cacheMode = cacheMode;
    }

    public String getBoundingBox() {
        return boundingBox;
    }

    public void setBoundingBox(String boundingBox) {
        this.boundingBox = boundingBox;
    }

    public int getMinZoom() {
        return minZoom;
    }

    public void setMinZoom(int minZoom) {
        this.minZoom = minZoom;
    }

    public int getMaxZoom() {
        return maxZoom;
    }

    public void setMaxZoom(int maxZoom) {
        this.maxZoom = maxZoom;
    }
}
