/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package farpackage;

import generalpackage.*;

/**
 *
 * @author Zbynda
 */
public class ConflictResolution {
    private Element thisElement;

    private ReservationType conflictFlag;
    private Reservation conflictWinner; // what reservation prevailed in the conflict
    private Reservation reservationChanged; // which reservation will have to be changed because of the conflict

    public ConflictResolution(Element thisElement, ReservationType conflictFlag, Reservation conflictWinner, Reservation reservationChanged) {
        this.thisElement = thisElement;
        this.conflictFlag = conflictFlag;
        this.conflictWinner = conflictWinner;
        this.reservationChanged = reservationChanged;
    }

    public Element getElement() {
        return thisElement;
    }

    public ReservationType getConflictFlag() {
        return conflictFlag;
    }

    public Reservation getConflictWinner() {
        return conflictWinner;
    }

    public Reservation getReservationChanged() {
        return reservationChanged;
    }
}
