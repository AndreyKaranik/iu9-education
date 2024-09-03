package org.example;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.SecureRandom;
import java.sql.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsServer;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class App {

    private static final String URL = "jdbc:postgresql://localhost:5432/charging_stations_database";
    private static final String USER = "postgres";
    private static final String PASSWORD = "postgres";

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", 8000), 0); // new InetSocketAddress("localhost",8080)
        server.createContext("/", new MyHandler());
        server.setExecutor(null);
        server.start();
        System.out.println("The server has started successfully.");


//        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
//        String rawPassword = "andrey2003";
//        String hashedPassword = passwordEncoder.encode(rawPassword);
//        System.out.println(hashedPassword);


//        URI uri = new URI(
//                "http",
//                "example.com",
//                "/query",
//                "q=ул. Брусилова",
//                null);
//
//        String request = uri.toASCIIString();
//        System.out.println(request);
    }

    private static Map<String, String> parseQueryParams(String query) {
        Map<String, String> queryParams = new HashMap<>();
        if (query != null && !query.isEmpty()) {
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                String[] keyValue = pair.split("=");
                if (keyValue.length > 1) {
                    queryParams.put(keyValue[0], keyValue[1]);
                } else {
                    queryParams.put(keyValue[0], "");
                }
            }
        }
        return queryParams;
    }

    static class MyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            System.out.println(httpExchange.getRequestMethod() + " " + httpExchange.getRequestURI());
            for (String key : httpExchange.getRequestHeaders().keySet()) {
                System.out.println(key + ": " + httpExchange.getRequestHeaders().get(key).toString());
            }
            String body = new String(httpExchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);

            if (httpExchange.getRequestMethod().equals("GET")) {
                Map<String, String> queryParams = parseQueryParams(httpExchange.getRequestURI().getQuery());
                System.out.println("Query Parameters:");
                for (Map.Entry<String, String> entry : queryParams.entrySet()) {
                    System.out.println(entry.getKey() + " = " + entry.getValue());
                }

//                if (queryParams.containsKey("lang")) {
//                    System.out.println("HERE: " + queryParams.get("lang"));
//                }


                // /charging-stations?lang=ru&simplify=true - example
                // .getRequestURI().getPath() -> "/charging-stations"
                // .getRequestURI().getQuery() -> "lang=ru&simplify=true"

                String pattern = "/charging-station-images/(\\d+)$";
                Pattern r = Pattern.compile(pattern);
                Matcher m = r.matcher(httpExchange.getRequestURI().toString());

                if (m.find()) {
                    String chargingStationImageId = m.group(1);

                    Connection connection = null;

                    try {
                        connection = DriverManager.getConnection(URL, USER, PASSWORD);

                        JSONObject object = Utils.getChargingStationImageById(connection, Integer.parseInt(chargingStationImageId));

                        String response = object.toString();
                        ArrayList<String> list = new ArrayList<>();
                        list.add("application/json");
                        httpExchange.getResponseHeaders().put("Content-Type", list);

                        httpExchange.sendResponseHeaders(200, response.getBytes(StandardCharsets.UTF_8).length);
                        OutputStream os = httpExchange.getResponseBody();
                        os.write(response.getBytes());
                        os.flush();
                        os.close();
                    } catch (SQLException e) {
                        httpExchange.sendResponseHeaders(500, 0);
                        OutputStream os = httpExchange.getResponseBody();
                        os.flush();
                        os.close();
                        e.printStackTrace();
                    } finally {
                        try {
                            if (connection != null) connection.close();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                    return;
                }

                pattern = "/privacy-policy$";
                r = Pattern.compile(pattern);
                m = r.matcher(httpExchange.getRequestURI().toString());

                if (m.find()) {
                    File file = new File("/root/chargingstations/privacy-policy.html");
                    String response = Files.readString(file.toPath());
                    ArrayList<String> list = new ArrayList<>();
                    list.add("text/html");
                    httpExchange.getResponseHeaders().put("Content-Type", list);

                    httpExchange.sendResponseHeaders(200, response.getBytes(StandardCharsets.UTF_8).length);
                    OutputStream os = httpExchange.getResponseBody();
                    os.write(response.getBytes());
                    os.flush();
                    os.close();
                }

                pattern = "/charging-stations/(\\d+)$";
                r = Pattern.compile(pattern);
                m = r.matcher(httpExchange.getRequestURI().toString());

                if (m.find()) {
                    String chargingStationId = m.group(1);

                    Connection connection = null;

                    try {
                        connection = DriverManager.getConnection(URL, USER, PASSWORD);

                        JSONObject object = Utils.getChargingStationDetailsByChargingStationId(connection, Integer.parseInt(chargingStationId), httpExchange);

                        String response = object.toString();
                        ArrayList<String> list = new ArrayList<>();
                        list.add("application/json");
                        httpExchange.getResponseHeaders().put("Content-Type", list);

                        httpExchange.sendResponseHeaders(200, response.getBytes(StandardCharsets.UTF_8).length);
                        OutputStream os = httpExchange.getResponseBody();
                        os.write(response.getBytes());
                        os.flush();
                        os.close();
                    } catch (SQLException e) {
                        httpExchange.sendResponseHeaders(500, 0);
                        OutputStream os = httpExchange.getResponseBody();
                        os.flush();
                        os.close();
                        e.printStackTrace();
                    } finally {
                        try {
                            if (connection != null) connection.close();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                    return;
                }

                pattern = "/charging-stations";
                r = Pattern.compile(pattern);
                m = r.matcher(httpExchange.getRequestURI().toString());

                if (m.find()) {
                    Connection connection = null;

                    try {
                        connection = DriverManager.getConnection(URL, USER, PASSWORD);

                        JSONArray array = Utils.getChargingStations(
                                connection,
                                queryParams.get("level") == null ? null : URLDecoder.decode(queryParams.get("level"), StandardCharsets.UTF_8),
                                queryParams.get("query") == null ? null : URLDecoder.decode(queryParams.get("query"), StandardCharsets.UTF_8)
                        );

                        String response = array.toString();
                        ArrayList<String> list = new ArrayList<>();
                        list.add("application/json");
                        httpExchange.getResponseHeaders().put("Content-Type", list);

                        httpExchange.sendResponseHeaders(200, response.getBytes(StandardCharsets.UTF_8).length);
                        OutputStream os = httpExchange.getResponseBody();
                        os.write(response.getBytes());
                        os.flush();
                        os.close();
                    } catch (SQLException e) {
                        httpExchange.sendResponseHeaders(500, 0);
                        OutputStream os = httpExchange.getResponseBody();
                        os.flush();
                        os.close();
                        e.printStackTrace();
                    } finally {
                        try {
                            if (connection != null) connection.close();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                    return;
                }
//                } else {
////                    String response = "Hello from server!";
////                    httpExchange.sendResponseHeaders(200, response.getBytes().length);
////                    OutputStream os = httpExchange.getResponseBody();
////                    os.write(response.getBytes());
////                    os.flush();
////                    os.close();
//                }

                pattern = "/confirm";
                r = Pattern.compile(pattern);
                m = r.matcher(httpExchange.getRequestURI().toString());

                if (m.find()) {
                    Connection connection = null;
                    try {
                        connection = DriverManager.getConnection(URL, USER, PASSWORD);

                        boolean status = Utils.confirm(connection, queryParams.get("token") == null ? null : URLDecoder.decode(queryParams.get("token"), StandardCharsets.UTF_8));

                        JSONObject object = new JSONObject();
                        object.put("status", status);

                        String response = object.toString();
                        ArrayList<String> list = new ArrayList<>();
                        list.add("application/json");
                        httpExchange.getResponseHeaders().put("Content-Type", list);

                        httpExchange.sendResponseHeaders(200, response.getBytes(StandardCharsets.UTF_8).length);
                        OutputStream os = httpExchange.getResponseBody();
                        os.write(response.getBytes());
                        os.flush();
                        os.close();
                    } catch (SQLException e) {
                        httpExchange.sendResponseHeaders(500, 0);
                        OutputStream os = httpExchange.getResponseBody();
                        os.flush();
                        os.close();
                        e.printStackTrace();
                    } finally {
                        try {
                            if (connection != null) connection.close();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                    return;
                }
            }


            if (httpExchange.getRequestMethod().equals("POST")) {
                String pattern = "/register";
                Pattern r = Pattern.compile(pattern);
                Matcher m = r.matcher(httpExchange.getRequestURI().toString());


                if (m.find()) {
                    RegistrationData registrationData = null;
                    try {
                        Gson gson = new Gson();
                        registrationData = gson.fromJson(body, RegistrationData.class);
                    } catch (JsonSyntaxException | JsonIOException e) {
                        e.printStackTrace();
                    }

                    Connection connection = null;
                    int emailStatus = 1;
                    Pair<Integer, Integer> isActiveStatusPair;
                    int userId = -1;

                    boolean success = false;

                    if (registrationData != null) {
                        try {
                            connection = DriverManager.getConnection(URL, USER, PASSWORD);
                            isActiveStatusPair = Utils.checkUserIsActive(connection, registrationData.getEmail());
                            if (isActiveStatusPair.getRight() == 1) {
                                emailStatus = 0;
                                userId = isActiveStatusPair.getLeft();
                            } else {
                                emailStatus = Utils.checkEmail(connection, registrationData.getEmail());
                            }

                            if (emailStatus == 0 && isActiveStatusPair.getRight() != 0) {
                                String token;
                                if (isActiveStatusPair.getRight() == 1) {
                                    token = Utils.getUserTokenByUserId(connection, userId);
                                } else {
                                    token = Utils.generateNewToken();
                                    BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
                                    String hashedPassword = passwordEncoder.encode(registrationData.getPassword());
                                    Utils.insertUser(connection, registrationData.getName(), registrationData.getEmail(), hashedPassword, token, false);
                                }
                                if (token != null && !token.isEmpty()) {
                                    success = EmailSender.sendEmail(registrationData.getEmail(), token);
                                }
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                        } finally {
                            try {
                                if (connection != null) connection.close();
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    JSONObject response = new JSONObject();
                    if (emailStatus == 0 && success) {
                        response.put("status", 0);      // success
                    } else if (emailStatus == 2) {
                        response.put("status", 2);      // email exist
                    } else {
                        response.put("status", 1);      // failed
                    }
                    ArrayList<String> list = new ArrayList<>();
                    list.add("application/json");
                    httpExchange.getResponseHeaders().put("Content-Type", list);

                    httpExchange.sendResponseHeaders(200, response.toString().getBytes(StandardCharsets.UTF_8).length);
                    OutputStream os = httpExchange.getResponseBody();
                    os.write(response.toString().getBytes());
                    os.flush();
                    os.close();
                }

                pattern = "/auth";
                r = Pattern.compile(pattern);
                m = r.matcher(httpExchange.getRequestURI().toString());

                if (m.find()) {
                    AuthData authData = null;
                    try {
                        Gson gson = new Gson();
                        authData = gson.fromJson(body, AuthData.class);
                    } catch (JsonSyntaxException | JsonIOException e) {
                        e.printStackTrace();
                    }

                    Connection connection = null;

                    String token = "";

                    if (authData != null) {
                        try {
                            connection = DriverManager.getConnection(URL, USER, PASSWORD);
                            token = Utils.auth(connection, authData.getEmail(), authData.getPassword());
                        } catch (SQLException e) {
                            e.printStackTrace();
                        } finally {
                            try {
                                if (connection != null) connection.close();
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    JSONObject response = new JSONObject();
                    response.put("token", token);
                    ArrayList<String> list = new ArrayList<>();
                    list.add("application/json");
                    httpExchange.getResponseHeaders().put("Content-Type", list);

                    httpExchange.sendResponseHeaders(200, response.toString().getBytes(StandardCharsets.UTF_8).length);
                    OutputStream os = httpExchange.getResponseBody();
                    os.write(response.toString().getBytes());
                    os.flush();
                    os.close();
                }

                pattern = "/charge";
                r = Pattern.compile(pattern);
                m = r.matcher(httpExchange.getRequestURI().toString());

                if (m.find()) {
                    OrderForm orderForm = null;
                    try {
                        Gson gson = new Gson();
                        orderForm = gson.fromJson(body, OrderForm.class);
                    } catch (JsonSyntaxException | JsonIOException e) {
                        e.printStackTrace();
                    }

                    Connection connection = null;

                    String token = "";

                    if (orderForm != null) {
                        try {
                            connection = DriverManager.getConnection(URL, USER, PASSWORD);
                            //token = Utils.charge(connection, orderForm.getUsername(), authData.getPassword());
                        } catch (SQLException e) {
                            e.printStackTrace();
                        } finally {
                            try {
                                if (connection != null) {
                                    connection.close();
                                }
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    JSONObject response = new JSONObject();
                    response.put("status", 1);
                    ArrayList<String> list = new ArrayList<>();
                    list.add("application/json");
                    httpExchange.getResponseHeaders().put("Content-Type", list);

                    httpExchange.sendResponseHeaders(200, response.toString().getBytes(StandardCharsets.UTF_8).length);
                    OutputStream os = httpExchange.getResponseBody();
                    os.write(response.toString().getBytes());
                    os.flush();
                    os.close();
                }
            }
        }
    }
}
