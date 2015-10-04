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
    
    //private int reservationIndex = -1; // where in the sequence of reservation is this one
    private int step = -1; // which step does this one represent
        
    private Reservation previousReservation = null;

    //private boolean isComplete;
    //private boolean isFailure;

    //private List reservationPath; // the path that the agent will get if this reservation is honored
    
    private Reservation dependentReservation = null; // reservation that will only be honored if this one is

    private Reservation ghostReservation = null; // reservation to prevent head-on collisions or corridor collisions
    private Reservation originalReservation = null;
    
    private TreeSet overridenAgents = new TreeSet(); // which agents were refused in favor of this one

    private Element proxyTarget = null; // if this is a proxy reservation, what is the target
    
    public Reservation(ReservationType type, Element thisElement, FARAgent agent, int step, Reservation previousReservation, Element proxyTarget) {
        this.type = type;
        this.thisElement = thisElement;
        this.agent = agent;
        this.step = step;
        this.previousReservation = previousReservation;
        this.proxyTarget = proxyTarget;
    }

    public Element getElement() {
        return thisElement;
    }

    public FARAgent getAgent() {
        return agent;
    }
    
    public Element getProxyTarget() {
        return proxyTarget;
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

    public void addOverridenAgent(FARAgent overridenAgent) {
        overridenAgents.add(overridenAgent, overridenAgent.FARAGENT_ID);
    }
    
    public boolean hasOverridenAgent(FARAgent agent) {
        return overridenAgents.contains(agent.FARAGENT_ID);
    }

    public Element getCameFrom() {
        if (previousReservation == null) return agent.START;
        else return previousReservation.getElement();
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
        if (type == ReservationType.WAIT) return true;
        else return false;
    }

    public boolean isProxyReservation() {
        //if (type == ReservationType.PROXY) return true;
        if (proxyTarget != null) return true;
        else return false;
    }

    public String print() {
        return toString();
    }

    @Override
    public String toString() {
        if (previousReservation == null) return ("Initial by agent "+agent.FARAGENT_ID+" (step "+step+")");
        if (type == ReservationType.INITIAL) return ("Initial by agent "+agent.FARAGENT_ID+" (step "+step+")"+" (from "+previousReservation.getElement().print()+" to "+thisElement+")");
        
        if (type == ReservationType.NORMAL) return ("Normal by agent "+agent.FARAGENT_ID+" (step "+step+")"+" (from "+previousReservation.getElement().print()+" to "+thisElement+")");
        
        if (type == ReservationType.WAIT && proxyTarget == null) return ("Wait by agent "+agent.FARAGENT_ID+" (step "+step+")"+" (from "+previousReservation.getElement().print()+" to "+thisElement+")");
        if (type == ReservationType.WAIT && proxyTarget != null) return ("Wait during proxy ending at "+proxyTarget+" by agent "+agent.FARAGENT_ID+" (step "+step+")"+" (from "+previousReservation.getElement().print()+" to "+thisElement+")");
        
        if (type == ReservationType.PROXY) return ("Proxy ending at "+proxyTarget+" by agent "+agent.FARAGENT_ID+" (step "+step+")"+" (from "+previousReservation.getElement().print()+" to "+thisElement+")");
        
        if (type == ReservationType.GHOST) return ("Ghost by agent "+agent.FARAGENT_ID+" (step "+step+")"+" (original reservation from "+originalReservation.getPreviousReservation().getElement().print()+" to "+originalReservation.getElement().print()+")");
        
        else throw new RuntimeException();
    }
}
