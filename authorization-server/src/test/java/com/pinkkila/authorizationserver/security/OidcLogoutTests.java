package com.pinkkila.authorizationserver.security;

import com.pinkkila.authorizationserver.TestcontainersConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@DisplayName("OpenID Connect RP-Initiated Logout")
class OidcLogoutTests {

    private static final String SPA_URI = "http://localhost:5173";

    @Autowired
    private MockMvc mockMvc;

    private AuthorizationCodeFlow.Tokens tokens;

    @BeforeEach
    void obtainTokens() throws Exception {
        this.tokens = new AuthorizationCodeFlow(this.mockMvc).obtainTokens();
    }

    @Test
    @DisplayName("Should end the login session and redirect to the SPA, so the next login asks for the password")
    void logout_EndsLoginSessionAndRedirectsToSpa() throws Exception {
        this.mockMvc.perform(logoutRequest(SPA_URI))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl(SPA_URI));

        assertThat(this.tokens.session().isInvalid()).isTrue();
    }

    @Test
    @DisplayName("Should answer 400 and keep the session when the post-logout redirect URI isn't registered")
    void unregisteredPostLogoutRedirectUri_Returns400() throws Exception {
        this.mockMvc.perform(logoutRequest("https://attacker.example"))
                .andExpect(status().isBadRequest())
                .andExpect(header().doesNotExist(HttpHeaders.LOCATION));

        assertThat(this.tokens.session().isInvalid()).isFalse();
    }

    private MockHttpServletRequestBuilder logoutRequest(String postLogoutRedirectUri) {
        return get("/connect/logout")
                .queryParam("id_token_hint", this.tokens.idToken())
                .queryParam("post_logout_redirect_uri", postLogoutRedirectUri)
                .session(this.tokens.session());
    }

}
