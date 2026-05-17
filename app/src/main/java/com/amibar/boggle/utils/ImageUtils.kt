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

/**
 * Utility class for image processing and data conversion.
 * Provides helper methods for converting between Bitmap, Base64 strings, and URIs,
 * as well as custom Data Binding adapters for ImageView.
 */
public class ImageUtils {
    /** Tag used for logging. */
    private final static String TAG = "ImageUtils";

    /**
     * Data Binding adapter to set a Bitmap to an ImageView.
     * Displays a default placeholder if the bitmap is null.
     *
     * @param imageView The target ImageView.
     * @param bitmap    The bitmap to display.
     */
    @BindingAdapter("imageBitmap")
    static public void setImageBitmap(ImageView imageView, Bitmap bitmap) {
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
        } else {
            imageView.setImageDrawable(AppCompatResources.getDrawable(imageView.getContext(),
                    R.drawable.ic_person));
        }
    }


    /**
     * Converts a content Uri to a Base64 encoded JPEG string.
     *
     * @param uri     The image Uri to convert.
     * @param context Application context for accessing ContentResolver.
     * @return A Base64 string representation of the image, or null if conversion fails.
     * @throws IOException If the input stream cannot be opened or read.
     */
    static public String uriToBase64(Uri uri, Context context) throws IOException {
        try (InputStream inputStream = context.getContentResolver().openInputStream(uri)) {
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            return bitmapToBase64(bitmap);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "File not found: " + uri, e);
            return null;
        }
    }

    /**
     * Converts a Bitmap object into a Base64 encoded JPEG string.
     * Uses 70% quality compression to balance size and visual fidelity.
     *
     * @param bitmap The Bitmap to encode.
     * @return Base64 encoded string, or null on error.
     */
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

    /**
     * Decodes a Base64 encoded string back into a Bitmap object.
     *
     * @param base64 The encoded image string.
     * @return The decoded Bitmap, or null if input is null or invalid.
     */
    static public Bitmap base64ToBitmap(String base64){
        if (base64 == null) return null;
        byte[] decodedArray = Base64.decode(base64, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedArray, 0, decodedArray.length);
    }
}
