package com.pinkkila.authorizationserver.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;

import java.util.List;

@Configuration
public class CustomClaimsConfiguration {

    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> jwtTokenCustomizer(
            @Value("${app.access-token-audience}") String accessTokenAudience) {
        return (context) -> {
            if (OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType())) {
                context.getClaims().audience(List.of(accessTokenAudience));
                Authentication authentication = context.getPrincipal();
                if (authentication.getPrincipal() instanceof UserDetailsServiceImpl.CustomUserDetails user) {
                    context.getClaims().claim("user_id", user.getId().toString());
                }
            }
        };
    }
}
