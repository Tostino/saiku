package org.saiku.web.security;


import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import java.util.Date;

public class JwtAuthenticationProvider extends DaoAuthenticationProvider
{
    private String issuer;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException
    {
        try
        {
            JwtToken token = JwtToken.class.cast(authentication);
            Date referenceTime = new Date();

            Date expirationTime = token.getDetails().getExpiration();
            if(expirationTime == null || expirationTime.before(referenceTime))
            {
                throw new CredentialsExpiredException("The token is expired");
            }

            Date notBefore = token.getDetails().getNotBefore();
            if(notBefore == null || notBefore.after(referenceTime))
            {
                throw new CredentialsExpiredException("The token is valid for a time in the future.");
            }

            String tokenIssuer = token.getDetails().getIssuer();
            if (issuer != null && !issuer.equals(tokenIssuer)) {
                throw new AuthenticationServiceException("Invalid issuer");
            }

            return token;
        }
        catch (Exception e)
        {
            return super.authenticate(authentication);
        }
    }

    @Override
    public boolean supports(Class<?> authentication)
    {
        if(JwtToken.class.isAssignableFrom(authentication))
        {
            return true;
        }
        else
        {
            return super.supports(authentication);
        }
    }

    public void setIssuer(String issuer)
    {
        this.issuer = issuer;
    }
}
