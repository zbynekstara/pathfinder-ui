/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package graphicspackage;

import java.awt.*;
import java.awt.geom.*;

/**
 *
 * @author Zbynda
 */
public class GraphicsUtil {
    private GraphicsConstants gc = new GraphicsConstants();

    public Color randomColor() {
        float r = (float) (Math.floor(Math.random()*6) / 5);
        float g = (float) (Math.floor(Math.random()*6) / 5);
        float b = (float) (Math.floor(Math.random()*6) / 5);

        return new Color(r, g, b);
    }

    public Color gradient(Color first, Color second, int numSubdivisions, int subdivisionIndex) {
        if (numSubdivisions <= 1) return first;
        if (subdivisionIndex < 0) return first;
        if (subdivisionIndex >= numSubdivisions) return second;

        float r1 = first.getRed();
        float r2 = second.getRed();

        float g1 = first.getGreen();
        float g2 = second.getGreen();

        float b1 = first.getBlue();
        float b2 = second.getBlue();

        float rDiff = (r2-r1) / ((float)(numSubdivisions-1));
        int rNew = (int) (r1 + (rDiff*subdivisionIndex));

        float gDiff = (g2-g1) / ((float)(numSubdivisions-1));
        int gNew = (int) (g1 + (gDiff*subdivisionIndex));

        float bDiff = (b2-b1) / ((float)(numSubdivisions-1));
        int bNew = (int) (b1 + (bDiff*subdivisionIndex));

        return new Color(rNew, gNew, bNew);
    }

    public Color contrastColor(Color backgroundColor) {
        if (luminosityContrast(backgroundColor, Color.BLACK) >= luminosityContrast(backgroundColor, Color.WHITE)) {
            return Color.BLACK;
        } else {
            return Color.WHITE;
        }
    }

    // adapted from:
    // http://www.splitbrain.org/blog/2008-09/18-calculating_color_contrast_with_php
    public double luminosityContrast(Color color1, Color color2) {
        float[] colorComponents1 = color1.getColorComponents(null);
        float[] colorComponents2 = color2.getColorComponents(null);

        float r1 = colorComponents1[0];
        float g1 = colorComponents1[1];
        float b1 = colorComponents1[2];

        float r2 = colorComponents2[0];
        float g2 = colorComponents2[1];
        float b2 = colorComponents2[2];

        double l1 =
                0.2126 * Math.pow(r1/255, 2.2) +
                0.7152 * Math.pow(g1/255, 2.2) +
                0.0722 * Math.pow(b1/255, 2.2);
        double l2 =
                0.2126 * Math.pow(r2/255, 2.2) +
                0.7152 * Math.pow(g2/255, 2.2) +
                0.0722 * Math.pow(b2/255, 2.2);

        if (l1 > l2) {
            return (l1+0.05) / (l2+0.05);
        } else {
            return (l2+0.05) / (l1+0.05);
        }
    }

