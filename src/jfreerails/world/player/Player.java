package jfreerails.world.player;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.logging.Logger;
import jfreerails.world.common.FreerailsSerializable;


/**
 * Represents a player within the game. The player model is such that a user can
 * start a client, create a new player on the server and start playing. They can
 * disconnect from the server, which may continue running with other players
 * still active. The server can then save the list of players and be stopped and
 * restarted again, the clients can then authenticate themselves to the server
 * and continue their sessions where they left off.
 *
 * XXX the player is only authenticated when the connection is opened, and
 * subsequent exchanges are not authenticated.
 *
 * TODO implement a more complete authentication system using certificates
 * rather than public keys.
 * @author rtuck99@users.sourceforge.net
 */
public class Player implements FreerailsSerializable {
    private static final Logger logger = Logger.getLogger(Player.class.getName());
    private static final long serialVersionUID = 1;

    /** A FreerailsPrincipal that is not a player.*/    
    private static class WorldPrincipal extends FreerailsPrincipal {
        private static final long serialVersionUID = 1;
        private final String m_name;

        public WorldPrincipal(String name) {
            m_name = name;
        }

        public String getName() {
            return m_name;
        }

        public String toString() {
            return m_name;
        }

        public int hashCode() {
            return m_name.hashCode();
        }

        public boolean equals(Object o) {
            if (!(o instanceof WorldPrincipal)) {
                return false;
            }

            return (m_name.equals(((WorldPrincipal)o).m_name));
        }
    }

    private FreerailsPrincipal principal;

    /**
     * Salt used to ensure signatures are always unique.
     */
    private /*=mutable*/ int salt;

    /**
     * This Principal can be granted all permissions.
     */
    public static final FreerailsPrincipal AUTHORITATIVE = new WorldPrincipal(
            "Authoritative Server");

    /**
     * This Principal has no permissions.
     */
    public static final FreerailsPrincipal NOBODY = new WorldPrincipal("Nobody");

    /**
     * Name of the player.
     */
    private final String name;

    /**
     * Private data (eg private keys) that should not be serialized in normal
     * use. Instead, when the client needs to save their session they should
     * call saveSession().
     */
    private /*=mutable*/ transient PrivateData privateData;

    /**
     * This is the clients public key.
     */
    private PublicKey publicKey;

    /**
     * This class is a container for private player-specific data that is
     * security-sensitive and should not be stored in unencrypted form or
     * transmitted to other players/systems. Note that we do not implement
     * FreerailsSerializable here as this is private data.
     */
    private static class PrivateData implements Serializable {
        private static final long serialVersionUID = 1;

        /**
         * The players private key. This is held by the client.
         */
        final PrivateKey privateKey;

        /**
         * record of "salt" used for previous connections. This is held by the
         * server.
         */
        final /*=mutable*/ HashSet<Integer> salts = new HashSet<Integer>();

        PrivateData(PrivateKey key) {
            privateKey = key;
        }

        /**
         * Default constructor called on server.
         */
        PrivateData() {
            privateKey = null;
        }
    }

    /**
     * Used by the client to generate a player with a particular name.
     */
    public Player(String name) {
        this.name = name;

        KeyPairGenerator kpg;

        /* generate our key pair */
        try {
            kpg = KeyPairGenerator.getInstance("DSA");
            kpg.initialize(1024);

            KeyPair kp = kpg.generateKeyPair();
            privateData = new PrivateData(kp.getPrivate());
            publicKey = kp.getPublic();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Used by the server to generate a player with a particular name and public
     * key.
     * @param publicKey the client's public key.
     * certificate.
     */
    public Player(String name, PublicKey publicKey, int id) {
        this.name = name;
        this.publicKey = publicKey;
        privateData = new PrivateData();
        this.principal = new PlayerPrincipal(id, name);
    }

    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }

        if (!(o instanceof Player)) {
            return false;
        }

        boolean keysEqual;

        if (null != publicKey) {
            byte[] encoded = publicKey.getEncoded();
            byte[] encoded2 = ((Player)o).publicKey.getEncoded();
            keysEqual = Arrays.equals(encoded, encoded2);
        } else {
            keysEqual = null == ((Player)o).publicKey;
        }

        return (name.equals(((Player)o).name) && keysEqual);
    }

    public int hashCode() {
        return name.hashCode();
    }

    public String toString() {
        return name;
    }

    public String getName() {
        return name;
    }

    /**
     * TODO save this player's private data so that they can be re-connected to
     * the server at a later point in time.
     */
    public void saveSession(ObjectOutputStream out) throws IOException {
        out.writeObject(privateData);
    }

    /**
     * Called by the client to reconstitute the data from a saved game.
     */
    public void loadSession(ObjectInputStream in) throws IOException {
        try {
            privateData = (PrivateData)in.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException("Couldn't find class:" + e);
        }
    }

    /**
     * @return a signature for a serialized instance of this class
     */
    public byte[] sign() throws GeneralSecurityException {
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
        byte[] saltBytes = new byte[4];
        sr.nextBytes(saltBytes);
        salt = ((saltBytes[0] << 24) & 0xff000000) |
            ((saltBytes[1] << 16) & 0x00ff0000) |
            ((saltBytes[2] << 8) & 0x0000ff00) | (saltBytes[3] & 0x000000ff);

        Signature sig = Signature.getInstance("DSA");
        sig.initSign(privateData.privateKey);
        sig.update(saltBytes);
        sig.update(publicKey.getEncoded());

        byte[] b = sig.sign();

        return b;
    }

    /**
     * @return true if the specified Player object has a valid signature and
     * has a matching public Key and is equal to this instance. Once this
     * player has been verified, the same player cannot be verified using the
     * same signature.
     */
    public boolean verify(Player player, byte[] signature) {
        assert privateData != null;
        assert player != null;

        boolean arraysEquals = java.util.Arrays.equals(player.publicKey.getEncoded(),
                publicKey.getEncoded());
        assert arraysEquals;

        if (privateData.salts.contains(new Integer(player.salt))) {
            logger.warning("Player " + player + " attempted to connect " +
                "with old salt");

            return false;
        }

        Signature sig;

        try {
            sig = Signature.getInstance("DSA");
        } catch (NoSuchAlgorithmException e) {
            assert false;

            return false;
        }

        try {
            /*
             * XXX verify with _our_own_ public key as we can't trust the one
             * provided.
             */
            sig.initVerify(this.publicKey);
        } catch (InvalidKeyException e) {
            logger.warning("Caught InvalidKeyException in Player.sign()");

            return false;
        }

        try {
            byte[] saltBytes = new byte[] {
                    (byte)(player.salt >> 24), (byte)(player.salt >> 16),
                    (byte)(player.salt >> 8), (byte)(player.salt)
                };
            sig.update(saltBytes);
            sig.update(player.publicKey.getEncoded());
        } catch (SignatureException e) {
            assert false;

            return false;
        }

        try {
            if (sig.verify(signature) == false) {
                logger.warning("Signature verification failed in " +
                    "Player.verify()");

                return false;
            }
        } catch (SignatureException e) {
            logger.warning("Caught SignatureException in Player.sign()");

            return false;
        }

        if (player.name.equals(name)) {
            /* remember this salt to prevent sniffing attacks */
            privateData.salts.add(new Integer(player.salt));

            return true;
        }

        logger.warning("Player name was different");

        return false;
    }

    public FreerailsPrincipal getPrincipal() {
        return principal;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }
}