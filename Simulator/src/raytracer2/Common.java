/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package raytracer2;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.LinkedList;
import java.util.Random;

/**
 *
 * @author chanjustin
 */
public class Common {
    public static final double epsilon = 0.000001;
    public static final int RAY_LEN = 2000; //effectively infinite
    public static final Random random = new Random();
    public static LinkedList<Obstacle> objs;
    public static String DIFFRACTED = "DIFFRACTED";
    public static String SPECULAR = "SPECULAR";
    public static String DIFFUSE = "DIFFUSE";
    public static String DIFFRACTION = "DIFFRACTION";
    public static String ROOT = "ROOT";
    public static String REFLECTED = "REFLECTED";
    public static String TRANSMITTED = "TRANSMITTED";
    public static int DIFFUSE_RAYS = 10;
    
    public static double euclidean(double x1, double y1, double x2, double y2)
    {
        return Math.sqrt((x2-x1)*(x2-x1) + (y2-y1)*(y2-y1));
    }
    
    public static boolean isPositiveGradient(Line2D wall)
    {
        if(wall.getX1() < wall.getX2() && wall.getY1() > wall.getY2() ||
           wall.getX2() < wall.getX1() && wall.getY2() > wall.getY1())
        {
            return true;
        }
        return false;
    }
    
    public static boolean isHorizontal(Line2D line)
    {
        return Math.abs(line.getY1() - line.getY2()) < Common.epsilon;
    }

    public static boolean isVertical(Line2D line)
    {
        return Math.abs(line.getX1() - line.getX2()) < Common.epsilon;
    }

    public static double getAngle(Line2D line)
    {
        double lineAngle;
        if(Common.isVertical(line))
        {
            lineAngle = Math.PI/2;
        }
        else if(Common.isHorizontal(line))
        {
            lineAngle = 0;
        }
        else
        {
            lineAngle = Math.atan2(Math.abs(line.getY2()-line.getY1()),
                                   Math.abs(line.getX2()-line.getX1()));
        }
        if(line.getX1() < line.getX2() &&
           line.getY1() < line.getY2() ||
           line.getX2() < line.getX1() &&
           line.getY2() < line.getY1() )
        {
            lineAngle = Math.PI-lineAngle;
        }
        return lineAngle;
    }
    
    public static double getGradient(double x1, double y1, double x2, double y2)
    {
        double gradient;
        if(x1 == x2)
        {
            gradient = 0;
        }
        else
        {
            gradient = ((y2-y1)/
                       (x2-x1));
        }
        return gradient;
    }
    
    public static double getGradient(Line2D line)
    {
        double gradient;
        if(Common.isVertical(line))
        {
            gradient = 0;
        }
        else
        {
            gradient =  (line.getY2()-line.getY1())/
                        (line.getX2()-line.getX1());
        }
        return gradient;
    }

    public static Point2D getIntersectionPoint(Line2D segment, Line2D wall)
    {
        double wallGradient = Common.getGradient(wall);
        double wallIntersect = wall.getY1()-wallGradient*wall.getX1();

        double intersectX;
        double intersectY;
        
        if(Common.isVertical(segment))
        {
            intersectX = segment.getX2();
            intersectY = wallGradient*intersectX+wallIntersect;
        }
        else
        {
            double rayGradient = Common.getGradient(segment.getX1(), segment.getY1(), segment.getX2(), segment.getY2());
            double rayIntersect = segment.getY2()-rayGradient*segment.getX2();

            if(Common.isVertical(wall))
            {
                intersectX = wall.getX1();
            }
            else
            {
                intersectX = (wallIntersect-rayIntersect)/
                             (rayGradient-wallGradient);
            }
            intersectY = rayGradient*intersectX+rayIntersect;
        }
        return new Point2D.Double(intersectX, intersectY);
    }
}
