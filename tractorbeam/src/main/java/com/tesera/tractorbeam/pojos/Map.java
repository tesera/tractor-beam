package com.tesera.tractorbeam.pojos;

import java.util.List;

public class Map {
    private String type;
    private String endpoint;
    private String geoJsonEndpoint;
    private String cacheMode;
    private List<Extents> extents;


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

    public List<Extents> getExtents() {
        return extents;
    }

    public void setExtents(List<Extents> extents) {
        this.extents = extents;
    }
}
