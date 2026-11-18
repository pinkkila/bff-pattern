package com.pinkkila.bff.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.DelegatingAuthenticationEntryPoint;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;

@Configuration(proxyBeanMethods = false)
@EnableWebSecurity
public class SecurityConfig {

    @Value("${app.base-uri}")
    private String appBaseUri;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   ClientRegistrationRepository clientRegistrationRepository) {
        // @formatter:off
        http
                .authorizeHttpRequests(authorize ->
                        authorize
                                .anyRequest().authenticated()
                )
                .csrf(CsrfConfigurer::spa)
                .cors(Customizer.withDefaults())
                .exceptionHandling(exceptionHandling ->
                        exceptionHandling
                                .authenticationEntryPoint(authenticationEntryPoint())
                )
                .oauth2Login(oauth2Login ->
                        oauth2Login
                                .successHandler(new SimpleUrlAuthenticationSuccessHandler(this.appBaseUri)))
                .logout(logout ->
                        logout
                                .logoutSuccessHandler(oidcLogoutSuccessHandler(clientRegistrationRepository))
                )
                .oauth2Client(Customizer.withDefaults());
        // @formatter:on
        return http.build();
    }

    @Bean
    public OAuth2AuthorizedClientRepository authorizedClientRepository() {
        return new HttpSessionOAuth2AuthorizedClientRepository();
    }

    private AuthenticationEntryPoint authenticationEntryPoint() {
        return DelegatingAuthenticationEntryPoint.builder()
                .addEntryPointFor(
                        new LoginUrlAuthenticationEntryPoint("/oauth2/authorization/bff-client"),
                        new MediaTypeRequestMatcher(MediaType.TEXT_HTML)
                )
                .defaultEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                .build();
    }

    private LogoutSuccessHandler oidcLogoutSuccessHandler(ClientRegistrationRepository clientRegistrationRepository) {
        OidcClientInitiatedLogoutSuccessHandler handler =
                new OidcClientInitiatedLogoutSuccessHandler(clientRegistrationRepository);
        handler.setPostLogoutRedirectUri(this.appBaseUri);
        // Used when there is no OIDC login to end, for example when the BFF session has already expired
        handler.setDefaultTargetUrl(this.appBaseUri);
        handler.setRedirectStrategy((request, response, url) -> {
            response.setStatus(HttpStatus.ACCEPTED.value());
            response.setHeader(HttpHeaders.LOCATION, url);
        });
        return handler;
    }

}
