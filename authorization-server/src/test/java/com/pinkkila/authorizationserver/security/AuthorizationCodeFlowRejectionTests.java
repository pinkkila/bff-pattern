package com.pinkkila.authorizationserver.security;

import com.pinkkila.authorizationserver.TestcontainersConfiguration;
import com.pinkkila.authorizationserver.security.AuthorizationCodeFlow.Pkce;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import static com.pinkkila.authorizationserver.security.AuthorizationCodeFlow.CLIENT_SECRET;
import static com.pinkkila.authorizationserver.security.AuthorizationCodeFlow.REDIRECT_URI;
import static com.pinkkila.authorizationserver.security.AuthorizationCodeFlow.STATE;
import static com.pinkkila.authorizationserver.security.AuthorizationCodeFlow.queryParam;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@DisplayName("Authorization code flow rejections")
class AuthorizationCodeFlowRejectionTests {

    @Autowired
    private MockMvc mockMvc;

    private AuthorizationCodeFlow flow;

    private MockHttpSession session;

    @BeforeEach
    void login() throws Exception {
        this.flow = new AuthorizationCodeFlow(this.mockMvc);
        this.session = this.flow.login();
    }

    @Test
    @DisplayName("Should answer 400 without redirecting when the redirect URI isn't registered, so it can't be an open redirect")
    void unregisteredRedirectUri_Returns400WithoutRedirect() throws Exception {
        this.mockMvc.perform(this.flow.authorizationRequest(Pkce.generate(), "https://attacker.example/callback")
                        .session(this.session))
                .andExpect(status().isBadRequest())
                .andExpect(header().doesNotExist(HttpHeaders.LOCATION));
    }

    @Test
    @DisplayName("Should reject an authorization request without a PKCE code challenge")
    void missingCodeChallenge_RedirectsWithInvalidRequest() throws Exception {
        String redirect = this.mockMvc.perform(this.flow.authorizationRequest(null, REDIRECT_URI).session(this.session))
                .andExpect(status().isFound())
                .andReturn()
                .getResponse()
                .getRedirectedUrl();

        assertThat(redirect).startsWith(REDIRECT_URI);
        assertThat(queryParam(redirect, "error")).isEqualTo("invalid_request");
        assertThat(queryParam(redirect, "state")).isEqualTo(STATE);
        assertThat(queryParam(redirect, "code")).isNull();
    }

    @Test
    @DisplayName("Should reject a code exchange with the wrong PKCE code verifier")
    void wrongCodeVerifier_ReturnsInvalidGrant() throws Exception {
        String code = this.flow.authorizationCode(this.session, Pkce.generate());

        this.mockMvc.perform(this.flow.tokenRequest(code, Pkce.generate().verifier(), CLIENT_SECRET))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("invalid_grant"));
    }

    @Test
    @DisplayName("Should reject an authorization code that has already been used")
    void reusedCode_ReturnsInvalidGrant() throws Exception {
        Pkce pkce = Pkce.generate();
        String code = this.flow.authorizationCode(this.session, pkce);

        this.mockMvc.perform(this.flow.tokenRequest(code, pkce.verifier(), CLIENT_SECRET))
                .andExpect(status().isOk());
        this.mockMvc.perform(this.flow.tokenRequest(code, pkce.verifier(), CLIENT_SECRET))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("invalid_grant"));
    }

    @Test
    @DisplayName("Should reject a valid code when the client secret is wrong")
    void wrongClientSecret_Returns401() throws Exception {
        Pkce pkce = Pkce.generate();
        String code = this.flow.authorizationCode(this.session, pkce);

        this.mockMvc.perform(this.flow.tokenRequest(code, pkce.verifier(), "wrong-secret"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("invalid_client"));
    }

}
