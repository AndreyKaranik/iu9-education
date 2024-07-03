package org.example;

import com.sun.net.httpserver.HttpExchange;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.ArrayList;

public class Utils {
    public static JSONArray getChargingMarksWithUserNameByChargingStationId(Connection connection, int chargingStationId, HttpExchange httpExchange) throws IOException {
        String sql = "SELECT * FROM charging_marks WHERE charging_station_id = " + chargingStationId;
        Statement stmt = null;
        ResultSet rs = null;

        JSONArray array = new JSONArray();
        try {
            stmt = connection.createStatement();
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                int id = rs.getInt("id");
                int markChargingStationId = rs.getInt("charging_station_id");
                int status = rs.getInt("status");
                int userId = rs.getInt("user_id");
                JSONObject object = new JSONObject();
                object.put("id", id);
                object.put("charging_station_id", markChargingStationId);
                object.put("status", status);
                if (userId != 0) {
                    object.put("user_id", userId);
                    object.put("user_name", Utils.getUserNameByUserId(connection, userId, httpExchange));
                }
                array.put(object);
            }
        } catch (SQLException e) {
            httpExchange.sendResponseHeaders(500, 0);
            OutputStream os = httpExchange.getResponseBody();
            os.flush();
            os.close();
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return array;
    }


    public static JSONArray getConnectorsByChargingStationId(Connection connection, int chargingStationId, HttpExchange httpExchange) throws IOException {
        String sql = "SELECT * FROM connectors WHERE charging_station_id = " + chargingStationId;
        Statement stmt = null;
        ResultSet rs = null;
        JSONArray array = new JSONArray();

        try {
            stmt = connection.createStatement();
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                int id = rs.getInt("id");
                int connectorChargingStationId = rs.getInt("charging_station_id");
                int status = rs.getInt("status");
                int chargingTypeId = rs.getInt("charging_type_id");
                double rate = rs.getDouble("rate");
                JSONObject o = new JSONObject();
                o.put("id", id);
                o.put("charging_station_id", connectorChargingStationId);
                o.put("status", status);
                o.put("rate", rate);
                o.put("charging_type", Utils.getChargingTypeByChargingTypeId(connection, chargingTypeId, httpExchange));

                array.put(o);
            }
        } catch (SQLException e) {
            httpExchange.sendResponseHeaders(500, 0);
            OutputStream os = httpExchange.getResponseBody();
            os.flush();
            os.close();
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return array;
    }

