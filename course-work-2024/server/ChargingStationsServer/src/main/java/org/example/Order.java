package org.example;

import com.google.gson.annotations.SerializedName;

public class Order {

    private int id;
    @SerializedName("connector_id")
    private int connectorId;
    @SerializedName("user_id")
    private int userId;
    private float amount;
    private int status;
    private int progress;

    public Order() {}

    public Order(int id, int connectorId, int userId, float amount, int status, int progress) {
        this.id = id;
        this.connectorId = connectorId;
        this.userId = userId;
        this.amount = amount;
        this.status = status;
        this.progress = progress;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getConnectorId() {
        return connectorId;
    }

    public void setConnectorId(int connectorId) {
        this.connectorId = connectorId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }
}
