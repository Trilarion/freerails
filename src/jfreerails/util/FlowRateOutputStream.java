package jfreerails.util;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.logging.Logger;


/**
*  A FilterOutputStream that measures flow rate.
* @author Patrice Espie
* Licensing: LGPL
*/
public class FlowRateOutputStream extends FilterOutputStream implements Runnable {
    private static final Logger logger = Logger.getLogger(FlowRateOutputStream.class.getName());

    public FlowRateOutputStream(OutputStream out, String streamName) {
        this(out, streamName, 60, 1000);
    }

    public FlowRateOutputStream(OutputStream out, String streamName,
        int measureDuration, int measureInterval) {
        super(out);
        byteSentCumul = 0L;
        totalByteSent = 0L;
        previousTotalByteSent = 0L;
        openTimeMillis = System.currentTimeMillis();
        nextFree = 0;
        nbUsed = 0;
        running = false;
        closeRequested = false;
        byteSent = new long[measureDuration];
        measureIntervall = measureInterval;
        this.streamName = streamName;

        if (measureIntervall == 0) {
            showTrace = false;
            measureIntervall = 1000L;
        } else {
            showTrace = true;
        }

        (new Thread(this)).start();
    }

    public FlowRateOutputStream(OutputStream out) {
        this(out, "FlowRateOutputStream", 60, 1000);
    }

    public void close() throws IOException {
        closeRequested = true;
        super.close();

        do {
            try {
                Thread.currentThread();
                Thread.sleep(50L);
            } catch (InterruptedException interruptedexception) {
            }
        } while (running);

        logger.info(String.valueOf(String.valueOf((new StringBuffer("Stream ")).append(
                        streamName).append(": Open duration = ")
                                                   .append((System.currentTimeMillis() -
                        openTimeMillis) / 1000D).append(", Byte sent = ")
                                                   .append(totalByteSent)
                                                   .append(" (")
                                                   .append((int)(totalByteSent / 1024D))
                                                   .append(" Ko), overall flow rate = ")
                                                   .append(overallRateString())
                                                   .append(" Ko/s"))));
    }

    public void write(byte[] b) throws IOException {
        super.out.write(b, 0, b.length);
        totalByteSent += b.length;
    }

    public void write(byte[] b, int off, int len) throws IOException {
        super.out.write(b, off, len);
        totalByteSent += len;
    }

    public void write(int b) throws IOException {
        super.out.write(b);
        totalByteSent++;
    }

    public int currentRate() throws IOException {
        return (int)(byteSentCumul / 1024D / (nbUsed * (measureIntervall / 1000D)));
    }

    public int overallRate() throws IOException {
        return (int)(totalByteSent / 1024D / ((System.currentTimeMillis() -
        openTimeMillis) / 1000D));
    }

    public String overallRateString() throws IOException {
        double d = totalByteSent / 1024D / ((System.currentTimeMillis() -
            openTimeMillis) / 1000D);

        return decimalFormat.format(d);
    }

    public String currentRateString() {
        double d = (byteSentCumul / 1024D / (nbUsed * (measureIntervall / 1000D)));

        return decimalFormat.format(d);
    }

    public void run() {
        if (running) {
            throw new Error("Starting thread task on an already-started object");
        }

        if (measureIntervall == 0x7fffffffffffffffL) {
            return;
        }

        running = true;

        try {
            do {
                try {
                    Thread.currentThread();
                    Thread.sleep(measureIntervall);
                } catch (InterruptedException interruptedexception) {
                }

                if (!closeRequested) {
                    long totalByteSentCopy = totalByteSent;
                    long byteSentThisTime = totalByteSentCopy -
                        previousTotalByteSent;
                    previousTotalByteSent = totalByteSentCopy;
                    byteSentCumul -= byteSent[nextFree];
                    byteSent[nextFree] = byteSentThisTime;
                    byteSentCumul += byteSentThisTime;
                    nextFree = (nextFree + 1) % byteSent.length;
                    nbUsed = Math.min(byteSent.length, nbUsed + 1);

                    if (showTrace) {
                        logger.info(String.valueOf(String.valueOf(
                                    (new StringBuffer("Stream ")).append(
                                        streamName).append(": Open duration = ")
                                     .append((System.currentTimeMillis() -
                                        openTimeMillis) / 1000D)
                                     .append(", Byte sent = ")
                                     .append(totalByteSent).append(" (")
                                     .append((int)(totalByteSent / 1024D))
                                     .append(" Ko), current flow rate = ")
                                     .append(currentRateString()).append(" Ko/s"))));
                    }
                }
            } while (!closeRequested);

            //} catch (IOException ioexception) {
        } finally {
            running = false;
        }
    }

    private long[] byteSent;
    private long byteSentCumul;
    private long totalByteSent;
    private long previousTotalByteSent;
    private long openTimeMillis;
    private long measureIntervall;
    private int nextFree;
    private int nbUsed;
    private boolean running;
    private boolean closeRequested;
    private String streamName;
    private boolean showTrace;
    private DecimalFormat decimalFormat = new DecimalFormat("0.00");
}