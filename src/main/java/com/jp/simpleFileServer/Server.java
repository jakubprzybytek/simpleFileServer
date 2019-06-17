package com.jp.simpleFileServer;

import com.jp.simpleFileServer.filesManager.AccessType;
import com.jp.simpleFileServer.filesManager.BlockingAction;
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

        // Dispatch to blocking action. Those blocking actions (Runnable) will be handled by Undertow's thread pool.
        RoutingHandler routingHandler = Handlers.routing()
                .get("/*", exchange -> {
                    exchange.dispatch(new BlockingAction(exchange, fileLocks,
                            AccessType.READ_ONLY,
                            new SendFileAction(filesRoot))
                    );
                })
                .post("/*", exchange -> {
                    exchange.dispatch(new BlockingAction(exchange, fileLocks,
                            AccessType.WRITE,
                            new WriteFileAction(filesRoot, true, false))
                    );
                })
                .put("/*", exchange -> {
                    exchange.dispatch(new BlockingAction(exchange, fileLocks,
                            AccessType.WRITE,
                            new WriteFileAction(filesRoot, false, true))
                    );
                })
                .delete("/*", exchange -> {
                    exchange.dispatch(new BlockingAction(exchange, fileLocks,
                            AccessType.WRITE,
                            new DeleteFileAction(filesRoot))
                    );
                });

        Undertow server = Undertow.builder()
                .addHttpListener(8080, "localhost")
                .setHandler(routingHandler)
                .build();

        server.start();
    }
}
