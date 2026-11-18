package com.pinkkila.bff.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Runs on a real server, because MockMvc doesn't create the container's session cookie.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestClientRegistrationConfig.class)
@DisplayName("Session cookie")
class SessionCookieTests {

    @Value("${local.server.port}")
    private int port;

    @Test
    @DisplayName("Should be HttpOnly, SameSite=Strict and host-only with Path=/")
    void sessionCookie_IsHttpOnlySameSiteStrictAndHostOnly() throws Exception {
        List<String> attributes = cookieAttributes(sessionCookie());

        assertThat(attributes).contains("Path=/", "HttpOnly", "SameSite=Strict");
        assertThat(attributes).noneMatch(attribute -> attribute.regionMatches(true, 0, "Domain=", 0, 7));
    }

    // Starting a login stores the authorization request in a new session
    private String sessionCookie() throws Exception {
        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest request = HttpRequest.newBuilder(
                    URI.create("http://localhost:" + this.port + "/oauth2/authorization/bff-client")).build();
            HttpResponse<Void> response = client.send(request, HttpResponse.BodyHandlers.discarding());
            return response.headers().allValues(HttpHeaders.SET_COOKIE).stream()
                    .filter(cookie -> cookie.startsWith("SESSION_BFF="))
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("No SESSION_BFF cookie in " + response.headers()));
        }
    }

    private static List<String> cookieAttributes(String setCookie) {
        return Arrays.stream(setCookie.split(";")).map(String::trim).toList();
    }

}
