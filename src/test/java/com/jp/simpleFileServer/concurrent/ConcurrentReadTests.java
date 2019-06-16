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

public class ConcurrentReadTests {

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

        delete(this.remoteFileName).then().statusCode(200);
    }

    @Test
    @DisplayName("Should allow for concurrent non-blocking read access")
    public void testSingleThreadRoutine() throws InterruptedException, ExecutionException {
        Future<Response> firstRequestFuture = this.executor.submit(ConcurrentTask.GET(1, this.remoteFileName));
        Future<Response> secondRequestFuture = this.executor.submit(ConcurrentTask.GET(2, this.remoteFileName));
        Future<Response> thirdRequestFuture = this.executor.submit(ConcurrentTask.GET(3, this.remoteFileName));

        assertEquals(200, firstRequestFuture.get().getStatusCode());
        assertArrayEquals(TestUtils.readBytes(TestUtils.FIRST_FILE), firstRequestFuture.get().asByteArray());

        assertEquals(200, secondRequestFuture.get().getStatusCode());
        assertArrayEquals(TestUtils.readBytes(TestUtils.FIRST_FILE), secondRequestFuture.get().asByteArray());

        assertEquals(200, thirdRequestFuture.get().getStatusCode());
        assertArrayEquals(TestUtils.readBytes(TestUtils.FIRST_FILE), thirdRequestFuture.get().asByteArray());
    }
}
