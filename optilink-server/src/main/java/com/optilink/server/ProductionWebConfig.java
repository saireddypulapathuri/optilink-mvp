package com.optilink.server;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.server.WebFilter;

/**
 * Browser security policy for the hosted application.
 */
@Configuration
public class ProductionWebConfig {

    /**
     * Adds security and cache headers to every response.
     *
     * @return response header filter
     */
    @Bean
    WebFilter browserSecurityHeaders() {
        return (exchange, chain) -> {
            HttpHeaders headers = exchange.getResponse().getHeaders();
            headers.set("X-Content-Type-Options", "nosniff");
            headers.set("Referrer-Policy", "no-referrer");
            headers.set("Permissions-Policy", "camera=(self), microphone=(), geolocation=()");
            headers.set("Cross-Origin-Opener-Policy", "same-origin");
            String path = exchange.getRequest().getPath().value();
            String inlinePolicy = path.startsWith("/transfers/") ? " 'unsafe-inline'" : "";
            headers.set(
                    "Content-Security-Policy",
                    "default-src 'self'; base-uri 'none'; frame-ancestors 'self'; form-action 'self'; "
                            + "script-src 'self'" + inlinePolicy + "; style-src 'self'" + inlinePolicy + "; "
                            + "img-src 'self' data: blob:; media-src 'self' data: blob:; connect-src 'self'");
            if (path.startsWith("/api/") || path.equals("/health")) {
                headers.setCacheControl("no-store");
            }
            return chain.filter(exchange);
        };
    }
}
