package adtpackage;

import generalpackage.*;

/**
 *
 * @author Zbynda
 */
public interface ADT {
    abstract boolean isEmpty();
    abstract int size();
    abstract <T extends Printable> ReturnCode insert(T data, double key);
    abstract Object search(double key);
    abstract Object delete(double key);
}
