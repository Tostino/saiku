package org.saiku.security;

import org.springframework.expression.ParseException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.Assert;
import org.springframework.web.filter.GenericFilterBean;

import javax.crypto.spec.SecretKeySpec;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.security.Key;

public class JwtFilter extends GenericFilterBean
{
    private final AuthenticationManager authenticationManager;
    private final Key authenticationKey;

    public JwtFilter(final AuthenticationManager authenticationManager, final String authenticationKey)
    {
        this.authenticationManager = authenticationManager;
        this.authenticationKey = new SecretKeySpec(authenticationKey.getBytes(), "AES");
    }

    @Override
    public void afterPropertiesSet() throws ServletException {
        Assert.notNull(authenticationManager);
    }

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException
    {
        HttpServletRequest req = (HttpServletRequest) request;

        try {
            String stringToken = req.getHeader("authorization");
            if (stringToken == null) {
                throw new InsufficientAuthenticationException("Authorization header not found");
            }

            // remove schema from token
            String authorizationSchema = "bearer";
            if (stringToken.indexOf(authorizationSchema) == -1) {
                throw new InsufficientAuthenticationException("Authorization schema not found");
            }
            stringToken = stringToken.substring(authorizationSchema.length()).trim();

            try {
                final JwtToken token = new JwtToken(stringToken, authenticationKey);

                final Authentication auth = authenticationManager.authenticate(token);
                SecurityContextHolder.getContext().setAuthentication(auth);
                chain.doFilter(request, response);
            } catch (ParseException e) {
                throw new InvalidObjectException("Invalid token");
            }
        } catch (AuthenticationException e) {
            SecurityContextHolder.clearContext();
        }
    }
}
