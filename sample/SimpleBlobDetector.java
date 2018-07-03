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

    Imgproc.findContours(input,contours,hirarchy,1,1); //find contours from the processed image and store them to the array contours


    for(int i =0; i < contours.size(); i++) {
        MatOfPoint points = contours.get(i);
        List<Point> pointList = points.toList(); // each pointlist represents a blob


        // find the center of the points from the point list
        double averageX = 0.0;
        double averageY = 0.0;

        if (pointList.size()> 50 && pointList.size() < 250) { // is the blob size in between store points from point list into a new point
            for (int j = 0; j < pointList.size(); j++) {
                Point point = pointList.get(j);
                averageX = averageX + point.x;
                averageY = averageY + point.y;

            }
            averageX = (int) averageX / pointList.size();
            averageY = (int) averageY / pointList.size();

            Point center = new Point(averageX,averageY); // average coordinates to a new point
            centers.add(center);                            //fill the array list
            System.out.println(center + "   actual centerpoint coordinates");
        }
    }

        return centers;



    }



}
