package io.github.lujian213.eggfund.filter;

import io.github.lujian213.eggfund.utils.Constants;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.stream.Collectors;

public class JWTTokenGeneratorFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JWTTokenGeneratorFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (null != authentication && authentication.isAuthenticated()) {
            String bearerToken = request.getHeader(Constants.JWT_HEADER);
            if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
                log.debug("JWT existed, no need regenerate.");
            } else {
                log.debug("Generating JWT for user: {}", authentication.getName());
                bearerToken = "Bearer " + generateJwt(authentication);
            }
            response.setHeader(Constants.JWT_HEADER, bearerToken);
            log.info("JWT added to response header for user: {}", authentication.getName());
        }
        filterChain.doFilter(request, response);
    }

    private String generateJwt(Authentication authentication) {
        Environment env = getEnvironment();
        String secret = env.getProperty(Constants.JWT_SECRET_KEY, Constants.JWT_SECRET_DEFAULT_VALUE);
        SecretKey secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        String jwt = Jwts.builder().issuer("EggFund").subject("JWT Token")
                .claim("username", authentication.getName())
                .claim("authorities", authentication.getAuthorities().stream().map(
                        GrantedAuthority::getAuthority).collect(Collectors.joining(",")))
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plusSeconds(86400))) // 1 day
                .signWith(secretKey).compact();
        log.debug("JWT generated: {}", jwt);
        return jwt;
    }

}