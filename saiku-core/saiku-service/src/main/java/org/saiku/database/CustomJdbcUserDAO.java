package org.saiku.database;

import org.saiku.database.dto.SaikuUser;

import java.util.Collection;

public class CustomJdbcUserDAO extends JdbcUserDAO
{
    public SaikuUser insert(SaikuUser user)
    {
        throw new UnsupportedOperationException("Unsupported operation.");
    }

    public void insertRole(SaikuUser user)
    {
        throw new UnsupportedOperationException("Unsupported operation.");
    }

    public void deleteUser(SaikuUser user)
    {
        throw new UnsupportedOperationException("Unsupported operation.");
    }

    public void deleteRole(SaikuUser user)
    {
        throw new UnsupportedOperationException("Unsupported operation.");
    }

    public String[] getRoles(SaikuUser user)
    {
        throw new UnsupportedOperationException("Unsupported operation.");
    }

    public SaikuUser findByUserId(int userId)
    {
        throw new UnsupportedOperationException("Unsupported operation.");
    }

    public Collection findAllUsers()
    {
        throw new UnsupportedOperationException("Unsupported operation.");
    }

    public void deleteUser(String username)
    {
        throw new UnsupportedOperationException("Unsupported operation.");
    }

    public SaikuUser updateUser(SaikuUser user)
    {
        throw new UnsupportedOperationException("Unsupported operation.");
    }

    public void updateRoles(SaikuUser user)
    {
        throw new UnsupportedOperationException("Unsupported operation.");
    }
}
