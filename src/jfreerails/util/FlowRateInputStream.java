package jfreerails.util;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.logging.Logger;


public class FlowRateInputStream extends FilterInputStream implements Runnable {
    private static final Logger logger = Logger.getLogger(FlowRateInputStream.class.getName());

    public FlowRateInputStream(InputStream in, String streamName) {
        this(in, streamName, 60, 1000);
    }

    public FlowRateInputStream(InputStream in, String streamName,
        int measureDuration, int measureInterval) {
        super(in);
        byteReceivedCumul = 0L;
        totalByteReceived = 0L;
        previousTotalByteReceived = 0L;
        openTimeMillis = System.currentTimeMillis();
        nextFree = 0;
        nbUsed = 0;
        running = false;
        closeRequested = false;
        byteReceived = new long[measureDuration];
        measureIntervall = measureInterval;
        this.streamName = streamName;

        if (measureIntervall == (long)0) {
            showTrace = false;
            measureIntervall = 1000L;
        } else {
            showTrace = true;
        }

        (new Thread(this)).start();
    }

    public FlowRateInputStream(InputStream in) {
        this(in, "FlowRateInputStream", 60, 1000);
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
                                                   .append((double)(System.currentTimeMillis() -
                        openTimeMillis) / 1000D).append(", Byte received = ")
                                                   .append(totalByteReceived)
                                                   .append(" (")
                                                   .append((int)((double)totalByteReceived / 1024D))
                                                   .append(" Ko), overall flow rate = ")
                                                   .append(overallRate())
                                                   .append(" Ko/s"))));
    }

    public int read() throws IOException {
        int r = super.in.read();
        totalByteReceived += r;

        return r;
    }

    public int read(byte[] b) throws IOException {
        int r = super.in.read(b);
        totalByteReceived += r;

        return r;
    }

    public int read(byte[] b, int off, int len) throws IOException {
        int r = super.in.read(b, off, len);
        totalByteReceived += r;

        return r;
    }

    public int currentRate() {
        return (int)((double)byteReceivedCumul / 1024D / ((double)nbUsed * ((double)measureIntervall / 1000D)));
    }

    public String currentRateString() {
        double d = ((double)byteReceivedCumul / 1024D / ((double)nbUsed * ((double)measureIntervall / 1000D)));

        return decimalFormat.format(d);
    }

    public int overallRate() {
        return (int)((double)totalByteReceived / 1024D / ((double)(System.currentTimeMillis() -
        openTimeMillis) / 1000D));
    }

    public void run() {
        if (running || measureIntervall == 0x7fffffffffffffffL) {
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
                    long totalByteReceivedCopy = totalByteReceived;
                    long byteSentThisTime = totalByteReceivedCopy -
                        previousTotalByteReceived;
                    previousTotalByteReceived = totalByteReceivedCopy;
                    byteReceivedCumul -= byteReceived[nextFree];
                    byteReceived[nextFree] = byteSentThisTime;
                    byteReceivedCumul += byteSentThisTime;
                    nextFree = (nextFree + 1) % byteReceived.length;
                    nbUsed = Math.min(byteReceived.length, nbUsed + 1);

                    if (showTrace) {
                        logger.info(String.valueOf(String.valueOf(
                                    (new StringBuffer("Stream ")).append(
                                        streamName).append(": Open duration = ")
                                     .append((double)(System.currentTimeMillis() -
                                        openTimeMillis) / 1000D)
                                     .append(", Byte sent = ")
                                     .append(totalByteReceived).append(" (")
                                     .append((int)((double)totalByteReceived / 1024D))
                                     .append(" Ko), current flow rate = ")
                                     .append(currentRateString()).append(" Ko/s"))));
                    }
                }
            } while (!closeRequested);
        } finally {
            running = false;
        }
    }

    private long[] byteReceived;
    private long byteReceivedCumul;
    private long totalByteReceived;
    private long previousTotalByteReceived;
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