package org.example;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.sql.*;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class App {

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost",8080), 0);
        server.createContext("/", new MyHandler());
        server.setExecutor(null);
        server.start();

        String url = "jdbc:postgresql://localhost:5432/charging_stations_database";
        String user = "postgres";
        String password = "karanik2003";

        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            conn = DriverManager.getConnection(url, user, password);

            stmt = conn.createStatement();

            String sql = "SELECT * FROM companies";

            rs = stmt.executeQuery(sql);

            while (rs.next()) {
                int id = rs.getInt("company_id");
                String name = rs.getString("company_name");

                System.out.println("ID: " + id + ", Name: " + name);
            }
        } catch (SQLException e) {
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
    }

    static class MyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            System.out.println(httpExchange.getRequestMethod() + " " + httpExchange.getRequestURI());
            for (String key : httpExchange.getRequestHeaders().keySet()) {
                System.out.println(key + ": " + httpExchange.getRequestHeaders().get(key).toString());
            }
            System.out.println(new String(httpExchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            String response = "MyResponse";
            httpExchange.sendResponseHeaders(200, response.length());
            OutputStream os = httpExchange.getResponseBody();
            os.write(response.getBytes());
            os.flush();
            os.close();
        }
    }

}
