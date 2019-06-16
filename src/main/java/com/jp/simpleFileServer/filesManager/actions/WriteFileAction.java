package com.jp.simpleFileServer.filesManager.actions;

import com.jp.simpleFileServer.filesManager.FileLocks;
import com.jp.simpleFileServer.handlers.ErrorHandlers;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.form.FormData;
import io.undertow.server.handlers.form.FormDataParser;
import io.undertow.server.handlers.form.FormParserFactory;
import org.apache.commons.lang3.StringUtils;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * Writes new file. Used for POST and PUT.
 * <code>allowCreate</code> - allows to create a file if it does not exist yet (POST only).
 * <code>allowOverride</code> - allows to override file if it exists already (PUT only).
 *
 * File created or overridden successfully - 201
 * Bad request - 400
 * File not found - 404
 * Any other problem - 500
 */
public class WriteFileAction extends AbstractFileAction {

    private static final Logger LOGGER = Logger.getLogger(WriteFileAction.class);

    private final FormParserFactory.Builder builder = FormParserFactory.builder();

    private final boolean allowCreate;

    private final boolean allowOverride;

    public WriteFileAction(HttpServerExchange exchange, String filesRoot, FileLocks fileLocks, boolean allowCreate, boolean allowOverride) {
        super(exchange, filesRoot, fileLocks);
        this.allowCreate = allowCreate;
        this.allowOverride = allowOverride;
    }

    @Override
    public void perform() throws IOException {
        final String fileName = getFileNameFromRequest();
        LOGGER.info(String.format("Received request to create a file: %s", fileName));

        if (StringUtils.isBlank(fileName)) {
            LOGGER.info(String.format("Bad file name: '%s'", fileName));
            ErrorHandlers.handleBadRequest(getExchange());
            return;
        }

        try {
            acquireWriteLock(fileName);

            Path targetPath = Paths.get(getFilesRoot(), fileName);

            if (Files.exists(targetPath)) {
                if (!this.allowOverride) {
                    LOGGER.info(String.format("Conflict - file already exists: %s", fileName));
                    ErrorHandlers.handleAlreadyExistingFile(getExchange());
                    return;
                }
            } else {
                if (!this.allowCreate) {
                    LOGGER.info(String.format("File not found: %s", fileName));
                    ErrorHandlers.handleFileNotFound(getExchange());
                    return;
                }
            }

            final FormDataParser formDataParser = builder.build().createParser(getExchange());
            final FormData formData = formDataParser.parseBlocking();

            for (String data : formData) {
                for (FormData.FormValue formValue : formData.get(data)) {
                    if (formValue.isFile()) {
                        Files.createDirectories(targetPath.getParent());
                        Files.copy(formValue.getPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
                        break;
                    }
                }
            }

            LOGGER.info(String.format("File created: %s", fileName));
            getExchange().setStatusCode(201);
        } finally {
            releaseWriteLock(fileName);
        }
    }
}
