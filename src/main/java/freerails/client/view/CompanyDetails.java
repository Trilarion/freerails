package freerails.client.view;

import java.awt.*;

// TODO this is part of the client model
/**
 * Stores the company details that are used to draw a line and title on the
 * graph.
 */
class CompanyDetails {

    /**
     * The company's net worth at the end of each year.
     */
    final long[] value = new long[100];

    /**
     * The colour for the line on the graph.
     */
    final Color color;

    /**
     * The company's name.
     */
    final String name;

    CompanyDetails(String n, Color c) {

        color = c;
        name = n;
        for (int i = 0; i < 100; i++) {
            value[i] = Integer.MIN_VALUE;
        }

    }
}
