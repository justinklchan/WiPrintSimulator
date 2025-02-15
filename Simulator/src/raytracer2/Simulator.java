package raytracer2;

import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.Random;
import java.util.Scanner;
import javafx.stage.WindowEvent;
import javax.imageio.ImageIO;
import javax.swing.JApplet;
import static javax.swing.JFrame.EXIT_ON_CLOSE;

/**
 *
 * @author chanjustin
 */
public class Simulator extends JApplet {
    //CONSTANTS
    private Random random;
    private LinkedList<Obstacle> walls;
    private static SimulatorFrame frame;
    private int MAX_LINES = 100;
    
    private String mode = "INTERACTIVE";
//    private String mode = "TEST";
    
    /**
     * Initialization method that will be called after the applet is loaded into
     * the browser.
     */
    @Override
    public void init() 
    {
        buildUI();
    }

    // TODO overwrite start(), stop() and destroy() methods
    static int BAD = 0;
    static int IGNORE = 1;
    static int GOOD = 2;
    int[][] desired = new int[18][18];
    
    /**
     * This discretizes the low-resolution signal map (as defined as an image)
     * into a set of cells. Each cell is marked as BAD, IGNORE or GOOD depending
     * if that region of the image is black, white or red respectively.
     * 
     * The printout of this method is to be placed in the 'coarse' text file.
     * This is a pre-processing step you can call once in buildUI().
     * 
     */
    public void imageToMap()
    {
        BufferedImage image = null;
        try
        {
            image = ImageIO.read(new File("src/coarse_map.jpg"));
            int w = image.getWidth();
            int h = image.getHeight();
            double xInc = w/18.0;
            double yInc = h/18.0;
            System.out.println(w+","+h);
            for(int i = 0; i < 18; i++)
            {
                for(int j = 0; j < 18; j++)
                {
                    Color c = new Color(image.getRGB((int)(xInc*i),(int)(yInc*j)));
                    if(c.equals(Color.black))
                    {
                        desired[j][i] = BAD;
                    }
                    else if(c.equals(Color.white))
                    {
                        desired[j][i] = IGNORE;
                    }
                    else
                    {
                        desired[j][i] = GOOD;
                    }
                }
            }
            for(int i = 0; i < 18; i++)
            {
                for(int j = 0; j < 18; j++)
                {
                    System.out.print(desired[i][j]+" ");
                }
                System.out.println();
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        System.out.println("========================");
    }
    
    public void buildUI()
    {
//        imageToMap();
        
        random = new Random();
        walls = new LinkedList<Obstacle>();
        double roomLengthInMeters = 0;
        double roomWidthInMeters = 0;
        int roomLengthInPixels = 0;
        int roomWidthInPixels = 0;
        
        Scanner input = null;
        try
        {
            input = new Scanner(new BufferedReader(new FileReader("src/raytracer2/walls.dat")));
            String dimensions[] = input.nextLine().split(",");
            roomLengthInMeters = Double.parseDouble(dimensions[0]);
            roomWidthInMeters = Double.parseDouble(dimensions[1]);
            
            String wallReflectance = Common.SPECULAR;
            
            double ratio = roomLengthInMeters/roomWidthInMeters;
            roomWidthInPixels = 500;
            roomLengthInPixels = (int)(roomWidthInPixels*ratio);
            
            //the 4 walls around the space
            walls.add(new Obstacle(new Line2D.Double(0,0,roomLengthInPixels,0), wallReflectance,false));
            walls.add(new Obstacle(new Line2D.Double(0,0,0,roomWidthInPixels), wallReflectance,false));
            walls.add(new Obstacle(new Line2D.Double(0,roomWidthInPixels,roomLengthInPixels,roomWidthInPixels), wallReflectance,false));
            walls.add(new Obstacle(new Line2D.Double(roomLengthInPixels,0,roomLengthInPixels,roomWidthInPixels), wallReflectance,false));
            
            //these coordinates were determined manually from src/map.jpg
            //it is a computer vision problem to generate the lines from the image
            walls.add(new Obstacle(new Line2D.Double(19,58,229,58), wallReflectance,true));
            walls.add(new Obstacle(new Line2D.Double(229,58,229,94), wallReflectance,true));
            walls.add(new Obstacle(new Line2D.Double(229,94,326,94), wallReflectance,true));
            walls.add(new Obstacle(new Line2D.Double(326,94,326,57), wallReflectance,true));
            walls.add(new Obstacle(new Line2D.Double(326,57,354,57), wallReflectance,true));
            walls.add(new Obstacle(new Line2D.Double(354,57,354,177), wallReflectance,true));
            walls.add(new Obstacle(new Line2D.Double(354,177,369,177), wallReflectance,true));
            walls.add(new Obstacle(new Line2D.Double(369,177,369,167), wallReflectance,true));
            walls.add(new Obstacle(new Line2D.Double(369,167,400,167), wallReflectance,true));
            walls.add(new Obstacle(new Line2D.Double(400,167,400,157), wallReflectance,true));
            walls.add(new Obstacle(new Line2D.Double(400,157,369,157), wallReflectance,true));
            walls.add(new Obstacle(new Line2D.Double(369,157,369,27), wallReflectance,true));
            walls.add(new Obstacle(new Line2D.Double(369,27,499,27), wallReflectance,true));
            walls.add(new Obstacle(new Line2D.Double(499,27,499,155), wallReflectance,true));
            walls.add(new Obstacle(new Line2D.Double(499,155,451,155), wallReflectance,true));
            walls.add(new Obstacle(new Line2D.Double(451,155,451,164), wallReflectance,true));
            walls.add(new Obstacle(new Line2D.Double(451,164,513,164), wallReflectance,true));
            walls.add(new Obstacle(new Line2D.Double(513,164,513,28), wallReflectance,true));
            walls.add(new Obstacle(new Line2D.Double(513,28,594,28), wallReflectance,true));
            walls.add(new Obstacle(new Line2D.Double(594,28,594,66), wallReflectance,true));
            walls.add(new Obstacle(new Line2D.Double(594,66,715,66), wallReflectance,true));
            walls.add(new Obstacle(new Line2D.Double(715,66,715,26), wallReflectance,true));
            walls.add(new Obstacle(new Line2D.Double(715,26,825,26), wallReflectance,true));
            walls.add(new Obstacle(new Line2D.Double(825,26,825,77), wallReflectance,true));
            walls.add(new Obstacle(new Line2D.Double(825,77,840,77), wallReflectance,true));
            walls.add(new Obstacle(new Line2D.Double(840,77,840,150), wallReflectance,true));
            walls.add(new Obstacle(new Line2D.Double(840,150,826,150), wallReflectance,true));
            walls.add(new Obstacle(new Line2D.Double(826,150,826,196), wallReflectance,true));
            walls.add(new Obstacle(new Line2D.Double(826,196,840,196), wallReflectance,true));
            walls.add(new Obstacle(new Line2D.Double(840,196,840,257), wallReflectance,true));
            walls.add(new Obstacle(new Line2D.Double(840,257,828,257), wallReflectance,true));
            walls.add(new Obstacle(new Line2D.Double(828,257,828,300), wallReflectance,true));
            walls.add(new Obstacle(new Line2D.Double(828,300,815,300), wallReflectance,true));
            walls.add(new Obstacle(new Line2D.Double(815,300,815,325), wallReflectance,true));
            walls.add(new Obstacle(new Line2D.Double(815,325,834,325), wallReflectance,true));
            walls.add(new Obstacle(new Line2D.Double(834,325,834,449), wallReflectance,true));
            walls.add(new Obstacle(new Line2D.Double(834,449,668,449), wallReflectance,true));
            walls.add(new Obstacle(new Line2D.Double(668,449,668,348), wallReflectance,true));
            walls.add(new Obstacle(new Line2D.Double(668,348,761,348), wallReflectance,true));
            walls.add(new Obstacle(new Line2D.Double(761,348,761,316), wallReflectance,true));
            walls.add(new Obstacle(new Line2D.Double(761,316,662,316), wallReflectance,true));
            walls.add(new Obstacle(new Line2D.Double(662,316,662,450), wallReflectance,true));
            walls.add(new Obstacle(new Line2D.Double(662,450,370,450), wallReflectance,true));
            walls.add(new Obstacle(new Line2D.Double(370,450,370,318), wallReflectance,true));
            walls.add(new Obstacle(new Line2D.Double(370,318,389,318), wallReflectance,true));
            walls.add(new Obstacle(new Line2D.Double(389,318,389,306), wallReflectance,true));
            walls.add(new Obstacle(new Line2D.Double(389,306,361,306), wallReflectance,true));
            walls.add(new Obstacle(new Line2D.Double(361,306,361,445), wallReflectance,true));
            walls.add(new Obstacle(new Line2D.Double(361,445,185,445), wallReflectance,true));
            walls.add(new Obstacle(new Line2D.Double(185,445,106,364), wallReflectance,true));
            walls.add(new Obstacle(new Line2D.Double(106,364,76,364), wallReflectance,true));
            walls.add(new Obstacle(new Line2D.Double(76,364,76,387), wallReflectance,true));
            walls.add(new Obstacle(new Line2D.Double(76,387,59,387), wallReflectance,true));
            walls.add(new Obstacle(new Line2D.Double(59,387,59,446), wallReflectance,true));
            walls.add(new Obstacle(new Line2D.Double(59,446,16,446), wallReflectance,true));
            walls.add(new Obstacle(new Line2D.Double(16,446,16,256), wallReflectance,true));
            walls.add(new Obstacle(new Line2D.Double(16,256,78,256), wallReflectance,true));
            walls.add(new Obstacle(new Line2D.Double(78,256,78,271), wallReflectance,true));
            walls.add(new Obstacle(new Line2D.Double(78,271,104,271), wallReflectance,true));
            walls.add(new Obstacle(new Line2D.Double(104,271,104,252), wallReflectance,true));
            walls.add(new Obstacle(new Line2D.Double(104,252,354,252), wallReflectance,true));
            walls.add(new Obstacle(new Line2D.Double(354,252,354,270), wallReflectance,true));
            walls.add(new Obstacle(new Line2D.Double(354,270,371,269), wallReflectance,true));
            walls.add(new Obstacle(new Line2D.Double(371,269,371,231), wallReflectance,true));
            walls.add(new Obstacle(new Line2D.Double(371,231,351,231), wallReflectance,true));
            walls.add(new Obstacle(new Line2D.Double(351,231,351,241), wallReflectance,true));
            walls.add(new Obstacle(new Line2D.Double(351,241,19,241), wallReflectance,true));
            walls.add(new Obstacle(new Line2D.Double(19,241,19,58), wallReflectance,true));
                        
            //walls can be defined in the walls.dat file as well
            while(input.hasNext())
            {
                String text = input.nextLine().trim();
                if(!text.startsWith("/") && text.length() > 0)
                {
                    String[] line = text.split(",");
                    walls.add(new Obstacle(new Line2D.Double(
                            Integer.parseInt(line[0]),
                            Integer.parseInt(line[1]),
                            Integer.parseInt(line[2]),
                            Integer.parseInt(line[3])),
                            Common.SPECULAR,true));
                }
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if(input != null)
            {
                input.close();
            }
        }
        
        //ADD REFLECTORS, REPRESENTED AS A CURVE
        LinkedList<LinkedList<Point2D>> controlPoints = new LinkedList<LinkedList<Point2D>>();
        LinkedList<LinkedList<Obstacle>> obs = new LinkedList<LinkedList<Obstacle>>();
        String reflectorMode = "none";
        try
        {
            //ADDING REFLECTORS
            input = new Scanner(new BufferedReader(new FileReader("src/raytracer2/curves.dat")));
            while(input.hasNext())
            {
                String line = input.nextLine();
                if(line.length() > 0 && !line.startsWith("/"))
                {
                    if(line.equals("polypoints"))
                    {
                        reflectorMode = line;
                        int num_points = Integer.parseInt(input.nextLine());
                        LinkedList<Point2D>controlPointSet = new LinkedList<Point2D>();
                        for(int i = 0; i < num_points; i++)
                        {
                            String text = input.nextLine();
                            String[] coords = text.split(",");
                            controlPointSet.add(new Point2D.Double(Integer.parseInt(coords[0]),Integer.parseInt(coords[1])));
                        }
                        controlPoints.add(controlPointSet);
                    }
                    else if(line.equals("lines"))
                    {
                        reflectorMode = line;
                        int num_points = Integer.parseInt(input.nextLine());
                        LinkedList<Point2D>controlPointSet = new LinkedList<Point2D>();
                        for(int i = 0; i < num_points; i++)
                        {
                            String text = input.nextLine();
                            String[] coords = text.split(",");
                            controlPointSet.add(new Point2D.Double(Integer.parseInt(coords[0]),Integer.parseInt(coords[1])));
                        }
                        LinkedList<Obstacle> obstacle = new LinkedList<Obstacle>();
                        for(int i = 0; i < num_points-1; i++)
                        {
                            Obstacle o =new Obstacle(new Line2D.Double(controlPointSet.get(i),controlPointSet.get(i+1)),Common.SPECULAR,true);
                            obstacle.add(o);
                        }
                        obs.add(obstacle);
//                        controlPoints.add(controlPointSet);
                    }
                    else if(line.equals("polynomial"))
                    {
                        reflectorMode = line;
                        int startX = Integer.parseInt(input.nextLine());
                        int endX = Integer.parseInt(input.nextLine());
                        String[] strings = input.nextLine().split(",");
                        double[] coeffs = new double[strings.length];
                        for(int i = 0; i < strings.length; i++)
                        {
                            coeffs[i] = Double.parseDouble(strings[i]);
                        }
                        LinkedList<Obstacle> ob = new LinkedList<Obstacle>();
                        
                        double y = -interpolate(startX,coeffs);
                        Point2D start = new Point2D.Double(startX,y);
                        for(int x = startX+1; x <= endX; x++)
                        {
                            Point2D end = new Point2D.Double(x,-interpolate(x,coeffs));
                            Obstacle o = new Obstacle(new Line2D.Double(start,end),Common.SPECULAR,true);
                            ob.add(o);
                            start = end;
                        }
                        obs.add(ob);
                    }
                }
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if(input != null)
            {
                input.close();
            }
        }
        
        if(mode.equals("INTERACTIVE"))
        {
            if(reflectorMode.equals("polypoints"))
            {
                LinkedList<Point2D> ref = controlPoints.getFirst();
                frame = new SimulatorFrame(walls, createCurve(ref), roomLengthInPixels, roomWidthInPixels, false, null);
            }
            else if(reflectorMode.equals("polygon") || reflectorMode.equals("lines"))
            {
                frame = new SimulatorFrame(walls, obs.getFirst(), roomLengthInPixels, roomWidthInPixels, false, null);
            }
            else if(reflectorMode.equals("none"))
            {
                frame = new SimulatorFrame(walls, new LinkedList<Obstacle>(), roomLengthInPixels, roomWidthInPixels, false, null);
            }
        }
        else if(mode.equals("TEST"))
        {
            frame = new SimulatorFrame(walls, null, roomLengthInPixels, roomWidthInPixels, true, null);
            simulatedAnnealing();
        }
    }

    public void simulatedAnnealing()
    {
        double Tmax = 1000;
        double Tmin = 0.01;
        double T = Tmax;
        double coolingRate = 0.99;
        int its = 0;
        
        String ftype = "";
        LinkedList<Point2D> fcurves = new LinkedList<Point2D>();
        int fsides=0;
        double flen=0;

        LinkedList<Point2D> curves = new LinkedList<Point2D>();
        int n = 10;
        for(int i = 0; i < n; i++)
        {
            curves.add(new Point2D.Double((50/(n-1))*i,-10));
        }
        LinkedList<Obstacle> r = createCurve(curves);
        
        frame.runTrial(walls, r, true, true, false, "src/raytracer2/coarse", 
                new Point2D[]{new Point2D.Double(425, 250),new Point2D.Double(445, 250)});
        double f = frame.ERROR;
        double f1 = frame.ERROR1;
        double f2 = frame.ERROR2;
        double fp=0;
        double f1p=0;
        double f2p=0;
        double alpha = 1.05;
        while(T > Tmin)
        {
            //MUTATE
            int type;
            LinkedList<Obstacle> rp = new LinkedList<Obstacle>();
            LinkedList<Point2D> curvesp = null;
            int sides=0;
            double len = 0;
            
            if(random.nextDouble() < 1)
            {
                for(int i = 0; i < n; i++)
                {
                    int offset = 40;
                    int a = random.nextInt(offset);
                    double x = curves.get(i).getX();
                    double y = curves.get(i).getY();
                    if(random.nextInt(2)==0 && y > -offset)
                    {
                        a = -a;
                    }
                    if(y > offset)
                    {
                        a = -Math.abs(a);
                    }
                    y += a;
                    curves.set(i, new Point2D.Double(x,y));
                }
                for(int i = 0; i < n-1; i++)
                {
                    rp.add(new Obstacle(new Line2D.Double(curves.get(i),curves.get(i+1)),Common.SPECULAR,true));
                }
//                rp = createCurve(curvesp);
                type=0;
            }
            else
            {
                sides = 3+random.nextInt(10);
                int increment = 360/sides;
                double initX=-5;
                double initY=5;
                len = 5+random.nextInt(20);
                Point2D start = new Point2D.Double(initX,initY);
                for(int i = 0; i < sides; i++)
                {
                    int angle = increment*i;
                    double finX = initX+len*Math.cos(Math.toRadians(angle));
                    double finY = initY-len*Math.sin(Math.toRadians(angle));
                    Point2D end = new Point2D.Double(finX,finY);
                    Obstacle o = new Obstacle(new Line2D.Double(start,end),Common.SPECULAR,true);
                    rp.add(o);
                    start = end;
                    initX = end.getX();
                    initY = end.getY();
                }
                type=1;
            }
            
            double w1 = random.nextDouble();
            double w2 = random.nextDouble();
            if(fp >= f || T == 1000)
            {
                double sum = w1+w2;
                w1 /= sum;
                w2 /= sum;
                frame.w1=w1;
                frame.w2=w2;
            }
            else
            {
                if(f1 >= f1p)
                {
                    w1 = alpha*frame.w1;
                }
                else
                {
                    w1 = frame.w1/alpha;
                }
                if(f2 >= f2p)
                {
                    w2 = alpha*frame.w2;
                }
                else
                {
                    w2 = alpha*frame.w2;
                }
                double sum = w1+w2;
                frame.w1 = w1/sum;
                frame.w2 = w2/sum;
            }
            frame.runTrial(walls, rp, true, true, false, "src/raytracer2/coarse", 
                    new Point2D[]{new Point2D.Double(425, 250),new Point2D.Double(445, 250)});
            fp = frame.ERROR;
            f1p = frame.ERROR1;
            f2p = frame.ERROR2;
            
            double prob=0;
            if(fp >= f)
            {
                prob = 1;
            }
            else
            {
//                prob = Math.exp((fp-f)/T);
                double m1 = w1*(f1p-f1)/T;
                double m2 = w2*(f2p-f2)/T;
//                double m3 = Math.max(m1,m2);
                double m3 = m1+m2;
                double temp = Math.exp(m3);
                prob = Math.min(1,temp);
            }
            
            if(random.nextDouble() < prob)
            {
                f = fp;
                f1 = f1p;
                f2 = f2p;
                r = rp;
                if(type==0)
                {
                    ftype="func";
                    fcurves = curves;
                }
                else
                {
                    ftype="func";
                    fsides=sides;
                    flen = len;
//                    fcurves = curvesp;
                }
//                System.out.println(fcurves.size());
            }
            System.out.println(f + " " + f1 + " " + f2 + " " + T);
            T *= coolingRate;
            its += 1;
        }
//        System.out.println("done "+f);
        if(ftype.equals("func"))
        {
            for(Point2D pt : fcurves)
            {
                System.out.println((int)pt.getX()+","+(int)pt.getY());
            }
        }
        System.out.println("Weights " +frame.w1+","+frame.w2);
    }
    
    //lagrangian interpolation
    private double lagrangeInterpolate(double target, int x[], int y[])
    {
        double result = 0;
        for(int i = 0; i < x.length; i++)
        {
            double weight = 1;
            for(int j = 0; j < x.length; j++)
            {
                if(j != i)
                {
                    weight *= (target-x[j])/(x[i]-x[j]);
                }
            }
            result += weight*y[i];
        }
        return result;
    }
    
    //simple interpolation, y for an x
    private double interpolate(double target, double eq[])
    {
        double result = 0;
        double weight = eq.length-1;
        for(int i = 0; i < eq.length-1; i++)
        {
            result += Math.pow(target, weight)*eq[i];
            weight -= 1;
        }
        result += eq[eq.length-1];
        return result;
    }
    
    private LinkedList<Obstacle> createCurve(LinkedList<Point2D> pts)
    {
        int[] x = new int[pts.size()];
        int[] y = new int[pts.size()];
        int i = 0;
        for(Point2D pt : pts)
        {
            x[i] = (int)pt.getX();
            y[i] = (int)pt.getY();
            i += 1;
        }
        LinkedList<Obstacle> curve = createCurve(x,y);
        return curve;
    }
    
    private LinkedList<Obstacle> createCurve(int[] x, int[] y)
    {
        LinkedList<Obstacle> curve = new LinkedList<Obstacle>();
        
        double increment = (double)(x[x.length-1]-x[0])/MAX_LINES;
        
        double point = x[0];
            for(int i = 0; i < MAX_LINES; i++)
        {
            Obstacle obs = new Obstacle(
                      new Line2D.Double(point, 
                      lagrangeInterpolate(point, x, y), 
                      point+increment, 
                      lagrangeInterpolate(point+increment, x, y)), 
                      Common.SPECULAR,true);
            curve.add(obs);
            point += increment;
        }
        
        return curve;
    }
    
    public static void main(String args[])
    {
        Simulator simulator = new Simulator();
        simulator.buildUI();
        frame.addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent e) {
            System.exit(0);
            }
        });
        
        frame.setDefaultCloseOperation(EXIT_ON_CLOSE);                                           
        frame.setVisible(true);
    }
    private DecimalFormat df = new DecimalFormat("0.##");
}
