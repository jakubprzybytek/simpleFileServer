package com.jp.simpleFileServer.concurrent;

import com.jp.simpleFileServer.TestUtils;
import com.jp.simpleFileServer.ConcurrentTask;
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

public class ConcurrentReadDeleteTests {

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

        // remove if failed test did not removed it yet
        delete(this.remoteFileName);
    }

    // I've just learnt that REST Assured is not fully thread-safe... D'oh!
    @Test
    @DisplayName("Should ensure correct delete behaviour for concurrent requests")
    public void testSingleThreadRoutine() throws InterruptedException, ExecutionException {

        Future<Response> firstGetRequestFuture =
                this.executor.submit(ConcurrentTask.GET(1, this.remoteFileName));

        TimeUnit.MILLISECONDS.sleep(500);

        Future<Response> deleteRequestFuture =
                this.executor.submit(ConcurrentTask.DELETE(2, this.remoteFileName));

        TimeUnit.MILLISECONDS.sleep(500);

        Future<Response> secondGetRequestFuture =
                this.executor.submit(ConcurrentTask.GET(3, this.remoteFileName));

        firstGetRequestFuture.get().then().statusCode(200);
        assertArrayEquals(TestUtils.readBytes(TestUtils.FIRST_FILE), firstGetRequestFuture.get().asByteArray(), "Task 1");

        deleteRequestFuture.get().then().statusCode(200);

        assertEquals(404, secondGetRequestFuture.get().getStatusCode(), "Task 3");
    }
}
