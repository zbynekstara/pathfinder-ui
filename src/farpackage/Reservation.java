package farpackage;

import generalpackage.*;
import adtpackage.*;

/**
 *
 * @author Zbyněk Stara
 */
public class Reservation implements Printable {
    private ReservationType type;
    private Element thisElement;

    private FARAgent agent = null;
    
    private int reservationIndex = -1; // where in the sequence of reservation is this one
    private int step = -1; // which step does this one represent
    
    private Reservation previousReservation = null;

    //private boolean isComplete;
    //private boolean isFailure;

    //private List reservationPath; // the path that the agent will get if this reservation is honored

    private Reservation dependentReservation = null; // reservation that will only be honored if this one is

    private Reservation ghostReservation = null; // reservation to prevent head-on collisions or corridor collisions
    private Reservation originalReservation = null;
    
    private TreeSet overridenReservations = new TreeSet(); // which reservations were refused in favor of this one

    public Reservation() {

    }

    public Reservation(ReservationType type, Element thisElement, FARAgent agent, int reservationIndex, int step) {
        // ghost reservations
        this.type = type;
        this.thisElement = thisElement;
        this.agent = agent;
        this.reservationIndex = reservationIndex;
        this.step = step;        
    }

    public Reservation(ReservationType type, Element thisElement, FARAgent agent, int reservationIndex, int step, Reservation previousReservation) {
        this.type = type;
        this.thisElement = thisElement;
        this.agent = agent;
        this.reservationIndex = reservationIndex;
        this.step = step;
        this.previousReservation = previousReservation;
    }

    public Element getElement() {
        return thisElement;
    }

    public FARAgent getAgent() {
        return agent;
    }

    public int getReservationIndex() {
        return reservationIndex;
    }

    public int getStep() {
        return step;
    }

    public Reservation getPreviousReservation() {
        return previousReservation;
    }

    public void setDependentReservation(Reservation dependentReservation) {
        this.dependentReservation = dependentReservation;
    }
    public Reservation getDependentReservation() {
        return dependentReservation;
    }

    public void setGhostReservation(Reservation ghostReservation) {
        this.ghostReservation = ghostReservation;
    }
    public Reservation getGhostReservation() {
        return ghostReservation;
    }

    public void setOriginalReservation(Reservation originalReservation) {
        this.originalReservation = originalReservation;
    }
    public Reservation getOriginalReservation() {
        return originalReservation;
    }

    public void addOverridenReservation(Reservation overridenReservation) {
        overridenReservations.add(overridenReservation, overridenReservation.getAgent().FARAGENT_ID);
    }
    
    public Reservation searchOverridenReservation(FARAgent agent) {
        Reservation returnReservation = (Reservation) overridenReservations.search(agent.FARAGENT_ID);
        return returnReservation;
    }
    
    /*public void setReservationPath(List reservationPath) {
        this.reservationPath = reservationPath;
    }
    public List getReservationPath() { // RETURNING A COPY, NOT THE PATH ITSELF
        List returnPath = new List();

        for (int i = 0; i < reservationPath.size(); i++) {
            returnPath.insertAtRear((Element) reservationPath.getNodeData(i));
        }

        return returnPath;
    }*/
    /*public Element getReservationElement(int step) {
        Element reservationElement = (Element) reservationPath.getNodeData(step);
        return reservationElement;
    }*/

    public Element getCameFrom() {
        return previousReservation.getElement();
    }

    public boolean isInitialReservation() {
        if (type == ReservationType.INITIAL) return true;
        else return false;
    }

    public boolean isNormalReservation() {
        if (type == ReservationType.NORMAL) return true;
        else return false;
    }

    public boolean isWaitReservation() {
        //if (previousReservation.getElement() == thisElement) return true;
        if (type == ReservationType.WAIT) return true;
        else return false;
    }

    public boolean isProxyReservation() {
        if (type == ReservationType.PROXY) return true;
        else return false;
    }

    public String print() {
        return toString();
    }

    @Override
    public String toString() {
        if (previousReservation == null) return ("Initial reservation of agent " + agent.FARAGENT_ID);
        else return ("Reservation from agent " + agent.FARAGENT_ID + " (from " + previousReservation.getElement().print()+" to "+thisElement+")");
    }
}
