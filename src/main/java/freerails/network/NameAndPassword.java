/*
 * Created on 25-Jan-2005
 *
 */
package freerails.network;

import java.io.Serializable;

/**
 * Used by the server to store a player's username and password.
 *
 */
public class NameAndPassword implements Serializable {
    private static final long serialVersionUID = 3258409551740155956L;

    /**
     *
     */
    public final String password;

    /**
     *
     */
    public final String username;

    /**
     *
     * @param u
     * @param p
     */
    public NameAndPassword(String u, String p) {
        username = u;
        password = p;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof NameAndPassword))
            return false;
        NameAndPassword test = (NameAndPassword) obj;
        return test.password.equals(password) && test.username.equals(username);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = result * 37 + password.hashCode();
        result = result * 37 + username.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return username;
    }
}
