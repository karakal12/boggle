package com.amibar.boggle.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;


public class ImageUtils {
    private final static String TAG = "ImageUtils";

    static public String bitmapToBase64(Bitmap bitmap) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream);
            byte[] byteArray = outputStream.toByteArray();
            return Base64.encodeToString(byteArray, Base64.DEFAULT);
        } catch (Exception e) {
            Log.e(TAG, "Error converting image to Base64", e);
            return null;
        }
    }

    static public Bitmap base64ToBitmap(String base64){
        byte[] decodedArray = Base64.decode(base64, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedArray, 0, decodedArray.length);
    }
}
