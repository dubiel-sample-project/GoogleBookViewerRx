package com.dubiel.sample.googlebookviewerrx.data;


public class VolumeInfo {
    public String title;
    public ImageLinks imageLinks;
    public String[] authors;
    public String description;
    public String infoLink;

    public VolumeInfo() {
    }

    public VolumeInfo(String title) {
        this.title = title;
    }

    public VolumeInfo(String title, ImageLinks imageLinks) {
        this.title = title;
        this.imageLinks = imageLinks;
    }

    public VolumeInfo(String title, ImageLinks imageLinks, String[] authors) {
        this.title = title;
        this.imageLinks = imageLinks;
        this.authors = authors;
    }

    public VolumeInfo(String title, ImageLinks imageLinks, String[] authors, String description) {
        this.title = title;
        this.imageLinks = imageLinks;
        this.authors = authors;
        this.description = description;
    }

    public VolumeInfo(String title, ImageLinks imageLinks, String[] authors, String description, String infoLink) {
        this.title = title;
        this.imageLinks = imageLinks;
        this.authors = authors;
        this.description = description;
        this.infoLink = infoLink;
    }

    public String getTitle() {
        return title;
    }

    public ImageLinks getImageLinks() {
        return imageLinks;
    }

    public String[] getAuthors() {
        return authors;
    }

    public String getDescription() {
        return description;
    }

    public String getInfoLink() {
        return infoLink;
    }
}
