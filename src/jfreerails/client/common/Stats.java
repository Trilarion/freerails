package jfreerails.client.common;


/**
 * Statistics gathering class for coarse-level timing
 */
public class Stats {
    int total = 0;
    int n = 0;
    String name;
    long started;
    public static boolean statsAreOn = (System.getProperty(
            "jfreerails.client.common.Stats.statsAreOn") != null);

    public Stats(String name) {
        this.name = name;
    }

    public void enter() {
        if (!statsAreOn) {
            return;
        }

        started = System.currentTimeMillis();
    }

    public void exit() {
        if (!statsAreOn) {
            return;
        }

        n++;
        total += System.currentTimeMillis() - started;

        if (n % 200 == 0) {
            System.out.println("Average time of " + name + ":" + (total / n));
            n = total = 0;
        }
    }
}