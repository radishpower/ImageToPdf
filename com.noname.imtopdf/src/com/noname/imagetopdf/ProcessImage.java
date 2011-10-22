package com.noname.imagetopdf;

import java.util.ArrayList;
import java.util.List;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import android.graphics.Bitmap;

public class ProcessImage {
	private Mat localYUV = new Mat();
	private Mat mRgba = new Mat();
	private Mat mGraySubmat = new Mat();
	private Mat bwimg = new Mat();
	private List<Point> corners = new ArrayList<Point>();
	
	public Bitmap process(Mat mRgba, int width, int height) {
		Bitmap ourData = Bitmap.createBitmap(width, height,
				Bitmap.Config.ARGB_8888);
		Imgproc.cvtColor(mRgba, bwimg, Imgproc.COLOR_RGBA2GRAY, 4);
		Imgproc.Canny(bwimg, bwimg, 80, 100);
		
		Imgproc.cvtColor(bwimg, mRgba, Imgproc.COLOR_GRAY2RGBA, 4);
		
		Utils.matToBitmap(mRgba, ourData);
		return ourData;
	}
	
	public Bitmap processIt(Mat mYuv, int width, int height, Mat output) {		
		
		
		localYUV = mYuv.clone();
		mGraySubmat = mYuv.submat(0, width, 0, height);
		List<Mat> contours = new ArrayList<Mat>();
		Mat unused = new Mat();
		
		Imgproc.Canny(mGraySubmat, bwimg, 80, 100);
		mGraySubmat = bwimg.clone();
		Imgproc.findContours(mGraySubmat, contours, unused, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
		Mat approxCurve = new Mat();
		Imgproc.cvtColor(bwimg, mRgba, Imgproc.COLOR_GRAY2BGRA, 4);
		int count = 0;
		for (int i=0; i<contours.size(); i++) {
			double tmpval = Imgproc.contourArea(contours.get(i));
			if (tmpval > 800) {
				Imgproc.approxPolyDP(contours.get(i), approxCurve, tmpval*0.0001, true);
				if (approxCurve.total() == 4) {
					count = count + 1;
					//Imgproc.drawContours(mRgba, contours, i, new Scalar(255, 0, 0, 255));
					//Make sure that the smallest coordinate gets added to upper left
					int tl = 0; int bl = 0; int br = 0; int tr = 0; 
					int tmpmin = (int) (approxCurve.get(0, 0)[0]+approxCurve.get(0, 0)[1]);
					int tmpmax = (int) (approxCurve.get(0, 0)[0]+approxCurve.get(0, 0)[1]);
					for (int j=1; j<4; j++) {
						if (((int) (approxCurve.get(j, 0)[0]+approxCurve.get(j, 0)[1])) < tmpmin) {
							tl = j;
							tmpmin = ((int) (approxCurve.get(j, 0)[0]+approxCurve.get(j, 0)[1]));
						}
						if (((int) (approxCurve.get(j, 0)[0]+approxCurve.get(j, 0)[1])) > tmpmax) {
							br = j;
							tmpmax = ((int) (approxCurve.get(j, 0)[0]+approxCurve.get(j, 0)[1]));
						}
					}
					tmpmin = (int) (approxCurve.get(((tl+1)%4), 0)[1]-approxCurve.get((tl), 0)[1]);
					tmpmax = (int) (approxCurve.get(((tl+7)%4), 0)[1]-approxCurve.get((tl), 0)[1]);
					if (tmpmin > tmpmax) {
					     tr = (tl+1)%4; bl = (tl+3)%4;
					}
					else {
						tr = (tl+7)%4; bl=(tl+9)%4;
					}
					
					corners.set(0, new Point(approxCurve.get(tl,0)[0], approxCurve.get(tl, 0)[1]));
					corners.set(1, new Point(approxCurve.get(bl,0)[0], approxCurve.get(bl, 0)[1]));
					corners.set(2, new Point(approxCurve.get(br,0)[0], approxCurve.get(br, 0)[1]));
					corners.set(3, new Point(approxCurve.get(tr,0)[0], approxCurve.get(tr, 0)[1]));
					
					Imgproc.cvtColor(localYUV, mRgba, Imgproc.COLOR_YUV420sp2RGB, 4);	
				}
			}
		}
		Imgproc.cvtColor(localYUV, mRgba, Imgproc.COLOR_YUV420sp2RGB, 4);
		Core.fillConvexPoly(mRgba, corners, new Scalar(0, 0, 128, 128));
		
		Bitmap ourData = null;
		Utils.matToBitmap(mRgba, ourData);
		return ourData;
	}
	
	 // Takes the corners and remaps it to a rectangular figure
    public Mat perspective_transform(Mat distorted, int width, int height) {
            Mat transformMatrix = new Mat();
            Mat result = new Mat();
            List<Point> newcorners = new ArrayList<Point>();
            newcorners.add(new Point(0,0));
            newcorners.add(new Point(height,0));
            newcorners.add(new Point(height,width));
            newcorners.add(new Point(0,width));

            transformMatrix = Imgproc.getPerspectiveTransform(corners, newcorners);
            Imgproc.warpPerspective(distorted, result, transformMatrix, distorted.size());

            return result;
    }
}