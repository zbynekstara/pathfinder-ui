package adtpackage;

import generalpackage.*;

/**
 *
 * @author Zbyněk Stara
 */
public class BinarySearchTree implements ADT {
    private class Node {
        private Object data;
        private double key;

        private Node left = null; // smaller
        private Node right = null; // bigger

        private Node(Object data, double key) {
            this.key = key;
            this.data = data;
        }

        @Override public String toString() {
            return printHelper(this, "");
        }

        private String printHelper(Node node, String outputString) {
            if ((node.left == null) && (node.right == null)) {
                outputString = outputString + node.key;
            } else {
                outputString = outputString + "(";
                if (node.left != null) {
                    outputString = printHelper(node.left, outputString);
                } else {
                    outputString = outputString + "x";
                }
                outputString = outputString + "-";
                outputString = outputString + node.key;
                outputString = outputString + "-";
                if (node.right != null) {
                    outputString = printHelper(node.right, outputString);
                } else {
                    outputString = outputString + "x";
                }
                outputString = outputString + ")";
            }
            return outputString;
        }
    }

    private class Del {
        private Node kappa; // storing the new organization of nodes
        private Node sigma; // storing the deleted node

        public Del (Node kappa, Node sigma) {
            this.kappa = kappa;
            this.sigma = sigma;
        }
    }

    private Node root = null;

    private int size = 0;

    private Object [] traverseInOrderArray;
    private int positionCounter = 0;
    private boolean arrayChanged = true;

    public BinarySearchTree() {

    }

    public <T extends Printable> ReturnCode insert(T data, double key) {
        if (isEmpty()) root = new Node(data, key);
        else root = insertHelper(data, key, root);
        size += 1;
        arrayChanged = true;
        return ReturnCode.SUCCESS; // showing that the insertion went without problems
    }

    private Node insertHelper(Object data, double key, Node currentNode) {
        if (key < currentNode.key) {
            if (currentNode.left == null) {
                currentNode.left = new Node(data, key);
                return currentNode;
            } else {
                currentNode.left = insertHelper(data, key, currentNode.left);
                return currentNode;
            }
        } else { // if key is the same or bigger than currentNode.key
            if (currentNode.right == null) {
                currentNode.right = new Node(data, key);
                return currentNode;
            } else {
                currentNode.right = insertHelper(data, key, currentNode.right);
                return currentNode;
            }
        }
    }

    public Object search(double key) { // gives data of searched node
        Node searchNode = searchHelper(key, root);
        if (searchNode != null) return searchNode.data;
        else return null;
    }

    private Node searchHelper(double key, Node currentNode) { // returns null if node doesn't exist
        if (currentNode == null) {
            return null;
        } else {
            if (key == currentNode.key) {
                return currentNode;
            } else if (key < currentNode.key) {
                return searchHelper(key, currentNode.left);
            } else {
                return searchHelper(key, currentNode.right);
            }
        }
    }

    public Object delete(double key) { // gives data of deleted nodes
        Del del = deleteHelper(key, root);
        Node deleteNode = del.sigma;
        root = del.kappa;
        size -= 1;
        arrayChanged = true;
        if (deleteNode != null) return deleteNode.data;
        else return null;
    }

    private Del deleteHelper(double key, Node currentNode) { // returns null if node being deleted doesn't exist
        if (currentNode == null) {
            Del delSent = new Del(null, null);
            return delSent;
        } else {
            if (key == currentNode.key) {
                Del delReceived = deleteMover(currentNode); // removing & fixing algorithm;
                return delReceived;
            } else if (key < currentNode.key) {
                Del delReceived = deleteHelper(key, currentNode.left);
                currentNode.left = delReceived.kappa;
                Del delSent = new Del(currentNode, delReceived.sigma);
                return delSent;
            } else {
                Del delReceived = deleteHelper(key, currentNode.right);
                currentNode.right = delReceived.kappa;
                Del delSent = new Del(currentNode, delReceived.sigma);
                return delSent;
            }
        }
    }

    public Object deleteMin() { // returns data of the smallest node
        Del del = deleteMinHelper(root);
        Node deleteMinNode = del.sigma;
        root = del.kappa;
        size -= 1;
        arrayChanged = true;
        if (deleteMinNode != null) return deleteMinNode.data;
        else return null;
    }

    private Del deleteMinHelper(Node currentNode) { // returns null if the tree is empty
        if (currentNode == null) {
            Del delSent = new Del(null, null);
            return delSent;
        } else {
            if (currentNode.left != null) {
                Del delReceived = deleteMinHelper(currentNode.left);
                currentNode.left = delReceived.kappa;
                Del delSent = new Del(currentNode, delReceived.sigma);
                return delSent;
            } else if (currentNode.right != null) { // if curentNode.left is null but currentNode.right is not
                Del delReceived = deleteMover(currentNode.right); // need to fix currentNode.right
                currentNode.right = delReceived.kappa;
                Del delSent = new Del(currentNode, delReceived.sigma);
                return delSent;
            } else { // if both left and right are null
                Node send = currentNode;
                currentNode = null;
                Del delSent = new Del(currentNode, send);
                return delSent;
            }
        }
    }

    private Del deleteMover(Node node) {
        if (node.right != null) {
            if (node.right.left != null) { // going to exchange node with node.right.left
                Node tempLeft = node.left;
                Node tempRight = node.right;
                Node send = node;
                Del delReceived = deleteMover(node.right.left); // fixing node.right.left
                node.right.left = delReceived.kappa;
                node = delReceived.sigma;
                node.left = tempLeft;
                node.right = tempRight;
                Del delSent = new Del(node, send);
                return delSent; // sending the deleted node
            } else { // node.right is not null but it has nothing to the left
                Node tempLeft = node.left;
                Node send = node;
                node = node.right;
                node.left = tempLeft;
                Del delSent = new Del(node, send);
                return delSent; // sending the deleted node
            }
        } else if (node.left != null) { // node.right is null but node.left is not
            Node send = node;
            node = node.left;
            Del delSent = new Del(node, send);
            return delSent; // sending the deleted node
        } else { // both nodde.right and node.left are null
            Node send = node;
            node = null;
            Del delSent = new Del(node, send);
            return delSent; // sending the deleted node
        }
    }

    public boolean isEmpty() {
        return root == null;
    }

    public int size() {
        return size;
    }

    public Object getNodeData(int index) {
        if (arrayChanged) traverseInOrder();
        return traverseInOrderArray[index];
    }

    public Object [] traverseInOrder() {
        traverseInOrderArray = new Object [size];
        positionCounter = 0;
        traverseInOrderHelper(root);
        arrayChanged = false;
        return traverseInOrderArray;
    }

    private void traverseInOrderHelper(Node node) {
        if (node.left != null) traverseInOrderHelper(node.left);
        traverseInOrderArray[positionCounter] = node.data;
        positionCounter += 1;
        if (node.right != null) traverseInOrderHelper(node.right);
    }
}
