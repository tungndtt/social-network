package com.example.tintok.DataLayer;

public abstract class AbstractDataRepository {
    boolean isReady = false;
    void setReady(){
        isReady = true;
    }
    void unsetReady(){
        isReady = false;
    }

    boolean isReady(){
        return isReady;
    }

    public abstract void initData();
}
