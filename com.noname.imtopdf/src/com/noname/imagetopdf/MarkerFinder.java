package com.noname.imagetopdf;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

public class MarkerFinder {
	private Mat localYUV = new Mat();
	private Mat mRgba = new Mat();
	private Mat mGraySubmat = new Mat();
	private Mat circles = new Mat();
	
	private int squareradius = 0;
	private int marklen = 0;
	private int linethickness = 10;
	
	public static class Result {
		int xcoord;
		int ycoord;
		double threshold;
		Result(int x, int y, double t) {
			xcoord = x;
			ycoord = y;
			threshold = t;
		}
	}
	
	public Result process(Mat mYuv, int height, int width, Mat output) {
		Result result = null;
		squareradius = width/10;
		marklen = width/10-width/20;
		localYUV = mYuv.clone();
		
		// To speed things up, only perform processing on cropped portion
		int xoffset = width/2-squareradius;
		int yoffset = height/2-squareradius;
		Rect roi = new Rect(xoffset, yoffset, 2*squareradius, 2*squareradius);
		Mat cropped = new Mat(localYUV, roi);
		
		mGraySubmat = cropped.submat(0, cropped.height(), 0, cropped.width());
		Imgproc.threshold(mGraySubmat, mGraySubmat, 100, 255, Imgproc.THRESH_BINARY);
		Imgproc.Canny(mGraySubmat, mGraySubmat, 80, 100);
		Imgproc.HoughCircles(mGraySubmat, circles, Imgproc.CV_HOUGH_GRADIENT, 2, 10);
		
		Imgproc.cvtColor(mYuv, mRgba, Imgproc.COLOR_YUV420sp2RGB, 4);
		drawOverlay(mRgba, height, width).copyTo(output);
		return result;
	}
	
	public Mat drawOverlay(Mat mRgba, int height, int width) {
		//Scalar color = new Scalar(0, 0, 0, 10);
		//Core.rectangle(mRgba, new Point(0, 0), new Point(width/2-squareradius, height-1), color, -1);
		//Core.rectangle(mRgba, new Point(width/2+squareradius, 0), new Point(width-1, height-1), color, -1);
		
		Scalar color = new Scalar(0, 255, 0, 200);
		Core.line(mRgba, new Point(width/2-squareradius, height/2 + squareradius), new Point(width/2-marklen, height/2 + squareradius), color, linethickness);
		Core.line(mRgba, new Point(width/2+marklen, height/2 + squareradius), new Point(width/2+squareradius, height/2 + squareradius), color, linethickness);
		Core.line(mRgba, new Point(width/2-squareradius, height/2 - squareradius), new Point(width/2-marklen, height/2 - squareradius), color, linethickness);
		Core.line(mRgba, new Point(width/2+marklen, height/2 - squareradius), new Point(width/2+squareradius, height/2 - squareradius), color, linethickness);
		
		Core.line(mRgba, new Point(width/2+squareradius, height/2-squareradius), new Point(width/2+squareradius, height/2-marklen), color, linethickness);
		Core.line(mRgba, new Point(width/2+squareradius, height/2+marklen), new Point(width/2+squareradius, height/2+squareradius), color, linethickness);
		Core.line(mRgba, new Point(width/2-squareradius, height/2-squareradius), new Point(width/2-squareradius, height/2-marklen), color, linethickness);
		Core.line(mRgba, new Point(width/2-squareradius, height/2+marklen), new Point(width/2-squareradius, height/2+squareradius), color, linethickness);
		
		return mRgba;
	}
}