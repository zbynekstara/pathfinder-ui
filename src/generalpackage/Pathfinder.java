package generalpackage;

import adtpackage.*;

/**
 *
 * @author Zbyněk Stara
 */
public interface Pathfinder {
    public int FAILURE_CRITERION = 100;

    public abstract void findAgentPaths();

    public abstract Element [] getAgentsAtStep(int drawStep);
    public abstract List [] getAgentPathsUntilStep(int drawStep);
    //public abstract Element [] getAgentStartElements(); DONE BY FIELD
    //public abstract Element [] getAgentGoalElements(); DONE BY FIELD
    //public abstract Agent [] getAgentSet(); DONE BY FIELD

    public abstract int getNumFailures();
}
