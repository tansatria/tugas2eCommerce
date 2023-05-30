package org.example;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;


public class HttpConnection {
    private Users usersHandler;
    private Products productsHandler;
    private Orders ordersHandler;

    public HttpConnection(){
        SqlConnection sqlcon = new SqlConnection();
        usersHandler = new Users(sqlcon);
        productsHandler = new Products(sqlcon);
        ordersHandler = new Orders(sqlcon);
    }

    public void startServer() throws IOException {
        HttpServer httpServer = HttpServer.create(new InetSocketAddress("localhost", 8127), 0);
        httpServer.createContext("/", new Handler());
        httpServer.setExecutor(Executors.newSingleThreadExecutor());
        httpServer.start();
    }

    private class Handler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            String[] path = exchange.getRequestURI().getPath().split("/");
            String query = exchange.getRequestURI().getQuery();
            String response = "";
            if(method.equals("GET")){
                if(path[1].equals("users")){
                    response = usersHandler.getUsersMethod(path, query);
                }else if(path[1].equals("products")){
                    if(path.length==3) {
                        response = productsHandler.getProducts(Integer.parseInt(path[2]));
                    }else {
                        response = productsHandler.getProducts(0);
                    }
                }else if(path[1].equals("orders")){
                    response = ordersHandler.getOrders(path[2]);
                }
            }else if(method.equals("DELETE")){
                if(path[1].equals("users")){
                    response = usersHandler.deleteMethod(Integer.parseInt(path[2]));
                }else if(path[1].equals("products")){
                    response = productsHandler.deleteMethod(path[2]);
                }else if(path[1].equals("orders")){
                    response = ordersHandler.deleteMethod(path[2]);
                }
            }else if(method.equals("POST")){
                if(path[1].equals("users")){
                    JSONObject requestBodyJson = parseRequestBody(exchange.getRequestBody());
                    response = usersHandler.postMethod(requestBodyJson);
                }else if(path[1].equals("products")){
                    JSONObject requestBodyJson = parseRequestBody(exchange.getRequestBody());
                    response = productsHandler.postMethod(requestBodyJson);
                }else if(path[1].equals("orders")){
                    JSONObject requestBodyJson = parseRequestBody(exchange.getRequestBody());
                    response = ordersHandler.postMethod(requestBodyJson);
                }
            }else if(method.equals("PUT")){
                if(path[1].equals("users")){
                    JSONObject requestBodyJson = parseRequestBody(exchange.getRequestBody());
                    response = usersHandler.putMethod(path[2], requestBodyJson);
                }else if(path[1].equals("products")){
                    JSONObject requestBodyJson = parseRequestBody(exchange.getRequestBody());
                    response = productsHandler.putMethod(path[2],requestBodyJson);
                }else if(path[1].equals("orders")){
                    JSONObject requestBodyJson = parseRequestBody(exchange.getRequestBody());
                    response = ordersHandler.putMethod(path[2],requestBodyJson);
                }
            }
            OutputStream outputStream = exchange.getResponseBody();
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length());
            outputStream.write(response.getBytes());
            outputStream.flush();
            outputStream.close();
        }
        private JSONObject parseRequestBody(InputStream requestBody) throws IOException {
            byte[] requestBodyBytes = requestBody.readAllBytes();
            String requestBodyString = new String(requestBodyBytes);
            return new JSONObject(requestBodyString);
        }
    }
}