/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.quarkus.component.flink.it;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;

@QuarkusTest
class FlinkTest {

    @Test
    public void dataSetCallback() throws IOException {
        Path path = Files.createTempFile("fileDataSet", ".txt");
        String text = "foo\n"
                + "bar\n"
                + "baz\n"
                + "qux\n"
                + "quux";
        Files.write(path, text.getBytes(StandardCharsets.UTF_8));
        RestAssured.given()
                .contentType(ContentType.TEXT)
                .post("/flink/dataset/{fileName}", path.toAbsolutePath().toString())
                .then()
                .statusCode(200)
                .and()
                .body(greaterThanOrEqualTo("5"));

    }

    @Test
    public void dataStreamCallback() throws IOException {
        String text = "Hello!!Camel flink!";
        RestAssured.given()
                .contentType(ContentType.TEXT)
                .body(text)
                .post("/flink/datastream")
                .then()
                .statusCode(200)
                .and()
                .body(greaterThanOrEqualTo("1"));

    }

}
