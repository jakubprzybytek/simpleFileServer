package com.jp.simpleFileServer;

import io.restassured.response.Response;

import java.io.File;
import java.util.concurrent.Callable;

import static io.restassured.RestAssured.*;

public class ConcurrentTask {

    public static Callable<Response> GET(int taskNumber, String remoteFileName) {
        return () -> {
            System.out.println(String.format("%s [%d] GET for '%s'",
                    TestUtils.getNowString(), taskNumber, remoteFileName));

            Response response = given()
                    .header("emulate-long-processing", 2) // tell server to pretend long processing
                    .get(remoteFileName);

            System.out.println(String.format("%s [%d] GET ended '%s' with [%d]",
                    TestUtils.getNowString(), taskNumber, remoteFileName, response.getStatusCode()));

            return response;
        };
    }

    public static Callable<Response> DELETE(int taskNumber, String remoteFileName) {
        return () -> {
            System.out.println(String.format("%s [%d] DELETE for '%s'",
                    TestUtils.getNowString(), taskNumber, remoteFileName));

            Response response = delete(remoteFileName);

            System.out.println(String.format("%s [%d] DELETE ended '%s' with [%d]",
                    TestUtils.getNowString(), taskNumber, remoteFileName, response.getStatusCode()));

            return response;
        };
    }

    public static Callable<Response> PUT(int taskNumber, String remoteFileName, File file) {
        return () -> {
            System.out.println(String.format("%s [%d] PUT for '%s'",
                    TestUtils.getNowString(), taskNumber, remoteFileName));

            Response response = given().multiPart("data", file, "text/html")
                    .expect().statusCode(201)
                    .when().put(remoteFileName);

            System.out.println(String.format("%s [%d] PUT ended '%s' with [%d]",
                    TestUtils.getNowString(), taskNumber, remoteFileName, response.getStatusCode()));

            return response;
        };
    }

}
