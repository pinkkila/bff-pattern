package com.pinkkila.bff.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.client.ClientAuthorizationException;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

/**
 * Ends the BFF session when the authorization server rejects the refresh token, for example because it has expired.
 * The session can't get new access tokens anymore, so the SPA gets a 401 and shows the logged-out view
 * (RFC 10017, section 6.1.2.2).
 */
@Component
public class InvalidRefreshTokenExceptionResolver implements HandlerExceptionResolver, Ordered {

    @Override
    public @Nullable ModelAndView resolveException(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                                   @Nullable Object handler, @NonNull Exception ex) {
        if (!(ex instanceof ClientAuthorizationException clientAuthorizationException)
                || !OAuth2ErrorCodes.INVALID_GRANT.equals(clientAuthorizationException.getError().getErrorCode())) {
            return null;
        }
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        return new ModelAndView();
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

}
