package sample;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import sample.Converter;
import javafx.event.ActionEvent;
import sample.ImageProcessing;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import static org.opencv.core.CvType.*;


public class Controller {

    private ImageProcessing imageProcessor = new ImageProcessing();
    private SimpleBlobDetector detector = new SimpleBlobDetector();

    // FXML Buttons
    @FXML
    private Button startbtn;

    // FXML Sliders
    @FXML
    private Slider sliderBlur;
    @FXML
    private Slider sliderThresh;

    // FXML area showing the current Frame
    @FXML
    private ImageView canvas;
    @FXML
    private ImageView canvas1;
    @FXML
    private  ImageView canvas2;

    // FXML Label
    @FXML
    private Label labelSliderBlur;
    @FXML
    private Label labelSliderThresh;

    // FXML TextBox
    @FXML
    private TextField blurValue;
    @FXML
    private TextField threshValue;




    private ScheduledExecutorService timer;

    private VideoCapture videoStream;

    private boolean cameraActive = false;

    private static int cameraId = 0;

    private static String AntVideo = "src/sampleants.mp4";


    private boolean frameGrabbed = false;

    private DecimalFormat df = new DecimalFormat("#.#");

    private List<Point>  pointsToDraw = new ArrayList<Point>();

    private  ArrayList<Blob> blobs = new ArrayList<Blob>();






    protected void init() {

        this.videoStream = new VideoCapture();

        sliderBlur.setMin(1);
        sliderBlur.setMax(100);
        sliderBlur.setValue(10);

        sliderThresh.setMin(1);
        sliderThresh.setMax(255);
        sliderThresh.setValue(100);



    }


    @FXML
    protected void startStream(javafx.event.ActionEvent actionEvent) {

        if (!this.cameraActive) {

            //start captureing
            this.videoStream.open("src/ants.mov");
            //this.videoStream.open(cameraId);


            if (this.videoStream.isOpened()) {

                this.cameraActive = true;


                //grab a Frame every 33 ms
                Runnable frameGrabber = new Runnable() {
                    @Override
                    public void run() {

                        //grab a single frame
                        Mat frame = grabFrame();
                        Mat drawLines = new Mat(frame.rows(),frame.cols(),CV_8UC3, new Scalar(0,0,0));

                        if (!frameGrabbed) {
                            frameGrabbed = true;
                        }


                       //Mat bluredframe = imageProcessor.Blur(frame, sliderBlur.getValue());
                       //Mat threshedframe = imageProcessor.Thresh(bluredframe, sliderThresh.getValue());
                       Mat subtractedBackground = imageProcessor.backroundSubtraction(frame,sliderBlur.getValue(),sliderThresh.getValue());

                       // draw center Points
                        ArrayList<Point> centerPoints =  detector.detectCenterPoints(subtractedBackground);

                        for (int i = 0; i < centerPoints.size(); i++ ) {

                            Imgproc.circle(drawLines,centerPoints.get(i),15,new Scalar(255,255,0),2);

                        }

                        int antCount = centerPoints.size();

                        ////

                        for(Blob b : blobs){
                            b.update();
                            if(b.isActive()){
                                double minDistance = 50;
                                int closestIndex = -1;
                                boolean found = false;
                                for(int i = 0; i < centerPoints.size(); i++){
                                    double distance = b.checkDistance(centerPoints.get(i));
                                    if(distance < minDistance){
                                        minDistance = distance;
                                        closestIndex = i;
                                        found = true;
                                    }
                                }

                                if(found){
                                    b.addLocation(centerPoints.get(closestIndex).clone());
                                    centerPoints.remove(closestIndex);
                                }
                            }
                        }

                        // add remaining points as new blobs

                        for(Point p : centerPoints){
                            Blob b = new Blob();
                            b.addLocation(p.clone());
                            b.colorID = new Scalar( 0 , Math.random() * 255, Math.random() * 255);
                            blobs.add(b);
                        }

                        // draw blob history

                        for(Blob b : blobs){
                            b.draw(drawLines);
                        }

                        Imgproc.putText(frame, "detected Ants: " + antCount,new Point(10, frame.height()-10),50, frame.width()/640,new Scalar(0,0,0),2,Imgproc.LINE_4,false );

                       /* for (int i = 0; i < pointsToDraw.size(); i++ ) {

                            Imgproc.circle(frame,pointsToDraw.get(i),1,new Scalar(0,255,0),1);



                        }



                        pointsToDraw.addAll(centerPoints);

                        if(pointsToDraw.size()>5000) {
                           pointsToDraw = pointsToDraw.subList(pointsToDraw.size()-5000, pointsToDraw.size()-1);
                        }

                        */



                        blurValue.setText(df.format(sliderBlur.getValue()));
                        threshValue.setText(df.format(sliderThresh.getValue()));

                        //convert and show the frame
                        Image imageToShow = Converter.mat2Image(frame);
                        Image imageToShowBehind = Converter.mat2Image(drawLines);
                        Image imageDebug = Converter.mat2Image(subtractedBackground);


                        updateImageView(canvas, imageToShow);
                        updateImageView(canvas1, imageToShowBehind);
                        updateImageView(canvas2, imageDebug);


                        //System.out.println("Blur " + Double.toString(sliderBlur.getValue()) + "      Thresh " + Double.toString(sliderThresh.getValue()));


                    }
                };

                this.timer = Executors.newSingleThreadScheduledExecutor();
                this.timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MILLISECONDS);

                // update the button content
                this.startbtn.setText("Stop Video");
            } else {
                // log the error
                System.err.println("Impossible to open the camera connection...");
            }
        } else {

            // the camera is not active at this point
            this.cameraActive = false;
            // update again the button content
            this.startbtn.setText("Start Video");

            // stop the timer
            this.stopAcquisition();
        }
    }

    private Mat grabFrame()
    {
        // init everything
        Mat frame = new Mat();

        // check if the capture is open
        if (this.videoStream.isOpened())
        {
            try
            {
                // read the current frame
                this.videoStream.read(frame);

                // if the frame is not empty, process it
                if (!frame.empty())
                {
                    //Imgproc.cvtColor(frame, frame, Imgproc.COLOR_BGR2GRAY);
                }

            }
            catch (Exception e)
            {
                // log the error
                System.err.println("Exception during the image elaboration: " + e);
            }
        }

        return frame;
    }

    private void stopAcquisition()
    {
        if (this.timer!=null && !this.timer.isShutdown())
        {
            try
            {
                // stop the timer
                this.timer.shutdown();
                this.timer.awaitTermination(33, TimeUnit.MILLISECONDS);
            }
            catch (InterruptedException e)
            {
                // log any exception
                System.err.println("Exception in stopping the frame capture, trying to release the camera now... " + e);
            }
        }

        if (this.videoStream.isOpened())
        {
            // release the camera
            this.videoStream.release();
        }
    }

    private void updateImageView(ImageView view, Image image)
    {
        Converter.onFXThread(view.imageProperty(), image);

    }


    public void setClosed() {
        this.stopAcquisition();
    }


}
