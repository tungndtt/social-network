package com.example.tintok.Model;

import android.graphics.Bitmap;
import android.net.Uri;

/**
 *  This class represents a MediaEntity that can either hold an URI, URL or Bitmap
 */
public class MediaEntity {
    public Uri uri;
    public String url;
    public Bitmap bitmap;
    public MediaEntity(Uri uri, String url){
        this.uri = uri;
        this.url = url;
        this.bitmap = null;
    }

    public MediaEntity(Uri uri){
        this.uri = uri;
    }

    public MediaEntity(String url){
        this.url = url;
    }


    public MediaEntity(Bitmap bitmap){
        this.bitmap = bitmap;
        this.uri = null;
        this.url = null;
    }
}
