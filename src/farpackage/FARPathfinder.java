package farpackage;

import generalpackage.*;
import adtpackage.*;
import guipackage.*;
import java.util.concurrent.ExecutionException;

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
        public Boolean doInBackground() throws InterruptedException, ExecutionException, RuntimeException {
            System.out.println("<BACKGROUND ALLOCATION>");
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
            System.out.println("<END BACKGROUND ALLOCATION>");
            
            // the allocation info dialog will be reset and hidden
            gui.getInfoStepTF().setText("");
            gui.getInfoSuccessTF().setText("");
            gui.getInfoTimeTF().setText("");
            gui.getInfoDialog().setVisible(false);
            
            // error handling
            try {
                get();
            } catch (InterruptedException e) {
                e.getCause().printStackTrace();
            } catch (ExecutionException e) {
                e.getCause().printStackTrace();
            } catch (RuntimeException e) {
                e.getCause().printStackTrace();
            }
        }
    }

    public void findAgentPaths() {
        // make a new allocation worker that will do the allocation in the background
        aw = new AllocationWorker();

        // this listener is added to update the numbers reported by the progress dialog
        aw.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                if ("update".equals(evt.getPropertyName())) {
                    thisPathfinder.updateInfo(); // update simulationStep
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

    private int simulationStep = 0;

    private IntegerList agentGroupOrder;
    
    private MinimalQueue agentReservationOrder;
        
    private int numSuccesses = 0;
    private int numFailures = 0;
    
    private int printLevel = 0;

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
        
        // setting the order of agent groups
        agentGroupOrder = new IntegerList();
        for (int i = 0; i < farAgentGroups.length; i++) {
            agentGroupOrder.insertAtRear(i);
        }

        // coordination stage
        printToScreen("Coordination stage");
        for (simulationStep = 0; simulationStep <= FAILURE_CRITERION; simulationStep++) {
            // simulationStep 0 is the initial reservation step for all agents
            // simulationStep 1 is the first step for which real reservations are needed
            // we end at the FAILURE_CRITERION step, not before

            // initialization
            aw.firePropertyChange("update", simulationStep-1, simulationStep);
            printToScreen("Step: "+simulationStep);
            
            incrementPrintLevel(); // 1
            printToScreen("Agent group order: "+agentGroupOrder);
            
            // randomization of agents
            agentReservationOrder = new MinimalQueue(); // it is necessary to mix up the agent reservation order
            MaximalQueue unassignedAgents = new MaximalQueue(); // queue of agents awaiting assignment to reservation order
            
            if (simulationStep == 0) {
                // if this is step 0, enqueue everyone for initial reservation order assignment
                for (int i = simulationStep; i < farAgentGroups.length; i++) {
                    for (int j = 0; j < farAgentGroups[i].size(); j++) {
                        unassignedAgents.enqueue((FARAgent) farAgentGroups[i].getNodeData(j), ((FARAgent) farAgentGroups[i].getNodeData(j)).FARAGENT_ID);
                    }
                }
            } else if (simulationStep > 0) {
                // if this is not step 0, enqueue only agents from the appropriate agent group
                int agentGroupForStep = agentGroupOrder.getLastNodeValue();
                printToScreen("Agent group for step: "+agentGroupForStep);
                
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

                agentReservationOrder.enqueue(chosenAgent, simulationStep); // enqueue the chosen agent into reservation order
                // this means that all keys of agent reservation order are 1 behind reservationStep!!
            }

            // reserving
            // during this part of the algrithm, some agents look ahead (up to RESERVATION_DEPTH steps) and reserve their required paths
            // this is different from the next step of the algorithm, where all agents actually perform the reservation at the current simulationStep
            while (!agentReservationOrder.isEmpty()) {
                // while there are agents to reserve for
                FARAgent currentAgent = (FARAgent) agentReservationOrder.dequeue();

                Reservation previousReservation = currentAgent.getLastReservation();

                // initializing
                int reservationStep;
                // this is different from the simulationStep
                // reservationStep can be up to RESERVATION_DEPTH (not inclusive) larger than simulationStep
                Element comingFrom;
                
                if (previousReservation == null) {
                    reservationStep = 0;
                    comingFrom = currentAgent.START;
                }
                else {
                    reservationStep = previousReservation.getStep() + 1;
                    comingFrom = previousReservation.getElement();
                    // if this is during normal reservations, reserve for the correct number of steps according to previous reservation
                }
                
                int reservationIndex = getReservationIndex(currentAgent, reservationStep); // how many more steps do we need to plan
                
                // making the actual reservations
                if ((currentAgent.getLastReservation() == null) || (reservationStep <= currentAgent.getAgentGroup() && currentAgent.getLastReservation().isInitialReservation())) {
                    // if this is the very first reservation
                    // or the simulationStep is lower than the agentGroup and previous reservation was initial = MAY LEAD TO PROBLEMS WHEN PROXY APPLIED AT VERY BEGINNING

                    if (currentAgent.getFutureReservationListSize() < (RESERVATION_DEPTH - reservationIndex)) {
                        // reserve only if there is not already a future reservation at reservation step
                        Element elementToBeReserved = comingFrom;
                        reserveInitial(currentAgent, ReservationType.INITIAL, previousReservation, elementToBeReserved);
                        
                        // if there are still reservations to be made, put the agent back in reservation order
                        if (reservationIndex > 0) agentReservationOrder.enqueue(currentAgent, reservationStep);
                    }

                } else if (reservationStep <= FAILURE_CRITERION) {
                    // normal reservation
                    // we have to end when currentStep reaches failure criterion
                    // we do not want to try to reserve at impossible times

                    if (reservationStep < currentAgent.getAgentReservationListSize()) {
                        // if currentStep is smaller than the size of the agent's reservation path
                        // we still have stuff to do

                        if (currentAgent.getFutureReservationListSize() < (RESERVATION_DEPTH - reservationIndex)) {
                            // reserve only if there is not already a future reservation at reservation step
                            Element elementToBeReserved = currentAgent.getPathElement(reservationStep);
                            reserveMovement(currentAgent, ReservationType.NORMAL, previousReservation, elementToBeReserved, null);
                            
                            // if there are still reservations to be made, put the agent back in reservation order
                            if (reservationIndex > 0) agentReservationOrder.enqueue(currentAgent, reservationStep);
                        }

                    } else {
                        // if this step is after the end of agent's path
                        // end by waiting at the finish point

                        if (currentAgent.getFutureReservationListSize() < (RESERVATION_DEPTH - reservationIndex)) {
                            // reserve only if there is not already a future reservation at reservation step
                            Element elementToBeReserved = comingFrom;
                            reserveWait(currentAgent, ReservationType.WAIT, previousReservation, elementToBeReserved, null);
                            
                            // if there are still reservations to be made, put the agent back in reservation order
                            if (reservationIndex > 0) agentReservationOrder.enqueue(currentAgent, reservationStep);
                        }
                    }
                }
            }

            // Moving agents
            printToScreen("Moving agents");
            incrementPrintLevel(); // 2
            for (FARAgent currentAgent : farAgents) {
                //printToScreen("Adjusting path of agent "+currentAgent.FARAGENT_ID+"");
                
                //incrementPrintLevel(); // 3
                //printToScreen("Last honored reservation: "+currentAgent.getLastHonoredReservation());
                
                // perform the reservation that is lined up for this agent                
                Reservation reservationToHonor = currentAgent.honorFutureReservation();
                //printToScreen("Reservation to honor: "+reservationToHonor);
                //decrementPrintLevel(); // 2
                
                // if this is the last element of the agent's path, make it complete
                Element currentElement = currentAgent.getFinalPathElement(simulationStep);
                if (currentElement == currentAgent.GOAL && !currentAgent.isComplete()) {
                    printToScreen("Success for agent: "+currentAgent.FARAGENT_ID);
                    numSuccesses += 1;
                    currentAgent.setIsComplete(true);
                }
            }
            decrementPrintLevel(); // 1
            
            // put the currently first agent group at the end of the agent group order
            int firstAgentGroup = agentGroupOrder.removeFirst();
            agentGroupOrder.insertAtRear(firstAgentGroup);
            
            decrementPrintLevel(); // 0
        }

        // wrap up afer all simulationSteps are exhausted
        printToScreen("After coordination stage");
        incrementPrintLevel(); // 1
        for (FARAgent currentAgent : farAgents) {
            if (!currentAgent.isComplete()) {
                printToScreen("Failure for agent: "+currentAgent.FARAGENT_ID);
                numFailures += 1;
            }
        }
        decrementPrintLevel(); // 0

        printToScreen("Number of failures: "+numFailures);
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

    // which one of the two agents has priority
    // look at path completion first, then at direction
    // negative result = firstAgent has priority
    // zero = the two agents have the same priority
    // positive result = secondAgent has priority
    private int comparePriority(int step, Reservation firstAgentPreviousReservation, Reservation secondAgentObstructingReservation, Element elementToBeReserved) {
        // try to settle this with path completion first
        int comparePathCompletionPriority = comparePathCompletionPriority(firstAgentPreviousReservation.getAgent(), secondAgentObstructingReservation.getAgent());
        if (comparePathCompletionPriority != 0) return comparePathCompletionPriority;
        
        // try to settle this with overriding second
        int compareOverridingPriority = compareOverridingPriority(firstAgentPreviousReservation.getAgent(), secondAgentObstructingReservation);
        if (compareOverridingPriority != 0) return compareOverridingPriority;
        
        // if no other way to settle this, use the rotating direction priority system
        return compareDirectionPriority(step, firstAgentPreviousReservation.getElement(), elementToBeReserved, secondAgentObstructingReservation.getCameFrom(), secondAgentObstructingReservation.getElement());
    }
    
    // which one of the two agents has priority based on initial path completion
    // negative result = firstAgent completed more = has priority
    // zero = the two agents completed the same percentage = have the same priority
    // positive result = secondAgent completed more = has priority
    private int comparePathCompletionPriority(FARAgent firstAgent, FARAgent secondAgent) {
        double firstAgentPathCompletion = firstAgent.getHonoredInitialPathCompletion();
        double secondAgentPathCompletion = secondAgent.getHonoredInitialPathCompletion();
        
        if (firstAgentPathCompletion > secondAgentPathCompletion) return -1;
        else if (firstAgentPathCompletion == secondAgentPathCompletion) return 0;
        else return 1;
    }
    
    // which one of the two agents has priority based on overriding
    // zero = neither agent overrode the other
    // positive result = secondAgent overrode firstAgent = has priority
    private int compareOverridingPriority(FARAgent firstAgent, Reservation secondAgentObstructingReservation) {
        FARAgent secondAgent = secondAgentObstructingReservation.getAgent();
        
        boolean secondAgentOverride = false;
        if (secondAgentObstructingReservation.hasOverridenAgent(firstAgent)) secondAgentOverride = true;
        
        if (secondAgentOverride) return 1;
        else return 0;
    }
    
    // which one of the two agents has priority based on direction of movement
    // negative result = firstAgent has priority
    // zero = the two agents have the same priority
    // positive result = secondAgent has priority
    private int compareDirectionPriority(int step, Element firstAgentComingFrom, Element firstAgentDestination, Element secondAgentComingFrom, Element secondAgentDestination) {
        int firstAgentDirectionPriority = getPriority(step, firstAgentComingFrom, firstAgentDestination);
        int secondAgentDirectionPriority = getPriority(step, secondAgentComingFrom, secondAgentDestination);
        
        if (firstAgentDirectionPriority < secondAgentDirectionPriority) return -1;
        else if (firstAgentDirectionPriority == secondAgentDirectionPriority) return 0;
        else return 1;
    }
    
    // this roates the priority directions according to step
    // lower number is better
    private int getPriority(int step, Element firstElement, Element secondElement) {
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
            System.out.println("ERROR: TRIED RESERVING FROM "+firstElement+" TO "+secondElement);
            throw new RuntimeException("Tried reserving from "+firstElement+" to "+secondElement);
        }
    }
    
    private Reservation reserveMovement(FARAgent agent, ReservationType reservationType, Reservation previousReservation, Element elementToBeReserved, Element proxyTarget) {
        int currentStep;
        Element comingFrom;
        if (previousReservation == null) {
            System.out.println("ERROR: AGENT "+agent.FARAGENT_ID+" TRIED TO RESERVE MOVEMENT WITH A NULL PREVIOUS RESERVATION");
            throw new RuntimeException("Agent "+agent.FARAGENT_ID+" tried to reserve movement with a null previous reservation");
        }
        else {
            currentStep = previousReservation.getStep() + 1;
            comingFrom = previousReservation.getElement();
        }
        
        printToScreen("Current agent: "+agent.FARAGENT_ID+" at element "+comingFrom);
        
        incrementPrintLevel(); // 3
        if (reservationType == ReservationType.NORMAL) printToScreen("Reserving a normal reservation at element: "+elementToBeReserved+" (step "+currentStep+")");
        else printToScreen("Reserving a proxy reservation at element: "+elementToBeReserved+" (step "+currentStep+")");

        Reservation obstructingReservation = elementToBeReserved.getFARExtension().getReservation(currentStep);
        Reservation obstructingGhostReservation = elementToBeReserved.getFARExtension().getGhostReservation(currentStep);

        Reservation obstructingGhostOriginalReservation;
        if (obstructingGhostReservation != null) obstructingGhostOriginalReservation = obstructingGhostReservation.getOriginalReservation();
        else obstructingGhostOriginalReservation = null;

        Reservation newReservation;
        
        incrementPrintLevel(); // 4
        if (obstructingGhostOriginalReservation != null && obstructingGhostOriginalReservation == comingFrom.getFARExtension().getReservation(currentStep)) {
            // head-on collision!
            
            printToScreen("Obstructing ghost reservation: "+obstructingGhostReservation);
            //printToScreen("Previous reservation: "+previousReservation);

            /*int ghostReservationPriority = getPriority(currentStep, elementToBeReserved, comingFrom);
            int intendedReservationPriority = getPriority(currentStep, comingFrom, elementToBeReserved);
            
            // if this agent was already overriden by the obstructing reservation
            // make intended reservation weaker than the obstructing reservation can possibly be
            if (obstructingGhostOriginalReservation.hasOverridenAgent(agent)) intendedReservationPriority = 5;*/
            
            int comparePriority = comparePriority(currentStep, previousReservation, obstructingGhostOriginalReservation, elementToBeReserved);
            
            //if (ghostReservationPriority == intendedReservationPriority) {
            if (comparePriority == 0) {
                FARAgent obstructingAgent = obstructingReservation.getAgent();
                System.out.println("ERROR: AGENTS "+obstructingAgent.FARAGENT_ID+" (GHOST) AND "+agent.FARAGENT_ID+" TRIED TO PERFORM THE SAME RESERVATION OF ELEMENT: "+elementToBeReserved+" (step "+currentStep+")");
                throw new RuntimeException("Agents "+obstructingAgent.FARAGENT_ID+" (ghost) and "+agent.FARAGENT_ID+" tried to perform the same reservation of element: "+elementToBeReserved+" (step "+currentStep+")");
            }

            else if (comparePriority > 0) {
                // the ghost reservation overrules intended reservation
                // current agent needs to move out of the way of the ghost's original agent

                if (getProxyStarts(comingFrom, currentStep, null).size() > 0) {
                    // if current agent has a place to go
                    // keep the obstructing agent at its reserved position and move away
                    printToScreen("Head-on collision; reserving a proxy because of ghost reservation: "+obstructingGhostReservation);
                    
                    obstructingGhostOriginalReservation.addOverridenAgent(agent);
                    
                    // add a proxy to current agent's path
                    newReservation = reserveProxy(agent, previousReservation, proxyTarget);
                }

                else {
                    // there is no proxy available to current agent
                    Reservation preObstructingGhostReservation = obstructingGhostReservation.getPreviousReservation();
                    
                    if (getProxyStarts(preObstructingGhostReservation.getElement(), currentStep, null).size() > 0) {
                        // if the ghost's original agent's previous reservation can be changed so that a proxy is chosen

                        // evict obstructing reservation
                        printToScreen("Head-on collision (no proxy available); evicting ghost original reservation: "+obstructingGhostOriginalReservation);
                        
                        Reservation firstRemovedReservation = evictObstructingReservation(obstructingGhostOriginalReservation);

                        // and set intended reservation to a be the reservation at current step
                        //newReservation = reserveElementForAgent(agent, reservationType, previousReservation, elementToBeReserved, proxyTarget);
                        newReservation = reserveMovement(agent, reservationType, previousReservation, elementToBeReserved, proxyTarget);
                        newReservation.addOverridenAgent(obstructingGhostOriginalReservation.getAgent());
                        
                        // repair obstructing proxy reservation, if needed
                        if (firstRemovedReservation != null) {
                            if (firstRemovedReservation.isProxyReservation()) {
                                repairProxy(firstRemovedReservation.getAgent(), firstRemovedReservation.getPreviousReservation(), firstRemovedReservation.getProxyTarget());
                            }
                        }
                    }

                    else {
                        // there is no proxy available to either one of the agents
                        // evict obstructing reservation and make them both wait
                        printToScreen("Head-on collision (no proxy available); evicting ghost original reservation: "+obstructingGhostOriginalReservation);

                        Reservation firstRemovedReservation = evictObstructingReservation(obstructingGhostOriginalReservation);

                        // and change intended reservation to a wait reservation
                        printToScreen("Head-on collision (no proxy available); waiting because of ghost reservation: "+obstructingGhostReservation);
                        newReservation = reserveWait(agent, ReservationType.WAIT, previousReservation, comingFrom, proxyTarget);
                        newReservation.addOverridenAgent(obstructingGhostOriginalReservation.getAgent());
                        
                        // repair obstructing proxy reservation, if needed
                        if (firstRemovedReservation != null) {
                            if (firstRemovedReservation.isProxyReservation()) {
                                repairProxy(firstRemovedReservation.getAgent(), firstRemovedReservation.getPreviousReservation(), firstRemovedReservation.getProxyTarget());
                            }
                        }
                    }
                }
            }

            else {
                // intended reservation overwrites the ghost reservation
                // ghost's original agent needs to move out of the way of the current agent
                
                Reservation preObstructingGhostReservation = obstructingGhostReservation.getPreviousReservation();
                
                if (getProxyStarts(preObstructingGhostReservation.getElement(), currentStep, null).size() > 0) {
                    // rewrite obstructing reservation since there is a proxy path available
                    printToScreen("Head-on collision; evicting ghost original reservation: "+obstructingGhostOriginalReservation);

                    Reservation firstRemovedReservation = evictObstructingReservation(obstructingGhostOriginalReservation);

                    // set reservation to be intended reservation
                    //newReservation = reserveElementForAgent(agent, reservationType, previousReservation, elementToBeReserved, proxyTarget);
                    newReservation = reserveMovement(agent, reservationType, previousReservation, elementToBeReserved, proxyTarget);
                    newReservation.addOverridenAgent(obstructingGhostOriginalReservation.getAgent());
                    
                    // repair obstructing proxy reservation, if needed
                    if (firstRemovedReservation != null) {
                        if (firstRemovedReservation.isProxyReservation()) {
                            repairProxy(firstRemovedReservation.getAgent(), firstRemovedReservation.getPreviousReservation(), firstRemovedReservation.getProxyTarget());
                        }
                    }
                }

                else {
                    // there is no proxy that the obstructing agent could take
                    // keep obstructing reservation
                    // can current agent move to a proxy?
                    
                    if (getProxyStarts(comingFrom, currentStep, null).size() > 0) {
                        // if current agent has a place to go
                        // keep the obstructing agent at its reserved position and move away
                        printToScreen("Head-on collision; reserving a proxy because of ghost reservation: "+obstructingGhostReservation);

                        obstructingGhostOriginalReservation.addOverridenAgent(agent);
                        
                        // add a proxy to current agent's path
                        newReservation = reserveProxy(agent, previousReservation, proxyTarget);
                    }
                    
                    else {
                        // neither agent can move to a proxy
                        printToScreen("Head-on collision (no proxy available); evicting ghost original reservation: "+obstructingGhostOriginalReservation);

                        Reservation firstRemovedReservation = evictObstructingReservation(obstructingGhostOriginalReservation);
                        
                        // and change intended reservation to a wait reservation
                        printToScreen("Head-on collision (no proxy available); waiting because of ghost reservation: "+obstructingGhostReservation);
                        newReservation = reserveWait(agent, ReservationType.WAIT, previousReservation, comingFrom, proxyTarget);
                        newReservation.addOverridenAgent(obstructingGhostOriginalReservation.getAgent());
                        
                        // repair obstructing proxy reservation, if needed
                        if (firstRemovedReservation != null) {
                            if (firstRemovedReservation.isProxyReservation()) {
                                repairProxy(firstRemovedReservation.getAgent(), firstRemovedReservation.getPreviousReservation(), firstRemovedReservation.getProxyTarget());
                            }
                        }
                    }
                }
            } 
        }

        else if (obstructingReservation == null) {
            // if there is no reservation at the currentStep
            // reserve this element for the current agent
            newReservation = reserveElementForAgent(agent, reservationType, previousReservation, elementToBeReserved, proxyTarget);
        }

        else {
            // side-on collision
            
            printToScreen("Obstructing reservation: "+obstructingReservation);
            //printToScreen("Previous reservation: "+previousReservation);

            /*int obstructingReservationPriority = getPriority(currentStep, obstructingReservation.getCameFrom(), elementToBeReserved);
            int intendedReservationPriority = getPriority(currentStep, comingFrom, elementToBeReserved);
            
            // if this agent was already overriden by the obstructing reservation
            // make intended reservation weaker than the obstructing reservation can possibly be
            if (obstructingReservation.hasOverridenAgent(agent)) intendedReservationPriority = 5;*/
            
            int comparePriority = comparePriority(currentStep, previousReservation, obstructingReservation, elementToBeReserved);
            
            if (comparePriority == 0) {
                // should not happen
                FARAgent obstructingAgent = obstructingReservation.getAgent();
                System.out.println("ERROR: AGENTS "+obstructingAgent.FARAGENT_ID+" AND "+agent.FARAGENT_ID+" TRIED TO PERFORM THE SAME RESERVATION OF ELEMENT: "+elementToBeReserved+" (step "+currentStep+")");
                throw new RuntimeException("Agents "+obstructingAgent.FARAGENT_ID+" and "+agent.FARAGENT_ID+" tried to perform the same reservation of element: "+elementToBeReserved+" (step "+currentStep+")");
            }

            else if (comparePriority > 0) {
                // obstructing reservation overrules intended reservation
                // keep the obstructing reservation

                obstructingReservation.addOverridenAgent(agent);
                
                // intended reservation needs to be replanned as wait reservation
                printToScreen("Side-on collision; waiting because of reservation: "+obstructingReservation);
                newReservation = reserveWait(agent, ReservationType.WAIT, previousReservation, comingFrom, proxyTarget);
            }
            
            else {
                // new reservation overwrites existing reservation
                
                if (obstructingReservation.isNormalReservation() || obstructingReservation.isProxyReservation()) {
                    // if obstructing reservation is movement reservation
                    printToScreen("Side-on collision; evicting reservation: "+obstructingReservation);

                    // make the obstructing agent move
                    Reservation firstRemovedReservation = evictObstructingReservation(obstructingReservation);

                    // rewrite reservation for the intended reservation
                    newReservation = reserveElementForAgent(agent, reservationType, previousReservation, elementToBeReserved, proxyTarget);
                    newReservation.addOverridenAgent(obstructingReservation.getAgent());
                    
                    // repair obstructing proxy reservation, if needed
                    if (firstRemovedReservation != null) {
                        if (firstRemovedReservation.isProxyReservation()) {
                            repairProxy(firstRemovedReservation.getAgent(), firstRemovedReservation.getPreviousReservation(), firstRemovedReservation.getProxyTarget());
                        }
                    }
                }

                else {
                    // if obstructing reservation is a wait reservation or an initial reservation
                    
                    if (getProxyStarts(elementToBeReserved, currentStep, null).size() > 0) {
                        // there is a proxy path available for the obstructing reservation
                        // reserve proxy for the obstructing agent
                        printToScreen("Wait conflict; evicting reservation: "+obstructingReservation);

                        // make the obstructing agent move
                        Reservation firstRemovedReservation = evictObstructingReservation(obstructingReservation);

                        // rewrite reservation for the intended reservation
                        newReservation = reserveElementForAgent(agent, reservationType, previousReservation, elementToBeReserved, proxyTarget);
                        newReservation.addOverridenAgent(obstructingReservation.getAgent());
                        
                        // repair obstructing proxy reservation, if needed
                        if (firstRemovedReservation != null) {
                            if (firstRemovedReservation.isProxyReservation()) {
                                repairProxy(firstRemovedReservation.getAgent(), firstRemovedReservation.getPreviousReservation(), firstRemovedReservation.getProxyTarget());
                            }
                        }
                    }

                    else {
                        // there is no proxy the waiting element could take
                        // keep the obstructing reservation
                        
                        obstructingReservation.addOverridenAgent(agent);
                        
                        // intended reservation needs to be replanned as wait reservation
                        printToScreen("Wait conflict (no proxy available); waiting because of reservation: "+obstructingReservation);
                        newReservation = reserveWait(agent, ReservationType.WAIT, previousReservation, comingFrom, proxyTarget);
                    }
                }
            }
        }
        decrementPrintLevel(); // 3
        
        printToScreen("New reservation: "+newReservation);
        decrementPrintLevel(); // 2
        
        return newReservation;
    }
    
    private Reservation reserveWait(FARAgent agent, ReservationType reservationType, Reservation previousReservation, Element elementToBeReserved, Element proxyTarget) {
        int currentStep;
        Element comingFrom;
        if (previousReservation == null) {
            System.out.println("ERROR: AGENT "+agent.FARAGENT_ID+" TRIED TO RESERVE WAIT WITH A NULL PREVIOUS RESERVATION");
            throw new RuntimeException("Agent "+agent.FARAGENT_ID+" tried to reserve wait with a null previous reservation");
        }
        else {
            currentStep = previousReservation.getStep() + 1;
            comingFrom = previousReservation.getElement();
        }
        
        printToScreen("Current agent: "+agent.FARAGENT_ID+" at element "+comingFrom);
        
        incrementPrintLevel(); // 3
        printToScreen("Reserving a wait reservation at element: "+elementToBeReserved+" (step "+currentStep+")");

        Reservation obstructingReservation = elementToBeReserved.getFARExtension().getReservation(currentStep);

        Reservation newReservation;
        
        incrementPrintLevel(); // 4
        if (obstructingReservation == null) {
            // there is no obstructing reservation at element at current step
            // reserve this element for the current agent
            newReservation = reserveElementForAgent(agent, reservationType, previousReservation, elementToBeReserved, proxyTarget);
            
            // if this wait has been inserted into a proxy, repair the proxy
            if (proxyTarget != null && newReservation.getStep() < FAILURE_CRITERION) {
                newReservation = repairProxy(agent, newReservation, proxyTarget);
            }
        }

        else {
            // there is a reservation at the currentStep
            
            printToScreen("Obstructing reservation: "+obstructingReservation);
            //printToScreen("Previous reservation: "+previousReservation);
            
            /*int obstructingReservationPriority = getPriority(currentStep, obstructingReservation.getCameFrom(), elementToBeReserved);
            int intendedReservationPriority = getPriority(currentStep, comingFrom, elementToBeReserved);

            // if this agent was already overriden by the obstructing reservation
            // make intended reservation weaker than the obstructing reservation can possibly be
            if (obstructingReservation.hasOverridenAgent(agent)) intendedReservationPriority = 5;*/
            
            int comparePriority = comparePriority(currentStep, previousReservation, obstructingReservation, elementToBeReserved);
            
            if (comparePriority == 0) {
                FARAgent obstructingAgent = obstructingReservation.getAgent();
                System.out.println("ERROR: AGENTS "+obstructingAgent.FARAGENT_ID+" (GHOST) AND "+agent.FARAGENT_ID+" TRIED TO PERFORM THE SAME RESERVATION OF ELEMENT: "+elementToBeReserved+" (step "+currentStep+")");
                throw new RuntimeException("Agents "+obstructingAgent.FARAGENT_ID+" (ghost) and "+agent.FARAGENT_ID+" tried to perform the same reservation of element: "+elementToBeReserved+" (step "+currentStep+")");
            }

            else if (comparePriority > 0) {
                // existing reservation overrules new reservation - ALWAYS
                // intended reservation needs to be replanned

                if (getProxyStarts(elementToBeReserved, currentStep, null).size() > 0) {
                    // a proxy is available instead of intended wait reservation
                    // keep obstructing reservation
                    printToScreen("Wait conflict; reserving a proxy because of reservation: "+obstructingReservation);
                    
                    obstructingReservation.addOverridenAgent(agent);
                    
                    // reserve current agent for a proxy
                    // proxyTarget may be null
                    // (that means that this started off as a normal wait reservation)
                    // if proxyTarget is null, it will be automatically assigned by reserveProxy
                    newReservation = reserveProxy(agent, previousReservation, proxyTarget);
                }

                else {
                    // there is no proxy this agent could take
                    // evict obstructing reservation
                    printToScreen("Wait conflict (no proxy available); evicting reservation: "+obstructingReservation);

                    Reservation firstRemovedReservation = evictObstructingReservation(obstructingReservation);

                    // proceed with the intended plan and reserve a wait at the element
                    newReservation = reserveElementForAgent(agent, reservationType, previousReservation, elementToBeReserved, proxyTarget);
                    newReservation.addOverridenAgent(obstructingReservation.getAgent());
                    
                    // if this wait has been inserted into a proxy, repair the proxy
                    if (proxyTarget != null && newReservation.getStep() < FAILURE_CRITERION) {
                        newReservation = repairProxy(agent, newReservation, proxyTarget);
                    }
                    
                    // repair obstructing proxy reservation, if needed
                    if (firstRemovedReservation != null) {
                        if (firstRemovedReservation.isProxyReservation()) {
                            repairProxy(firstRemovedReservation.getAgent(), firstRemovedReservation.getPreviousReservation(), firstRemovedReservation.getProxyTarget());
                        }
                    }
                }
            }

            else {
                // new reservation overwrites existing reservation - NEVER
                System.out.println("ERROR: A WAIT RESERVATION TRIED TO OVERWRITE EXISTING NON-WAIT RESERVATION AT ELEMENT: "+elementToBeReserved+" (step "+currentStep+")");
                throw new RuntimeException("A wait reservation tried to overwrite existing non-wait reservation at element: "+elementToBeReserved+" (step "+currentStep+")");
            }
        }
        decrementPrintLevel(); // 3
        
        printToScreen("New reservation: "+newReservation);
        decrementPrintLevel(); // 2
        
        return newReservation;
    }
    
    private Reservation reserveInitial(FARAgent agent, ReservationType reservationType, Reservation previousReservation, Element elementToBeReserved) {
        // proxyTarget not provided becaue there is no situation in which it would happen
        
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
        
        printToScreen("Current agent: "+agent.FARAGENT_ID+" at element "+comingFrom);
        
        incrementPrintLevel(); // 3
        printToScreen("Reserving an initial reservation at element: "+elementToBeReserved+" (step "+currentStep+")");

        Reservation obstructingReservation = elementToBeReserved.getFARExtension().getReservation(currentStep);

        Reservation newReservation;
        
        incrementPrintLevel(); // 4
        if (obstructingReservation == null) {
            // if there is no reservation at the currentStep
            // reserve this element for the current agent
            
            newReservation = reserveElementForAgent(agent, reservationType, previousReservation, elementToBeReserved, null);
        }

        else {
            // there is a reservation at the currentStep
            
            printToScreen("Obstructing reservation: "+obstructingReservation);
            //printToScreen("Previous reservation: "+previousReservation);
            
            /*int obstructingReservationPriority = getPriority(currentStep, obstructingReservation.getCameFrom(), elementToBeReserved);
            int intendedReservationPriority = getPriority(currentStep, comingFrom, elementToBeReserved);

            // if this agent was already overriden by the obstructing reservation
            // make intended reservation weaker than the obstructing reservation can possibly be
            if (obstructingReservation.hasOverridenAgent(agent)) intendedReservationPriority = 5;*/
            
            int comparePriority = comparePriority(currentStep, previousReservation, obstructingReservation, elementToBeReserved);
            
            if (comparePriority == 0) {
                FARAgent obstructingAgent = obstructingReservation.getAgent();
                System.out.println("ERROR: AGENTS "+obstructingAgent.FARAGENT_ID+" AND "+agent.FARAGENT_ID+" TRIED TO PERFORM THE SAME RESERVATION OF ELEMENT: "+elementToBeReserved+" (step "+currentStep+")");
                throw new RuntimeException("Agents "+obstructingAgent.FARAGENT_ID+" and "+agent.FARAGENT_ID+" tried to perform the same reservation of element: "+elementToBeReserved+" (step "+currentStep+")");
            }

            else if (comparePriority > 0) {
                // obstructing reservation overrules intended reservation - ALWAYS
                // intended reservation needs to be replanned as a proxy
                
                if (getProxyStarts(comingFrom, currentStep, null).size() > 0) {
                    // if there is a proxy path this agent could take
                    // keep obstructing reservation
                    printToScreen("Wait conflict; reserving a proxy because of reservation: "+obstructingReservation);
                    
                    obstructingReservation.addOverridenAgent(agent);
                    
                    // rewrite intended reservation since there is a proxy path available for it
                    newReservation = reserveProxy(agent, previousReservation, null);
                }

                else {
                    // there is no proxy this agent could take
                    // evict obstructing reservation
                    printToScreen("Wait conflict (no proxy available); evicting reservation: "+obstructingReservation);

                    Reservation firstRemovedReservation = evictObstructingReservation(obstructingReservation);

                    // reserve for current agent according to intended plan
                    newReservation = reserveElementForAgent(agent, reservationType, previousReservation, elementToBeReserved, null);
                    newReservation.addOverridenAgent(obstructingReservation.getAgent());
                    
                    // repair obstructing proxy reservation, if needed
                    if (firstRemovedReservation != null) {
                        if (firstRemovedReservation.isProxyReservation()) {
                            repairProxy(firstRemovedReservation.getAgent(), firstRemovedReservation.getPreviousReservation(), firstRemovedReservation.getProxyTarget());
                        }
                    }
                }
            }

            else { // new reservation overwrites existing reservation - NEVER
                System.out.println("ERROR: AN INITIAL RESERVATION TRIED TO OVERWRITE EXISTING NON-WAIT RESERVATION AT ELEMENT: "+elementToBeReserved+" (step "+currentStep+")");
                throw new RuntimeException("An initial reservation tried to overwrite existing non-wait reservation at elemnt: "+elementToBeReserved+" (step "+currentStep+")");
            }
        }
        decrementPrintLevel(); // 3
        
        printToScreen("New reservation: "+newReservation);
        decrementPrintLevel(); // 2
        
        return newReservation;
    }
    
    private Reservation reserveProxy(FARAgent agent, Reservation previousReservation, Element proxyTarget) {
        //reservationElement.getFARExtension().setIsProxy(true);

        int currentStep;
        Element comingFrom;
        
        if (previousReservation == null) {
            // should not happen
            throw new RuntimeException();
        }
        else {
            currentStep = previousReservation.getStep() + 1;
            comingFrom = previousReservation.getElement();
        }
                
        // creating proxy path
        Element proxyStart = getRandomProxyStart(getProxyStarts(comingFrom, currentStep, null));
        if (proxyTarget == null) proxyTarget = comingFrom;
        
        if (proxyStart == null) {
            System.out.println("ERROR: AGENT "+agent.FARAGENT_ID+" TRIED TO RESERVE A PROXY PATH WITHOUT POSSIBLE START");
            throw new RuntimeException("Agent "+agent.FARAGENT_ID+" tried to reserve a proxy path without any possible start");
        }
        
        incrementPrintLevel(); // 5
        printToScreen("Finding proxy path for agent "+agent.FARAGENT_ID+" with start at "+proxyStart+" and end at "+proxyTarget);
        List proxyPath = getProxyPath(proxyStart, proxyTarget);
        // proxy path should be inserted after the last okay element
        
        // making corresponding reservation
        Reservation newReservation = reserveElementForAgent(agent, ReservationType.PROXY, previousReservation, proxyStart, proxyTarget);
        
        incrementPrintLevel(); // 5
        printToScreen("New reservation: "+newReservation);
        decrementPrintLevel(); // 4
        
        Reservation previousIterationReservation;
        Reservation currentIterationReservation = newReservation;
        int previousFutureReservationsListSize;
        int currentFutureReservationListSize = agent.getFutureReservationListSize();
        Element elementToBeReserved;

        for (int i = 1; i < (proxyPath.size()); i++) {
            if (currentIterationReservation.getStep() >= FAILURE_CRITERION) break; // to prevent reservations at impossible times

            // get the appropriate element from proxy path
            elementToBeReserved = (Element) proxyPath.getNodeData(i);

            // save current variables as previous
            previousIterationReservation = currentIterationReservation;
            previousFutureReservationsListSize = currentFutureReservationListSize;

            // reserve new reservation and set it to be current reservation
            // update future reservation list size
            newReservation = currentIterationReservation = reserveMovement(agent, ReservationType.PROXY, previousIterationReservation, elementToBeReserved, proxyTarget);
            currentFutureReservationListSize = agent.getFutureReservationListSize();

            if (currentFutureReservationListSize > (previousFutureReservationsListSize + 1)) {
                // if current future reservation list size increased by more than one
                // that was not a simple reservation, but some repairs were involved
                // do not continue with this
                break;
            }
        }

        decrementPrintLevel(); // 4
        
        return newReservation;
    }
    
    private Reservation repairProxy(FARAgent agent, Reservation previousReservation, Element proxyTarget) {
        if (proxyTarget == null) {
            throw new RuntimeException();
        }
                
        int currentStep;
        Element comingFrom;
        
        if (previousReservation == null) {
            // should not happen
            throw new RuntimeException();
        }
        else {
            currentStep = previousReservation.getStep() + 1;
            comingFrom = previousReservation.getElement();
        }
        
        // creating proxy path
        Element proxyStart = getRandomProxyStart(getProxyStarts(comingFrom, currentStep, null));
        
        incrementPrintLevel(); // 5
        printToScreen("Repairing proxy path for agent "+agent.FARAGENT_ID+" with start at "+proxyStart+" and end at "+proxyTarget);
        
        Reservation newReservation = null;
        if (proxyStart == null) {
            // reserve wait at previous reservation
            // this calls a repair proxy by itself
            newReservation = reserveWait(agent, ReservationType.WAIT, previousReservation, comingFrom, proxyTarget);            
        }
        else {
            List proxyPath = getProxyPath(proxyStart, proxyTarget);

            Reservation previousIterationReservation;
            Reservation currentIterationReservation = previousReservation;
            int previousFutureReservationsListSize;
            int currentFutureReservationListSize = agent.getFutureReservationListSize();
            Element elementToBeReserved;
            
            for (int i = 0; i < (proxyPath.size()); i++) {
                if (currentIterationReservation.getStep() >= FAILURE_CRITERION) break; // to prevent reservations at impossible times
                
                // get the appropriate element from proxy path
                elementToBeReserved = (Element) proxyPath.getNodeData(i);

                // save current variables as previous
                previousIterationReservation = currentIterationReservation;
                previousFutureReservationsListSize = currentFutureReservationListSize;

                // reserve new reservation and set it to be current reservation
                // update future reservation list size
                newReservation = currentIterationReservation = reserveMovement(agent, ReservationType.PROXY, previousIterationReservation, elementToBeReserved, proxyTarget);
                currentFutureReservationListSize = agent.getFutureReservationListSize();
                
                if (currentFutureReservationListSize > (previousFutureReservationsListSize + 1)) {
                    // if current future reservation list size increased by more than one
                    // that was not a simple reservation, but some repairs were involved
                    // do not continue with this
                    break;
                }
            }
        }
        
        decrementPrintLevel(); // 4
        
        return newReservation;
    }
    
    private Reservation reserveElementForAgent(FARAgent agent, ReservationType reservationType, Reservation previousReservation, Element elementToBeReserved, Element proxyTarget) {
        //if (reservationType == ReservationType.WAIT) reservationElement.getFARExtension().setIsWait(true);

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
        
        // making corresponding reservation
        Reservation newReservation = new Reservation(reservationType, elementToBeReserved, agent, currentStep, previousReservation, proxyTarget);

        Reservation newGhostReservation = null;
        if (previousReservation != null) {
            if (elementToBeReserved != comingFrom) {
                // like a wait reservation
                newGhostReservation = new Reservation(ReservationType.GHOST, comingFrom, agent, currentStep, previousReservation, null);
                newGhostReservation.setOriginalReservation(newReservation);
            }
        }
        newReservation.setGhostReservation(newGhostReservation);

        // adding reservations to elements
        elementToBeReserved.getFARExtension().setReservation(currentStep, newReservation);
        comingFrom.getFARExtension().setGhostReservation(currentStep, newGhostReservation);

        // adding reservation to agent
        if (previousReservation != null) previousReservation.setDependentReservation(newReservation);
        agent.enqueueFutureReservation(newReservation);
        
        return newReservation;
    }
    
    private Reservation evictObstructingReservation(Reservation obstructingReservation) {
        FARAgent obstructingAgent = obstructingReservation.getAgent();

        Reservation preObstructingReservation = obstructingReservation.getPreviousReservation();
        
        // determine what to remove
        Reservation reservationToRemove = obstructingReservation;
        Reservation ghostReservationToRemove = obstructingReservation.getGhostReservation(); // may be null

        int removeStep = reservationToRemove.getStep();
        
        incrementPrintLevel(); // 4
        
        // going down the dependecy chain of reservations
        int counter = 0;
        while (reservationToRemove != null) {            
            reservationToRemove.getElement().getFARExtension().setReservation(removeStep + counter, null);
            if (ghostReservationToRemove != null) {
                //printToScreen("Removing ghost reservation (element): "+ghostReservationToRemove);
                ghostReservationToRemove.getElement().getFARExtension().setGhostReservation(removeStep + counter, null);
                //printToScreen("Element check: "+ghostReservationToRemove.getElement().getFARExtension().getGhostReservation(removeStep + counter));
            }
                        
            reservationToRemove = reservationToRemove.getDependentReservation();
            counter += 1;            
        }
        
        // consolidate the agent
        preObstructingReservation.setDependentReservation(null);
        Reservation firstRemovedReservation = obstructingAgent.removeFutureReservations(removeStep);
        
        decrementPrintLevel(); // 3
        
        // add the agent back to reservation order queue
        int enqueueStep = removeStep - 1;
        agentReservationOrder.enqueue(obstructingAgent, enqueueStep);
        
        // if removing a proxy path, the path needs to be repaired
        // but only after the reservation that started this is fulfilled!
        return firstRemovedReservation;
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

    private int getReservationIndex(FARAgent agent, int step) {
        // could be made more efficient by caching
        if (step < simulationStep) throw new RuntimeException();
        
        IntegerList tempAgentGroupOrder = new IntegerList();
        for (int i = 0; i < agentGroupOrder.size(); i++) {
            int currentAgentGroup = agentGroupOrder.getNodeValue(i);
            tempAgentGroupOrder.insertAtRear(currentAgentGroup);
        }
        
        int stepsBetween = step - simulationStep;
        for (int i = 0; i < stepsBetween; i++) {
            int firstAgentGroup = tempAgentGroupOrder.removeFirst();
            tempAgentGroupOrder.insertAtRear(firstAgentGroup);
        }
        
        int reservationIndex = tempAgentGroupOrder.search(agent.getAgentGroup());
        return reservationIndex;
    }

    private List getProxyStarts(Element element, int step, Element excludeElement) {        
        List proxyStarts = new List();
        List accessibleElements = element.getFARExtension().getAccessibleElements();

        //incrementPrintLevel(); // 5
        //printToScreen("Checking proxy availability at step "+step);
        
        //incrementPrintLevel(); // 6
        for (int i = 0; i < accessibleElements.size(); i++) {
            Element currentAccessible = (Element) accessibleElements.getNodeData(i);
            // for each accessible element
            
            if (currentAccessible == excludeElement) {
                // if current accessible is the excluded element, ignore it
                continue;
            }
            
            if (currentAccessible.getFARExtension().getReservation(step) == null) {
                // if there is no reservation at the element yet
                
                if (currentAccessible.getFARExtension().getGhostReservation(step) == null) {
                    // if there is no ghost reservation either at the element
                    //printToScreen("Completely available proxy start: "+currentAccessible);
                    proxyStarts.insertAtFront(currentAccessible);
                    // the accessible element can be used as proxy start!
                }
                
                else if (currentAccessible.getFARExtension().getGhostReservation(step).getOriginalReservation().getElement() != element) {
                    // if there is a ghost reservation at the element
                    //printToScreen("Ghost-occupied proxy start: "+currentAccessible);
                    proxyStarts.insertAtFront(currentAccessible);
                    // the accessible element can be used as proxy start
                    // as long as the original element of the ghost is not this element!
                    // prevents head-on collisions
                }
            }
        }
        //decrementPrintLevel(); // 5
        
        //decrementPrintLevel(); // 4

        return proxyStarts;
    }
    
    private Element getRandomProxyStart(List proxyStarts) {
        int randomProxyStartIndex = (int) (Math.random() * (proxyStarts.size()));
        return (Element) proxyStarts.getNodeData(randomProxyStartIndex);
    }

    private List getProxyPath(Element proxyStart, Element proxyTarget) {
        // returns a new path, starting from first available proxy spot, and ending at this element
        // note that step should be the step BEFORE the step the proxy is needed to start on
        FAR farAlgorithm = new FAR();
        List proxyPath = farAlgorithm.far(proxyStart, proxyTarget, field);
        return proxyPath;
    }
    
    public void printToScreen(String printString) {
        String whitespaceString = "";
        for (int i = 0; i < printLevel; i++) {
            //whitespaceString += "\t";
            whitespaceString += "    ";
        }
        System.out.println(whitespaceString + printString);
    }
    
    private void incrementPrintLevel() {
        printLevel += 1;
    }
    
    private void decrementPrintLevel() {
        printLevel -= 1;
    }
    
    private void updateInfo() {
        gui.getInfoStepTF().setText(simulationStep + "");
        gui.getInfoSuccessTF().setText(numSuccesses + "");
        gui.getInfoTimeTF().setText(((System.currentTimeMillis() - startTime) / 1000) + "");
    }

    @Override public String toString() {
        return ("Pathfinder at step " + simulationStep + ": Number of FARAgents: " + farAgents.length);
    }
}
