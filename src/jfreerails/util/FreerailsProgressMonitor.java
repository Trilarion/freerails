/*
 * ProgressMonitor.java
 *
 * Created on 08 September 2003, 21:56
 */
package jfreerails.util;


/** This interface defines callbacks that can be used to let the user know how a slow task is progressing.
 *
 * @author  Luke Lindsay
 */
public interface FreerailsProgressMonitor {
    public static final FreerailsProgressMonitor NULL_INSTANCE = new FreerailsProgressMonitor() {
            public void setMessage(String s) {
            }

            public void setValue(int i) {
            }

            public void setMax(int max) {
            }
        };

    void setMessage(String s);

    void setValue(int i);

    void setMax(int max);
}