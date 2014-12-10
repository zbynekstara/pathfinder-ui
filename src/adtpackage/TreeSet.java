package adtpackage;

import generalpackage.Printable;

/**
 *
 * @author Zbyněk Stara
 */
public class TreeSet extends BinarySearchTree implements Printable, Set {
    public TreeSet() {
        super();
    }

    public <T extends Printable> ReturnCode add(T data, double key) {
        ReturnCode insertionResult;

        if (!contains(key)) {
            insertionResult = insert(data, key);
        } else {
            insertionResult = ReturnCode.SKIP;
        }

        return insertionResult;
    }

    public Object remove(double key) { // will return null if it doesn't exist
        return delete(key);
    }

    public Object removeMin() {
        return deleteMin();
    }

    public boolean contains(double key) {
        return search(key) != null;
    }

    @Override public String print() {
        return toString();
    }

    @Override public String toString() { // THIS MIGHT NOT WORK BECAUSE KEYS ARE NOT GOING IN ORDER - FIXED?
        if (!isEmpty()) {
            String printedNodes = "";
            for (int i = 0; i < (size() - 1); i++) {
                printedNodes = (printedNodes + "{" + printNode(i) + "}, ");
            }
            printedNodes = (printedNodes + "{" + printNode(size() - 1) + "}");
            return ("Set size: " + size() + "; Contents: " + printedNodes);
        } else {
            return "Set is empty";
        }
    }

    private <T extends Printable> String printNode(int node) {
        return ((T) getNodeData(node)).print();
    }
}
