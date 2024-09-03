package org.example;

import com.sun.net.httpserver.HttpExchange;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.KeyPair;
import java.security.SecureRandom;
import java.sql.*;
import java.util.ArrayList;
import java.util.Base64;

public class Utils {

    private static final SecureRandom secureRandom = new SecureRandom(); //threadsafe
    private static final Base64.Encoder base64Encoder = Base64.getUrlEncoder(); //threadsafe

    public static String generateNewToken() {
        byte[] randomBytes = new byte[24];
        secureRandom.nextBytes(randomBytes);
        return base64Encoder.encodeToString(randomBytes);
    }

    public static JSONArray getChargingMarksWithUserNameByChargingStationId(Connection connection, int chargingStationId) {
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
                int chargingTypeId = rs.getInt("charging_type_id");
                Timestamp timestamp = rs.getTimestamp("time");
                JSONObject object = new JSONObject();
                object.put("id", id);
                object.put("charging_station_id", markChargingStationId);
                object.put("status", status);
                if (userId != 0) {
                    object.put("user_id", userId);
                    object.put("user_name", Utils.getUserNameByUserId(connection, userId));
                }
                object.put("charging_type", Utils.getChargingTypeByChargingTypeId(connection, chargingTypeId));
                object.put("time", timestamp);
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
        return array;
    }


    public static JSONArray getConnectorsByChargingStationId(Connection connection, int chargingStationId) throws IOException {
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
                o.put("charging_type", Utils.getChargingTypeByChargingTypeId(connection, chargingTypeId));

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

    public static JSONObject getChargingTypeByChargingTypeId(Connection connection, int chargingTypeId) {
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
            String hours = rs.getString("opening_hours");
            String description = rs.getString("description");
            object.put("id", id);
            object.put("name", name);
            object.put("address", address);
            object.put("latitude", latitude);
            object.put("longitude", longitude);
            object.put("opening_hours", hours);
            object.put("description", description);
            object.put("connectors", Utils.getConnectorsByChargingStationId(connection, id));
            object.put("charging_marks", Utils.getChargingMarksWithUserNameByChargingStationId(connection, id));
            object.put("image_ids", Utils.getChargingStationImageIdsByChargingStationId(connection, id));
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

    public static String getUserNameByUserId(Connection connection, int userId) {
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
                        String hours = rs.getString("opening_hours");
                        String description = rs.getString("description");
                        JSONObject object = new JSONObject();
                        object.put("id", id);
                        object.put("name", name);
                        object.put("address", address);
                        object.put("latitude", latitude);
                        object.put("longitude", longitude);
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
                    String hours = rs.getString("opening_hours");
                    String description = rs.getString("description");
                    JSONObject object = new JSONObject();
                    object.put("id", id);
                    object.put("name", name);
                    object.put("address", address);
                    object.put("latitude", latitude);
                    object.put("longitude", longitude);
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

    public static JSONObject getChargingStationImageById(Connection connection, int chargingStationImageId) {
        String sql = "SELECT * FROM charging_station_images WHERE id = " + chargingStationImageId;
        Statement stmt = null;
        ResultSet rs = null;
        JSONObject object = new JSONObject();

        try {
            stmt = connection.createStatement();
            rs = stmt.executeQuery(sql);
            rs.next();
            int id = rs.getInt("id");
            String path = rs.getString("path");
            File imageFile = new File("/root/chargingstations/images/" + path);
            byte[] imageBytes = Files.readAllBytes(imageFile.toPath());
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);

            object.put("id", id);
            object.put("data", base64Image);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
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

    public static JSONArray getChargingStationImageIdsByChargingStationId(Connection connection, int chargingStationId) {
        String sql = "SELECT id FROM charging_station_images WHERE charging_station_id = " + chargingStationId;
        Statement stmt = null;
        ResultSet rs = null;
        JSONArray array = new JSONArray();

        try {
            stmt = connection.createStatement();
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                int id = rs.getInt("id");
                JSONObject o = new JSONObject();
                o.put("id", id);
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

    /**
     *
     * @param connection
     * @param name
     * @param email
     * @return (userId, 0) - is active, (userId, 1) - is not active, (0, 2) - not found or exception
     */
    public static Pair<Integer, Integer> checkUserIsActive(Connection connection, String email) {
        String sql = "SELECT id, is_active FROM users WHERE email = ?";
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            stmt = connection.prepareStatement(sql);
            stmt.setString(1, email);
            rs = stmt.executeQuery();

            if (!rs.next()) {
                return new Pair<>(0, 2);
            }

            boolean isActive = rs.getBoolean("is_active");
            int id = rs.getInt("id");

            if (isActive) {
                return new Pair<>(id, 0);
            } else {
                return new Pair<>(id, 1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return new Pair<>(0, 2);
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static int checkUsername(Connection connection, String username) {
        String sql = "SELECT COUNT(1) FROM users WHERE name = " + '\'' + username + '\'';
        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = connection.createStatement();
            rs = stmt.executeQuery(sql);
            rs.next();
            int count = rs.getInt("count");
            if (count == 1) {
                return 2;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return 1;
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    public static int checkEmail(Connection connection, String email) {
        String sql = "SELECT COUNT(1) FROM users WHERE email = " + '\'' + email + '\'';
        Statement stmt = null;
        ResultSet rs = null;

        try {
            stmt = connection.createStatement();
            rs = stmt.executeQuery(sql);
            rs.next();
            int count = rs.getInt("count");
            if (count == 1) {
                return 2;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return 1;
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    public static boolean confirm(Connection connection, String token) {
        if (token == null) {
            return false;
        }

        String sql = "SELECT id FROM users WHERE token = " + '\'' + token + '\'';
        Statement stmt = null;
        ResultSet rs = null;

        try {
            stmt = connection.createStatement();
            rs = stmt.executeQuery(sql);
            if (rs.next()) {
                int id = rs.getInt("id");
                if (updateUserIsActive(connection, id, true)) {
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }


        return false;
    }

    public static boolean updateUserIsActive(Connection connection, int userId, boolean isActive) {
        String sql = "UPDATE users SET is_active = ? WHERE id = ?";
        PreparedStatement stmt = null;

        try {
            stmt = connection.prepareStatement(sql);
            stmt.setBoolean(1, isActive);
            stmt.setInt(2, userId);
            int updatedRows = stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static String getUserTokenByUserId(Connection connection, int userId) {
        String sql = "SELECT token FROM users WHERE id = ?";
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            stmt = connection.prepareStatement(sql);
            stmt.setInt(1, userId);
            rs = stmt.executeQuery();

            if (!rs.next()) {
                return "";
            }
            String token = rs.getString("token");

            return token;
        } catch (SQLException e) {
            e.printStackTrace();
            return "";
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean insertUser(Connection connection, String name, String email, String password, String token, boolean isActive) {
        String sql = "INSERT INTO users(name, email, password, token, is_active) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement stmt = null;

        try {
            stmt = connection.prepareStatement(sql);
            stmt.setString(1, name);
            stmt.setString(2, email);
            stmt.setString(3, password);
            stmt.setString(4, token);
            stmt.setBoolean(5, isActive);
            int insertedRows = stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static String auth(Connection connection, String email, String password) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String sql = "SELECT password, token FROM users WHERE email = ?";
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            stmt = connection.prepareStatement(sql);
            stmt.setString(1, email);
            rs = stmt.executeQuery();
            if (!rs.next()) {
                return "";
            }
            String hashedPassword = rs.getString("password");
            if (passwordEncoder.matches(password, hashedPassword)) {
                String token = rs.getString("token");
                return token;
            } else {
                return "";
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "";
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}