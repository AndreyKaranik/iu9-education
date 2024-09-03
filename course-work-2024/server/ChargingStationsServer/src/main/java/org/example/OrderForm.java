package org.example;

public class OrderForm {
    private String token;

    private String amount;
    private String charging_station_id;
    private String connector_id;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getCharging_station_id() {
        return charging_station_id;
    }

    public void setCharging_station_id(String charging_station_id) {
        this.charging_station_id = charging_station_id;
    }

    public String getConnector_id() {
        return connector_id;
    }

    public void setConnector_id(String connector_id) {
        this.connector_id = connector_id;
    }
}

