package farpackage;

import generalpackage.*;
import adtpackage.*;

/**
 *
 * @author Zbyněk Stara
 */
public class FARExtension {
    private final Field field;
    private final Element thisElement;

    private Element verticalAccessTo = null; // priority = 3
    private Element horizontalAccessTo = null; // priority = 4
    private Element extraHorizontalAccessTo = null; // priority = 2
    private Element extraVerticalAccessTo = null; // priority = 1

    //private boolean isWait = false;
    //private boolean isProxy = false;
    //private boolean isFailure = false;

    private Reservation [] reservations;
    private Reservation [] ghostReservations;
    // FAULURE_CRITERION+1 deep = because we end at the number and start with 0
        
    public FARExtension(FARPathfinder pathfinder, Field field, Element thisElement) {
        this.field = field;

        this.thisElement = thisElement;

        reservations = new Reservation[FARPathfinder.FAILURE_CRITERION+1];
        for (int i = 0; i <= FARPathfinder.FAILURE_CRITERION; i++) {
            reservations[i] = null;
        }

        ghostReservations = new Reservation[FARPathfinder.FAILURE_CRITERION+1];
        for (int i = 0; i <= FARPathfinder.FAILURE_CRITERION; i++) {
            ghostReservations[i] = null;
        }
    }

    public void setReservation(int step, Reservation reservation) {
        reservations[step] = reservation;
    }
    public Reservation getReservation(int step) {
        return reservations[step];
    }

    public void setGhostReservation(int step, Reservation reservation) {
        ghostReservations[step] = reservation;
    }
    public Reservation getGhostReservation(int step) {
        return ghostReservations[step];
    }

    public void setVerticalAccessTo(Element element) {
        verticalAccessTo = element;
    }
    public void setHorizontalAccessTo(Element element) {
        horizontalAccessTo = element;
    }
    public void setExtraHorizontalAccessTo(Element element) {
        extraHorizontalAccessTo = element;
    }
    public void setExtraVerticalAccessTo(Element element) {
        extraVerticalAccessTo = element;
    }

    /*public void setIsWait(boolean isWait) {
        this.isWait = isWait;
    }
    public boolean isWait() {
        return isWait;
    }
    public void setIsProxy(boolean isProxy) {
        this.isProxy = isProxy;
    }
    public boolean isProxy() {
        return isProxy;
    }
    public void setIsFailure(boolean isFailure) {
        this.isFailure = isFailure;
    }
    public boolean isFailure() {
        return isFailure;
    }*/

    public List getAccessibleElements() {
        List accessibleList = new List();

        if (verticalAccessTo != null) accessibleList.insertAtFront(verticalAccessTo);
        if (horizontalAccessTo != null) accessibleList.insertAtFront(horizontalAccessTo);
        if (extraHorizontalAccessTo != null) accessibleList.insertAtFront(extraHorizontalAccessTo);
        if (extraVerticalAccessTo != null) accessibleList.insertAtFront(extraVerticalAccessTo);

        return accessibleList;
    }

    public String printReservations() {
        String returnString = "";
        for (int i = 0; i < reservations.length; i++) {
            if (reservations[i] != null) returnString += reservations[i].toString() + "; ";
            else returnString += "null; ";
        }
        return returnString;
    }
}
