package org.example.request;

public class ChargeRequest {
    private int connector_id;

    private float amount;
    private String token;

    public int getConnector_id() {
        return connector_id;
    }

    public void setConnector_id(int connector_id) {
        this.connector_id = connector_id;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
