/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package graphicspackage;

/**
 *
 * @author Zbynda
 */
public enum RepaintState {
    REPAINT_NONE(0),
    REPAINT_TABLE(1),
    REPAINT_OBSTACLES(2), // requires field
    REPAINT_STARTS(3), // requires field
    REPAINT_GOALS(4), // requires field
    REPAINT_PATHS(5), // requires pathfinder
    REPAINT_AGENTS(6); // requires pathfinder

    private int value;

    private RepaintState(int i) {
        value = i;
    }

    public int getValue() {
        return value;
    }
}
