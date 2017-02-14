package com.globaldelight.boom.ui.widgets;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;

import com.globaldelight.boom.R;

/**
 * Created by Manoj on 14/02/2017.
 */
public class BoomLoaderView extends DialogFragment {

  public BoomLoaderView() {
  }

  private Animation operatingAnim;
  private Dialog mDialog;
  private View mLoader;


  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    if (mDialog == null) {
      mDialog = new Dialog(getActivity(), R.style.cart_dialog);
      mDialog.setContentView(R.layout.loader_boom);
      mDialog.setCanceledOnTouchOutside(false);
      mDialog.getWindow().setGravity(Gravity.CENTER);

      operatingAnim = new RotateAnimation(360f, 0f, Animation.RELATIVE_TO_SELF, 0.5f,
          Animation.RELATIVE_TO_SELF, 0.5f);
      operatingAnim.setRepeatCount(Animation.INFINITE);
      operatingAnim.setDuration(1000);
      LinearInterpolator lin = new LinearInterpolator();
      operatingAnim.setInterpolator(lin);
      View view = mDialog.getWindow().getDecorView();
      mLoader = view.findViewById(R.id.loader);

    }
    return mDialog;
  }

  @Override
  public void onResume() {
    super.onResume();
    mLoader.setAnimation(operatingAnim);
  }

  @Override
  public void onPause() {
    super.onPause();
    operatingAnim.reset();
    mLoader.clearAnimation();
  }

  @Override
  public void onDismiss(DialogInterface dialog) {
    super.onDismiss(dialog);
    mDialog.dismiss();
    mDialog = null;
    System.gc();
  }
}
