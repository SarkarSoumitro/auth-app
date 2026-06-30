package com.practice.auth_app.security;

import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.Setter;
;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collection;


@Service
@Getter
@Setter
public class CookieService {
    private final String refreshTokenCookieName;
    private final boolean cookieHttpOnly;
    private final boolean cookieSecure;
    private final String cookieSameSite;
    private final String cookieDomain;
    //logger
    private final Logger logger =  LoggerFactory.getLogger(Collection.class);

    public CookieService(
            @Value("${security.jwt.refresh-token-cookie.name:refreshToken}") String refreshTokenCookieName,
            @Value("${security.jwt.refresh-token-cookie.http-only:true}") boolean cookieHttpOnly,
            @Value("${security.jwt.refresh-token-cookie.secure:true}") boolean cookieSecure,
            @Value("${security.jwt.refresh-token-cookie.domain:}") String cookieDomain,
            @Value("${security.jwt.refresh-token-cookie.same-site:lax}") String cookieSameSite) {
        this.refreshTokenCookieName = refreshTokenCookieName;
        this.cookieHttpOnly = cookieHttpOnly;
        this.cookieSecure = cookieSecure;
        this.cookieDomain = cookieDomain;
        this.cookieSameSite = cookieSameSite;
    }

    public void addRefreshTokenCookie(String value, long maxAgeSeconds, HttpServletResponse response) {
       logger.info("adding value name : {} and value :  {}",refreshTokenCookieName,value);
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie
                .from(refreshTokenCookieName, value)
                .maxAge(Duration.ofSeconds(maxAgeSeconds))
                .path("/")
                .httpOnly(cookieHttpOnly)
                .secure(cookieSecure)
                .sameSite(cookieSameSite);

        if (cookieDomain != null && !cookieDomain.isBlank()) {
            builder.domain(cookieDomain);
        }
        ResponseCookie cookie = builder.build();
        //this line can be removed later
        addNoStoreHeader(response);
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    public void removeRefreshTokenCookie(HttpServletResponse response) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie
                .from(refreshTokenCookieName, "")
                .maxAge(0)
                .path("/")
                .httpOnly(cookieHttpOnly)
                .secure(cookieSecure)
                .sameSite(cookieSameSite);

        if (cookieDomain != null && !cookieDomain.isBlank()) {
            builder.domain(cookieDomain);
        }
        ResponseCookie cookie = builder.build();
        response.addHeader("Set-Cookie", cookie.toString());
    }

    public void addNoStoreHeader(HttpServletResponse response){
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store, no-cache, must-revalidate, max-age=0");
        response.setHeader("pragma", "no-cache");
        response.setHeader("expires", "0");
    }

}
