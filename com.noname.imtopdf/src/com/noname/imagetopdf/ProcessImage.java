package com.noname.imagetopdf;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;

/**
* Created by IntelliJ IDEA.
* User: gracewoo
* Date: 10/3/11
* Time: 2:19 PM
* To change this template use File | Settings | File Templates.
*/
public class ProcessImage {
    private Bitmap ourData;

    public ProcessImage(Bitmap now)
    {
        //ourData = Bitmap.createBitmap(now.getWidth(), now.getHeight(), now.getConfig());
        process(now);
    }

    public void process(Bitmap now) {
        // for now just threshold the image
        Matrix matrix = new Matrix();
        matrix.postScale((float) 0.2, (float) 0.2);
        matrix.postRotate(90);

        ourData = Bitmap.createBitmap(now, 0, 0, now.getWidth(), now.getHeight(), matrix, true);

        float[] result = new float[3];
        for (int i=0; i<ourData.getWidth(); i++) {
            for (int j=0; j<ourData.getHeight(); j++) {
                int color = ourData.getPixel(i, j);
                Color.colorToHSV(color, result);
                int r = (color >> 16) & 0xff;
                int g = (color >> 8) & 0xff;
                int b = (color) & 0xff;
                if ((r < 128) && (g < 128) && (b < 128) && (result[1] < 0.5)) {
                    ourData.setPixel(i, j, Color.WHITE);
                }
                else ourData.setPixel(i, j, Color.BLACK);
            }
        }
    }

    public Bitmap getBitmap()
    {
        return ourData;
    }
}