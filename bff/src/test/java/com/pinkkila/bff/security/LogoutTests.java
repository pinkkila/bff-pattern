package com.pinkkila.bff.security;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.OidcLoginRequestPostProcessor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestClientRegistrationConfig.class)
class LogoutTests {

    private static final String SPA_URI = "http://localhost:5173";
    private static final String CSRF_TOKEN = "test-csrf-token";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ClientRegistrationRepository clientRegistrationRepository;

    @Autowired
    private OAuth2AuthorizedClientRepository authorizedClientRepository;

    @Test
    void logoutEndsSessionAndReturnsAuthorizationServerLogoutUrl() throws Exception {
        MockHttpSession session = new MockHttpSession();

        this.mockMvc.perform(logoutRequest().session(session).with(login("id-token-value")))
                .andExpect(status().isAccepted())
                .andExpect(header().string(HttpHeaders.LOCATION, TestClientRegistrationConfig.END_SESSION_ENDPOINT
                        + "?id_token_hint=id-token-value&post_logout_redirect_uri=" + SPA_URI));

        assertThat(session.isInvalid()).isTrue();
    }

    @Test
    void logoutExposesLocationHeaderToSpa() throws Exception {
        this.mockMvc.perform(logoutRequest().header(HttpHeaders.ORIGIN, SPA_URI).with(login("id-token-value")))
                .andExpect(status().isAccepted())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.LOCATION));
    }

    @Test
    void logoutWithoutSessionReturnsSpaUrl() throws Exception {
        this.mockMvc.perform(logoutRequest())
                .andExpect(status().isAccepted())
                .andExpect(header().string(HttpHeaders.LOCATION, SPA_URI));
    }

    @Test
    void logoutWithoutCsrfTokenIsForbidden() throws Exception {
        this.mockMvc.perform(post("/logout").with(login("id-token-value")))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist(HttpHeaders.LOCATION));
    }

    @Test
    void tokensAreStoredInSession() {
        assertThat(this.authorizedClientRepository).isInstanceOf(HttpSessionOAuth2AuthorizedClientRepository.class);
    }

    private static MockHttpServletRequestBuilder logoutRequest() {
        return post("/logout")
                .cookie(new Cookie("XSRF-TOKEN", CSRF_TOKEN))
                .header("X-XSRF-TOKEN", CSRF_TOKEN);
    }

    private OidcLoginRequestPostProcessor login(String idTokenValue) {
        return oidcLogin()
                .clientRegistration(this.clientRegistrationRepository.findByRegistrationId("bff-client"))
                .idToken(idToken -> idToken.tokenValue(idTokenValue));
    }

}
