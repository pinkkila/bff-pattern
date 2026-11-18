package com.pinkkila.authorizationserver.security;

import com.jayway.jsonpath.JsonPath;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Runs the authorization code flow the way the BFF and the browser do: form login, an authorization request with PKCE,
 * consent, and the code exchange with the client secret. The client values match {@code application.yaml}.
 */
final class AuthorizationCodeFlow {

    static final String CLIENT_ID = "bff-client-id";
    static final String CLIENT_SECRET = "secret";
    static final String REDIRECT_URI = "http://localhost:8080/login/oauth2/code/bff-client";
    static final String STATE = "test-state";
    static final String NONCE = "test-nonce";
    static final String USERNAME = "user";

    private static final Pattern CONSENT_STATE = Pattern.compile("name=\"state\"[^>]*value=\"([^\"]+)\"");

    private final MockMvc mockMvc;

    AuthorizationCodeFlow(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    /**
     * Signs in as the demo user from {@code data.sql} and returns the login session.
     */
    MockHttpSession login() throws Exception {
        MockHttpSession session = new MockHttpSession();
        this.mockMvc.perform(post("/login")
                        .session(session)
                        .param("username", USERNAME)
                        .param("password", "password")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(authenticated());
        return session;
    }

    MockHttpServletRequestBuilder authorizationRequest(Pkce pkce, String redirectUri) {
        MockHttpServletRequestBuilder request = get("/oauth2/authorize")
                .queryParam("response_type", "code")
                .queryParam("client_id", CLIENT_ID)
                .queryParam("scope", "openid profile message:read message:write")
                .queryParam("state", STATE)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("nonce", NONCE);
        if (pkce != null) {
            request.queryParam("code_challenge", pkce.challenge())
                    .queryParam("code_challenge_method", "S256");
        }
        return request;
    }

    /**
     * Sends the authorization request, gives consent if the consent page is shown, and returns the redirect back to
     * the client. Consent is asked only once per user, so later requests in the same test context skip it.
     */
    String authorize(MockHttpSession session, Pkce pkce) throws Exception {
        MvcResult result = this.mockMvc.perform(authorizationRequest(pkce, REDIRECT_URI).session(session)).andReturn();
        if (result.getResponse().getStatus() == HttpStatus.OK.value()) {
            result = this.mockMvc.perform(post("/oauth2/authorize")
                            .session(session)
                            .param("client_id", CLIENT_ID)
                            .param("state", consentState(result.getResponse().getContentAsString()))
                            .param("scope", "profile", "message:read", "message:write"))
                    .andReturn();
        }
        assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.FOUND.value());
        return result.getResponse().getRedirectedUrl();
    }

    String authorizationCode(MockHttpSession session, Pkce pkce) throws Exception {
        String code = queryParam(authorize(session, pkce), "code");
        assertThat(code).isNotBlank();
        return code;
    }

    MockHttpServletRequestBuilder tokenRequest(String code, String codeVerifier, String clientSecret) {
        return post("/oauth2/token")
                .with(httpBasic(CLIENT_ID, clientSecret))
                .param("grant_type", "authorization_code")
                .param("code", code)
                .param("redirect_uri", REDIRECT_URI)
                .param("code_verifier", codeVerifier);
    }

    /**
     * Runs the whole flow and returns the tokens together with the login session they belong to.
     */
    Tokens obtainTokens() throws Exception {
        MockHttpSession session = login();
        Pkce pkce = Pkce.generate();
        String code = authorizationCode(session, pkce);
        String body = this.mockMvc.perform(tokenRequest(code, pkce.verifier(), CLIENT_SECRET))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return new Tokens(session, JsonPath.read(body, "$.access_token"), JsonPath.read(body, "$.refresh_token"),
                JsonPath.read(body, "$.id_token"));
    }

    static String queryParam(String url, String name) {
        return UriComponentsBuilder.fromUriString(url).build().getQueryParams().getFirst(name);
    }

    private static String consentState(String consentPage) {
        Matcher matcher = CONSENT_STATE.matcher(consentPage);
        assertThat(matcher.find()).as("state field on the consent page").isTrue();
        return matcher.group(1);
    }

    record Tokens(MockHttpSession session, String accessToken, String refreshToken, String idToken) {
    }

    /**
     * A PKCE code verifier and its S256 code challenge (RFC 7636).
     */
    record Pkce(String verifier, String challenge) {

        private static final SecureRandom RANDOM = new SecureRandom();

        static Pkce generate() throws NoSuchAlgorithmException {
            byte[] bytes = new byte[32];
            RANDOM.nextBytes(bytes);
            String verifier = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(verifier.getBytes(StandardCharsets.US_ASCII));
            return new Pkce(verifier, Base64.getUrlEncoder().withoutPadding().encodeToString(hash));
        }

    }

}
