package com.iwinter.ppoint.models;

public class TextItemPair<T> {
    private String text;
    private T item;
    public TextItemPair(T item, String text) {
        this.text = text;
        this.item = item;
    }
    public String getText() {
        return text;
    }
    public T getItem() {
        return item;
    }
    @Override
    public String toString() {
        return getText();
    }
}
