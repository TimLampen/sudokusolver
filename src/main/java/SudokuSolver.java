import javafx.application.Application;

import javafx.stage.Stage;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.opencv.imgproc.Imgproc.contourArea;
import static org.opencv.imgproc.Imgproc.floodFill;


public class SudokuSolver{

    static int[][] board= new int[][]{
            {0,4,0,0,6,0,7,0,0},
            {0,0,0,0,0,0,0,8,3},
            {0,1,0,0,0,3,9,6,0},
            {0,0,6,0,8,0,0,0,9},
            {0,7,0,0,4,1,0,2,0},
            {0,0,0,6,2,0,0,0,8},
            {0,0,0,0,0,0,0,0,0},
            {1,0,7,0,0,5,0,0,0},
            {0,6,2,0,0,0,0,9,1}
    };
    static int iterations = 0;

    public static void main(String[] args) throws IOException {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        Mat sudoku = Imgcodecs.imread("C:/Users/lampe/OneDrive - Queen's University/Intellij/IdeaProjects/sudokusolver/src/main/resources/puzzle.jpg", Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);

        Mat outerBox = new Mat(sudoku.size(), CvType.CV_8UC1);

        Imgproc.GaussianBlur(sudoku, sudoku, new Size(11,11), 0);
        Imgproc.adaptiveThreshold(sudoku, outerBox, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 5,2);
        Core.bitwise_not(outerBox, outerBox);
        Mat filter = new Mat(3,3, CvType.CV_8UC1);
        filter.put(0,0,0);
        filter.put(0,1,1);
        filter.put(0,2,0);
        filter.put(1,0,1);
        filter.put(1,1,1);
        filter.put(1,2,1);
        filter.put(2,0,0);
        filter.put(2,1,1);
        filter.put(2,2,0);
        Imgproc.dilate(outerBox, outerBox, filter);


        Rect largestBox = getLargestRect(outerBox);
        Mat flooded = new Mat();
        Imgproc.floodFill(outerBox, flooded, new Point(largestBox.x, largestBox.y), new Scalar(255,255,255), largestBox);

        BufferedImage img = matToBufferedImage(outerBox, null);
        ImageIO.write(img, "jpg", new File("C:/Users/lampe/OneDrive - Queen's University/Intellij/IdeaProjects/sudokusolver/src/main/resources/flood.jpg"));

        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(outerBox, contours, new Mat(),Imgproc.RETR_TREE,  Imgproc.CHAIN_APPROX_SIMPLE, new Point(0, 0));
        for(int i = 0; i < contours.size(); i++){
            MatOfPoint c = contours.get(i);
            Rect boundingRect = Imgproc.boundingRect(c);
            if(!boundingRect.equals(largestBox))
                floodFill(outerBox, new Mat(), new Point(boundingRect.x, boundingRect.y), new Scalar(0,0,0), Imgproc.boundingRect(c));
        }

        Imgproc.erode(outerBox, outerBox, filter);

        img = matToBufferedImage(outerBox, null);
        ImageIO.write(img, "jpg", new File("C:/Users/lampe/OneDrive - Queen's University/Intellij/IdeaProjects/sudokusolver/src/main/resources/solved.jpg"));

    }

    private static Rect getLargestRect(Mat img) {
        List<MatOfPoint> contours = new ArrayList<>();
        List<Rect> rects = new ArrayList<>();
        List<Double> araes = new ArrayList<>();
        Imgproc.findContours(img, contours, new Mat(),Imgproc.RETR_TREE,  Imgproc.CHAIN_APPROX_SIMPLE, new Point(0, 0));
        for (int i = 0; i < contours.size(); i++) {
            MatOfPoint c = contours.get(i);
            double area = contourArea(c);
            Rect boundingRect = Imgproc.boundingRect(c);
            araes.add(area);
            rects.add(boundingRect);
        }
        if (araes.isEmpty() || Collections.max(araes) < 4000) {
            return new Rect(0, 0, img.cols(), img.rows());
        } else {
            Double d = Collections.max(araes);
            return rects.get(araes.indexOf(d));
        }
    }


    /**
     * Converts/writes a Mat into a BufferedImage.
     *
     * @param matrix Mat of type CV_8UC3 or CV_8UC1
     * @return BufferedImage of type TYPE_3BYTE_BGR or TYPE_BYTE_GRAY
     */
    public static BufferedImage matToBufferedImage(Mat matrix, BufferedImage bimg)
    {
        if ( matrix != null ) {
            int cols = matrix.cols();
            int rows = matrix.rows();
            int elemSize = (int)matrix.elemSize();
            byte[] data = new byte[cols * rows * elemSize];
            int type;
            matrix.get(0, 0, data);
            switch (matrix.channels()) {
                case 1:
                    type = BufferedImage.TYPE_BYTE_GRAY;
                    break;
                case 3:
                    type = BufferedImage.TYPE_3BYTE_BGR;
                    // bgr to rgb
                    byte b;
                    for(int i=0; i<data.length; i=i+3) {
                        b = data[i];
                        data[i] = data[i+2];
                        data[i+2] = b;
                    }
                    break;
                default:
                    return null;
            }

            // Reuse existing BufferedImage if possible
            if (bimg == null || bimg.getWidth() != cols || bimg.getHeight() != rows || bimg.getType() != type) {
                bimg = new BufferedImage(cols, rows, type);
            }
            bimg.getRaster().setDataElements(0, 0, cols, rows, data);
        } else { // mat was null
            bimg = null;
        }
        return bimg;
    }

    boolean solve(){
        for(int i = 0; i < 9; i++){
            for(int j = 0; j < 9; j++){
                if(board[j][i]==0){
                    for(int val = 1; val < 10; val++){
                        if(isValidAddition(j, i, val)){
                            board[j][i] = val;
                            iterations++;
                            if(solve()){
                                return true;
                            }
                            else
                                board[j][i] = 0;
                        }
                    }
                    return false;
                }
            }
        }
        return true;
    }

    boolean isValidAddition(int col, int row, int val){

        if(board[col][row]!=0)
            return false;

        for(int i = 0; i < 9; i++) {//checks the horizontal and vertical boxes
            if (board[col][i] == val) {
                return false;
            }
            if(board[i][row] == val){
                return false;
            }
        }

        int boxX = row / 3;//uses integer divide
        int boxY = col / 3;
        int sudokuX = boxX * 3;
        int sudokuY = boxY * 3;//both of these map to the top left corner of the specific box

        for(int i = sudokuX; i <= sudokuX+2; i++){
            for(int j = sudokuY; j <= sudokuY+2; j++){
                if(board[j][i]==val)
                    return false;
            }
        }


        return true;
    }


}