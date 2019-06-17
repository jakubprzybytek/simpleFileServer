package com.jp.simpleFileServer.filesManager.actions;

import com.jp.simpleFileServer.handlers.ErrorHandlers;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.MimeMappings;
import org.apache.commons.lang3.StringUtils;
import org.jboss.logging.Logger;
import org.xnio.channels.StreamSinkChannel;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.TimeUnit;

/**
 * Sends back the requested file. Tries to determine mime type.
 * <p>
 * File sent successfully - 200
 * Bad request - 400
 * File not found - 404
 * Any other problem - 500
 */
public class SendFileAction extends AbstractFileAction {

    private static final Logger LOGGER = Logger.getLogger(SendFileAction.class);

    // Undertow tool to determine mime types
    private static final MimeMappings mimeMappings = MimeMappings.builder().build();

    public SendFileAction(String filesRoot) {
        super(filesRoot);
    }

    @Override
    public void perform(HttpServerExchange exchange, String fileName) throws IOException {
        LOGGER.info(String.format("Received request to download a file: '%s'", fileName));

        final Path requestedFilePath = Paths.get(getFilesRoot(), fileName);
        final String fileExtension = StringUtils.substringAfterLast(requestedFilePath.getFileName().toString(), ".");
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, mimeMappings.getMimeType(fileExtension));

        try (FileChannel fileChannel = FileChannel.open(requestedFilePath, StandardOpenOption.READ)) {
            final StreamSinkChannel responseChannel = exchange.getResponseChannel();

            responseChannel.transferFrom(fileChannel, 0, fileChannel.size());

            LOGGER.info(String.format("File sent: '%s'", fileName));

        } catch (NoSuchFileException e) {
            LOGGER.info(String.format("File not found: '%s'", fileName));
            ErrorHandlers.handleFileNotFound(exchange);
        }
    }
}
