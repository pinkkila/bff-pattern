package com.pinkkila.authorizationserver.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;

@Configuration
public class CustomClaimsConfiguration {
    
    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> jwtTokenCustomizer() {
        return (context) -> {
            if (OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType())) {
                Authentication authentication = context.getPrincipal();
                if (authentication.getPrincipal() instanceof UserDetailsServiceImpl.CustomUserDetails user) {
                    context.getClaims().claim("user_id", user.getId().toString());
                }
            }
        };
    }
}
