package com.pinkkila.resourceserver.security;

import com.pinkkila.resourceserver.TestcontainersConfiguration;
import com.pinkkila.resourceserver.mockauthserver.JwtTestHelper;
import com.pinkkila.resourceserver.userid.UserId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;
import java.util.UUID;

import static com.pinkkila.resourceserver.mockauthserver.JwtTestHelper.DEFAULT_AUDIENCE;
import static com.pinkkila.resourceserver.mockauthserver.JwtTestHelper.DEFAULT_ISSUER;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
@DisplayName("JWT issuer and audience validation")
class JwtValidationTests {

    private final UserId userId = new UserId(UUID.randomUUID());

    @Autowired
    private WebTestClient webTestClient;

    @Test
    @DisplayName("Should accept a token with the expected issuer and audience")
    void expectedIssuerAndAudience_Returns200() throws Exception {
        String token = token(DEFAULT_ISSUER, List.of(DEFAULT_AUDIENCE));

        getMessages(token).expectStatus().isOk();
    }

    @Test
    @DisplayName("Should reject a token from another issuer, even when it is signed with a trusted key")
    void otherIssuer_Returns401() throws Exception {
        String token = token("http://127.0.0.1:9000", List.of(DEFAULT_AUDIENCE));

        getMessages(token).expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("Should reject a token meant for another audience, such as an ID token for the BFF")
    void otherAudience_Returns401() throws Exception {
        String token = token(DEFAULT_ISSUER, List.of("bff-client-id"));

        getMessages(token).expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("Should reject a token without an audience")
    void noAudience_Returns401() throws Exception {
        String token = token(DEFAULT_ISSUER, null);

        getMessages(token).expectStatus().isUnauthorized();
    }

    private String token(String issuer, List<String> audience) throws Exception {
        return JwtTestHelper.generateToken(this.userId.toString(), List.of("message:read"), issuer, audience,
                this.userId);
    }

    private WebTestClient.ResponseSpec getMessages(String token) {
        return this.webTestClient.get()
                .uri("/messages")
                .header("Authorization", "Bearer " + token)
                .exchange();
    }

}