    public void drawNumber(Graphics2D g2, Point2D.Float ec, int number, Color textColor) { // ec is elementCorner
        int firstDigit = (number%100) / 10; // if the number is higher than 100, it shows just the two last digits
        int secondDigit = number % 10;

        g2.setColor(textColor);

        switch (firstDigit) {
            case 0:
                g2.draw(new Line2D.Float((float) ec.x+4, (float) ec.y+5, (float) ec.x+6, (float) ec.y+5)); // top horizontal
                g2.draw(new Line2D.Float((float) ec.x+4, (float) ec.y+9, (float) ec.x+6, (float) ec.y+9)); // bottom horizontal
                g2.draw(new Line2D.Float((float) ec.x+4, (float) ec.y+5, (float) ec.x+4, (float) ec.y+9)); // left full vertical
                g2.draw(new Line2D.Float((float) ec.x+6, (float) ec.y+5, (float) ec.x+6, (float) ec.y+9)); // right full vertical
                break;
            case 1:
                g2.draw(new Line2D.Float((float) ec.x+6, (float) ec.y+5, (float) ec.x+6, (float) ec.y+9)); // right full vertical
                break;
            case 2:
                g2.draw(new Line2D.Float((float) ec.x+4, (float) ec.y+5, (float) ec.x+6, (float) ec.y+5)); // top horizontal
                g2.draw(new Line2D.Float((float) ec.x+4, (float) ec.y+7, (float) ec.x+6, (float) ec.y+7)); // central horizontal
                g2.draw(new Line2D.Float((float) ec.x+4, (float) ec.y+9, (float) ec.x+6, (float) ec.y+9)); // bottom horizontal
                g2.draw(new Line2D.Float((float) ec.x+4, (float) ec.y+7, (float) ec.x+4, (float) ec.y+9)); // left bottom vertical
                g2.draw(new Line2D.Float((float) ec.x+6, (float) ec.y+5, (float) ec.x+6, (float) ec.y+7)); // right top vertical
                break;
            case 3:
                g2.draw(new Line2D.Float((float) ec.x+4, (float) ec.y+5, (float) ec.x+6, (float) ec.y+5)); // top horizontal
                g2.draw(new Line2D.Float((float) ec.x+4, (float) ec.y+7, (float) ec.x+6, (float) ec.y+7)); // central horizontal
                g2.draw(new Line2D.Float((float) ec.x+4, (float) ec.y+9, (float) ec.x+6, (float) ec.y+9)); // bottom horizontal
                g2.draw(new Line2D.Float((float) ec.x+6, (float) ec.y+5, (float) ec.x+6, (float) ec.y+9)); // right full vertical
                break;
            case 4:
                g2.draw(new Line2D.Float((float) ec.x+4, (float) ec.y+7, (float) ec.x+6, (float) ec.y+7)); // central horizontal
                g2.draw(new Line2D.Float((float) ec.x+4, (float) ec.y+5, (float) ec.x+4, (float) ec.y+7)); // left top vertical
                g2.draw(new Line2D.Float((float) ec.x+6, (float) ec.y+5, (float) ec.x+6, (float) ec.y+9)); // right full vertical
                break;
            case 5:
                g2.draw(new Line2D.Float((float) ec.x+4, (float) ec.y+5, (float) ec.x+6, (float) ec.y+5)); // top horizontal
                g2.draw(new Line2D.Float((float) ec.x+4, (float) ec.y+7, (float) ec.x+6, (float) ec.y+7)); // central horizontal
                g2.draw(new Line2D.Float((float) ec.x+4, (float) ec.y+9, (float) ec.x+6, (float) ec.y+9)); // bottom horizontal
                g2.draw(new Line2D.Float((float) ec.x+4, (float) ec.y+5, (float) ec.x+4, (float) ec.y+7)); // left top vertical
                g2.draw(new Line2D.Float((float) ec.x+6, (float) ec.y+7, (float) ec.x+6, (float) ec.y+9)); // right bottom vertical
                break;
            case 6:
                g2.draw(new Line2D.Float((float) ec.x+4, (float) ec.y+5, (float) ec.x+6, (float) ec.y+5)); // top horizontal
                g2.draw(new Line2D.Float((float) ec.x+4, (float) ec.y+7, (float) ec.x+6, (float) ec.y+7)); // central horizontal
                g2.draw(new Line2D.Float((float) ec.x+4, (float) ec.y+9, (float) ec.x+6, (float) ec.y+9)); // bottom horizontal
                g2.draw(new Line2D.Float((float) ec.x+4, (float) ec.y+5, (float) ec.x+4, (float) ec.y+9)); // left full vertical
                g2.draw(new Line2D.Float((float) ec.x+6, (float) ec.y+7, (float) ec.x+6, (float) ec.y+9)); // right bottom vertical
                break;
            case 7:
                g2.draw(new Line2D.Float((float) ec.x+4, (float) ec.y+5, (float) ec.x+6, (float) ec.y+5)); // top horizontal
                g2.draw(new Line2D.Float((float) ec.x+6, (float) ec.y+5, (float) ec.x+6, (float) ec.y+9)); // right full vertical
                break;
            case 8:
                g2.draw(new Line2D.Float((float) ec.x+4, (float) ec.y+5, (float) ec.x+6, (float) ec.y+5)); // top horizontal
                g2.draw(new Line2D.Float((float) ec.x+4, (float) ec.y+7, (float) ec.x+6, (float) ec.y+7)); // central horizontal
                g2.draw(new Line2D.Float((float) ec.x+4, (float) ec.y+9, (float) ec.x+6, (float) ec.y+9)); // bottom horizontal
                g2.draw(new Line2D.Float((float) ec.x+4, (float) ec.y+5, (float) ec.x+4, (float) ec.y+9)); // left full vertical
                g2.draw(new Line2D.Float((float) ec.x+6, (float) ec.y+5, (float) ec.x+6, (float) ec.y+9)); // right full vertical
                break;
            case 9:
                g2.draw(new Line2D.Float((float) ec.x+4, (float) ec.y+5, (float) ec.x+6, (float) ec.y+5)); // top horizontal
                g2.draw(new Line2D.Float((float) ec.x+4, (float) ec.y+7, (float) ec.x+6, (float) ec.y+7)); // central horizontal
                g2.draw(new Line2D.Float((float) ec.x+4, (float) ec.y+9, (float) ec.x+6, (float) ec.y+9)); // bottom horizontal
                g2.draw(new Line2D.Float((float) ec.x+4, (float) ec.y+5, (float) ec.x+4, (float) ec.y+7)); // left top vertical
                g2.draw(new Line2D.Float((float) ec.x+6, (float) ec.y+5, (float) ec.x+6, (float) ec.y+9)); // right full vertical
                break;
        }

        switch (secondDigit) {
            case 0:
                g2.draw(new Line2D.Float((float) ec.x+8, (float) ec.y+5, (float) ec.x+10, (float) ec.y+5)); // top horizontal
                g2.draw(new Line2D.Float((float) ec.x+8, (float) ec.y+9, (float) ec.x+10, (float) ec.y+9)); // bottom horizontal
                g2.draw(new Line2D.Float((float) ec.x+8, (float) ec.y+5, (float) ec.x+8, (float) ec.y+9)); // left full vertical
                g2.draw(new Line2D.Float((float) ec.x+10, (float) ec.y+5, (float) ec.x+10, (float) ec.y+9)); // right full vertical
                break;
            case 1:
                g2.draw(new Line2D.Float((float) ec.x+10, (float) ec.y+5, (float) ec.x+10, (float) ec.y+9)); // right full vertical
                break;
            case 2:
                g2.draw(new Line2D.Float((float) ec.x+8, (float) ec.y+5, (float) ec.x+10, (float) ec.y+5)); // top horizontal
                g2.draw(new Line2D.Float((float) ec.x+8, (float) ec.y+7, (float) ec.x+10, (float) ec.y+7)); // central horizontal
                g2.draw(new Line2D.Float((float) ec.x+8, (float) ec.y+9, (float) ec.x+10, (float) ec.y+9)); // bottom horizontal
                g2.draw(new Line2D.Float((float) ec.x+8, (float) ec.y+7, (float) ec.x+8, (float) ec.y+9)); // left bottom vertical
                g2.draw(new Line2D.Float((float) ec.x+10, (float) ec.y+5, (float) ec.x+10, (float) ec.y+7)); // right top vertical
                break;
            case 3:
                g2.draw(new Line2D.Float((float) ec.x+8, (float) ec.y+5, (float) ec.x+10, (float) ec.y+5)); // top horizontal
                g2.draw(new Line2D.Float((float) ec.x+8, (float) ec.y+7, (float) ec.x+10, (float) ec.y+7)); // central horizontal
                g2.draw(new Line2D.Float((float) ec.x+8, (float) ec.y+9, (float) ec.x+10, (float) ec.y+9)); // bottom horizontal
                g2.draw(new Line2D.Float((float) ec.x+10, (float) ec.y+5, (float) ec.x+10, (float) ec.y+9)); // right full vertical
                break;
            case 4:
                g2.draw(new Line2D.Float((float) ec.x+8, (float) ec.y+7, (float) ec.x+10, (float) ec.y+7)); // central horizontal
                g2.draw(new Line2D.Float((float) ec.x+8, (float) ec.y+5, (float) ec.x+8, (float) ec.y+7)); // left top vertical
                g2.draw(new Line2D.Float((float) ec.x+10, (float) ec.y+5, (float) ec.x+10, (float) ec.y+9)); // right full vertical
                break;
            case 5:
                g2.draw(new Line2D.Float((float) ec.x+8, (float) ec.y+5, (float) ec.x+10, (float) ec.y+5)); // top horizontal
                g2.draw(new Line2D.Float((float) ec.x+8, (float) ec.y+7, (float) ec.x+10, (float) ec.y+7)); // central horizontal
                g2.draw(new Line2D.Float((float) ec.x+8, (float) ec.y+9, (float) ec.x+10, (float) ec.y+9)); // bottom horizontal
                g2.draw(new Line2D.Float((float) ec.x+8, (float) ec.y+5, (float) ec.x+8, (float) ec.y+7)); // left top vertical
                g2.draw(new Line2D.Float((float) ec.x+10, (float) ec.y+7, (float) ec.x+10, (float) ec.y+9)); // right bottom vertical
                break;
            case 6:
                g2.draw(new Line2D.Float((float) ec.x+8, (float) ec.y+5, (float) ec.x+10, (float) ec.y+5)); // top horizontal
                g2.draw(new Line2D.Float((float) ec.x+8, (float) ec.y+7, (float) ec.x+10, (float) ec.y+7)); // central horizontal
                g2.draw(new Line2D.Float((float) ec.x+8, (float) ec.y+9, (float) ec.x+10, (float) ec.y+9)); // bottom horizontal
                g2.draw(new Line2D.Float((float) ec.x+8, (float) ec.y+5, (float) ec.x+8, (float) ec.y+9)); // left full vertical
                g2.draw(new Line2D.Float((float) ec.x+10, (float) ec.y+7, (float) ec.x+10, (float) ec.y+9)); // right bottom vertical
                break;
            case 7:
                g2.draw(new Line2D.Float((float) ec.x+8, (float) ec.y+5, (float) ec.x+10, (float) ec.y+5)); // top horizontal
                g2.draw(new Line2D.Float((float) ec.x+10, (float) ec.y+5, (float) ec.x+10, (float) ec.y+9)); // right full vertical
                break;
            case 8:
                g2.draw(new Line2D.Float((float) ec.x+8, (float) ec.y+5, (float) ec.x+10, (float) ec.y+5)); // top horizontal
                g2.draw(new Line2D.Float((float) ec.x+8, (float) ec.y+7, (float) ec.x+10, (float) ec.y+7)); // central horizontal
                g2.draw(new Line2D.Float((float) ec.x+8, (float) ec.y+9, (float) ec.x+10, (float) ec.y+9)); // bottom horizontal
                g2.draw(new Line2D.Float((float) ec.x+8, (float) ec.y+5, (float) ec.x+8, (float) ec.y+9)); // left full vertical
                g2.draw(new Line2D.Float((float) ec.x+10, (float) ec.y+5, (float) ec.x+10, (float) ec.y+9)); // right full vertical
                break;
            case 9:
                g2.draw(new Line2D.Float((float) ec.x+8, (float) ec.y+5, (float) ec.x+10, (float) ec.y+5)); // top horizontal
                g2.draw(new Line2D.Float((float) ec.x+8, (float) ec.y+7, (float) ec.x+10, (float) ec.y+7)); // central horizontal
                g2.draw(new Line2D.Float((float) ec.x+8, (float) ec.y+9, (float) ec.x+10, (float) ec.y+9)); // bottom horizontal
                g2.draw(new Line2D.Float((float) ec.x+8, (float) ec.y+5, (float) ec.x+8, (float) ec.y+7)); // left top vertical
                g2.draw(new Line2D.Float((float) ec.x+10, (float) ec.y+5, (float) ec.x+10, (float) ec.y+9)); // right full vertical
                break;
        }
    }

    public Point2D.Float getElementCorner(int elementX, int elementY) { // this is the top left corner
        float pointX = (float) elementX*(gc.ELEMENT_SIZE+gc.LINE_WIDTH) + gc.LINE_WIDTH;
        float pointY = (float) elementY*(gc.ELEMENT_SIZE+gc.LINE_WIDTH) + gc.LINE_WIDTH;

        return new Point2D.Float(pointX, pointY);
    }

    public Point2D.Float getElementCenter(int elementX, int elementY) {
        float pointX = (float) elementX*(gc.ELEMENT_SIZE+gc.LINE_WIDTH) + gc.LINE_WIDTH + (gc.ELEMENT_SIZE/2);
        float pointY = (float) elementY*(gc.ELEMENT_SIZE+gc.LINE_WIDTH) + gc.LINE_WIDTH + (gc.ELEMENT_SIZE/2);

        return new Point2D.Float(pointX, pointY);
    }
}
