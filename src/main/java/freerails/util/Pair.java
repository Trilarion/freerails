package freerails.util;

public class Pair<A, B> {
    private final A e1;

    private final B e2;

    public Pair(A e1, B e2) {
        this.e1 = e1;
        this.e2 = e2;
    }

    public boolean equals(Pair<A, B> other) {
        if (this == other)
            return true;
        return null != other && (e1.equals(other.e1) && e2.equals(other.e2));
    }

    public String toString() {
        return "(" + e1.toString() + ", " + e2.toString() + ")";
    }

    public A getA() {
        return e1;
    }

    public B getB() {
        return e2;
    }

}