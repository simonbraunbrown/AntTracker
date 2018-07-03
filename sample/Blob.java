package sample;


import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.core.Point;;
import java.util.ArrayList;


import static java.lang.Math.sqrt;

public class Blob {

    public Scalar colorID;

    private ArrayList<Point> locationHistory = new ArrayList<Point>();
    int activationTime = 12;



    public void addLocation (Point newLocation) {

        locationHistory.add(newLocation);
        activationTime = 12;
    }

    public double checkDistance (Point otherLocation) {
        Point pointA = otherLocation;
        Point pointB = locationHistory.get(locationHistory.size()-1);
        Point dist = new Point(pointA.x-pointB.x,pointA.y-pointB.y);
        double distance = sqrt(dist.x * dist.x + dist.y * dist.y);
        return distance;
    }

    public void update() {
        activationTime --;
    }

    public boolean isActive(){
        return activationTime > 0;
    }

    public void draw(Mat image){

        Scalar color;

        if(isActive()){
             color = colorID;
        }
        else {
            color = new Scalar(255,180,180);
        }

        Point previous = locationHistory.get(0);
        Point first = previous.clone();
        Point last = locationHistory.get(locationHistory.size()-1);

        for (Point l : locationHistory){

            Imgproc.line(image,previous,l,color,3, Imgproc.LINE_AA,0);
            previous = l;

        }
        //Imgproc.line(image,first,last, new Scalar(0,255,0),2); //Kine from start to end
    }
}
