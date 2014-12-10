/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package farpackage;

/**
 *
 * @author Zbyněk Stara
 */
public enum ReservationType {
    INITIAL(0),
    NORMAL(1),
    WAIT(2),
    PROXY(3),
    GHOST(10);

    private int value;

    private ReservationType(int i) {
        value = i;
    }

    public int getValue() {
        return value;
    }
}
