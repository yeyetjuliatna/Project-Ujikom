package com.example.mymusik;

import android.net.Uri;

import java.io.File;

public class Musik extends File {

    private final Uri uri;
    private final String title;

    public Musik(Uri uri, String title) {
        super(uri.toString());
        this.uri = uri;
        this.title = title;
    }

    public Uri getUri() {
        return uri;
    }

    public String getTitle() {
        return title;
    }
}


