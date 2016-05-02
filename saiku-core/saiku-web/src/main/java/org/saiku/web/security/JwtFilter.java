package org.saiku.web.security;

import org.saiku.service.ISessionService;
import org.springframework.expression.ParseException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.Assert;
import org.springframework.web.filter.GenericFilterBean;

import javax.crypto.spec.SecretKeySpec;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.security.Key;

public class JwtFilter extends GenericFilterBean
{
    private final ISessionService sessionService;
    private final AuthenticationManager authenticationManager;
    private final Key authenticationKey;

    public JwtFilter(final ISessionService sessionService, final AuthenticationManager authenticationManager, final String authenticationKey)
    {
        this.sessionService = sessionService;
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


        try {
            if(!sessionService.isAuthenticated())
            {
                HttpServletRequest req = (HttpServletRequest) request;
                String stringToken = null;
                for (Cookie cookie : req.getCookies())
                {
                    if ("authorization".equals(cookie.getName()))
                    {
                        stringToken = cookie.getValue();
                        break;
                    }
                }
                if (stringToken == null)
                {
                    return;
                }

                try
                {
                    final JwtToken token = new JwtToken(stringToken, authenticationKey);
                    sessionService.login(token);

                }
                catch (ParseException e)
                {
                    throw new InvalidObjectException("Invalid token");
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            chain.doFilter(request, response);
        }
    }
}
