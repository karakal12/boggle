package com.amibar.boggle.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.databinding.BindingAdapter;

import com.amibar.boggle.R;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;


public class ImageUtils {
    private final static String TAG = "ImageUtils";

    @BindingAdapter("imageBitmap")
    static public void setImageBitmap(ImageView imageView, Bitmap bitmap) {
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
        } else {
            imageView.setImageDrawable(AppCompatResources.getDrawable(imageView.getContext(),
                    R.drawable.ic_person));
        }
    }



    static public String uriToBase64(Uri uri, Context context) throws IOException {
        try (InputStream inputStream = context.getContentResolver().openInputStream(uri)) {
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            return bitmapToBase64(bitmap);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "File not found: " + uri, e);
            return null;
        }
    }

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
        if (base64 == null) return null;
        byte[] decodedArray = Base64.decode(base64, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedArray, 0, decodedArray.length);
    }
}
