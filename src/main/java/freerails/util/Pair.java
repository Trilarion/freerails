package freerails.util;

/**
 *
 * @author FreeRails team
 * @param <A>
 * @param <B>
 */
public class Pair<A, B> {

    private final A a;
    private final B b;

    /**
     *
     * @param a
     * @param b
     */
    public Pair(A a, B b) {
        this.a = a;
        this.b = b;
    }

    /**
     *
     * @param other
     * @return
     */
    public boolean equals(Pair<A, B> other) {
        return this == other || null != other && (a.equals(other.a) && b.equals(other.b));
    }

    public String toString() {
        return "(" + a.toString() + ", " + b.toString() + ")";
    }

    /**
     *
     * @return
     */
    public A getA() {
        return a;
    }

    /**
     *
     * @return
     */
    public B getB() {
        return b;
    }

}