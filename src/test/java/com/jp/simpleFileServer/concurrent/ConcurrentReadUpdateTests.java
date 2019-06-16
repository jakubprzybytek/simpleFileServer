package com.jp.simpleFileServer.concurrent;

import com.jp.simpleFileServer.ConcurrentTask;
import com.jp.simpleFileServer.TestUtils;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.*;

import static io.restassured.RestAssured.delete;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConcurrentReadUpdateTests {

    private String remoteFileName;

    private ExecutorService executor;

    @BeforeEach
    public void prepareTestFile() {
        this.remoteFileName = TestUtils.randomFileName();
        given().multiPart("data", TestUtils.FIRST_FILE, "text/html")
                .expect().statusCode(201)
                .when().post(this.remoteFileName);

        this.executor = Executors.newFixedThreadPool(3);
    }

    @AfterEach
    public void removeTestFile() throws InterruptedException {
        this.executor.shutdown();
        this.executor.awaitTermination(5, TimeUnit.SECONDS);

        // remove if not removed yet
        delete(this.remoteFileName);
    }

    @Test
    @DisplayName("Should ensure correct upgrade behaviour for concurrent requests")
    public void testSingleThreadRoutine() throws InterruptedException, ExecutionException {

        Future<Response> firstGetRequestFuture =
                this.executor.submit(ConcurrentTask.GET(1, this.remoteFileName));

        TimeUnit.MILLISECONDS.sleep(500);

        Future<Response> putRequestFuture =
                this.executor.submit(ConcurrentTask.PUT(2, this.remoteFileName, TestUtils.SECOND_FILE));

        TimeUnit.MILLISECONDS.sleep(500);

        Future<Response> secondGetRequestFuture =
                this.executor.submit(ConcurrentTask.GET(3, this.remoteFileName));

        firstGetRequestFuture.get().then().statusCode(200);
        assertArrayEquals(TestUtils.readBytes(TestUtils.FIRST_FILE), firstGetRequestFuture.get().asByteArray(), "Task 1");

        putRequestFuture.get().then().statusCode(201);

        firstGetRequestFuture.get().then().statusCode(200);
        assertArrayEquals(TestUtils.readBytes(TestUtils.SECOND_FILE), secondGetRequestFuture.get().asByteArray(), "Task 2");
    }
}
