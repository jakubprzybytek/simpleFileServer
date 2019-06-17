package com.jp.simpleFileServer.filesManager;

public class AccessType {

    private final AccessOperation requestAccess;

    private final AccessOperation releaseAccess;

    private AccessType(AccessOperation requestAccess, AccessOperation releaseAccess) {
        this.requestAccess = requestAccess;
        this.releaseAccess = releaseAccess;
    }

    public void request(FileLocks fileLocks, String fileName) {
        this.requestAccess.perform(fileLocks, fileName);
    }

    public void release(FileLocks fileLocks, String fileName) {
        this.releaseAccess.perform(fileLocks, fileName);
    }

    public static AccessType READ_ONLY = new AccessType(
            (fileLocks, fileName) -> {
                fileLocks.acquireReadLock(fileName);
            }, (fileLocks, fileName) -> {
                fileLocks.releaseReadLock(fileName);
            });

    public static AccessType WRITE = new AccessType(
            (fileLocks, fileName) -> {
                fileLocks.acquireWriteLock(fileName);
            }, (fileLocks, fileName) -> {
                fileLocks.releaseWriteLock(fileName);
            });

    public interface AccessOperation {
        void perform(FileLocks fileLocks, String fileName);
    }

}
