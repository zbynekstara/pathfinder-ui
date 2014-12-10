package guipackage;

import adtpackage.*;
import generalpackage.*;
import apackage.*;
import farpackage.*;

/**
 *
 * @author Zbyněk Stara
 */
public abstract class GUI extends javax.swing.JFrame {
    protected int numElements; // elements proper, without boundaries

    protected Field field;

    protected APathfinder aPathfinder;
    protected FARPathfinder farPathfinder;

    protected boolean isFieldSet = false;
    protected boolean areAgentsSet = false;
    protected boolean arePathsSet = false;

    protected int stepCounter;

    protected int algorithmToUse = 0; // 0 = A*, 1 = D*, 2 = CA*, 3 = WHCA*, 4 = FAR

    public abstract javax.swing.JDialog getInfoDialog();

    public abstract javax.swing.JTextField getInfoStepTF();

    public abstract javax.swing.JTextField getInfoSuccessTF();

    public abstract javax.swing.JTextField getInfoTimeTF();

    protected void initializePathfinder(GUI gui) {
        switch (algorithmToUse) {
            case 0:
                aPathfinder = new APathfinder(gui, field);
                break;
            case 1:
                // return dPathfinder.getDAgentSet();
                break;
            case 2:
                // return caPathfinder.getCAAgentSet();
                break;
            case 3:
                // return whcaPathfinder.getWHCAAgentSet();
                break;
            case 4:
                farPathfinder = new FARPathfinder(gui, field);
                break;
        }
    }

    protected Pathfinder choosePathfinder() {
        switch (algorithmToUse) {
            case 0:
                return aPathfinder;
            case 1:
                // return dPathfinder.getDAgentSet();
            case 2:
                // return caPathfinder.getCAAgentSet();
            case 3:
                // return whcaPathfinder.getWHCAAgentSet();
            case 4:
                return farPathfinder;
            default: // will not happen
                return null;
        }
    }

    protected abstract void refreshField();

    protected abstract void refreshPathEndpoints();

    protected abstract void refreshPaths();

    protected abstract void refreshAgents();
}
