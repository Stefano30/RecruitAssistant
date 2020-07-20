package it.allos.dto;

import java.util.ArrayList;
import java.util.List;

public class Message {
    private String text;
    private List<String> options;

    public Message() {
        options = null;
    }

    public Message(String text) {
        options = null;
        this.text = text;
    }

    public Message(List<String> options) {
        this.options = options;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void addOption(String option) {
        if (options == null)
            options = new ArrayList<String>();
        options.add(option);
    }

    public List<String> getOptions() {
        return options;
    }

}