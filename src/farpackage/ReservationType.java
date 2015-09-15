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
    GHOST(4);//,
    /*GHOST_NORTH(11), // ghost to the north of originating agent
    GHOST_EAST(12), // ghost to the east of originating agent
    GHOST_SOUTH(13), // ghost to the south of originating agent
    GHOST_WEST(14); // ghost to the west of originating agent*/

    private int value;

    private ReservationType(int i) {
        value = i;
    }

    public int getValue() {
        return value;
    }
}
