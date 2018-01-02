package com.dubiel.sample.googlebookviewerrx.data;

public class BookListItem {
    public String id;
    public String selfLink;
    public VolumeInfo volumeInfo;

    public BookListItem() {
    }

    public BookListItem(String id, String selfLink, VolumeInfo volumeInfo) {
        this.id = id;
        this.selfLink = selfLink;
        this.volumeInfo = volumeInfo;
    }

    public String getId() {
        return id;
    }

    public String getSelfLink() {
        return selfLink;
    }

    public VolumeInfo getVolumeInfo() {
        return volumeInfo;
    }
}