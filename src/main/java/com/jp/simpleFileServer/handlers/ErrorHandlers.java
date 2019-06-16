package com.jp.simpleFileServer.handlers;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;

public class ErrorHandlers implements HttpHandler {

    private final int errorCode;

    private final String errorMessage;

    private ErrorHandlers(int errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) {
        exchange.setStatusCode(this.errorCode);
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/html");
        exchange.getResponseSender().send(this.errorMessage);
    }

    /**
     * Sends response with information about bad response, i.e. wrong/insufficient input parameters.
     *
     * @param exchange HttpServerExchange from the connection.
     */
    static public void handleBadRequest(HttpServerExchange exchange) {
        final ErrorHandlers handler = new ErrorHandlers(400, "Bad request!");
        handler.handleRequest(exchange);
    }

    /**
     * Sends response with information that the requested file does not exist.
     *
     * @param exchange HttpServerExchange from the connection.
     */
    static public void handleFileNotFound(HttpServerExchange exchange) {
        final ErrorHandlers handler = new ErrorHandlers(404, "File not found!");
        handler.handleRequest(exchange);
    }

    /**
     * Sends response with information that the requested resource already exists.
     *
     * @param exchange HttpServerExchange from the connection.
     */
    static public void handleAlreadyExistingFile(HttpServerExchange exchange) {
        final ErrorHandlers handler = new ErrorHandlers(409, "Such file already exists!");
        handler.handleRequest(exchange);
    }

    /**
     * Sends response with information generic 500 error.
     *
     * @param exchange HttpServerExchange from the connection.
     */

    static public void handleServerError(HttpServerExchange exchange) {
        final ErrorHandlers handler = new ErrorHandlers(500, "Server error!");
        handler.handleRequest(exchange);
    }

}
