package com.jp.simpleFileServer.filesManager;

import com.jp.simpleFileServer.filesManager.actions.AbstractFileAction;
import com.jp.simpleFileServer.handlers.ErrorHandlers;
import io.undertow.server.HttpServerExchange;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.jboss.logging.Logger;

import java.util.concurrent.TimeUnit;

public class BlockingAction implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(BlockingAction.class);

    private final HttpServerExchange exchange;

    private final FileLocks fileLocks;

    private final AccessType accessType;

    private final AbstractFileAction fileAction;

    public BlockingAction(HttpServerExchange exchange, FileLocks fileLocks, AccessType accessType, AbstractFileAction fileAction) {
        this.exchange = exchange;
        this.fileLocks = fileLocks;
        this.accessType = accessType;
        this.fileAction = fileAction;
    }

    public void run() {
        // Parse file name from request path
        // TODO: make it bullet-proof
        String fileName = StringUtils.removeStart(this.exchange.getRequestURI(), "/");

        // validate input file name
        if (StringUtils.isBlank(fileName)) {
            LOGGER.info(String.format("Bad file name: '%s'", fileName));
            ErrorHandlers.handleBadRequest(this.exchange);
            return;
        }

        this.exchange.startBlocking();

        try {
            this.accessType.request(this.fileLocks, fileName);

            // temporal dirty hack for testing concurrency - fix it!
            int emulateLongProcessing = getEmulateLongProcessing();
            if (emulateLongProcessing > 0) {
                try {
                    TimeUnit.SECONDS.sleep(emulateLongProcessing);
                } catch (InterruptedException e) {
                    LOGGER.error("Sleep interrupted", e);
                }
            }

            this.fileAction.perform(this.exchange, fileName);
        } catch (Exception e) {
            LOGGER.error("Cannot perform action", e);
            ErrorHandlers.handleServerError(this.exchange);
        } finally {
            this.accessType.release(this.fileLocks, fileName);
        }

        this.exchange.endExchange();
    }

    protected int getEmulateLongProcessing() {
        final String emulateLongProcessingHeader = this.exchange.getRequestHeaders().getFirst("emulate-long-processing");
        return NumberUtils.toInt(emulateLongProcessingHeader, 0);
    }

}
