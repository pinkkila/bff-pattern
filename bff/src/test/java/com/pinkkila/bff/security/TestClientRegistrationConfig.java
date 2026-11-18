package com.pinkkila.bff.security;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;

import java.util.Map;

/**
 * A client registration that doesn't need the authorization server running at startup.
 */
@TestConfiguration(proxyBeanMethods = false)
public class TestClientRegistrationConfig {

    public static final String END_SESSION_ENDPOINT = "http://localhost:9000/connect/logout";

    @Bean
    ClientRegistrationRepository clientRegistrationRepository() {
        ClientRegistration registration = ClientRegistration.withRegistrationId("bff-client")
                .clientId("bff-client-id")
                .clientSecret("secret")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                .scope("openid", "profile", "message:read", "message:write")
                .authorizationUri("http://localhost:9000/oauth2/authorize")
                .tokenUri("http://localhost:9000/oauth2/token")
                .jwkSetUri("http://localhost:9000/oauth2/jwks")
                .userNameAttributeName("sub")
                .providerConfigurationMetadata(Map.of("end_session_endpoint", END_SESSION_ENDPOINT))
                .build();
        return new InMemoryClientRegistrationRepository(registration);
    }

}
