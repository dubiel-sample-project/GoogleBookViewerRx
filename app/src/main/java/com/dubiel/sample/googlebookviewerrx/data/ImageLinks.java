package com.dubiel.sample.googlebookviewerrx.data;

public class ImageLinks {
    public String smallThumbnail;
    public String small;

    public ImageLinks() {
    }

    public ImageLinks(String smallThumbnail) {
        this.smallThumbnail = smallThumbnail;
    }

    public ImageLinks(String smallThumbnail, String small) {
        this.smallThumbnail = smallThumbnail;
        this.small = small;
    }

    public String getSmallThumbnail() {
        return smallThumbnail;
    }

    public String getSmall() {
        return small;
    }
}
