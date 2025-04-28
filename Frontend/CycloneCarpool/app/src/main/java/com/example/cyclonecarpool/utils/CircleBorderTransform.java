package com.example.cyclonecarpool.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;

import java.security.MessageDigest;

public class CircleBorderTransform extends BitmapTransformation {

    private final int borderColor;
    private final int borderWidth;

    public CircleBorderTransform(int borderColor, int borderWidth) {
        this.borderColor = borderColor;
        this.borderWidth = borderWidth;
    }

    @Override
    protected Bitmap transform(BitmapPool pool, Bitmap toTransform, int outWidth, int outHeight) {
        return createCircularImageWithBorder(pool, toTransform);
    }

    private Bitmap createCircularImageWithBorder(BitmapPool pool, Bitmap source) {
        if (source == null) return null;

        int size = Math.min(source.getWidth(), source.getHeight());
        int newSize = size + borderWidth * 2;

        Bitmap result = pool.get(newSize, newSize, Bitmap.Config.ARGB_8888);
        if (result == null) {
            result = Bitmap.createBitmap(newSize, newSize, Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(result);

        Paint imagePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        BitmapShader shader = new BitmapShader(source, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        imagePaint.setShader(shader);
        float radius = size / 2f;
        canvas.drawCircle(newSize / 2f, newSize / 2f, radius, imagePaint);

        Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setColor(borderColor);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(borderWidth);
        float borderRadius = (newSize - borderWidth) / 2f;
        canvas.drawCircle(newSize / 2f, newSize / 2f, borderRadius, borderPaint);

        return result;
    }

    @Override
    public void updateDiskCacheKey(MessageDigest messageDigest) {
        messageDigest.update(("CircleBorderTransform" + borderColor + borderWidth).getBytes());
    }
}

