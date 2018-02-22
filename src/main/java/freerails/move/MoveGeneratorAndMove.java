package freerails.move;

import freerails.move.generator.MoveGenerator;

import java.io.Serializable;

/**
 *
 */
public class MoveGeneratorAndMove implements Serializable {

    private static final long serialVersionUID = 3256443607635342897L;
    public final Move move;
    public final MoveGenerator moveGenerator;

    /**
     * Both parameters could be null.
     *
     * @param moveGenerator
     * @param move
     */
    public MoveGeneratorAndMove(MoveGenerator moveGenerator, Move move) {
        this.move = move;
        this.moveGenerator = moveGenerator;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof MoveGeneratorAndMove)) return false;

        final MoveGeneratorAndMove preMoveAndMove = (MoveGeneratorAndMove) obj;

        if (move != null ? !move.equals(preMoveAndMove.move) : preMoveAndMove.move != null) return false;
        return moveGenerator != null ? moveGenerator.equals(preMoveAndMove.moveGenerator) : preMoveAndMove.moveGenerator == null;
    }

    @Override
    public int hashCode() {
        int result;
        result = (move != null ? move.hashCode() : 0);
        result = 29 * result + (moveGenerator != null ? moveGenerator.hashCode() : 0);
        return result;
    }
}
