package org.example;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.json.JSONArray;
import org.json.JSONObject;

public class App {

    private static final String URL = "jdbc:postgresql://localhost:5432/charging_stations_database";
    private static final String USER = "postgres";
    private static final String PASSWORD = "postgres";

    private final static boolean IS_LOCAL = false;

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(IS_LOCAL ? "localhost" : "0.0.0.0",8000), 0); // new InetSocketAddress("localhost",8080)
        server.createContext("/", new MyHandler());
        server.setExecutor(null);
        server.start();
        System.out.println("The server has started successfully.");
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
                if (httpExchange.getRequestURI().toString().equals("/charging-stations")) {
                    Connection conn = null;
                    Statement stmt = null;
                    ResultSet rs = null;

                    try {
                        conn = DriverManager.getConnection(URL, USER, PASSWORD);

                        stmt = conn.createStatement();

                        String sql = "SELECT * FROM charging_stations";

                        rs = stmt.executeQuery(sql);

                        JSONArray array = new JSONArray();

                        while (rs.next()) {
                            int id = rs.getInt("id");
                            String name = rs.getString("name");
                            String address = rs.getString("address");
                            int companyId = rs.getInt("id");
                            String hours = rs.getString("opening_hours");
                            String description = rs.getString("description");
                            JSONObject object = new JSONObject();
                            object.put("id", id);
                            object.put("name", name);
                            object.put("address", address);
                            object.put("company_id", companyId);
                            object.put("opening_hours", hours);
                            object.put("description", description);
                            array.put(object);
                        }
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
                            if (rs != null) rs.close();
                            if (stmt != null) stmt.close();
                            if (conn != null) conn.close();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    String response = "Hello from server!";
                    httpExchange.sendResponseHeaders(200, response.getBytes().length);
                    OutputStream os = httpExchange.getResponseBody();
                    os.write(response.getBytes());
                    os.flush();
                    os.close();
                }
            }
        }
    }

}
