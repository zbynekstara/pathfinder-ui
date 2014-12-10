package adtpackage;

import generalpackage.*;

/**
 *
 * @author Zbynda
 */
public class HashTable implements ADT, Printable {
    protected class TableElement <T extends Printable> {
        protected Object data = null ;
        protected double key = -999;

        protected TableElement(T data, double key) {
            this.data = data;
            this.key = key;
        }
    }

    protected TableElement [] dataArray;

    protected int size = 0;

    private Object [] traverseArray;
    private int positionCounter = 0;
    protected boolean arrayChanged = true;

    public HashTable(int dimension) {
        dataArray = new TableElement[dimension];
        for (int i = 0; i < dataArray.length; i++) {
            dataArray[i] = null;
        }
    }

    public boolean isEmpty() {
        return (size == 0);
    }

    public int size() {
        return size;
    }

    public <T extends Printable> ReturnCode insert(T data, double key) {
        int checkCounter = 0;
        boolean positionFound = false;

        int hashValue = hashCalculation(key);

        while (checkCounter < dataArray.length) {
            if (dataArray[hashValue] == null) {
                dataArray[hashValue] = new TableElement(data, key);
                positionFound = true;
                size += 1;
                arrayChanged = true;
                break;
            } else {
                checkCounter += 1;
                hashValue += 1;
                if (hashValue == dataArray.length) hashValue = 0;
            }
        }

        if (positionFound) return ReturnCode.SUCCESS;
        else return ReturnCode.OVERFLOW;
    }

    public Object search(double key) {
        int checkCounter = 0;
        boolean tableElementFound = false;

        int hashValue = hashCalculation(key);

        while (checkCounter < dataArray.length) {
            if (dataArray[hashValue] != null) {
                if (dataArray[hashValue].key == key) {
                    tableElementFound = true;
                    break;
                } else {
                    checkCounter += 1;
                    hashValue += 1;
                    if (hashValue == dataArray.length) hashValue = 0;
                }
            } else {
                checkCounter += 1;
                hashValue += 1;
                if (hashValue == dataArray.length) hashValue = 0;
            }
        }

        if (tableElementFound) return dataArray[hashValue].data;
        else return null;
    }

    public Object delete(double key) {
        int checkCounter = 0;
        boolean tableElementFound = false;
        Object dataReturned = null;

        int hashValue = hashCalculation(key);

        while (checkCounter < dataArray.length) {
            if (dataArray[hashValue].key == key) {
                dataReturned = dataArray[hashValue].data;
                size -= 1;
                arrayChanged = true;
                dataArray[hashValue] = null;
                tableElementFound = true;
                break;
            } else {
                checkCounter += 1;
                hashValue += 1;
                if (hashValue == dataArray.length) hashValue = 0;
            }
        }

        if (tableElementFound) return dataReturned;
        else return null;
    }

    protected int hashCalculation(double key) {
        return ((int) key % dataArray.length);
    }

    public Object getNodeData(int node) {
        if (!arrayChanged) {
            return traverseArray[node];
        } else {
            traverse();
            return traverseArray[node];
        }
    }

    public Object [] traverse() {
        traverseArray = new Object[size];
        positionCounter = 0;

        for (int i = 0; i < dataArray.length; i++) {
            if (dataArray[i] != null) {
                traverseArray[positionCounter] = dataArray[i].data;
                positionCounter += 1;
            }
        }

        arrayChanged = false;
        return traverseArray;
    }


    public String print() {
        return toString();
    }

    @Override public String toString() {
        String string = "";
        if (!isEmpty()) {
            string = "Size: " + size +"; Contents: [";
            int i = 0;
            for (; i < dataArray.length; i++) {
                if (dataArray[i] != null) {
                    string += "{";
                    string += dataArray[i].data.toString();
                    string += "}, ";
                }
                
            }
        } else {
            string = "Hash table is empty";
        }
        return string;
    }
}
