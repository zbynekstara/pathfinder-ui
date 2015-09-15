package farpackage;

import generalpackage.*;
import adtpackage.*;
import guipackage.*;

/**
 *
 * @author Zbyněk Stara
 */
public class FARPathfinder implements Pathfinder {
    /**
     * The AllocationWorker class is an extension of the SwingWorker<V, T> class
     * from the extension package javax.swing. Its purpose is to allow for a
     * computation-extensive part of the program to be executed in the
     * background, which is useful for the allocation algorithm.
     *
     * @author Zbyněk Stara
     */
    private class AllocationWorker extends javax.swing.SwingWorker<Boolean, Boolean> {
        AllocationWorker () {
            
        }

        /**
         * This method specifies what will be done in the background after the
         * allocation worker is executed with the aw.execute() method
         *
         * @return true in any case
         * @throws Exception if the allocation worker is unable to complete the
         * task
         *
         * @author Zbyněk Stara
         */
        @Override
        public Boolean doInBackground() throws Exception {
            startTime = System.currentTimeMillis();
            findAgentPathsHandler();
            return true;
        }

        /**
         * This method specifies what the program will do after the task in
         * the doInBackground() method finishes.
         *
         * @author Zbyněk Stara
         */
        @Override
        protected void done() {
            // the allocation info dialog will be reset and hidden
            gui.getInfoStepTF().setText("");
            gui.getInfoSuccessTF().setText("");
            gui.getInfoTimeTF().setText("");
            gui.getInfoDialog().setVisible(false);
        }
    }

