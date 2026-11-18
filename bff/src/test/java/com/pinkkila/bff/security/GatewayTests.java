package com.pinkkila.bff.security;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;
import org.wiremock.spring.WireMockConfigurationCustomizer;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.anyRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.http.clients.imperative.factory=jdk")
@AutoConfigureMockMvc
@Import(TestClientRegistrationConfig.class)
@EnableWireMock(@ConfigureWireMock(name = "downstream",
        baseUrlProperties = {"app.resource-server-uri", "app.authorization-server-uri"},
        configurationCustomizers = GatewayTests.NoHttp2Upgrade.class))
@DisplayName("Gateway routes")
class GatewayTests {

    private static final String ACCESS_TOKEN = "test-access-token";
    private static final String CSRF_TOKEN = "test-csrf-token";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ClientRegistrationRepository clientRegistrationRepository;

    @Autowired
    private OAuth2AuthorizedClientRepository authorizedClientRepository;

    @InjectWireMock("downstream")
    private WireMockServer downstream;

    private MockHttpSession session;

    private OAuth2AuthenticationToken user;

    @BeforeEach
    void login() {
        OAuth2User principal = new DefaultOAuth2User(AuthorityUtils.createAuthorityList("OAUTH2_USER"),
                Map.of("sub", "user"), "sub");
        this.user = new OAuth2AuthenticationToken(principal, principal.getAuthorities(), "bff-client");

        OAuth2AccessToken accessToken = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, ACCESS_TOKEN,
                Instant.now(), Instant.now().plus(Duration.ofMinutes(5)));
        OAuth2AuthorizedClient authorizedClient = new OAuth2AuthorizedClient(
                this.clientRegistrationRepository.findByRegistrationId("bff-client"), "user", accessToken);
        this.session = new MockHttpSession();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setSession(this.session);
        this.authorizedClientRepository.saveAuthorizedClient(authorizedClient, this.user, request,
                new MockHttpServletResponse());
    }

    @Test
    @DisplayName("Should relay an API request to the resource server with the access token instead of the browser's cookies")
    void apiRequest_IsRelayedWithAccessTokenWithoutCookies() throws Exception {
        this.downstream.stubFor(WireMock.get("/messages").willReturn(okJson("{\"content\":[]}")));

        this.mockMvc.perform(fromBrowser(get("/api/messages")))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"content\":[]}"));

        this.downstream.verify(getRequestedFor(urlEqualTo("/messages"))
                .withHeader(HttpHeaders.AUTHORIZATION, equalTo("Bearer " + ACCESS_TOKEN))
                .withoutHeader(HttpHeaders.COOKIE));
    }

    @Test
    @DisplayName("Should relay a userinfo request to the authorization server with the access token instead of the browser's cookies")
    void userinfoRequest_IsRelayedWithAccessTokenWithoutCookies() throws Exception {
        this.downstream.stubFor(WireMock.get("/userinfo").willReturn(okJson("{\"sub\":\"user\"}")));

        this.mockMvc.perform(fromBrowser(get("/userinfo")))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"sub\":\"user\"}"));

        this.downstream.verify(getRequestedFor(urlEqualTo("/userinfo"))
                .withHeader(HttpHeaders.AUTHORIZATION, equalTo("Bearer " + ACCESS_TOKEN))
                .withoutHeader(HttpHeaders.COOKIE));
    }

    @Test
    @DisplayName("Should relay a state-changing request that has the CSRF token in the custom header")
    void postWithCsrfHeader_IsRelayed() throws Exception {
        this.downstream.stubFor(WireMock.post("/messages").willReturn(aResponse().withStatus(201)));

        this.mockMvc.perform(fromBrowser(newMessage()).header("X-XSRF-TOKEN", CSRF_TOKEN))
                .andExpect(status().isCreated());

        this.downstream.verify(postRequestedFor(urlEqualTo("/messages")));
    }

    @Test
    @DisplayName("Should answer 403 and relay nothing when a state-changing request lacks the custom CSRF header")
    void postWithoutCsrfHeader_Returns403() throws Exception {
        this.mockMvc.perform(fromBrowser(newMessage()))
                .andExpect(status().isForbidden());

        this.downstream.verify(0, anyRequestedFor(anyUrl()));
    }

    private MockHttpServletRequestBuilder fromBrowser(MockHttpServletRequestBuilder request) {
        return request.session(this.session)
                .with(authentication(this.user))
                .cookie(new Cookie("SESSION_BFF", "browser-session-id"), new Cookie("XSRF-TOKEN", CSRF_TOKEN));
    }

    private static MockHttpServletRequestBuilder newMessage() {
        return post("/api/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"content\":\"Hello\"}");
    }

    /**
     * The JDK client offers to switch each request to HTTP/2. WireMock's server accepts and then resets the
     * connection, so the switch is turned off here. The real services keep using HTTP/1.1 either way.
     */
    public static class NoHttp2Upgrade implements WireMockConfigurationCustomizer {

        @Override
        public void customize(WireMockConfiguration configuration, ConfigureWireMock options) {
            configuration.http2PlainDisabled(true);
        }

    }

}
