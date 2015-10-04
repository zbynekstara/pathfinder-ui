package adtpackage;

import generalpackage.*;

/**
 *
 * @author Zbynda
 */
public class MaximalQueue implements Printable {
    private class Node <T extends Printable> {
        private T data = null;
        private double key = -999;
        private Node next = null;

        private Node(T data, double key) {
            this.data = data;
            this.key = key;
        }

        private Node(T data, double key, Node next) {
            this.data = data;
            this.key = key;
            this.next = next;
        }
    }

    private Node head = null;
    private int size = 0;

    public MaximalQueue() {
        
    }

    public boolean isEmpty() {
        return (head == null);
    }

    public int size() {
        return size;
    }

    public boolean contains(double key) {
        if (isEmpty()) return false;
        else {
            Node current = head;

            while (current.key > key && current.next != null) {
                current = current.next;
            }

            if (current.key == key) return true;
            else return false;
        }
    }

    public <T extends Printable> int search(T data) {
        if (isEmpty()) {
            return -1;
        } else {
            Node current = head;

            int counter = 0;
            while (current.data != data && current.next != null) {
                counter++;
                current = current.next;
            }

            if (current.data == data) return counter;
            else return -1;
        }
    }

    public <T extends Printable> void enqueue(T data, double key) { // adds to the list according to node keys
        if (isEmpty()) {
            head = new Node(data, key);
        } else if (size() == 1) {
            if (head.key >= key) {
                head.next = new Node(data, key);
            } else {
                Node temp = head;
                head = new Node(data, key, temp);
            }
        } else {
            if (head.key >= key) { // if adding the same key, the new one goes behind the old one
                if (head.next == null) {
                    head.next = new Node(data, key);
                }

                Node current = head.next;
                Node previous = head;
                while (current.key >= key) {
                    if (current.next == null) {
                        previous = current;
                        current = null;
                        break;
                    } else {
                        previous = current;
                        current = current.next;
                    }
                }
                Node temp = current;
                previous.next = new Node(data, key, temp);
            } else {
                Node temp = head;
                head = new Node(data, key, temp);
            }
        }
        size += 1;
    }

    public Object dequeue() { // removes the first element from the queue - the largest one
        if (isEmpty()) {
            return null;
        } else {
            Node temp = head;
            head = head.next;
            size -= 1;
            return temp.data;
        }
    }

    public Object remove(double key) { // removes the first of given key
        if (isEmpty()) {
            return null;
        } else if (size == 1) {
            if (head.key == key) {
                Node temp = head;
                head = null;
                size -= 1;
                return temp.data;
            } else {
                return null;
            }
        } else {
            if (head.key != key) {
                Node current = head.next;
                Node previous = head;
                while (current.key != key) {
                    if (current.next == null) {
                        return null;
                    } else {
                        previous = current;
                        current = current.next;
                    }
                }
                Node temp = current;
                previous.next = current.next;
                size -= 1;
                return temp.data;
            } else {
                Node temp = head;
                head = head.next;
                size -= 1;
                return temp.data;
            }
        }
    }

    public <T extends Printable> int delete(T data) {
        if (isEmpty()) { // if there are no elements
            return -1;
        } else if (size == 1) { // if there is only one element
            if (head.data == data) { // if the only element is the correct element
                head = null;
                size -= 1;
                return 0;
            } else { // if the only element is not what we are looking for
                return -1;
            }
        } else { // if there are more elements
            if (head.data != data) { // if the element is not the head
                Node current = head.next;
                Node previous = head;

                int counter = -1;
                while (current.data != data) {
                    counter++;
                    if (current.next == null) { // if the last event isn't it
                        return -1;
                    } else {
                        previous = current;
                        current = current.next;
                    }
                }
                previous.next = current.next;
                size -= 1;
                return counter;
            } else { // if the head is what we are looking for
                head = head.next;
                size -= 1;
                return 0;
            }
        }
    }

    public Object getNodeData(int node) {
        if ((!isEmpty()) && (node < size)) {
            Node current = head;
            for (int i = 0; i < node; i++) {
                current = current.next;
            }
            return current.data;
        } else {
            return null;
        }
    }

    public double getNodeKey(int node) {
        if ((!isEmpty()) && (node < size)) {
            Node current = head;
            for (int i = 0; i < node; i++) {
                current = current.next;
            }
            return current.key;
        } else {
            return -1;
        }
    }

    @Override public String print() {
        return toString();
    }

    @Override public String toString() {
        if (!isEmpty()) {
            String printedNodes = "";
            for (int i = 0; i < (size - 1); i++) {
                printedNodes = (printedNodes+"{"+printNodeData(i)+"}<"+printNodeKey(i)+">, ");
            }
            printedNodes = (printedNodes+"{"+printNodeData(size - 1)+"}<"+printNodeKey(size - 1)+">");
            return ("MaximalQueue size: "+size+"; MaximalQueue contents: ["+printedNodes+"]");
        } else {
            return ("MaximalQueue is empty");
        }
    }

    private <T extends Printable> String printNodeData(int node) {
        return ((T) getNodeData(node)).print();
    }
    
    private String printNodeKey(int node) {
        return getNodeKey(node)+"";
    }
}
