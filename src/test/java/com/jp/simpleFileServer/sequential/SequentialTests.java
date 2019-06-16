package com.jp.simpleFileServer.sequential;

import com.jp.simpleFileServer.TestUtils;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;

import static io.restassured.RestAssured.*;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class SequentialTests {

    @Test
    @DisplayName("Should return 400 on invalid request")
    public void testInvalidRequests() {
        get("").then().statusCode(400);
        get("/").then().statusCode(400);
        post("").then().statusCode(400);
        post("/").then().statusCode(400);
        put("").then().statusCode(400);
        put("/").then().statusCode(400);
        delete("").then().statusCode(400);
        delete("/").then().statusCode(400);
    }

    @Test
    @DisplayName("Should perform POST")
    public void testPost() throws IOException {
        String remoteFileName = TestUtils.randomFileName();

        // create first version of file
        given().multiPart("data", TestUtils.FIRST_FILE, "text/html")
                .expect().statusCode(201)
                .when().post(remoteFileName);

        // get the file and validate
        Response response = get(remoteFileName);
        response.then().statusCode(200);
        assertArrayEquals(Files.readAllBytes(TestUtils.FIRST_FILE.toPath()), response.asByteArray());

        // try to create again and fail
        given().multiPart("data", TestUtils.FIRST_FILE, "text/html")
                .expect().statusCode(409)
                .when().post(remoteFileName);

        // delete file
        delete(remoteFileName).then().statusCode(200);

        // file does not exist anymore
        get(remoteFileName).then().statusCode(404);
    }

    @Test
    @DisplayName("Should perform PUT")
    public void testPut() throws IOException {
        String remoteFileName = TestUtils.randomFileName();

        // try to put to not existing file and fail
        given().multiPart("data", TestUtils.FIRST_FILE, "text/html")
                .expect().statusCode(404)
                .when().put(remoteFileName);

        // create first version of file
        given().multiPart("data", TestUtils.FIRST_FILE, "text/html")
                .expect().statusCode(201)
                .when().post(remoteFileName);

        // get the file and validate
        Response response = get(remoteFileName);
        response.then().statusCode(200);
        assertArrayEquals(Files.readAllBytes(TestUtils.FIRST_FILE.toPath()), response.asByteArray());

        // put to update the file
        given().multiPart("data", TestUtils.SECOND_FILE, "text/html")
                .expect().statusCode(201)
                .when().put(remoteFileName);

        // get the file and validate
        response = get(remoteFileName);
        response.then().statusCode(200);
        assertArrayEquals(Files.readAllBytes(TestUtils.SECOND_FILE.toPath()), response.asByteArray());

        // delete file
        delete(remoteFileName).then().statusCode(200);

        // file does not exist anymore
        get(remoteFileName).then().statusCode(404);
    }

    @Test
    @DisplayName("Should perform DELETE")
    public void testDelete() throws IOException {

        String remoteFileName = TestUtils.randomFileName();

        // try to delete file before it is created
        delete(remoteFileName).then().statusCode(404);

        // create first version of file
        given().multiPart("data", TestUtils.FIRST_FILE, "text/html")
                .expect().statusCode(201)
                .when().post(remoteFileName);

        // get the file and validate
        Response response = get(remoteFileName);
        response.then().statusCode(200);
        assertArrayEquals(Files.readAllBytes(TestUtils.FIRST_FILE.toPath()), response.asByteArray());

        // delete file
        delete(remoteFileName).then().statusCode(200);

        // try to delete file again
        delete(remoteFileName).then().statusCode(404);

        // file does not exist anymore
        get(remoteFileName).then().statusCode(404);
    }

    @Test
    @DisplayName("Should perform all operations in sequence")
    public void testSingleThreadRoutine() throws IOException {

        String remoteFileName = TestUtils.randomFileName();

        // file does not exist yet
        get(remoteFileName).then().statusCode(404);

        // create first version of file
        given().multiPart("data", TestUtils.FIRST_FILE, "text/html")
                .expect().statusCode(201)
                .when().post(remoteFileName);

        // try to create it again
        given().multiPart("data", TestUtils.FIRST_FILE, "text/html")
                .expect().statusCode(409)
                .when().post(remoteFileName);

        // get the file and validate
        Response response = get(remoteFileName);
        response.then().statusCode(200);
        assertArrayEquals(Files.readAllBytes(TestUtils.FIRST_FILE.toPath()), response.asByteArray());

        // put to update the file
        given().multiPart("data", TestUtils.SECOND_FILE, "text/html")
                .expect().statusCode(201)
                .when().put(remoteFileName);

        // get the file and validate
        response = get(remoteFileName);
        response.then().statusCode(200);
        assertArrayEquals(Files.readAllBytes(TestUtils.SECOND_FILE.toPath()), response.asByteArray());

        // delete file
        delete(remoteFileName).then().statusCode(200);

        // try to delete file again
        delete(remoteFileName).then().statusCode(404);

        // file does not exist anymore
        get(remoteFileName).then().statusCode(404);
    }
}
