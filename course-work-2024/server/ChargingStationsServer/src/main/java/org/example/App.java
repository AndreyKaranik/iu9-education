package org.example;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsServer;
import org.json.JSONArray;
import org.json.JSONObject;

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
            System.out.println(new String(httpExchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));

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

                String pattern = "/charging-stations/(\\d+)$";
                Pattern r = Pattern.compile(pattern);
                Matcher m = r.matcher(httpExchange.getRequestURI().toString());

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
                }
//                } else {
////                    String response = "Hello from server!";
////                    httpExchange.sendResponseHeaders(200, response.getBytes().length);
////                    OutputStream os = httpExchange.getResponseBody();
////                    os.write(response.getBytes());
////                    os.flush();
////                    os.close();
//                }
            }
        }
    }

}
