package com.amibar.boggle.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class ImageUtils {
    private final static String TAG = "ImageUtils";

    static public String uriToBase64(InputStream inputStream) {
        try {
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            // Compress the image to keep the Base64 string size reasonable for Realtime Database
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
