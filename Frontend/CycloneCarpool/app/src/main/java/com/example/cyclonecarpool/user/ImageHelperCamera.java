package com.example.cyclonecarpool.user;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.ByteArrayOutputStream;

public class ImageHelperCamera {

    /**
     * Converts a Bitmap into a Uri by storing it in the MediaStore.
     *
     * @param context The application context.
     * @param bitmap The bitmap to be converted.
     * @return The Uri pointing to the saved image.
     */
    public static Uri getImageUri(Context context, Bitmap bitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, "CapturedImage", null);
        return Uri.parse(path);
    }
}
