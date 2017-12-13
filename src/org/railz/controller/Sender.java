/*
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

/**
 * Created on Feb 18, 2004
 */
package org.railz.controller;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;


class Sender {
    private BufferedOutputStream sendQueue;
    private ObjectOutputStream objectOutputStream;

    public Sender(OutputStream out) throws IOException {
        open(out);
    }

    private void open(OutputStream out) throws IOException {
        /* open the ObjectOutputStream first and then flush it so that the
        * remote side doesn't block waiting for it */
        sendQueue = new BufferedOutputStream(out);
        objectOutputStream = new ObjectOutputStream(sendQueue);
        objectOutputStream.flush();
        sendQueue.flush();
    }

    synchronized void send(Serializable s) throws IOException {
        objectOutputStream.writeObject(s);
    }

    public synchronized void flush() {
        try {
            objectOutputStream.flush();
        } catch (IOException e) {
            // ignore it
        }
    }

    /**
     * Closes the output stream. Called by disconnect()
     */
    public synchronized void close() {
        try {
            objectOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            objectOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            sendQueue.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Call reset on the Sender's ObjectOutputStream */
    synchronized void reset() {
	try {
	    objectOutputStream.reset();
	} catch (IOException e) {
	    System.err.println ("Caught an IOException resetting the " +
		    "ObjectOutputStream:" + e);
	}
    }
}
