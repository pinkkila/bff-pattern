package com.pinkkila.bff.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestClientRegistrationConfig.class)
@DisplayName("Login")
class LoginTests {

    private static final String LOGIN_PATH = "/oauth2/authorization/bff-client";

    @Autowired
    private MockMvc mockMvc;

    @ParameterizedTest(name = "Accept: {0}")
    @ValueSource(strings = {"*/*", "application/json"})
    @DisplayName("Should answer an API request without a session with 401, not a redirect, so the SPA can show the logged-out view")
    void apiRequestWithoutSession_Returns401(String accept) throws Exception {
        this.mockMvc.perform(get("/api/messages").header(HttpHeaders.ACCEPT, accept))
                .andExpect(status().isUnauthorized())
                .andExpect(header().doesNotExist(HttpHeaders.LOCATION));
    }

    @Test
    @DisplayName("Should redirect a browser navigation without a session to the login")
    void navigationWithoutSession_RedirectsToLogin() throws Exception {
        this.mockMvc.perform(get("/").header(HttpHeaders.ACCEPT,
                        "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl(LOGIN_PATH));
    }

    @Test
    @DisplayName("Should send the browser to the authorization server with a PKCE challenge, state and nonce")
    void login_RedirectsToAuthorizationServerWithPkceStateAndNonce() throws Exception {
        String location = this.mockMvc.perform(get(LOGIN_PATH))
                .andExpect(status().isFound())
                .andReturn()
                .getResponse()
                .getRedirectedUrl();

        UriComponents redirect = UriComponentsBuilder.fromUriString(location).build();
        MultiValueMap<String, String> params = redirect.getQueryParams();
        assertThat(location).startsWith("http://localhost:9000/oauth2/authorize?");
        assertThat(params.getFirst("response_type")).isEqualTo("code");
        assertThat(params.getFirst("client_id")).isEqualTo("bff-client-id");
        assertThat(decode(params.getFirst("redirect_uri"))).isEqualTo("http://localhost/login/oauth2/code/bff-client");
        assertThat(decode(params.getFirst("scope"))).isEqualTo("openid profile message:read message:write");
        assertThat(params.getFirst("state")).isNotBlank();
        assertThat(params.getFirst("nonce")).isNotBlank();
        assertThat(params.getFirst("code_challenge")).isNotBlank();
        assertThat(params.getFirst("code_challenge_method")).isEqualTo("S256");
    }

    private static String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

}
