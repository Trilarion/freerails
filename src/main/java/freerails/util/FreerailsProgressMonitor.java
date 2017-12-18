/*
 * ProgressMonitor.java
 *
 * Created on 08 September 2003, 21:56
 */
package freerails.util;

/**
 * This interface defines callbacks that can be used to let the user know how a
 * slow task is progressing.
 *
 * @author Luke Lindsay
 */
public interface FreerailsProgressMonitor {

    /**
     *
     */
    FreerailsProgressMonitor NULL_INSTANCE = new FreerailsProgressMonitor() {

        public void setValue(int i) {
        }

        public void nextStep(int max) {
        }

        public void finished() {
        }
    };

    /**
     *
     * @param i
     */
    void setValue(int i);

    /**
     *
     * @param max
     */
    void nextStep(int max);

    /**
     *
     */
    void finished();
}