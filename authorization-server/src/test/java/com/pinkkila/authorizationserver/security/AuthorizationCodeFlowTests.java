package com.pinkkila.authorizationserver.security;

import com.pinkkila.authorizationserver.TestcontainersConfiguration;
import com.pinkkila.authorizationserver.appuser.AppUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import static com.pinkkila.authorizationserver.security.AuthorizationCodeFlow.CLIENT_ID;
import static com.pinkkila.authorizationserver.security.AuthorizationCodeFlow.NONCE;
import static com.pinkkila.authorizationserver.security.AuthorizationCodeFlow.USERNAME;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@DisplayName("Authorization code flow")
class AuthorizationCodeFlowTests {

    private static final String ISSUER = "http://localhost:9000";
    private static final String RESOURCE_SERVER_AUDIENCE = "http://localhost:8090";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtDecoder jwtDecoder;

    @Autowired
    private AppUserRepository appUserRepository;

    private AuthorizationCodeFlow.Tokens tokens;

    @BeforeEach
    void obtainTokens() throws Exception {
        this.tokens = new AuthorizationCodeFlow(this.mockMvc).obtainTokens();
    }

    @Test
    @DisplayName("Should issue a signed access token for the resource server, with the user's ID and the granted scopes")
    void accessToken_HasIssuerAudienceUserIdAndScopes() {
        Jwt accessToken = this.jwtDecoder.decode(this.tokens.accessToken());
        String userId = this.appUserRepository.findByUsername(USERNAME).orElseThrow().getId().toString();

        assertThat(accessToken.getIssuer()).hasToString(ISSUER);
        assertThat(accessToken.getAudience()).containsExactly(RESOURCE_SERVER_AUDIENCE);
        assertThat(accessToken.getSubject()).isEqualTo(USERNAME);
        assertThat(accessToken.getClaimAsString("user_id")).isEqualTo(userId);
        assertThat(accessToken.getClaimAsStringList("scope"))
                .containsExactlyInAnyOrder("openid", "profile", "message:read", "message:write");
    }

    @Test
    @DisplayName("Should issue a signed ID token for the BFF only, with the nonce from the authorization request")
    void idToken_IsForTheBffOnly() {
        Jwt idToken = this.jwtDecoder.decode(this.tokens.idToken());

        assertThat(idToken.getIssuer()).hasToString(ISSUER);
        assertThat(idToken.getAudience()).containsExactly(CLIENT_ID);
        assertThat(idToken.getSubject()).isEqualTo(USERNAME);
        assertThat(idToken.getClaimAsString("nonce")).isEqualTo(NONCE);
        assertThat(idToken.hasClaim("user_id")).isFalse();
    }

}
