package sample;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;

import java.util.*;

public class SimpleBlobDetector {

    public ArrayList<Point> detectCenterPoints (Mat input) {
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        ArrayList<Point> centers = new ArrayList<Point>();
        Mat hirarchy = new Mat();

    Imgproc.findContours(input,contours,hirarchy,1,1);


    for(int i =0; i < contours.size(); i++) {
        MatOfPoint points = contours.get(i);
        List<Point> pointList = points.toList();

        double avarageX = 0.0;
        double avarageY = 0.0;

        if (pointList.size()> 50 && pointList.size() < 250) {
            for (int j = 0; j < pointList.size(); j++) {
                Point point = pointList.get(j);
                avarageX = avarageX + point.x;
                avarageY = avarageY + point.y;

            }
            avarageX = avarageX / pointList.size();
            avarageY = avarageY / pointList.size();

            Point center = new Point(avarageX, avarageY);
            centers.add(center);
            //System.out.println(center);
        }
    }

        return centers;



    }



}
