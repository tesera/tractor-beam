package com.tesera.tractorbeam.callbacks;

public interface OnConfigParsed {
    public void onSuccess();

    public void onError(Exception e);
}
