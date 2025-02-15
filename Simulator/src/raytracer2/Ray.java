package raytracer2;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.LinkedList;
/**
 * There are options to try either specular or diffuse reflection of a ray,
 * or diffraction of a ray. specularUpdate, diffuseUpdate, diffractionUpdate
 * are called by Simulator depending on the material the ray has intersected with
 * the reflective properties of materials is determined in Simulator.java in buildUI()
 * currently, all the walls have diffraction behavior.
 * @author chanjustin
 */
public class Ray {
    Line2D rootSegment;
    LinkedList<Ray> childSegments;
    double angle;
    double closestObsReflectionCoeff;
    double closestObsTransmissionCoeff;
    String type;
    Obstacle nearestObs;
    
    public Ray(double initX, double initY, double angle, int len, String type)
    {
        childSegments = new LinkedList<Ray>();
        this.angle = angle;
        rootSegment = completeRay(initX, initY, angle, len);
        nearestObs = closestObstacle(rootSegment);
        if(nearestObs != null)
        {
            try
            {
                Point2D pt = Common.getIntersectionPoint(rootSegment, nearestObs.getGeom());
                rootSegment.setLine(rootSegment.getX1(), rootSegment.getY1(), pt.getX(), pt.getY());
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }
        this.type = type;
    }
    
    public double segLength()
    {
        return Common.euclidean(rootSegment.getX1(), rootSegment.getY1(), 
                                rootSegment.getX2(), rootSegment.getY2());
    }
    
    public Line2D completeRay(double initX, double initY, double angle, int len)
    {
        Point2D.Double initPoint = 
                new Point2D.Double(initX, initY); 
        double finX = initX+len*Math.cos(angle);
        double finY = initY-len*Math.sin(angle);
        Point2D.Double finPoint = new Point2D.Double(finX,finY);
        return new Line2D.Double(initPoint, finPoint);
    }
    
    public Obstacle closestObstacle(Line2D segment)
    {
        Obstacle minWall = null;
        double minDist = Integer.MAX_VALUE;
       
        for(Obstacle obs : Common.objs)
        {
            Line2D wall = obs.getGeom();
            if(segment.intersectsLine(wall))
            {
                Point2D pt = Common.getIntersectionPoint(segment, wall);
                double dist = Common.euclidean(segment.getX1(), segment.getY1(), 
                              pt.getX(), pt.getY());
                if(dist < minDist)
                {
                    minWall = obs;
                    minDist = dist;
                }
            }
        }
        return minWall;
    }
        
    public Point2D setNextReflectionPoint(double minIntersectX, double minIntersectY,
                             double wallAngle, double prevRayAngle, 
                             Line2D minObs)
    {
        double initX = minIntersectX;
        double initY = minIntersectY;
        
        if(wallAngle == 0) //horizontal wall
        {
            if(0 < prevRayAngle && prevRayAngle < 180)
            {
                initY += Common.epsilon;
            }
            else if(180 < prevRayAngle && prevRayAngle < 360)
            {
                initY -= Common.epsilon;
            }
        }
        else if(Double.compare(wallAngle, (Math.PI/2)) == 0) //vertical wall
        {
            if(90 < prevRayAngle && prevRayAngle < 270)
            {
                initX += Common.epsilon;
            }
            else if(0 <= prevRayAngle && prevRayAngle < 90 ||
                    270 < prevRayAngle && prevRayAngle < 360)
            {
                initX -= Common.epsilon;
            }
        }
        else if(Common.isPositiveGradient(minObs))
        {
            if(0 < prevRayAngle && prevRayAngle < 90)
            {
                if(Math.toRadians(prevRayAngle) > wallAngle)
                {
                    initX += Common.epsilon;
                    initY -= Common.epsilon;
                }
                else
                {
                    initX -= Common.epsilon;
                    initY -= Common.epsilon;
                }
            }
            else if(90 < prevRayAngle && prevRayAngle < 180)
            {
                initX += Common.epsilon;
                initY += Common.epsilon;
            }
            else if(180 < prevRayAngle && prevRayAngle < 270)
            {
                if(Math.toRadians(prevRayAngle) > wallAngle+Math.PI)
                {
                    initX -= Common.epsilon;
                    initY -= Common.epsilon;
                }
                else
                {
                    initX += Common.epsilon;
                    initY += Common.epsilon;
                }
            }
            else if(270 < prevRayAngle && prevRayAngle < 360)
            {
                initX -= Common.epsilon;
                initY -= Common.epsilon;
            }
            else if(prevRayAngle == 90)
            {
                initY += Common.epsilon;
            }
            else if(prevRayAngle == 180)
            {
                initX += Common.epsilon;
            }
            else if(prevRayAngle == 270)
            {
                initY -= Common.epsilon;
            }
            else if(prevRayAngle == 0)
            {
                initX -= Common.epsilon;
            }
        }
        else if(!Common.isPositiveGradient(minObs))
        {
            if(0 < prevRayAngle && prevRayAngle < 90)
            {
                initX -= Common.epsilon;
                initY += Common.epsilon;
            }
            else if(90 < prevRayAngle && prevRayAngle < 180)
            {
                if(Math.toRadians(prevRayAngle) > wallAngle)
                {
                    initX += Common.epsilon;
                    initY -= Common.epsilon;
                }
                else
                {
                    initX -= Common.epsilon;
                    initY += Common.epsilon;
                }
            }
            else if(180 < prevRayAngle && prevRayAngle < 270)
            {
                initX += Common.epsilon;
                initY -= Common.epsilon;
            }
            else if(270 < prevRayAngle && prevRayAngle < 360)
            {
                if(Math.abs(Math.toRadians(prevRayAngle) - wallAngle) < Math.PI)
                {
                    //top
                    initX += Common.epsilon;
                    initY -= Common.epsilon;
                }
                else
                {
                    //bottom
                    initX -= Common.epsilon;
                    initY += Common.epsilon;
                }
            }
            else if(prevRayAngle == 90)
            {
                initY += Common.epsilon;
            }
            else if(prevRayAngle == 180)
            {
                initX += Common.epsilon;
            }
            else if(prevRayAngle == 270)
            {
                initY -= Common.epsilon;
            }
            else if(prevRayAngle == 0)
            {
                initX -= Common.epsilon;
            }
        }
        return new Point2D.Double(initX, initY);
    }
    
    public Point2D setNextTransmissionPoint(double minIntersectX, double minIntersectY,
                             double wallAngle, double prevRayAngle, 
                             Line2D minObs)
    {
        double initX = minIntersectX;
        double initY = minIntersectY;
        
        if(wallAngle == 0) //horizontal wall
        {
            if(0 < prevRayAngle && prevRayAngle < 180)
            {
                initY -= Common.epsilon;
            }
            else if(180 < prevRayAngle && prevRayAngle < 360)
            {
                initY += Common.epsilon;
            }
        }
        else if(Double.compare(wallAngle, (Math.PI/2)) == 0) //vertical wall
        {
            if(90 < prevRayAngle && prevRayAngle < 270)
            {
                initX -= Common.epsilon;
            }
            else if(0 <= prevRayAngle && prevRayAngle < 90 ||
                    270 < prevRayAngle && prevRayAngle < 360)
            {
                initX += Common.epsilon;
            }
        }
        else if(Common.isPositiveGradient(minObs))
        {
            if(0 < prevRayAngle && prevRayAngle < 90)
            {
                if(Math.toRadians(prevRayAngle) > wallAngle)
                {
                    initX -= Common.epsilon;
                    initY += Common.epsilon;
                }
                else
                {
                    initX += Common.epsilon;
                    initY += Common.epsilon;
                }
            }
            else if(90 < prevRayAngle && prevRayAngle < 180)
            {
                initX -= Common.epsilon;
                initY -= Common.epsilon;
            }
            else if(180 < prevRayAngle && prevRayAngle < 270)
            {
                if(Math.toRadians(prevRayAngle) > wallAngle+Math.PI)
                {
                    initX += Common.epsilon;
                    initY += Common.epsilon;
                }
                else
                {
                    initX -= Common.epsilon;
                    initY -= Common.epsilon;
                }
            }
            else if(270 < prevRayAngle && prevRayAngle < 360)
            {
                initX += Common.epsilon;
                initY += Common.epsilon;
            }
            else if(prevRayAngle == 90)
            {
                initY -= Common.epsilon;
            }
            else if(prevRayAngle == 180)
            {
                initX -= Common.epsilon;
            }
            else if(prevRayAngle == 270)
            {
                initY += Common.epsilon;
            }
            else if(prevRayAngle == 0)
            {
                initX += Common.epsilon;
            }
        }
        else if(!Common.isPositiveGradient(minObs))
        {
            if(0 < prevRayAngle && prevRayAngle < 90)
            {
                initX += Common.epsilon;
                initY -= Common.epsilon;
            }
            else if(90 < prevRayAngle && prevRayAngle < 180)
            {
                if(Math.toRadians(prevRayAngle) > wallAngle)
                {
                    initX -= Common.epsilon;
                    initY += Common.epsilon;
                }
                else
                {
                    initX += Common.epsilon;
                    initY -= Common.epsilon;
                }
            }
            else if(180 < prevRayAngle && prevRayAngle < 270)
            {
                initX -= Common.epsilon;
                initY += Common.epsilon;
            }
            else if(270 < prevRayAngle && prevRayAngle < 360)
            {
                if(Math.abs(Math.toRadians(prevRayAngle) - wallAngle) < Math.PI)
                {
                    //top
                    initX -= Common.epsilon;
                    initY += Common.epsilon;
                }
                else
                {
                    //bottom
                    initX += Common.epsilon;
                    initY -= Common.epsilon;
                }
            }
            else if(prevRayAngle == 90)
            {
                initY -= Common.epsilon;
            }
            else if(prevRayAngle == 180)
            {
                initX -= Common.epsilon;
            }
            else if(prevRayAngle == 270)
            {
                initY += Common.epsilon;
            }
            else if(prevRayAngle == 0)
            {
                initX += Common.epsilon;
            }
        }
        return new Point2D.Double(initX, initY);
    }
    
    public void specularUpdate(Obstacle minObs, Line2D segment)
    {
        double wallAngle = Common.getAngle(minObs.getGeom());
        double prevRayAngle = Math.toDegrees(angle);

        //LAW OF REFLECTION
        Point2D pt = setNextReflectionPoint(segment.getX2(), segment.getY2(),
                     wallAngle, prevRayAngle, 
                     minObs.getGeom());
        
        double refAngle = (2*wallAngle - angle)%(Math.PI*2);
        //reflected segment
        childSegments.add(new Ray(pt.getX(), pt.getY(), refAngle, Common.RAY_LEN, Common.REFLECTED));
        
        pt = setNextTransmissionPoint(segment.getX2(), segment.getY2(),
                     wallAngle, prevRayAngle, 
                     minObs.getGeom());

        if(minObs.isTransmissionable())
        {
            //transmitted segment
            childSegments.add(new Ray(pt.getX(), pt.getY(), angle, Common.RAY_LEN, Common.TRANSMITTED));
        }
    }
    
    public void diffuseUpdate(Obstacle minObs)
    {
        double wallAngle = Common.getAngle(minObs.getGeom());
        double prevRayAngle = Math.toDegrees(angle);

        Point2D pt = setNextReflectionPoint(rootSegment.getX2(), rootSegment.getY2(),
                     wallAngle, prevRayAngle, 
                     minObs.getGeom());
        
        //SPECULAR REFLECTION
        double refAngle = (2*wallAngle - angle)%(Math.PI*2);
        double refAngDeg = Math.toDegrees(refAngle);
        //reflected segment
        childSegments.add(new Ray(pt.getX(), pt.getY(), refAngle, Common.RAY_LEN, Common.REFLECTED));
        
        //DIFFUSE
        double increment = 180/Common.DIFFUSE_RAYS; 
        for(int i = 0; i < Common.DIFFUSE_RAYS; i++)
        {
            double ang = Math.toDegrees(wallAngle);
            if(ang == 0)
            {
                if(prevRayAngle < 90)
                {
                    if(refAngDeg > 180)
                    {
                        ang += 180+i*increment;
                    }
                    else if(refAngDeg < 180)
                    {
                        ang -= i*increment;
                    }
                }
                else
                {
                    if(refAngDeg > 180)
                    {
                        ang += 180+i*increment;
                    }
                    else if(refAngDeg < 180)
                    {
                        ang += i*increment;
                    }
                }
            }
            else if(ang == 90)
            {
                if(90 < prevRayAngle && prevRayAngle < 180)
                {
                    if(refAngDeg > 180)
                    {
                        ang -= 180+i*increment;
                    }
                    else if(refAngDeg < 180)
                    {
                        ang -= i*increment;
                    }
                }
                else if(180 < prevRayAngle && prevRayAngle < 270)
                {
                    if(refAngDeg > 180)
                    {
                        ang += 180+i*increment;
                    }
                    else if(refAngDeg < 180)
                    {
                        ang -= i*increment;
                    }
                }
                else
                {
                    if(refAngDeg > 180)
                    {
                        ang -= 180+i*increment;
                    }
                    else if(refAngDeg < 180)
                    {
                        ang += i*increment;
                    }
                }
            }
            if(ang % 90 != 0)
            {
                childSegments.add(new Ray(pt.getX(), pt.getY(), Math.toRadians(ang), Common.RAY_LEN, Common.REFLECTED));
            }
        }
    }
    
    public void diffractionUpdate(Obstacle minObs)
    {
        double wallAngle = Common.getAngle(minObs.getGeom());
        double wallAngleDeg = Math.toDegrees(wallAngle);
        double prevRayAngle = Math.toDegrees(angle);

        Point2D pt = setNextReflectionPoint(rootSegment.getX2(), rootSegment.getY2(),
                     wallAngle, prevRayAngle, 
                     minObs.getGeom());
        
        //SPECULAR REFLECTION
        double refAngle = (2*wallAngle - angle)%(Math.PI*2);
        double refAngDeg = Math.toDegrees(refAngle);
        
        //reflected segment
        childSegments.add(new Ray(pt.getX(), pt.getY(), refAngle, Common.RAY_LEN, Common.REFLECTED));
        
        //DIFFUSE
        double increment = 0; 
        for(int i = 0; i < Common.DIFFUSE_RAYS; i++)
        {
            double ang = 0;
            
            if(wallAngleDeg == 0)
            {
                if(prevRayAngle < 90)
                {
                    if(refAngDeg > 180)
                    {
                        increment = (360-refAngDeg)/Common.DIFFUSE_RAYS;
                        ang = refAngDeg+i*increment;
                    }
                    else if(refAngDeg < 180)
                    {
                        ang -= i*increment;
                    }
                }
                else if(270 < prevRayAngle && prevRayAngle < 360)
                {
                    if(refAngDeg > 180)
                    {
                        increment = (360-refAngDeg)/Common.DIFFUSE_RAYS;
                        ang = refAngDeg-i*increment;
                    }
                    else if(refAngDeg < 180)
                    {
                        increment = (refAngDeg)/Common.DIFFUSE_RAYS;
                        ang = refAngDeg+i*increment;
                    }
                }
                else
                {
                    if(refAngDeg > 180)
                    {
                        increment = (360-refAngDeg)/Common.DIFFUSE_RAYS;
                        ang = refAngDeg-i*increment;
                    }
                    else if(refAngDeg < 180)
                    {
                        increment = (180-refAngDeg)/Common.DIFFUSE_RAYS;
                        ang = refAngDeg+i*increment;
                    }
                }
            }
            else if(wallAngleDeg == 90)
            {
                if(90 < prevRayAngle && prevRayAngle < 180)
                {
                    if(refAngDeg > 180)
                    {
                        ang -= 180+i*increment;
                    }
                    else if(refAngDeg < 180)
                    {
                        increment = (90-refAngDeg)/Common.DIFFUSE_RAYS;
                        ang = refAngDeg+i*increment;
                    }
                }
                else if(180 < prevRayAngle && prevRayAngle < 270)
                {
                    if(refAngDeg > 180)
                    {
                        increment = Math.abs(180-refAngDeg)/Common.DIFFUSE_RAYS;
                        ang += refAngDeg-i*increment;
                    }
                    else if(refAngDeg < 180)
                    {
                        ang -= i*increment;
                    }
                }
                else
                {
                    if(refAngDeg > 180)
                    {
                        increment = Math.abs(180-refAngDeg)/Common.DIFFUSE_RAYS;
                        ang = refAngDeg+i*increment;
                    }
                    else if(refAngDeg < 180)
                    {
                        increment = Math.abs(refAngDeg-wallAngleDeg)/Common.DIFFUSE_RAYS;
                        ang = refAngDeg-i*increment;
                    }
                }
            }
            if(ang % 90 != 0)
            {
                childSegments.add(new Ray(pt.getX(), pt.getY(), Math.toRadians(ang), Common.RAY_LEN, Common.DIFFRACTED));
            }
        }
    }
}

