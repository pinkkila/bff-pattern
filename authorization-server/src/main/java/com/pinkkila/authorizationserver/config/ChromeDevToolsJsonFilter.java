package com.pinkkila.authorizationserver.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Chrome DevTools requests this file on every page load from a local origin
 * (localhost, 127.0.0.1, [::1]). Without this filter, Spring Security would
 * save it as the request to return to after login, replacing the pending
 * /oauth2/authorize request.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ChromeDevToolsJsonFilter extends OncePerRequestFilter {

    private static final String DEVTOOLS_JSON_PATH = "/.well-known/appspecific/com.chrome.devtools.json";

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !DEVTOOLS_JSON_PATH.equals(request.getRequestURI());
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, HttpServletResponse response, @NonNull FilterChain chain) {
        // setStatus, not sendError: sendError would forward to /error, which Spring Security would save instead
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
    }

}
