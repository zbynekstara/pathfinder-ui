package adtpackage;

import generalpackage.*;

/**
 *
 * @author Zbyněk Stara
 */
public class HashSet extends HashTable implements Set {
    public HashSet(int dimension) {
        super(dimension);
    }

    public <T extends Printable> ReturnCode add(T data, double key) {
        int checkCounter = 0;
        boolean positionFound = false;
        ReturnCode additionResult = ReturnCode.UNDEFINED;

        int hashValue = hashCalculation(key);

        while (checkCounter < dataArray.length) {
            if (dataArray[hashValue] == null) {
                dataArray[hashValue] = new TableElement(data, key);
                positionFound = true;
                size += 1;
                arrayChanged = true;
                additionResult = ReturnCode.SUCCESS;
                break;
            } else if (dataArray[hashValue].key == key) {
                positionFound = true;
                additionResult = ReturnCode.SKIP;
                break;
            } else {
                checkCounter += 1;
                hashValue += 1;
                if (hashValue == dataArray.length) hashValue = 0;
            }
        }

        if (!positionFound) additionResult = ReturnCode.OVERFLOW;
        
        return additionResult;
    }

    public Object remove(double key) { // will return null if it doesn't exist
        return delete(key);
    }

    public boolean contains(double key) {
        return search(key) != null;
    }
}

