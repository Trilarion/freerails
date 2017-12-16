/*
 * TrainCrashException.java
 *
 * Created on January 25, 2005, 2:32 PM
 */

package freerails.move;

/**
 * @author mduarte-leon
 */
public class TrainCrashException extends Exception {
    private static final long serialVersionUID = 3978710596948342065L;

    private int trainA;

    private int trainB;

    public TrainCrashException() {

    }

    public TrainCrashException(int aTrain, int bTrain) {
        trainA = aTrain;
        trainB = bTrain;
    }

    public int getTrainA() {
        return trainA;
    }

    public int getTrainB() {
        return trainB;
    }
}
