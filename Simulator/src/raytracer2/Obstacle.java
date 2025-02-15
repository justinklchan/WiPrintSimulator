package raytracer2;

import java.awt.geom.Line2D;

/**
 *
 * @author chanjustin
 */
public class Obstacle {
    private Line2D geom;
    private String reflectance;
    private boolean transmissionable;
    
    public Obstacle(Line2D geom, String reflectance, boolean transmissionable)
    {
        this.geom = (Line2D)geom.clone();
        this.reflectance = reflectance;
        this.transmissionable = transmissionable;
    }
    
    public Line2D getGeom()
    {
        return geom;
    }
    
    public String getReflectance()
    {
        return reflectance;
    }
    
    public boolean isTransmissionable()
    {
        return transmissionable;
    }
    
    public Obstacle clone()
    {
        Obstacle obs = new Obstacle((Line2D)geom.clone(), new String(reflectance), transmissionable);
        return obs;
    }
}
