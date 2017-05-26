package com.globaldelight.boom.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;

import com.globaldelight.boom.R;

/**
 * Created by Manoj on 14/02/2017.
 */


public class BoomDialogView extends Dialog {

  private Animation operatingAnim;
  private View mLoader;

    public BoomDialogView(Context context) {
      super(context, R.style.cart_dialog);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      requestWindowFeature(Window.FEATURE_NO_TITLE);
      setContentView(R.layout.loader_boom);

      operatingAnim = new RotateAnimation(360f, 0f, Animation.RELATIVE_TO_SELF, 0.5f,
              Animation.RELATIVE_TO_SELF, 0.5f);
      operatingAnim.setRepeatCount(Animation.INFINITE);
      operatingAnim.setDuration(1200);
      LinearInterpolator lin = new LinearInterpolator();
      operatingAnim.setInterpolator(lin);
      View view = getWindow().getDecorView();
      mLoader = view.findViewById(R.id.loader);
      mLoader.setAnimation(operatingAnim);
    }
}

