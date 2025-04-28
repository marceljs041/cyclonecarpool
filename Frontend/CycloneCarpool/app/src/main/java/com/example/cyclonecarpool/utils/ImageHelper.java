package com.example.cyclonecarpool.utils;

import android.graphics.Color;
import android.widget.ImageView;

import androidx.fragment.app.FragmentActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.cyclonecarpool.R;

public class ImageHelper {
    public static void loadProfilePic(FragmentActivity activity, String profilePicUrl, ImageView target) {
        int borderColor = Color.BLACK;
        int borderWidth = 6;

        Glide.with(activity)
                .load(profilePicUrl)
                .apply(RequestOptions.bitmapTransform(new CircleBorderTransform(borderColor, borderWidth))
                        .placeholder(R.drawable.icon_round_trip)
                        .error(R.drawable.icon_one_way))
                .into(target);
    }
}
