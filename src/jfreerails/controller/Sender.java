/**
 * Created on Feb 18, 2004
 */
package jfreerails.controller;

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
}