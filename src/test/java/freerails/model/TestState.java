package freerails.model;

import java.io.Serializable;

/**
 *
 */
class TestState implements Serializable {

    private static final long serialVersionUID = 5122023949873919060L;

    /**
     *
     */
    private final int x;

    /**
     * @param x
     */
    TestState(int x) {
        this.x = x;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof TestState))
            return false;

        final TestState testState = (TestState) obj;

        return x == testState.x;
    }

    @Override
    public int hashCode() {
        return x;
    }
}
