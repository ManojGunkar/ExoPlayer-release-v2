package com.globaldelight.boom.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.Image;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.globaldelight.boom.R;
import com.globaldelight.boom.collection.local.MediaItem;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Rahul Agarwal on 30-11-16.
 */

public class PlayerUtils {
    private static final float BITMAP_SCALE = 1.0f;
    private static final float BLUR_RADIUS = 4.0f;

    public static void ImageViewAnimatedChange(Context context, final ImageView v, final Bitmap new_image) {
        final Animation anim_in = AnimationUtils.loadAnimation(context, android.R.anim.fade_in);
        v.setImageBitmap(new_image);
        v.startAnimation(anim_in);

//        anim_out.setAnimationListener(new Animation.AnimationListener() {
//            @Override
//            public void onAnimationStart(Animation animation) {
//            }
//
//            @Override
//            public void onAnimationRepeat(Animation animation) {
//            }
//
//            @Override
//            public void onAnimationEnd(Animation animation) {
//            }
//        });
//        v.startAnimation(anim_out);
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

    public static Bitmap createBackgoundBitmap(Context context, Bitmap bitmap, int dstWidth, int dstHeight) {
        int imgWidth = dstWidth;
        int imgHeight = dstHeight;

        float scaleX = (float)imgWidth / (float)bitmap.getWidth();
        float scaleY = (float)imgHeight / (float)bitmap.getHeight();
        float scale = Math.max(scaleX, scaleY);

        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, Math.round(bitmap.getWidth() * scale),
                Math.round(bitmap.getHeight() * scale), false);

        Bitmap tempBitmap = Bitmap.createBitmap(scaledBitmap,
                (scaledBitmap.getWidth() - imgWidth)/2,
                (scaledBitmap.getHeight() - imgHeight)/2,
                imgWidth, imgHeight);

        scaledBitmap.recycle();

        Bitmap out = PlayerUtils.blur(context, tempBitmap);
        tempBitmap.recycle();

        System.gc();

        return out;
    }

    public static synchronized Bitmap blur(Context context, Bitmap inputBitmap) {
        Bitmap outputBitmap = Bitmap.createBitmap(inputBitmap);

        RenderScript rs = RenderScript.create(context);
        ScriptIntrinsicBlur theIntrinsic = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
        Allocation tmpIn = Allocation.createFromBitmap(rs, inputBitmap);
        Allocation tmpOut = Allocation.createFromBitmap(rs, outputBitmap);
        theIntrinsic.setRadius(BLUR_RADIUS);
        theIntrinsic.setInput(tmpIn);
        theIntrinsic.forEach(tmpOut);
        tmpOut.copyTo(outputBitmap);

        rs.destroy();

        return outputBitmap;
    }

    public static void setSongsArtTable(Context context, ArrayList<String> imageUrls, ImageView views[] ) {
        if ( views.length < 4 ) {
            return;
        }

        int count = imageUrls.size() > 4 ? 4 : imageUrls.size();
        String urls[] = new String[4];
        switch (count){
            case 1:
                urls[0] = urls[1] = urls[2] = urls[3] = imageUrls.get(0);
                break;
            case 2:
                urls[0] = urls[3] = imageUrls.get(0);
                urls[1] = urls[2] = imageUrls.get(1);
                break;
            case 3:
                urls[0] = urls[3] = imageUrls.get(0);
                urls[1] = imageUrls.get(1);
                urls[2] = imageUrls.get(2);
                break;
            case 4:
                urls[0] = imageUrls.get(0);
                urls[1] = imageUrls.get(1);
                urls[2] = imageUrls.get(2);
                urls[3] = imageUrls.get(3);
                break;
        }

        for (int i = 0; i < views.length; i++ ) {
            Glide.with(context)
                    .load(urls[i])
                    .centerCrop()
                    .placeholder(R.drawable.ic_default_art_grid)
                    .skipMemoryCache(true)
                    .into(views[i]);
        }
    }
}
