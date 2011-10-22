package com.noname.imagetopdf;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Bitmap.Config;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;

public class BasicCamera extends Activity {
	private static final String TAG = "CameraDemo";
	private Mat mYuv = new Mat();
	
	ProcessImage processingobject = new ProcessImage();
	
	Camera camera;
	Preview preview;
	Button buttonClick;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		preview = new Preview(this);
		((FrameLayout) findViewById(R.id.preview)).addView(preview);

		buttonClick = (Button) findViewById(R.id.buttonClick);
		buttonClick.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				preview.camera.takePicture(shutterCallback, rawCallback,
						jpegCallback);
			}
		});

		Log.d(TAG, "onCreate'd");
	}

	private Bitmap JPEGtoRGB888(Bitmap img)
    {
        int numPixels = img.getWidth()* img.getHeight();
        int[] pixels = new int[numPixels];

        //Get JPEG pixels.  Each int is the color values for one pixel.
        img.getPixels(pixels, 0, img.getWidth(), 0, 0, img.getWidth(), img.getHeight());

        //Create a Bitmap of the appropriate format.
        Bitmap result = Bitmap.createBitmap(img.getWidth(), img.getHeight(), Config.ARGB_8888);

        //Set RGB pixels.
        result.setPixels(pixels, 0, result.getWidth(), 0, 0, result.getWidth(), result.getHeight());
        return result;
    } 
	
	ShutterCallback shutterCallback = new ShutterCallback() {
		public void onShutter() {
			Log.d(TAG, "onShutter'd");
		}
	};

	/** Handles data for raw picture */
	PictureCallback rawCallback = new PictureCallback() {
		public void onPictureTaken(byte[] data, Camera camera) {
			Log.d(TAG, "onPictureTaken - raw");
		}
	};

	/** Handles data for jpeg picture */
	PictureCallback jpegCallback = new PictureCallback() {
		public void onPictureTaken(byte[] data, Camera camera) {
			try {
				// Bitmap factory
				Bitmap tmp = BitmapFactory.decodeByteArray(data,0,data.length);
		        Matrix matrix = new Matrix();
		        matrix.postScale((float) 0.5, (float) 0.5);
				tmp = Bitmap.createBitmap(tmp, 0, 0,
                        tmp.getWidth(), tmp.getHeight(), matrix, true);
				tmp = JPEGtoRGB888(tmp);
				Mat mRgba = Utils.bitmapToMat(tmp);
				
				//Bitmap ourData = Bitmap.createBitmap(tmp.getWidth(), tmp.getHeight(),
				//		Bitmap.Config.ARGB_8888);
				//Utils.matToBitmap(mRgba, ourData);
				Bitmap ourData = processingobject.process(mRgba, tmp.getWidth(), tmp.getHeight());
									
				FileOutputStream out = new FileOutputStream(String.format(
							"/sdcard/%d.jpg", System.currentTimeMillis()));
		        ourData.compress(Bitmap.CompressFormat.JPEG, 90, out);
				out.close();
				
				Log.d(TAG, "onPictureTaken - wrote bytes: " + data.length);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
			}
			Log.d(TAG, "onPictureTaken - jpeg");
		}
	};

}