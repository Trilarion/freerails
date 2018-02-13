package freerails.client.model;

import java.awt.*;

/**
 * Stores the company details that are used to draw a line and title on the
 * graph.
 */
public class CompanyDetails {

    /**
     * The company's net worth at the end of each year.
     */
    public final long[] value = new long[100];

    /**
     * The colour for the line on the graph.
     */
    public final Color color;

    /**
     * The company's name.
     */
    public final String name;

    public CompanyDetails(String n, Color c) {

        color = c;
        name = n;
        for (int i = 0; i < 100; i++) {
            value[i] = Integer.MIN_VALUE;
        }
    }
}
