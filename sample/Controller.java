package sample;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;



import javafx.scene.control.*;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;



import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;



import static org.opencv.core.CvType.*;
import static org.opencv.videoio.Videoio.*;


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

    private boolean videoActive = false;

    private boolean frameGrabbed = false;

    private DecimalFormat df = new DecimalFormat("#.#");

    private  ArrayList<Blob> blobs = new ArrayList<Blob>();

    private static String AntVideo = "src/ants.mov";





    protected void init() {

        this.videoStream = new VideoCapture();

        sliderBlur.setMin(1);
        sliderBlur.setMax(100);
        sliderBlur.setValue(10);

        sliderThresh.setMin(1);
        sliderThresh.setMax(255);
        sliderThresh.setValue(100);

        canvas.setFitWidth(640);    //Image for the video
        canvas1.setFitWidth(640);   //Image for the lines
        canvas1.setOpacity(0.5);    // set opacity for overlaying video image
        canvas2.setFitWidth(640);   //Debug Image


    }


    @FXML
    protected void startStream(javafx.event.ActionEvent actionEvent) {

        if (!this.videoActive) {

            //start reading the video
            this.videoStream.open(AntVideo);


            if (this.videoStream.isOpened()) {

                this.videoActive = true;






                //grab a Frame every 33 ms
                Runnable frameGrabber = new Runnable() {
                    @Override
                    public void run() {

                        //grab a single frame
                        Mat frame = grabFrame();
                        Mat drawLines = new Mat(frame.rows(),frame.cols(),CV_8UC3, new Scalar(0,0,0)); // Mat for the Lines image with the same size of the video image


                        if (!frameGrabbed) {
                            frameGrabbed = true;


                        }


                       Mat subtractedBackground = imageProcessor.backroundSubtraction(frame,sliderBlur.getValue(),sliderThresh.getValue());

                       // draw center Points
                        ArrayList<Point> centerPoints =  detector.detectCenterPoints(subtractedBackground); // save center points after detecting the blobs and calculated the centers

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


                        // update the UI

                        blurValue.setText(df.format(sliderBlur.getValue()));
                        threshValue.setText(df.format(sliderThresh.getValue()));


                        //convert and show the frame
                        Image imageToShow = Converter.mat2Image(frame);
                        Image imageToShowOverlay = Converter.mat2Image(drawLines);
                        Image imageDebug = Converter.mat2Image(subtractedBackground);



                        updateImageView(canvas, imageToShow);
                        updateImageView(canvas1, imageToShowOverlay);
                        updateImageView(canvas2, imageDebug);


                        //System.out.println("Blur " + Double.toString(sliderBlur.getValue()) + "      Thresh " + Double.toString(sliderThresh.getValue()));

                        //LOOP the video

                        double totalFrames = videoStream.get(CV_CAP_PROP_FRAME_COUNT);
                        double framesPos = videoStream.get(CV_CAP_PROP_POS_FRAMES);


                        //System.out.println(Double.toString(framesPos));

                    if ( totalFrames - 500.0 == framesPos) {

                        videoStream.set(CV_CAP_PROP_POS_FRAMES,0.0);
                        blobs.clear();


                        }


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
            this.videoActive = false;
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
