package com.jp.simpleFileServer.filesManager.actions;

import io.undertow.server.HttpServerExchange;
import org.jboss.logging.Logger;

public abstract class AbstractFileAction {

    private static final Logger LOGGER = Logger.getLogger(AbstractFileAction.class);

    private final String filesRoot;

    public AbstractFileAction(String filesRoot) {
        this.filesRoot = filesRoot;
    }

    public abstract void perform(HttpServerExchange exchange, String fileName) throws Exception;

    protected String getFilesRoot() {
        return this.filesRoot;
    }

}