    public void findAgentPaths() {
        // make a new allocation worker that will do the allocation in the background
        aw = new AllocationWorker();

        // this listener is added to update the numbers reported by the progress dialog
        aw.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                if ("update".equals(evt.getPropertyName())) {
                    thisPathfinder.updateInfo(); // update simStep
                }
            }
        });

        // this is to execute the allocation in the background
        // calls the doInBackground() function of the AllocationWorker
        aw.execute();
        
        // set up the allocation info dialog
        gui.getInfoStepTF().setText("");
        gui.getInfoSuccessTF().setText("");
        gui.getInfoTimeTF().setText("");
        gui.getInfoDialog().setVisible(true);
    }

    public final int RESERVATION_DEPTH = 3; // FAR's k parameter - reservation depth

    private final GUI gui;
    private final Field field;
    private final FARPathfinder thisPathfinder = this;

    private AllocationWorker aw; // allocation worker to handle the allocation in the background
    private long startTime;
    private int numUpdates;

    private FARAgent [] farAgents;

    private TreeSet [] farAgentGroups;

    private int simStep = 0; // simulation step

    private MinimalQueue agentReservationOrderAtStep = new MinimalQueue();

    private int numSuccesses = 0;
    private int numFailures = 0;

    public FARPathfinder(GUI gui, Field field) {
        // INITIALIZATION:
        this.gui = gui;
        this.field = field;

        // set farExtensions of all elements
        for (int x = 1; x < (field.X_DIMENSION - 1); x++) {
           for (int y = 1; y < (field.Y_DIMENSION - 1); y++) {
               field.getElement(x, y).setFARExtension(new FARExtension(this, field, field.getElement(x, y)));
           }
        }

        // field annotation
        fieldFlowAnnotation();
    }

    public void findAgentPathsHandler() {
        // make array of farAgents
        farAgents = new FARAgent[field.getAgentArray().length];
        for (int i = 0; i < field.getAgentArray().length; i++) {
            Agent agent = field.getAgentArray()[i];

            FARAgent newFARAgent = new FARAgent(field, this, agent.getStart(), agent.getGoal(), i);

            farAgents[i] = newFARAgent;
        }

        // make the farAgentGroups array
        farAgentGroups = new TreeSet[RESERVATION_DEPTH];
        for (int i = 0; i < RESERVATION_DEPTH; i++) {
            farAgentGroups[i] = new TreeSet();
        }
        TreeSet ungrouppedSet = new TreeSet();
        for (FARAgent currentAgent : farAgents) {
            ungrouppedSet.add(currentAgent, currentAgent.FARAGENT_ID);
        }

        // intitialize the farAgentGroupsArray with randomly selected sets of farAgents
        int currentGroupNum = 0;
        while (!ungrouppedSet.isEmpty()) {
            FARAgent addedAgent = null;
            do {
                int randomAgentNum = (int) (Math.random() * (farAgents.length));
                addedAgent = (FARAgent) ungrouppedSet.remove(farAgents[randomAgentNum].FARAGENT_ID);
            } while (addedAgent == null);

            farAgentGroups[currentGroupNum].add(addedAgent, addedAgent.FARAGENT_ID);

            addedAgent.setAgentGroup(currentGroupNum);

            currentGroupNum += 1;
            if (currentGroupNum == RESERVATION_DEPTH) currentGroupNum = 0;
        }

        // coordination stage
        System.out.println("Coordination stage");
        for (simStep = 0; simStep < FAILURE_CRITERION; simStep++) {
            // simStep 0 is the initial reservation step for all agents
            // simStep 1 is the first step for which real reservations are needed

            // initialization
            aw.firePropertyChange("update", simStep-1, simStep);
            System.out.println("Step: "+simStep);

            int agentGroupForStep = (simStep-1) % RESERVATION_DEPTH; // each step only sees coordination from a portion of agents

            // randomization of agents
            agentReservationOrderAtStep = new MinimalQueue(); // it is necessary to mix up the agent reservation order
            MaximalQueue unassignedAgents = new MaximalQueue(); // queue of agents awaiting assignment to reservation order

            if (simStep == 0) {
                // if this is step 0, enqueue everyone for initial reservation order assignment
                for (int i = simStep; i < farAgentGroups.length; i++) {
                    for (int j = 0; j < farAgentGroups[i].size(); j++) {
                        unassignedAgents.enqueue((FARAgent) farAgentGroups[i].getNodeData(j), ((FARAgent) farAgentGroups[i].getNodeData(j)).FARAGENT_ID);
                    }
                }
            } else if (simStep > 0) {
                // if this is not step 0, enqueue only agents from the appropriate agent group
                for (int i = 0; i < farAgentGroups[agentGroupForStep].size(); i++) {
                    unassignedAgents.enqueue((FARAgent) farAgentGroups[agentGroupForStep].getNodeData(i), ((FARAgent) farAgentGroups[agentGroupForStep].getNodeData(i)).FARAGENT_ID);
                }
            }

            while (!unassignedAgents.isEmpty()) {
                FARAgent chosenAgent = null;
                do {
                    int randomAgentIndex = (int) (Math.random() * (unassignedAgents.size())); // get a random index
                    double randomAgentKey = unassignedAgents.getNodeKey(randomAgentIndex); // identify an agent
                    chosenAgent = (FARAgent) unassignedAgents.remove(randomAgentKey); // get the agent
                } while (chosenAgent == null); // the above procedure may fail

                agentReservationOrderAtStep.enqueue(chosenAgent, simStep); // enqueue the chosen agent into reservation order
            }

            // reserving
            // during this part of the algrithm, some agents look ahead (up to RESERVATION_DEPTH steps) and reserve their required paths
            // this is different from the next step of the algorithm, where all agents actually perform the reservation at the current simStep
            while (!agentReservationOrderAtStep.isEmpty()) {
                // while there are agents to reserve for
                FARAgent currentAgent = (FARAgent) agentReservationOrderAtStep.dequeue();

                Reservation previousReservation = currentAgent.getLastReservation();

                // initializing
                int resStep; // reservation step
                // this is different from the simStep
                // currentStep can be up to RESERVATION_DEPTH larger than simStep

                if (previousReservation == null) resStep = 0;
                else resStep = previousReservation.getStep() + 1;

                int reservationIndex; // how many steps ahead should we plan

                if (currentAgent.getLastReservation() == null) reservationIndex = (RESERVATION_DEPTH - currentAgent.getAgentGroup()) - 1;
                // if this is the first time we reserve, reserve 1-RESERVATION_DEPTH steps
                else reservationIndex = (previousReservation.getReservationIndex() + 1) % RESERVATION_DEPTH;
                // if this is during normal reservations, reserve for the correct number of steps according to previous reservation

                System.out.println("\t\tCurrent agent: "+currentAgent.FARAGENT_ID+" (G: "+currentAgent.getAgentGroup()+", RI: "+reservationIndex+")");

                // making the actual reservations
                if ((currentAgent.getLastReservation() == null) || (simStep <= currentAgent.getAgentGroup() && currentAgent.getLastReservation().isInitialReservation())) {
                    // if this is the very first reservation
                    // or the simStep is lower than the agentGroup and previous reservation was initial = MAY LEAD TO PROBLEMS WHEN PROXY APPLIED AT VERY BEGINNING

                    reserve(currentAgent, ReservationType.INITIAL, previousReservation);

                    if (((RESERVATION_DEPTH - reservationIndex) - 1) > 0) { // THIS WAS !=
                        // add the agent back to the reservation order to get another initial reservation as needed
                        agentReservationOrderAtStep.enqueue(currentAgent, resStep);
                    } else {
                        // normally, this would do nothing
                        // but reassignments make it possible for a proxy reservation to be made before some of the later initial reservations are made
                        // that would increase the reservation index
                        // thus, it may be required to delete this extra initial assignment
                        while (agentReservationOrderAtStep.search(currentAgent) != -1) { // -1 means it was not found
                            System.out.println("\t\tDeleting extra assignment of agent "+currentAgent.FARAGENT_ID);
                            agentReservationOrderAtStep.delete(currentAgent);
                        }
                    }

                } else if (resStep < FAILURE_CRITERION) {
                    // normal reservation
                    // we have to end before currentStep reaches failure criterion
                    // we do not want to try to reserve at impossible times

                    if (resStep < currentAgent.getAgentPathSize()) {
                        // if currentStep is smaller than the size of the agent's reservation path
                        // we still have stuff to do

                        reserve(currentAgent, ReservationType.NORMAL, previousReservation);

                        if (((RESERVATION_DEPTH - reservationIndex) - 1) > 0) { // SHOULD THIS BE > ?
                            // if there are still reservations to be made, put the agent back in reservation order
                            agentReservationOrderAtStep.enqueue(currentAgent, resStep);
                        } else {
                            // normally, this would do nothing
                            // but it is possible for proxy assignments to be made before this happens
                            // then the reservation index would be bigger
                            // and thus the extra assignemnts would need to be scrapped
                            while (agentReservationOrderAtStep.search(currentAgent) != -1) { // -1 means not found
                                System.out.println("\t\tDeleting extra assignment of agent "+currentAgent.FARAGENT_ID);
                                agentReservationOrderAtStep.delete(currentAgent);
                            }
                        }

                    } else {
                        // if this step is after the end of the reservation path
                        // end by waiting at the finish point

                        reserve(currentAgent, ReservationType.WAIT, previousReservation);

                        if (((RESERVATION_DEPTH - reservationIndex) - 1) > 0) {
                            // if there are still some resSteps for which to reserve
                            // add the agent back to reservation order
                            agentReservationOrderAtStep.enqueue(currentAgent, resStep);
                        } else {
                            while (agentReservationOrderAtStep.search(currentAgent) != -1) { // -1 means not found
                                System.out.println("\t\tDeleting extra assignment of agent "+currentAgent.FARAGENT_ID);
                                agentReservationOrderAtStep.delete(currentAgent);
                            }
                        }
                    }
                }
            }

            // Moving agents
            System.out.println("\tMoving agents");
            for (FARAgent currentAgent : farAgents) {
                // going through all agents

                System.out.println("\t\tAdjusting path of agent "+currentAgent.FARAGENT_ID+"");

                System.out.println("\t\t\tLast honored reservation: "+currentAgent.getLastHonoredReservation());
                
                // perform the reservation that is lined up for this agent
                Reservation reservationToHonor;
                if (currentAgent.getLastHonoredReservation() == null) reservationToHonor = currentAgent.getFirstReservation(); // first reservation
                else reservationToHonor = currentAgent.getLastHonoredReservation().getDependentReservation();
                System.out.println("\t\t\tReservation to honor: "+reservationToHonor);
                
                // modify agent path
                //currentAgent.setAgentPath(reservationToHonor.getReservationPath());
                
                //currentAgent.splitPathSegment(simStep);
                Element pathElement = currentAgent.getPathElement(simStep);
                currentAgent.enqueueFinalPathElement(pathElement);
                
                currentAgent.setLastHonoredReservation(reservationToHonor);

                // if this is the last element of the agent's path, finish
                Element currentElement = currentAgent.getPathElement(simStep);
                if (currentElement == currentAgent.GOAL && !currentAgent.isComplete()) {
                    System.out.println("\t\tSuccess for agent: "+currentAgent.FARAGENT_ID);
                    numSuccesses += 1;
                    currentAgent.setIsComplete(true);
                }
            }
        }

        // wrap up afer all simSteps are exhausted
        System.out.println("After coordination stage");
        for (FARAgent currentAgent : farAgents) {
            if (!currentAgent.isComplete()) {
                //currentAgent.getPathElement(FAILURE_CRITERION - 1).getFARExtension().setIsFailure(true);
                System.out.println("\tFailure for agent: "+currentAgent.FARAGENT_ID);
                numFailures += 1;
            }
        }

        System.out.println("Number of failures: "+numFailures);
    }

    // draw method for gui
    public Element [] getAgentsAtStep(int drawStep) { // returns an array of elements
        Element [] elementArray = new Element[farAgents.length];

        for (int i = 0; i < farAgents.length; i++) {
            FARAgent currentAgent = farAgents[i];
            if (drawStep < currentAgent.getFinalAgentPathSize()) { // this should not be a thing
                elementArray[i] = (Element) currentAgent.getFinalPathElement(drawStep);
            } else {
                elementArray[i] = currentAgent.GOAL;
            }
        }

        return elementArray;
    }

    // draw method for gui
    public List [] getAgentPathsUntilStep(int drawStep) { // returns an array of lists
        List[] pathArray = new List[farAgents.length];

        for (int i = 0; i < farAgents.length; i++) {
            FARAgent currentAgent = farAgents[i];
            pathArray[i] = currentAgent.getFinalAgentPathUntilStep(drawStep);
        }

        return pathArray;
    }

    public int getNumFailures() {
        return numFailures;
    }

    // this roates the priority directions according to simStep
    // lower number is better
    public int getPriority(int step, Element firstElement, Element secondElement) {
        if (firstElement.isEqual(secondElement)) return 4; // waiting

        int incrementor = step % 4;

        if (field.getNorthElement(firstElement).isEqual(secondElement)) {
            int priority = (0 + incrementor) % 4;
            return priority;
        } else if (field.getEastElement(firstElement).isEqual(secondElement)) {
            int priority = (1 + incrementor) % 4;
            return priority;
        } else if (field.getSouthElement(firstElement).isEqual(secondElement)) {
            int priority = (2 + incrementor) % 4;
            return priority;
        } else if (field.getWestElement(firstElement).isEqual(secondElement)) {
            int priority = (3 + incrementor) % 4;
            return priority;
        } else {
            throw new RuntimeException();
        }
    }

    private void reserve(FARAgent agent, ReservationType reservationType, Reservation previousReservation) {
        if (reservationType == ReservationType.NORMAL || reservationType == ReservationType.PROXY) {
            reserveMovement(agent, reservationType, previousReservation);
        }
        
        else if (reservationType == ReservationType.WAIT) {
            reserveWait(agent, reservationType, previousReservation);
        }
        
        else if (reservationType == ReservationType.INITIAL) {
            reserveInitial(agent, reservationType, previousReservation);
        }
        
        else { // if reservationType is NULL
            throw new RuntimeException();
        }
    }
    
    private void reserveMovement(FARAgent agent, ReservationType reservationType, Reservation previousReservation) {
        int currentStep;
        Element comingFrom;
        if (previousReservation == null) {
            currentStep = 0;
            comingFrom = agent.START;
        }
        else {
            currentStep = previousReservation.getStep() + 1;
            comingFrom = previousReservation.getElement();
        }
        
        Element elementToBeReserved = agent.getPathElement(currentStep);

        if (reservationType == ReservationType.NORMAL) System.out.println("\t\t\tReserving a normal reservation at element: "+elementToBeReserved+" (step "+currentStep+")");
        else System.out.println("\t\t\tReserving a proxy reservation at element: "+elementToBeReserved+" (step "+currentStep+")");

        Reservation obstructingReservation = elementToBeReserved.getFARExtension().getReservation(currentStep);
        Reservation obstructingGhostReservation = elementToBeReserved.getFARExtension().getGhostReservation(currentStep);

        Reservation obstructingGhostOriginalReservation;
        if (obstructingGhostReservation != null) obstructingGhostOriginalReservation = obstructingGhostReservation.getOriginalReservation();
        else obstructingGhostOriginalReservation = null;

        //if (obstructingGhostOriginalReservation != null && obstructingGhostOriginalReservation.getElement() == comingFrom) { // head-on collision!
        if (obstructingGhostOriginalReservation != null && obstructingGhostOriginalReservation == comingFrom.getFARExtension().getReservation(currentStep)) {
            // head-on collision!
            
            System.out.println("\t\t\t\tObstructing ghost reservation: "+obstructingGhostReservation);
            System.out.println("\t\t\t\tPrevious reservation: "+previousReservation);

            int ghostReservationPriority = getPriority(currentStep, elementToBeReserved, comingFrom);
            int intendedReservationPriority = getPriority(currentStep, comingFrom, elementToBeReserved);
            
            if (ghostReservationPriority == intendedReservationPriority) {
                throw new RuntimeException();
            }

            else if (ghostReservationPriority < intendedReservationPriority) {
                // the ghost reservation overrules intended reservation
                // current agent needs to move out of the way of the ghost's original agent

                if (comingFrom.getFARExtension().isProxyAvailable(currentStep)) {
                    // if current agent has a place to go
                    // keep the obstructing agent at its reserved position and move away

                    FARAgent obstructingAgent = obstructingGhostOriginalReservation.getAgent();
                    System.out.println("\t\t\t\tConflict; normal reservation changed to proxy because of head-on collision with agent: "+obstructingAgent.FARAGENT_ID+" ("+obstructingGhostOriginalReservation.getReservationIndex()+")"+" (step "+currentStep+")");
                    
                    // add a proxy to current agent's path
                    reserveProxyForAgent(agent, previousReservation);
                }

                else {
                    // there is no proxy available to current agent
                    
                    Reservation preObstructingGhostOriginalReservation = obstructingGhostOriginalReservation.getPreviousReservation();
                    
                    if (preObstructingGhostOriginalReservation.getElement().getFARExtension().isProxyAvailable(currentStep)) {
                        // if the ghost's original agent's previous reservation can be changed so that a proxy is chosen
                        // WATCH OUT WITH RESERVATION INDICES - MIGHT NOT BE ABLE TO GO AT ALL BECAUSE OF NATURE OF RESERVATION
                        // evict obstructing reservation

                        FARAgent obstructingAgent = obstructingGhostOriginalReservation.getAgent();
                        System.out.println("\t\t\t\tConflict (head-on) with no proxy available; reservation evicts agent: "+obstructingAgent.FARAGENT_ID+" ("+obstructingGhostOriginalReservation.getReservationIndex()+")"+" (step "+currentStep+")");

                        evictObstructingReservation(obstructingGhostOriginalReservation); // PROBLEM - HOW TO ENSURE THE AGENT DOESN'T RE-RESERVE THE ELEMENT?

                        // and set intended reservation to a be the reservation at current step
                        reserveElementForAgent(agent, reservationType, previousReservation);

                        System.out.println("\t\t\t\t\tNew reservation: "+elementToBeReserved.getFARExtension().getReservation(currentStep));
                    }

                    else {
                        // there is no proxy available to either one of the agents
                        // evict obstructing reservation of them and make them both wait
                        
                        FARAgent obstructingAgent = obstructingGhostOriginalReservation.getAgent();
                        System.out.println("\t\t\t\tConflict (head-on) with no proxy available; reservation evicts agent: "+obstructingAgent.FARAGENT_ID+" ("+obstructingGhostOriginalReservation.getReservationIndex()+")"+" (step "+currentStep+")");

                        evictObstructingReservation(obstructingGhostOriginalReservation); // PROBLEM - HOW TO ENSURE THE AGENT DOESN'T RE-RESERVE THE ELEMENT?

                        // and change intended reservation to a wait reservation
                        System.out.println("\t\t\t\tConflict (head-on); agent has to replan with a wait reservation because of agent: "+obstructingAgent.FARAGENT_ID+" ("+obstructingGhostOriginalReservation.getReservationIndex()+")"+" (step "+currentStep+")");
                        reserve(agent, ReservationType.WAIT, previousReservation); // IMPORTANT
                    }
                }
            }

            else {
                // intended reservation overwrites the ghost reservation
                // ghost's original agent needs to move out of the way of the current agent
                
                Reservation preObstructingGhostOriginalReservation = obstructingGhostOriginalReservation.getPreviousReservation();
                
                if (preObstructingGhostOriginalReservation.getElement().getFARExtension().isProxyAvailable(currentStep)) {
                    // rewrite obstructing reservation since there is a proxy path available
                    
                    FARAgent obstructingAgent = obstructingGhostOriginalReservation.getAgent();
                    System.out.println("\t\t\t\tConflict (head-on); reservation evicts agent: "+obstructingAgent.FARAGENT_ID+" ("+obstructingGhostOriginalReservation.getReservationIndex()+")"+" (step "+currentStep+")");

                    evictObstructingReservation(obstructingGhostOriginalReservation);

                    // set reservation to be intended reservation
                    reserveElementForAgent(agent, reservationType, previousReservation);

                    System.out.println("\t\t\t\t\tNew reservation: "+elementToBeReserved.getFARExtension().getReservation(currentStep));
                }

                else {
                    // there is no proxy that the obstructing agent could take
                    // keep obstructing reservation
                    // can current agent move to a proxy?
                    
                    if (comingFrom.getFARExtension().isProxyAvailable(currentStep)) {
                        // if current agent has a place to go
                        // keep the obstructing agent at its reserved position and move away

                        FARAgent obstructingAgent = obstructingGhostOriginalReservation.getAgent();
                        System.out.println("\t\t\t\tConflict; normal reservation changed to proxy because of head-on collision with agent: "+obstructingAgent.FARAGENT_ID+" ("+obstructingGhostOriginalReservation.getReservationIndex()+")"+" (step "+currentStep+")");

                        // add a proxy to current agent's path
                        reserveProxyForAgent(agent, previousReservation);
                    }
                    
                    else {
                        // neither agent can move to a proxy
                        FARAgent obstructingAgent = obstructingGhostOriginalReservation.getAgent();
                        System.out.println("\t\t\t\tConflict (head-on) with no proxy available; reservation evicts agent: "+obstructingAgent.FARAGENT_ID+" ("+obstructingGhostOriginalReservation.getReservationIndex()+")"+" (step "+currentStep+")");

                        evictObstructingReservation(obstructingGhostOriginalReservation); // PROBLEM - HOW TO ENSURE THE AGENT DOESN'T RE-RESERVE THE ELEMENT?

                        // and change intended reservation to a wait reservation
                        System.out.println("\t\t\t\tConflict (head-on); agent has to replan with a wait reservation because of agent: "+obstructingAgent.FARAGENT_ID+" ("+obstructingGhostOriginalReservation.getReservationIndex()+")"+" (step "+currentStep+")");
                        reserve(agent, ReservationType.WAIT, previousReservation); // IMPORTANT
                    }
                }
            } 
        }

        else if (obstructingReservation == null) {
            // if there is no reservation at the currentStep
            // reserve this element for the current agent
            reserveElementForAgent(agent, reservationType, previousReservation);
        }

        else {
            // side-on collision
            
            System.out.println("\t\t\t\tObstructing reservation: "+obstructingReservation);
            System.out.println("\t\t\t\tPrevious reservation: "+previousReservation);

            int obstructingReservationPriority = getPriority(currentStep, obstructingReservation.getCameFrom(), elementToBeReserved);
            int intendedReservationPriority = getPriority(currentStep, comingFrom, elementToBeReserved); // THIS IS THE PROBLEM
                        
            if (obstructingReservationPriority == intendedReservationPriority) {
                // should not happen
                throw new RuntimeException();
            }

            else if (obstructingReservationPriority < intendedReservationPriority) {
                // obstructing reservation overrules intended reservation
                // keep the obstructing reservation

                // intended reservation needs to be replanned as wait reservation
                System.out.println("\t\t\t\tConflict; agent has to replan with a wait reservation because of agent: "+obstructingReservation.getAgent().FARAGENT_ID+" (step "+currentStep+")");
                reserve(agent, ReservationType.WAIT, previousReservation);
            }

            else {
                // new reservation overwrites existing reservation
                
                if (obstructingReservation.isNormalReservation() || obstructingReservation.isProxyReservation()) {
                    // if obstructing reservation is movement reservation
                    
                    FARAgent obstructingAgent = obstructingReservation.getAgent();
                    System.out.println("\t\t\t\tConflict; agent overrides existing non-wait reservation of: "+obstructingAgent.FARAGENT_ID+" ("+obstructingReservation.getReservationIndex()+")"+" (step "+currentStep+")");

                    // make the obstructing agent move
                    evictObstructingReservation(obstructingReservation);

                    // rewrite reservation for the intended reservation
                    reserveElementForAgent(agent, reservationType, previousReservation);

                    System.out.println("\t\t\t\t\tNew reservation: "+elementToBeReserved.getFARExtension().getReservation(currentStep));
                }

                else {
                    // if obstructing reservation is a wait reservation or an initial reservation
                    
                    if (elementToBeReserved.getFARExtension().isProxyAvailable(currentStep)) {
                        // remove obstructing reservation since there is a proxy path available for it
                        
                        FARAgent obstructingAgent = obstructingReservation.getAgent();
                        if (obstructingReservation.isWaitReservation()) System.out.println("\t\t\t\tConflict; agent overrides existing wait reservation of: "+obstructingAgent.FARAGENT_ID+" ("+obstructingReservation.getReservationIndex()+")"+" (step "+currentStep+")");
                        else System.out.println("\t\t\t\tConflict; agent overrides existing initial reservation of: "+obstructingAgent.FARAGENT_ID+" ("+obstructingReservation.getReservationIndex()+")"+" (step "+currentStep+")");

                        // make the obstructing agent move
                        evictObstructingReservation(obstructingReservation);

                        // rewrite reservation for the intended reservation
                        reserveElementForAgent(agent, reservationType, previousReservation);

                        System.out.println("\t\t\t\t\tNew reservation: "+elementToBeReserved.getFARExtension().getReservation(currentStep));
                    }

                    else {
                        // there is no proxy the waiting element could take
                        // keep the obstructing reservation
                        
                        // intended reservation needs to be replanned as wait reservation
                        System.out.println("\t\t\t\tConflict; agent has to replan with a wait reservation because of agent: "+obstructingReservation.getAgent().FARAGENT_ID+" ("+obstructingReservation.getReservationIndex()+")"+" (step "+currentStep+")");
                        reserve(agent, ReservationType.WAIT, previousReservation);
                    }
                }
            }
        }
    }
    
    private void reserveWait(FARAgent agent, ReservationType reservationType, Reservation previousReservation) {
        int currentStep;
        Element comingFrom;
        if (previousReservation == null) {
            currentStep = 0;
            comingFrom = agent.START;
        }
        else {
            currentStep = previousReservation.getStep() + 1;
            comingFrom = previousReservation.getElement();
        }
        
        Element elementToBeReserved = comingFrom;

        System.out.println("\t\t\tWait at element: "+elementToBeReserved+" (step "+currentStep+")");

        Reservation obstructingReservation = elementToBeReserved.getFARExtension().getReservation(currentStep);

        if (obstructingReservation == null) {
            // there is no obstructing reservation at element at current step
            // reserve this element for the current agent
            reserveElementForAgent(agent, reservationType, previousReservation);
        }

        else {
            // there is a reservation at the currentStep
            
            System.out.println("\t\t\t\tExisting reservation: "+obstructingReservation);
            System.out.println("\t\t\t\tPrevious reservation: "+previousReservation);
            
            int obstructingReservationPriority = getPriority(currentStep, obstructingReservation.getCameFrom(), elementToBeReserved);
            int intendedReservationPriority = getPriority(currentStep, comingFrom, elementToBeReserved); // THIS IS THE PROBLEM

            if (obstructingReservationPriority == intendedReservationPriority) {
                throw new RuntimeException();
            }

            else if (obstructingReservationPriority < intendedReservationPriority) {
                // existing reservation overrules new reservation - ALWAYS
                // there are some decisions to be done about optimization here - does wait always give way?
                // intended reservation needs to be replanned

                if (elementToBeReserved.getFARExtension().isProxyAvailable(currentStep)) {
                    // a proxy is available instead of intended reservation
                    // keep existing reservation

                    // reserve current agent for a proxy
                    FARAgent obstructingAgent = obstructingReservation.getAgent();
                    System.out.println("\t\t\t\tConflict; wait reservation changed to proxy because of agent: "+obstructingAgent.FARAGENT_ID+" ("+obstructingReservation.getReservationIndex()+")"+" (step "+currentStep+")");

                    reserveProxyForAgent(agent, previousReservation);
                }

                else {
                    // there is no proxy current agent could take
                    // evict obstructing reservation
                    
                    FARAgent existingAgent = obstructingReservation.getAgent();
                    System.out.println("\t\t\t\tConflict with no proxy available; wait reservation evicts agent: "+existingAgent.FARAGENT_ID+" ("+obstructingReservation.getReservationIndex()+")"+" (step "+currentStep+")");

                    evictObstructingReservation(obstructingReservation);

                    // proceed with the intended plan and reserve a wait at the element
                    reserveElementForAgent(agent, reservationType, previousReservation);

                    System.out.println("\t\t\t\t\tNew reservation: "+elementToBeReserved.getFARExtension().getReservation(currentStep));
                }
            }

            else {
                // new reservation overwrites existing reservation - NEVER
                throw new RuntimeException();
            }
        }
    }
    
    private void reserveInitial(FARAgent agent, ReservationType reservationType, Reservation previousReservation) {
        int currentStep;
        Element comingFrom;
        if (previousReservation == null) {
            currentStep = 0;
            comingFrom = agent.START;
        }
        else {
            currentStep = previousReservation.getStep() + 1;
            comingFrom = previousReservation.getElement();
        }
        
        Element elementToBeReserved = comingFrom;

        System.out.println("\t\t\tInitial reservation at element: "+elementToBeReserved+" (step "+currentStep+")");

        Reservation obstructingReservation = elementToBeReserved.getFARExtension().getReservation(currentStep);

        if (obstructingReservation == null) { // if there is no reservation at the currentStep
            // reserve this element for the current agent
            reserveElementForAgent(agent, reservationType, previousReservation);
        }

        else { // there is a reservation at the currentStep
            System.out.println("\t\t\t\tObstructing reservation: "+obstructingReservation);
            System.out.println("\t\t\t\tPrevious reservation: "+previousReservation);
            
            int obstructingReservationPriority = getPriority(currentStep, obstructingReservation.getCameFrom(), elementToBeReserved);
            int intendedReservationPriority = getPriority(currentStep, comingFrom, elementToBeReserved);

            if (obstructingReservationPriority == intendedReservationPriority) {
                throw new RuntimeException();
            }

            else if (obstructingReservationPriority < intendedReservationPriority) {
                // obstructing reservation overrules intended reservation - ALWAYS
                // there are some decisions to be done about optimization here - does wait always give way?
                // intended reservation needs to be replanned
                
                if (comingFrom.getFARExtension().isProxyAvailable(currentStep)) {
                    // if there is a proxy path current agent could take
                    // keep obstructing reservation

                    // rewrite intended reservation since there is a proxy path available for it
                    FARAgent obstructingAgent = obstructingReservation.getAgent();
                    System.out.println("\t\t\t\tConflict; initial reservation changed to proxy because of agent: "+obstructingAgent.FARAGENT_ID+" ("+obstructingReservation.getReservationIndex()+")"+" (step "+currentStep+")");

                    reserveProxyForAgent(agent, previousReservation);
                }

                else {
                    // there is no proxy current agent could take
                    // evict obstructing reservation
                    FARAgent obstructingAgent = obstructingReservation.getAgent();
                    System.out.println("\t\t\t\tConflict with no proxy available; initial reservation evicts agent: "+obstructingAgent.FARAGENT_ID+" ("+obstructingReservation.getReservationIndex()+")"+" (step "+currentStep+")");

                    evictObstructingReservation(obstructingReservation);

                    // reserve for current agent according to intended plan
                    reserveElementForAgent(agent, reservationType, previousReservation);

                    System.out.println("\t\t\t\t\tNew reservation: "+elementToBeReserved.getFARExtension().getReservation(currentStep));
                }
            }

            else { // new reservation overwrites existing reservation - NEVER
                throw new RuntimeException();
            }
        }
    }
    
    private boolean wasOverriden(FARAgent agent, Reservation obstructingReservation) {
        // determines whether the agent was already overriden by this obstructingReservation
        // is it necessary to specify previousReservation or is the information sufficient?
        
        Reservation overridenReservation = obstructingReservation.searchOverridenReservation(agent);
        if (overridenReservation != null) return true;
        else return false;
    }
    
    private void reserveProxyForAgent(FARAgent agent, Reservation previousReservation) {
        //reservationElement.getFARExtension().setIsProxy(true);

        int currentStep;
        int reservationIndex;
        Element comingFrom;
        
        if (previousReservation == null) {
            // should not happen
            throw new RuntimeException();
            
            /*currentStep = 0;
            reservationIndex = RESERVATION_DEPTH-agent.getAgentGroup()-1;
            comingFrom = agent.START;*/
        }
        else {
            currentStep = previousReservation.getStep() + 1;
            reservationIndex = (previousReservation.getReservationIndex() + 1) % RESERVATION_DEPTH;
            comingFrom = previousReservation.getElement();
        }
        
        // creating proxy path
        List proxyPath = comingFrom.getFARExtension().getProxyPath(currentStep);
        // proxy path should be inserted after the last okay element

        Element proxyStart = (Element) proxyPath.getNodeData(0);

        //List agentPath = previousReservation.getReservationPath();

        /*for (int i = 0; i < proxyPath.size(); i++) { // ADDING THE PROXY PATH TO THE AGENT PATH
            Element currentProxyElement = (Element) proxyPath.getNodeData(i);
            agentPath.insertAsNode(currentProxyElement, currentStep+i);
        }*/
        
        // adding proxy path to agent's path
        agent.insertPathSegment(currentStep, proxyPath);

        // making corresponding reservation
        Reservation newReservation = new Reservation(ReservationType.PROXY, proxyStart, agent, reservationIndex, currentStep, previousReservation); // CHECK IF THIS IS CORRECT
        //newReservation.setReservationPath(agentPath);
        
        /*Reservation newGhostReservation = null;
        if (previousReservation != null) {
            newGhostReservation = new Reservation(ReservationType.GHOST, comingFrom, agent, reservationIndex, currentStep);
            newGhostReservation.setOriginalReservation(newReservation);
        }
        newReservation.setGhostReservation(newGhostReservation);*/
        
        Reservation newGhostReservation = new Reservation(ReservationType.GHOST, comingFrom, agent, reservationIndex, currentStep);
        newGhostReservation.setOriginalReservation(newReservation);
        newReservation.setGhostReservation(newGhostReservation);

        // adding reservation to element
        proxyStart.getFARExtension().setReservation(currentStep, newReservation);

        // linking reservation
        previousReservation.setDependentReservation(newReservation);
        
        // acknowledging reservation at the agent
        agent.setLastReservation(newReservation);
    }

    private void reserveElementForAgent(FARAgent agent, ReservationType reservationType, Reservation previousReservation) {
        //if (reservationType == ReservationType.WAIT) reservationElement.getFARExtension().setIsWait(true);

        int currentStep;
        int reservationIndex;
        Element comingFrom;
        
        if (/*reservationType == ReservationType.INITIAL && */previousReservation == null) {
            currentStep = 0;
            reservationIndex = RESERVATION_DEPTH-agent.getAgentGroup()-1;
            comingFrom = agent.START;
        }
        else {
            currentStep = previousReservation.getStep() + 1;
            reservationIndex = (previousReservation.getReservationIndex() + 1) % RESERVATION_DEPTH;
            comingFrom = previousReservation.getElement();
        }
        
        // adding to agent path
        /*List agentPath;
        if (reservationType == ReservationType.INITIAL && previousReservation == null) agentPath = agent.getAgentPath();
        else agentPath = previousReservation.getReservationPath();*/
        
        Element elementToBeReserved;
        if (reservationType == ReservationType.INITIAL) {
            elementToBeReserved = comingFrom;
            agent.insertPathElement(currentStep, elementToBeReserved);
        }
        else if (reservationType == ReservationType.WAIT) {
            elementToBeReserved = comingFrom;
            agent.insertPathElement(currentStep, elementToBeReserved);
        }
        else if (reservationType == ReservationType.NORMAL) {
            elementToBeReserved = agent.getPathElement(currentStep);
        }
        else {
            throw new RuntimeException();
        }

        // making corresponding reservation
        Reservation newReservation = new Reservation(reservationType, elementToBeReserved, agent, reservationIndex, currentStep, previousReservation);
        //newReservation.setReservationPath(agentPath);

        Reservation newGhostReservation = null;
        if (previousReservation != null) {
            newGhostReservation = new Reservation(ReservationType.GHOST, comingFrom, agent, reservationIndex, currentStep);
            newGhostReservation.setOriginalReservation(newReservation);
        }
        newReservation.setGhostReservation(newGhostReservation);

        // adding reservation to element
        elementToBeReserved.getFARExtension().setReservation(currentStep, newReservation);

        // linking reservation
        if (previousReservation != null) previousReservation.setDependentReservation(newReservation);
        else agent.setFirstReservation(newReservation);
        
        // acknowledging reservation at the agent
        agent.setLastReservation(newReservation);
    }

    private void evictObstructingReservation(Reservation obstructingReservation) {
        FARAgent obstructingAgent = obstructingReservation.getAgent();

        // determine what to remove
        Reservation reservationToRemove = obstructingReservation;
        Reservation ghostReservationToRemove = obstructingReservation.getGhostReservation();
        System.out.println("\t\t\t\t\t"+(RESERVATION_DEPTH-obstructingReservation.getReservationIndex())+" reservations should be removed:");
        //reservationToRemove.getAgent().setLastReservation(reservationToRemove.getPreviousReservation()); // set the removed agent's last reservation to be the last one that was not removed

        // going down the dependecy chain of reservations
        int removeStep = reservationToRemove.getStep();
        int counter = 0;
        while (reservationToRemove != null) {
            System.out.println("\t\t\t\t\t\tRemoving reservation: "+reservationToRemove);
            
            reservationToRemove.getElement().getFARExtension().setReservation(removeStep + counter, null);
            ghostReservationToRemove.getElement().getFARExtension().setGhostReservation(removeStep + counter, null);
            
            reservationToRemove = reservationToRemove.getDependentReservation();
            counter += 1;
            
            // memory leaks
        }

        // consolidate the path of the agent
        Reservation preObstructingReservation = obstructingReservation.getPreviousReservation();

        // set the pre-obstructing reservation to be the last reservation of the agent
        preObstructingReservation.setDependentReservation(null);
        obstructingAgent.setLastReservation(preObstructingReservation);

        // add the reservation back to reservation order queue
        agentReservationOrderAtStep.enqueue(obstructingAgent, preObstructingReservation.getStep() + 1);
    }

    private void fieldFlowAnnotation() {
        for (int i = 1; i < (field.X_DIMENSION - 1); i++) {
            for (int j = 1; j < (field.Y_DIMENSION - 1); j++) {
                Element currentElement = field.getElement(i, j);

                boolean flowNorthward = false;
                boolean flowSouthward = false;
                boolean flowWestward = false;
                boolean flowEastward = false;

                boolean nBl = field.getNorthElement(currentElement).isBlocked();
                boolean neBl = field.getNorthEastElement(currentElement).isBlocked();
                boolean eBl = field.getEastElement(currentElement).isBlocked();
                boolean seBl = field.getSouthEastElement(currentElement).isBlocked();
                boolean sBl = field.getSouthElement(currentElement).isBlocked();
                boolean swBl = field.getSouthWestElement(currentElement).isBlocked();
                boolean wBl = field.getWestElement(currentElement).isBlocked();
                boolean nwBl = field.getNorthWestElement(currentElement).isBlocked();

                boolean northBlocked = field.getNorthElement(currentElement).isBlocked();
                boolean eastBlocked = field.getEastElement(currentElement).isBlocked();
                boolean southBlocked = field.getSouthElement(currentElement).isBlocked();
                boolean westBlocked = field.getWestElement(currentElement).isBlocked();

                if (!currentElement.isObstacle()) {
                    if ((i % 2) == 1) {
                        flowNorthward = true;
                    } else {
                        flowSouthward = true;
                    }

                    if ((j % 2) == 1) {
                        flowEastward = true;
                    } else {
                        flowWestward = true;
                    }

                    if (flowEastward && !eastBlocked) {
                        currentElement.getFARExtension().setHorizontalAccessTo(field.getEastElement(currentElement));
                    } else if (flowWestward && !westBlocked) {
                        currentElement.getFARExtension().setHorizontalAccessTo(field.getWestElement(currentElement));
                    }

                    if (flowNorthward && !northBlocked) {
                        currentElement.getFARExtension().setVerticalAccessTo(field.getNorthElement(currentElement));
                    } else if (flowSouthward && !southBlocked) {
                        currentElement.getFARExtension().setVerticalAccessTo(field.getSouthElement(currentElement));
                    }

                    // Zbyněk Stara
                    if (flowNorthward && !northBlocked) {
                        boolean nnBl = field.getNorthElement(field.getNorthElement(currentElement)).isBlocked();
                        if (flowEastward) {
                            boolean nneBl = field.getNorthEastElement(field.getNorthElement(currentElement)).isBlocked();
                            if (!((!wBl && !nwBl) || (!nnBl && !nneBl && !neBl && !eBl && !seBl && !sBl))) {
                                field.getNorthElement(currentElement).getFARExtension().setExtraVerticalAccessTo(currentElement);
                            }
                        } else if (flowWestward) {
                            boolean nnwBl = field.getNorthWestElement(field.getNorthElement(currentElement)).isBlocked();
                            if (!((!eBl && !neBl) || (!nnBl && !nnwBl && !nwBl && ! wBl && !swBl && !sBl))) {
                                field.getNorthElement(currentElement).getFARExtension().setExtraVerticalAccessTo(currentElement);
                            }
                        }
                    } else if (flowSouthward && !southBlocked) {
                        boolean ssBl = field.getSouthElement(field.getSouthElement(currentElement)).isBlocked();
                        if (flowEastward) {
                            boolean sseBl = field.getSouthEastElement(field.getSouthElement(currentElement)).isBlocked();
                            if (!((!wBl && !swBl) || (!ssBl && !sseBl && !seBl && !eBl && !neBl && !nBl))) {
                                field.getSouthElement(currentElement).getFARExtension().setExtraVerticalAccessTo(currentElement);
                            }
                        } else if (flowWestward) {
                            boolean sswBl = field.getSouthWestElement(field.getSouthElement(currentElement)).isBlocked();
                            if (!((!eBl && !seBl) || (!ssBl && !sswBl && !swBl && !wBl && !nwBl && !nBl))) {
                                field.getSouthElement(currentElement).getFARExtension().setExtraVerticalAccessTo(currentElement);
                            }
                        }
                    }

                    if (flowEastward && !eastBlocked) {
                        boolean eeBl = field.getEastElement(field.getEastElement(currentElement)).isBlocked();
                        if (flowNorthward) {
                            boolean eneBl = field.getNorthEastElement(field.getEastElement(currentElement)).isBlocked();
                            if (!((!sBl && !seBl) || (!eeBl && !eneBl && !neBl && !nBl && !nwBl && !wBl))) {
                                field.getEastElement(currentElement).getFARExtension().setExtraHorizontalAccessTo(currentElement);
                            }
                        } else if (flowSouthward) {
                            boolean eseBl = field.getSouthEastElement(field.getEastElement(currentElement)).isBlocked();
                            if (!((!nBl && !neBl) || (!eeBl && !eseBl && !seBl && !sBl && !swBl && !wBl))) {
                                field.getEastElement(currentElement).getFARExtension().setExtraHorizontalAccessTo(currentElement);
                            }
                        }
                    } else if (flowWestward && !westBlocked) {
                        boolean wwBl = field.getWestElement(field.getWestElement(currentElement)).isBlocked();
                        if (flowNorthward) {
                            boolean wnwBl = field.getNorthWestElement(field.getWestElement(currentElement)).isBlocked();
                            if (!((!sBl && !swBl) || (!wwBl && !wnwBl && !nwBl && !nBl && !neBl && !eBl))) {
                                field.getWestElement(currentElement).getFARExtension().setExtraHorizontalAccessTo(currentElement);
                            }
                        } else if (flowSouthward) {
                            boolean wswBl = field.getSouthWestElement(field.getWestElement(currentElement)).isBlocked();
                            if (!((!nBl && !nwBl) || (!wwBl && !wswBl && !swBl && !sBl && !seBl && !eBl))) {
                                field.getWestElement(currentElement).getFARExtension().setExtraHorizontalAccessTo(currentElement);
                            }
                        }
                    }
                }
            }
        }
    }

    private void updateInfo() {
        gui.getInfoStepTF().setText(simStep + "");
        gui.getInfoSuccessTF().setText(numSuccesses + "");
        gui.getInfoTimeTF().setText(((System.currentTimeMillis() - startTime) / 1000) + "");
    }

    @Override public String toString() {
        return ("Pathfinder at step " + simStep + ": Number of AAgents: " + farAgents.length);
    }
}
