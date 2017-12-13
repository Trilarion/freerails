/*
 * Copyright (C) Robert Tuck
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package org.railz.world.player;

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
import org.railz.world.common.FreerailsSerializable;


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
    private static class WorldPrincipal extends FreerailsPrincipal {
        private final String name;

        public WorldPrincipal(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public String toString() {
            return name;
        }

        public int hashCode() {
            return name.hashCode();
        }

        public boolean equals(Object o) {
            if (!(o instanceof WorldPrincipal)) {
                return false;
            }

            return (name.equals(((WorldPrincipal)o).name));
        }
    }

    private FreerailsPrincipal principal;

    /**
     * salt used to ensure signatures are always unique
     */
    private int salt;

    /**
     * This Principal can be granted all permissions
     */
    public static final FreerailsPrincipal AUTHORITATIVE = new WorldPrincipal(
            "Authoritative Server");

    /**
     * This Principal has no permissions
     */
    public static final FreerailsPrincipal NOBODY = new WorldPrincipal("Nobody");

    /**
     * name of the player
     */
    public String name;

    /**
     * Private data (eg private keys) that should not be serialized in normal
     * use. Instead, when the client needs to save their session they should
     * call saveSession()
     */
    private transient PrivateData privateData;

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
    private class PrivateData implements Serializable {
        /**
         * The players private key. This is held by the client.
         */
        final PrivateKey privateKey;

        /**
         * record of "salt" used for previous connections. This is held by the
         * server.
         */
        HashSet salts = new HashSet();

        PrivateData(PrivateKey key) {
            privateKey = key;
        }

        /**
         * Default constructor called on server
         */
        PrivateData() {
            privateKey = null;
        }
    }

    /**
     * Used by the client to generate a player with a particular name.
     * The player is given a public and private keypair.
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
            System.err.println("DSA encryption algorithm no supported by" +
                " JVM!");
            throw new RuntimeException(e);
        }
    }

    /**
     * Used by the server to generate a player with a particular name and public
     * key. The server does not know the private key of the client.
     * @param name the name of the player
     * @param id a unique id for the player
     * @param publicKey the client's public key.
     * certificate.
     */
    public Player(String name, PublicKey publicKey, int id) {
        this.name = name;
        this.publicKey = publicKey;
        privateData = new PrivateData();
        this.principal = new PlayerPrincipal(id);
    }

    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }

        if (!(o instanceof Player)) {
            return false;
        }

        return (name.equals(((Player)o).name) &&
        Arrays.equals(publicKey.getEncoded(), ((Player)o).publicKey.getEncoded()));
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

    private String dump(byte[] b) {
        String s = "";

        for (int i = 0; i < b.length; i++) {
            s += Integer.toString(b[i], 16);

            if (i > 0) {
                s += ", ";
            }
        }

        return s;
    }

    /**
     * @return true if the specified Player object has a valid signature and
     * has a matching public Key and is equal to this instance. Once this
     * player has been verified, the same player cannot be verified using the
     * same signature.
     */
    public boolean verify(Player player, byte[] signature) {
        byte[] encoded;
        assert privateData != null;
        assert player != null;
        assert java.util.Arrays.equals(player.publicKey.getEncoded(),
            publicKey.getEncoded());

        if (privateData.salts.contains(new Integer(player.salt))) {
            System.err.println("Player " + player + " attempted to connect " +
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
            System.err.println("Caught InvalidKeyException in Player.sign()");

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
                System.err.println("Signature verification failed in " +
                    "Player.verify()");

                return false;
            }
        } catch (SignatureException e) {
            System.err.println("Caught SignatureException in Player.sign()");

            return false;
        }

        if (player.name.equals(name)) {
            /* remember this salt to prevent sniffing attacks */
            privateData.salts.add(new Integer(player.salt));

            return true;
        }

        System.err.println("Player name was different");

        return false;
    }

    public FreerailsPrincipal getPrincipal() {
        return principal;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }
}
