package com.globaldelight.boom.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.support.v4.app.FragmentActivity;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.globaldelight.boom.data.DeviceMediaCollection.MediaItem;

import java.io.File;
import com.dropbox.client2.session.Session.AccessType;

/**
 * Created by Rahul Agarwal on 30-11-16.
 */

public class PlayerUtils {
    private static final float BITMAP_SCALE = 1.0f;
    private static final float BLUR_RADIUS = 25.0f;

    public static void ImageViewAnimatedChange(Context context, final ImageView v, final Bitmap new_image) {
        final Animation anim_out = AnimationUtils.loadAnimation(context, android.R.anim.fade_out);
        final Animation anim_in = AnimationUtils.loadAnimation(context, android.R.anim.fade_in);
        anim_out.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                v.setImageBitmap(new_image);
                anim_in.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                    }
                });
                v.startAnimation(anim_in);
            }
        });
        v.startAnimation(anim_out);
    }

    public static void ImageViewAnimatedChange(final Context context, final ImageView v, final int new_image) {
        final Animation anim_out = AnimationUtils.loadAnimation(context, android.R.anim.fade_out);
        final Animation anim_in = AnimationUtils.loadAnimation(context, android.R.anim.fade_in);
        anim_out.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                v.setImageDrawable(context.getResources().getDrawable(new_image, null));
                anim_in.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                    }
                });
                v.startAnimation(anim_in);
            }
        });
        v.startAnimation(anim_out);
    }

    public static boolean isPathValid(String path) {
        return path != null && !path.equals(MediaItem.UNKNOWN_ART_URL) && fileExist(path);
    }

    private static boolean fileExist(String albumArtPath) {
        File imgFile = new File(albumArtPath);
        return imgFile.exists();
    }

    public static Bitmap blur(Context activity, Bitmap bitmap) {
        int width = Math.round(bitmap.getWidth() * BITMAP_SCALE);
        int height = Math.round(bitmap.getHeight() * BITMAP_SCALE);

        Bitmap inputBitmap = Bitmap.createScaledBitmap(bitmap, width, height, false);
        Bitmap outputBitmap = Bitmap.createBitmap(inputBitmap);

        RenderScript rs = RenderScript.create(activity);
        ScriptIntrinsicBlur theIntrinsic = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
        Allocation tmpIn = Allocation.createFromBitmap(rs, inputBitmap);
        Allocation tmpOut = Allocation.createFromBitmap(rs, outputBitmap);
        theIntrinsic.setRadius(BLUR_RADIUS);
        theIntrinsic.setInput(tmpIn);
        theIntrinsic.forEach(tmpOut);
        tmpOut.copyTo(outputBitmap);

        return outputBitmap;
    }
}
