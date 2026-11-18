package com.pinkkila.bff.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.oauth2.client.ClientAuthorizationException;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.OidcLoginRequestPostProcessor;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestClientRegistrationConfig.class)
class InvalidRefreshTokenTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ClientRegistrationRepository clientRegistrationRepository;

    @MockitoBean
    private OAuth2AuthorizedClientManager authorizedClientManager;

    @Test
    void rejectedRefreshTokenEndsSessionAndReturns401() throws Exception {
        givenRefreshFailsWith(OAuth2ErrorCodes.INVALID_GRANT);
        MockHttpSession session = new MockHttpSession();

        this.mockMvc.perform(get("/api/messages").session(session).with(login()))
                .andExpect(status().isUnauthorized());

        assertThat(session.isInvalid()).isTrue();
    }

    @Test
    void otherRefreshErrorKeepsSession() {
        givenRefreshFailsWith(OAuth2ErrorCodes.SERVER_ERROR);
        MockHttpSession session = new MockHttpSession();

        assertThatThrownBy(() -> this.mockMvc.perform(get("/api/messages").session(session).with(login())))
                .rootCause()
                .isInstanceOf(ClientAuthorizationException.class);

        assertThat(session.isInvalid()).isFalse();
    }

    private void givenRefreshFailsWith(String errorCode) {
        given(this.authorizedClientManager.authorize(any()))
                .willThrow(new ClientAuthorizationException(new OAuth2Error(errorCode), "bff-client"));
    }

    private OidcLoginRequestPostProcessor login() {
        return oidcLogin().clientRegistration(this.clientRegistrationRepository.findByRegistrationId("bff-client"));
    }

}
