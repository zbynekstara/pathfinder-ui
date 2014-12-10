package generalpackage;

import adtpackage.TreeSet;

/**
 *
 * @author Zbyněk Stara
 */
public class FreeGroup implements Printable {
    private Field field;
    private int freeGroupNumber = -999;
    private int numberElements = -999;
    private TreeSet elementSet;

    public FreeGroup(int freeGroupNumber, Element element, Field field) {
        this.field = field;
        this.freeGroupNumber = freeGroupNumber;
        elementSet = new TreeSet();

        numberElements = 1;
        elementSet.add(element, element.getKey(field.Y_DIMENSION));
    }

    public void addElement(Element element) {
        numberElements += 1;
        elementSet.add(element, element.getKey(field.Y_DIMENSION));
    }

    public void removeElement(Element element) {
        numberElements -= 1;
        elementSet.remove(element.getKey(field.Y_DIMENSION));
    }

    public Element removeMinElement() {
        numberElements -= 1;
        return (Element) elementSet.removeMin();
    }

    public int getFreeGroupNumber() {
        return freeGroupNumber;
    }

    public int getNumberElements() {
        return numberElements;
    }

    @Override public String print() {
        return toString();
    }

    @Override public String toString() {
        return "FreeGroup number: " + freeGroupNumber + "; Number of elements: " + numberElements + "; Contents: {" + elementSet.print() + "}";
    }
}
