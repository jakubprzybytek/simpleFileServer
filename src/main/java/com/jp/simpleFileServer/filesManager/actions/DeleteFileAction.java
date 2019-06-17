package com.jp.simpleFileServer.filesManager.actions;

import com.jp.simpleFileServer.handlers.ErrorHandlers;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.form.FormParserFactory;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Removes file.
 * <p>
 * File removed successfully - 200
 * Bad request - 400
 * File not found - 404
 * Any other problem - 500
 */
public class DeleteFileAction extends AbstractFileAction {

    private static final Logger LOGGER = Logger.getLogger(DeleteFileAction.class);

    private final FormParserFactory.Builder builder = FormParserFactory.builder();

    public DeleteFileAction(String filesRoot) {
        super(filesRoot);
    }

    @Override
    public void perform(HttpServerExchange exchange, String fileName) throws IOException {
        LOGGER.info(String.format("Received request to delete a file: %s", fileName));

        Path targetPath = Paths.get(getFilesRoot(), fileName);

        if (!Files.exists(targetPath)) {
            LOGGER.info(String.format("File not found: '%s'", fileName));
            ErrorHandlers.handleFileNotFound(exchange);
            return;
        }

        Files.delete(targetPath);

        LOGGER.info(String.format("File deleted: %s", fileName));
        exchange.setStatusCode(200);
    }
}
