package raytracer2;

import java.awt.geom.Point2D;

/**
 *
 * @author chanjustin
 */
public class Cell {
    
    private Point2D point; //top left point of cell
    private int width;
    private int height;
    private double rss;
    private double distFromRouter;
    private Point2D midpoint;
    
    public Cell(double rss)
    {
        this.rss = rss;
    }
    
    public Cell(Point2D point, int width, int height)
    {
        this.point = point;
        this.width = width;
        this.height = height;
        this.midpoint = new Point2D.Double(point.getX()+width/2,point.getY()+height/2);
    }

    public Point2D getPoint() {
        return point;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public double getRss() {
        return rss;
    }

    public void setRss(double rss) {
        this.rss = rss;
    }
    
    public double getDistFromRouter() {
        return distFromRouter;
    }

    public void setDistFromRouter(double distFromRouter) {
        this.distFromRouter = distFromRouter;
    }
    
    public Point2D getMidpoint() {
        return midpoint;
    }
}
