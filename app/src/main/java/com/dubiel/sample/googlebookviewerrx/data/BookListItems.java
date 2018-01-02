package com.dubiel.sample.googlebookviewerrx.data;

public class BookListItems {
    public BookListItem[] items;
    public int startIndex;

    public BookListItems(BookListItem[] items) {
        this.items = items;
    }

    public BookListItems(BookListItem[] items, int startIndex) {
        this.items = items;
        this.startIndex = startIndex;
    }

    public BookListItem[] getItems() {
        return items;
    }

    public int getStartIndex() {
        return startIndex;
    }
}
