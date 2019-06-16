package com.jp.simpleFileServer.filesManager.actions;

import com.jp.simpleFileServer.filesManager.FileLocks;
import com.jp.simpleFileServer.handlers.ErrorHandlers;
import io.undertow.server.HttpServerExchange;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.jboss.logging.Logger;

public abstract class AbstractFileAction implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(AbstractFileAction.class);

    private final HttpServerExchange exchange;

    private final String filesRoot;

    private final FileLocks fileLocks;

    public AbstractFileAction(HttpServerExchange exchange, String filesRoot, FileLocks fileLocks) {
        this.exchange = exchange;
        this.filesRoot = filesRoot;
        this.fileLocks = fileLocks;
    }

    public void run() {
        this.exchange.startBlocking();
        try {
            perform();
        } catch (Exception e) {
            LOGGER.error("Cannot perform action", e);
            ErrorHandlers.handleServerError(exchange);
        }
        this.exchange.endExchange();
    }

    protected abstract void perform() throws Exception;

    protected HttpServerExchange getExchange() {
        return this.exchange;
    }

    protected String getFilesRoot() {
        return this.filesRoot;
    }

    protected String getFileNameFromRequest() {
        return StringUtils.removeStart(getExchange().getRequestURI(), "/");
    }

    protected int getEmulateLongProcessing() {
        final String emulateLongProcessingHeader = this.exchange.getRequestHeaders().getFirst("emulate-long-processing");
        return NumberUtils.toInt(emulateLongProcessingHeader, 0);
    }

    protected void acquireReadLock(String fileName) {
        this.fileLocks.acquireReadLock(fileName);
    }

    protected void acquireWriteLock(String fileName) {
        this.fileLocks.acquireWriteLock(fileName);
    }

    protected void releaseReadLock(String fileName) {
        this.fileLocks.releaseReadLock(fileName);
    }

    protected void releaseWriteLock(String fileName) {
        this.fileLocks.releaseWriteLock(fileName);
    }

}
