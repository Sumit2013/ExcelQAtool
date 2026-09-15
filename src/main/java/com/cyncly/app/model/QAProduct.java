package com.cyncly.app.model;

import java.util.List;

public class QAProduct {
    private String sku;
    private String type;
    private String subtype;
    private String description;
    private List<Checkpoint> checkpoints;

    public QAProduct(String sku, String type, String subtype, String description) {
        this.sku = sku;
        this.type = type;
        this.subtype = subtype;
        this.description = description;

    }

    public void setCheckpoints(List<Checkpoint> checkpoints) {
        this.checkpoints = checkpoints;
    }

    public List<Checkpoint> getCheckpoints() {
        return checkpoints;
    }

    public String getSku() {
        return sku;
    }

    public String getType() {
        return type;
    }

    public String getSubtype() {
        return subtype;
    }

    public String getDescription() {
        return description;
    }
}
