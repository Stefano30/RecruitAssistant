package it.allos.dto;

public class Message<T> {
    private T text;

    public Message() {}

    public Message(T text) {
        this.text = text;
    }

    public T getText() {
        return text;
    }

    public void setText(T text) {
        this.text = text;
    }

}