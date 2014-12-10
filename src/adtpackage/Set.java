package adtpackage;

import generalpackage.*;

/**
 *
 * @author Zbynda
 */
public interface Set extends ADT {
    abstract <T extends Printable> ReturnCode add(T data, double key);
    abstract Object remove(double key);
    abstract boolean contains(double key);
}
