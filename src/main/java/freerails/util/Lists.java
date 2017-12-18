/*
 * Created on 29-Jul-2005
 *
 */
package freerails.util;

/**
 *
 * @author jkeller1
 */
public class Lists {

    /**
     *
     * @param a
     * @param b
     * @return
     */
    @SuppressWarnings("unchecked")
    public static boolean equals(List1D a, List1D b) {
        if (a.size() != b.size())
            return false;
        for (int i = 0; i < a.size(); i++) {
            if (!Utils.equal(a.get(i), b.get(i)))
                return false;
        }
        return true;
    }

    /**
     *
     * @param a
     * @param b
     * @return
     */
    @SuppressWarnings("unchecked")
    public static boolean equals(List2D a, List2D b) {
        if (a.sizeD1() != b.sizeD1())
            return false;
        for (int d1 = 0; d1 < a.sizeD1(); d1++) {
            if (a.sizeD2(d1) != b.sizeD2(d1))
                return false;
            for (int d2 = 0; d2 < a.sizeD2(d1); d2++) {
                if (!Utils.equal(a.get(d1, d2), b.get(d1, d2)))
                    return false;
            }
        }
        return true;
    }

    /**
     *
     * @param a
     * @param b
     * @return
     */
    @SuppressWarnings("unchecked")
    public static boolean equals(List3D a, List3D b) {
        if (a.sizeD1() != b.sizeD1())
            return false;
        for (int d1 = 0; d1 < a.sizeD1(); d1++) {
            if (a.sizeD2(d1) != b.sizeD2(d1))
                return false;
            for (int d2 = 0; d2 < a.sizeD2(d1); d2++) {
                if (a.sizeD3(d1, d2) != b.sizeD3(d1, d2))
                    return false;
                for (int d3 = 0; d3 < a.sizeD3(d1, d2); d3++) {
                    if (!Utils.equal(a.get(d1, d2, d3), b.get(d1, d2, d3)))
                        return false;
                }
            }
        }
        return true;
    }

}