    public static JSONObject getChargingTypeByChargingTypeId(Connection connection, int chargingTypeId, HttpExchange httpExchange) throws IOException {
        String sql = "SELECT * FROM charging_types WHERE id = " + chargingTypeId;
        Statement stmt = null;
        ResultSet rs = null;

        JSONObject object = new JSONObject();
        try {
            stmt = connection.createStatement();
            rs = stmt.executeQuery(sql);
            rs.next();
            int charging_type_id = rs.getInt("id");
            String charging_type_name = rs.getString("name");
            String currentType = rs.getString("current_type");
            object.put("id", charging_type_id);
            object.put("name", charging_type_name);
            object.put("current_type", currentType);
        } catch (SQLException e) {
            httpExchange.sendResponseHeaders(500, 0);
            OutputStream os = httpExchange.getResponseBody();
            os.flush();
            os.close();
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return object;
    }

    public static JSONObject getChargingStationDetailsByChargingStationId(Connection connection, int chargingStationId, HttpExchange httpExchange) throws IOException {
        String sql = "SELECT * FROM charging_stations WHERE id = " + chargingStationId;
        Statement stmt = null;
        ResultSet rs = null;

        JSONObject object = new JSONObject();
        try {
            stmt = connection.createStatement();
            rs = stmt.executeQuery(sql);

            rs.next();
            int id = rs.getInt("id");
            String name = rs.getString("name");
            String address = rs.getString("address");
            double latitude = rs.getDouble("latitude");
            double longitude = rs.getDouble("longitude");
            int companyId = rs.getInt("company_id");
            String hours = rs.getString("opening_hours");
            String description = rs.getString("description");
            object.put("id", id);
            object.put("name", name);
            object.put("address", address);
            object.put("latitude", latitude);
            object.put("longitude", longitude);
            object.put("company_id", companyId);
            object.put("opening_hours", hours);
            object.put("description", description);
            object.put("connectors", Utils.getConnectorsByChargingStationId(connection, id, httpExchange));
            object.put("charging_marks", Utils.getChargingMarksWithUserNameByChargingStationId(connection, id, httpExchange));
        } catch (SQLException e) {
            httpExchange.sendResponseHeaders(500, 0);
            OutputStream os = httpExchange.getResponseBody();
            os.flush();
            os.close();
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return object;
    }

    public static String getUserNameByUserId(Connection connection, int userId, HttpExchange httpExchange) throws IOException {
        String sql = "SELECT * FROM users WHERE id = " + userId;
        Statement stmt = null;
        ResultSet rs = null;
        String name = null;
        try {
            stmt = connection.createStatement();
            rs = stmt.executeQuery(sql);

            rs.next();
            name = rs.getString("name");
        } catch (SQLException e) {
            httpExchange.sendResponseHeaders(500, 0);
            OutputStream os = httpExchange.getResponseBody();
            os.flush();
            os.close();
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return name;
    }

    public static JSONArray getChargingStations(Connection connection, String level, String query) {
        Statement stmt = null;
        ResultSet rs = null;

        JSONArray array = new JSONArray();

        if (level != null) {
            if (level.equals("min")) {
                try {
                    String sql = "SELECT id, latitude, longitude FROM charging_stations;";
                    stmt = connection.createStatement();
                    rs = stmt.executeQuery(sql);
                    while (rs.next()) {
                        int id = rs.getInt("id");
                        double latitude = rs.getDouble("latitude");
                        double longitude = rs.getDouble("longitude");
                        JSONObject object = new JSONObject();
                        object.put("id", id);
                        object.put("latitude", latitude);
                        object.put("longitude", longitude);
                        array.put(object);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (rs != null) rs.close();
                        if (stmt != null) stmt.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
            if (level.equals("medium")) {
                try {
                    String sql = "SELECT id, name, address, latitude, longitude FROM charging_stations";
                    stmt = connection.createStatement();
                    rs = stmt.executeQuery(sql);

                    while (rs.next()) {
                        int id = rs.getInt("id");
                        String name = rs.getString("name");
                        String address = rs.getString("address");
                        double latitude = rs.getDouble("latitude");
                        double longitude = rs.getDouble("longitude");
                        JSONObject object = new JSONObject();
                        object.put("id", id);
                        object.put("name", name);
                        object.put("address", address);
                        object.put("latitude", latitude);
                        object.put("longitude", longitude);
                        object.put("charging_types", Utils.getChargingTypesByChargingStationId(connection, id));
                        if (query != null) {
                            if (name.toLowerCase().contains(query.toLowerCase()) ||
                                    address.toLowerCase().contains(query.toLowerCase())) {
                                array.put(object);
                            }
                        } else {
                            array.put(object);
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (rs != null) rs.close();
                        if (stmt != null) stmt.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
            if (level.equals("full")) {
                try {
                    String sql = "SELECT * FROM charging_stations";
                    stmt = connection.createStatement();
                    rs = stmt.executeQuery(sql);

                    while (rs.next()) {
                        int id = rs.getInt("id");
                        String name = rs.getString("name");
                        String address = rs.getString("address");
                        double latitude = rs.getDouble("latitude");
                        double longitude = rs.getDouble("longitude");
                        int companyId = rs.getInt("company_id");
                        String hours = rs.getString("opening_hours");
                        String description = rs.getString("description");
                        JSONObject object = new JSONObject();
                        object.put("id", id);
                        object.put("name", name);
                        object.put("address", address);
                        object.put("latitude", latitude);
                        object.put("longitude", longitude);
                        object.put("company_id", companyId);
                        object.put("opening_hours", hours);
                        object.put("description", description);
                        array.put(object);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (rs != null) rs.close();
                        if (stmt != null) stmt.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else {
            try {
                String sql = "SELECT * FROM charging_stations";
                stmt = connection.createStatement();
                rs = stmt.executeQuery(sql);

                while (rs.next()) {
                    int id = rs.getInt("id");
                    String name = rs.getString("name");
                    String address = rs.getString("address");
                    double latitude = rs.getDouble("latitude");
                    double longitude = rs.getDouble("longitude");
                    int companyId = rs.getInt("company_id");
                    String hours = rs.getString("opening_hours");
                    String description = rs.getString("description");
                    JSONObject object = new JSONObject();
                    object.put("id", id);
                    object.put("name", name);
                    object.put("address", address);
                    object.put("latitude", latitude);
                    object.put("longitude", longitude);
                    object.put("company_id", companyId);
                    object.put("opening_hours", hours);
                    object.put("description", description);
                    array.put(object);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (rs != null) rs.close();
                    if (stmt != null) stmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        return array;
    }


    public static JSONArray getChargingTypesByChargingStationId(Connection connection, int chargingStationId) {
        String sql = "SELECT DISTINCT ct.id, ct.name, ct.current_type\n" +
        "FROM charging_stations cs\n" +
        "JOIN connectors c ON cs.id = c.charging_station_id\n" +
        "JOIN charging_types ct ON c.charging_type_id = ct.id\n" +
        "WHERE cs.id = " + chargingStationId;
        Statement stmt = null;
        ResultSet rs = null;
        JSONArray array = new JSONArray();

        try {
            stmt = connection.createStatement();
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String currentType = rs.getString("current_type");
                JSONObject o = new JSONObject();
                o.put("id", id);
                o.put("name", name);
                o.put("current_type", currentType);
                array.put(o);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return array;
    }
}