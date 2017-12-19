package freerails.move;

/**
 * Thrown when there is a problem generating a move.
 *
 */
public class PreMoveException extends Exception {

    private static final long serialVersionUID = 3257007635675755061L;

    /**
     *
     * @param s
     */
    public PreMoveException(String s) {
        super(s);
    }

}
