package org.saiku.database;

import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.security.authentication.encoding.PasswordEncoder;

public class PostgresPasswordEncoder extends JdbcDaoSupport implements PasswordEncoder
{
    public String encodePassword(String rawPass, Object salt)
    {
        String sql = "SELECT crypt(?, gen_salt('bf', 12));";
        String result = getJdbcTemplate().queryForObject(sql, new Object[] { rawPass }, String.class);
        return result;
    }
    public boolean isPasswordValid(String encPass, String rawPass, Object salt)
    {
        String sql = "SELECT ? = crypt(?, ?) AS matched;";
        boolean result = getJdbcTemplate().queryForObject(sql, new Object[] { encPass, rawPass, encPass }, Boolean.class);
        return result;
    }
}
