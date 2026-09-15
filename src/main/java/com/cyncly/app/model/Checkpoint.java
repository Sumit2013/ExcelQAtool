package com.cyncly.app.model;

import java.util.List;

public class Checkpoint {
    private String name;
    private String comment;
    private List<String> values;
    private String selectedValue;

    public Checkpoint(String name, List<String> values) {
        this.name = name;
        this.values = values;
    }

    public void setSelectedValue(String selectedValue) {
        this.selectedValue = selectedValue;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getName() {
        return name;
    }

    public List<String> getValues() {
        return values;
    }

    public String getSelectedValue() {
        return selectedValue;
    }

    public String getComment() {
        return comment;
    }
}
