package com.jp.simpleFileServer;

import com.jp.simpleFileServer.filesManager.FileLocks;
import com.jp.simpleFileServer.filesManager.actions.DeleteFileAction;
import com.jp.simpleFileServer.filesManager.actions.SendFileAction;
import com.jp.simpleFileServer.filesManager.actions.WriteFileAction;
import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.RoutingHandler;

import java.io.IOException;
import java.util.logging.LogManager;

public class Server {

    public static void main(final String[] args) throws IOException {
        LogManager.getLogManager().readConfiguration(Server.class.getClassLoader().getResourceAsStream("logging.properties"));

        //TODO: un-hardcode 'fileRoot'
        final String filesRoot = "./filesRoot";
        final FileLocks fileLocks = new FileLocks();

        RoutingHandler routingHandler = Handlers.routing()
                .get("/*", exchange -> {
                    exchange.dispatch(new SendFileAction(exchange, filesRoot, fileLocks));
                })
                .post("/*", exchange -> {
                    exchange.dispatch(new WriteFileAction(exchange, filesRoot, fileLocks, true, false));
                })
                .put("/*", exchange -> {
                    exchange.dispatch(new WriteFileAction(exchange, filesRoot, fileLocks, false, true));
                })
                .delete("/*", exchange -> {
                    exchange.dispatch(new DeleteFileAction(exchange, filesRoot, fileLocks));
                });

        Undertow server = Undertow.builder()
                .addHttpListener(8080, "localhost")
                .setHandler(routingHandler)
                .build();

        server.start();
    }
}
