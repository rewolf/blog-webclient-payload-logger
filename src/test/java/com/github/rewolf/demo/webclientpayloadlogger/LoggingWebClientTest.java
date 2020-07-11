package com.github.rewolf.demo.webclientpayloadlogger;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.*;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class LoggingWebClientTest {
    static final TestModel TEST_DATA = new TestModel("Smith");
    static MockWebServer mockBackEnd;

    @BeforeAll
    static void setUp() throws IOException {
        mockBackEnd = new MockWebServer();
        mockBackEnd.start();
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockBackEnd.shutdown();
    }

    @Test
    @DisplayName("Should log the same JSON as received by the server for the request")
    public void postPayloadLoggedAfterEncoding() throws Exception {
        mockBackEnd.enqueue(new MockResponse().setBody("").addHeader("Content-Type", "application/json"));
        final StringBuffer loggedJsonBuffer = new StringBuffer();
        final LoggingJsonEncoder encoder = new LoggingJsonEncoder(
                data -> loggedJsonBuffer.append(new String(data)));
        final WebClient webClient = WebClient.builder()
                                       .baseUrl("http://localhost:" + mockBackEnd.getPort() + "/")
                                       .codecs(c -> c.defaultCodecs().jackson2JsonEncoder(encoder))
                                       .build();

        webClient.post()
                 .uri("/aa")
                 .contentType(MediaType.APPLICATION_JSON)
                 .body(BodyInserters.fromValue(TEST_DATA))
                 .exchange()
                 .block()
                 .releaseBody()
                 .block();

        final String transmittedJson = mockBackEnd.takeRequest().getBody().readString(StandardCharsets.UTF_8);
        assertEquals(transmittedJson, loggedJsonBuffer.toString());
    }

    @Test
    @DisplayName("Should log the same data as sent by the server in the response")
    public void responseLoggedBeforeDecoding() throws Exception {
        final StringBuffer loggedJsonBuffer = new StringBuffer();
        final LoggingJsonDecoder decoder = new LoggingJsonDecoder(
                data -> loggedJsonBuffer.append(new String(data)));
        WebClient webClient = WebClient.builder()
                                       .baseUrl("http://localhost:" + mockBackEnd.getPort() + "/")
                                       .codecs(c -> c.defaultCodecs().jackson2JsonDecoder(decoder))
                                       .build();
        final String responseJsonStub = new ObjectMapper().writeValueAsString(TEST_DATA);
        mockBackEnd.enqueue(new MockResponse()
                                    .setBody(responseJsonStub)
                                    .addHeader("Content-Type", "application/json"));

        final TestModel parsedData = webClient.get()
                                              .uri("/aa")
                                              .accept(MediaType.APPLICATION_JSON)
                                              .exchange()
                                              .block()
                                              .bodyToMono(TestModel.class)
                                              .block();

        mockBackEnd.takeRequest();
        assertEquals(TEST_DATA, parsedData);
        assertEquals(responseJsonStub, loggedJsonBuffer.toString());
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class TestModel {
        String name;
    }
}
