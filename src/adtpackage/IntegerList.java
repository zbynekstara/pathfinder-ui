package adtpackage;

/**
 *
 * @author Zbyněk Stara
 */
public class IntegerList {
    private class Node {
        private Integer value;
        private Node next = null;

        private Node(int value) {
            this.value = value;
        }
    }

    private Node head = null;
    private int size = 0;

    public IntegerList() {
        head = null;
    }

    public boolean isEmpty() {
        return (head == null);
    }

    public int size() { // size is 1 when there is 1 element – which the 0th element
        return size;
    }

    public void insertAtFront(int value) {
        Node newNode = new Node(value);
        newNode.next = head;
        head = newNode;
        size += 1;
    }

    public void insertAtRear(int value) {
        if (isEmpty()) {
            head = new Node(value);
        } else {
            Node current = head;
            while (current.next != null) {
                current = current.next;
            }
            current.next = new Node(value);
        }
        size += 1;
    }

    public void insertAsNode(int value, int node) {
        // insert the value to be the node at specified index
        // whatever might have been there is moved back
        if (node == 0) {
            insertAtFront(value);
        } else if((!isEmpty()) && (node < size)) {
            Node current = head;
            for (int i = 1; i < node; i++) {
                current = current.next;
            }

            Node temp = current.next;
            current.next = new Node(value);
            current.next.next = temp;

            size += 1;
        } else {
            insertAtRear(value);
        }
    }
    
    public int search(int value) {
        if (isEmpty()) {
            return -1;
        } else {
            Node current = head;

            int counter = 0;
            while (current.value != value && current.next != null) {
                counter++;
                current = current.next;
            }

            if (current.value == value) return counter;
            else return -1;
        }
    }

    public Integer removeFirst() {
        if (isEmpty()) {
            return null;
        }
        Node first = head;
        head = head.next;
        size -= 1;
        return first.value;
    }

    public Integer removeLast() {
        if (isEmpty()) {
            return null;
        }
        Node current = head;
        if (current.next == null) {
            head = null;
            return current.value;
        }
        Node previous = null;
        while (current.next != null) {
            previous = current;
            current = current.next;
        }
        previous.next = null;
        size -= 1;
        return current.value;
    }

    public Integer removeNode(int node) {
        if ((!isEmpty()) && (node < size)) {
            Node current = head;
            if (node == 0) {
                return removeFirst();
            }
            current = current.next;
            Node previous = head;

            for (int i = 1; i < node; i++) {
                current = current.next;
                previous = previous.next;
            }

            Node temp = current;
            previous.next = current.next;
            size -= 1;
            return temp.value;
        } else {
            return null;
        }
    }

    public Integer getNodeValue(int node) {
        if ((!isEmpty()) && (node < size)) {
            Node current = head;
            for (int i = 0; i < node; i++) {
                current = current.next;
            }
            return current.value;
        } else {
            return null;
        }
    }
    
    public Integer getLastNodeValue() {
        return getNodeValue(size-1);
    }

    @Override public String toString() {
        if (!isEmpty()) {
            String printedNodes = "";
            for (int i = 0; i < (size - 1); i++) {
                printedNodes = (printedNodes+"{"+printNodeValue(i)+"}, ");
            }
            printedNodes = (printedNodes+"{"+printNodeValue(size - 1)+"}");
            return ("IntegerList size: "+size+"; IntegerList contents: ["+printedNodes+"]");
        } else {
            return ("IntegerList is empty");
        }
    }

    private String printNodeValue(int node) {
        if (getNodeValue(node) == null) return null;
        else return getNodeValue(node).toString();
    }
}
