package com.jp.simpleFileServer.filesManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class FileLocks {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileLocks.class);

    private final ConcurrentMap<String, ReadWriteLock> locks = new ConcurrentHashMap<>();

    private ReadWriteLock getLockForFile(String fileName) {
        return this.locks.compute(fileName, (key, lock) -> {
            return lock != null ? lock : new ReentrantReadWriteLock();
        });
    }

    public void acquireReadLock(String fileName) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format(" +> Read lock request - '%s'", fileName));
        }
        getLockForFile(fileName).readLock().lock();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format(" +< Read lock granted - '%s'", fileName));
        }
    }

    public void acquireWriteLock(String fileName) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format(" +> Write lock request - '%s'", fileName));
        }
        getLockForFile(fileName).writeLock().lock();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format(" +< Write lock granted - '%s'", fileName));
        }
    }

    //TODO: Remove lock from map if it is not used anymore
    public void releaseReadLock(String fileName) {
        getLockForFile(fileName).readLock().unlock();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format(" -< Read lock released - '%s'", fileName));
        }
    }

    public void releaseWriteLock(String fileName) {
        getLockForFile(fileName).writeLock().unlock();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format(" -< Write lock released - '%s'", fileName));
        }
    }

}
