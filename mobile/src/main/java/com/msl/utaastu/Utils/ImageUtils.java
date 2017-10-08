package com.msl.utaastu.Utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import com.msl.utaastu.Application.MyApplication;

import java.io.ByteArrayOutputStream;

/**
 * Created by Malek Shefat on 8/1/2017.
 */

public class ImageUtils {

    public static String bitmapToBase64String(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
        byte[] imageBytes = baos.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.NO_WRAP);
    }

    public static Bitmap base64StringToBitmap(String base64) {
        byte[] imageBytes = Base64.decode(base64, Base64.NO_WRAP);
        Bitmap decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
        MyApplication.setProfileImage(decodedImage);
        return decodedImage;
    }

}
