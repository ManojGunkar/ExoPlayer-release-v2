package com.globaldelight.boom.app.dialogs;

import android.app.Activity;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.globaldelight.boom.R;
import com.globaldelight.boom.app.adapters.utils.EqualizerDialogAdapter;
import com.globaldelight.boom.app.analytics.flurry.FlurryAnalytics;
import com.globaldelight.boom.app.analytics.flurry.FlurryEvents;
import com.globaldelight.boom.player.AudioEffect;
import com.globaldelight.boom.utils.Utils;

import java.util.Arrays;
import java.util.List;

/**
 * Created by adarsh on 11/07/17.
 */

public class EqualizerDialog implements EqualizerDialogAdapter.IEqualizerSelect{

    private Activity mActivity;
    private int ScreenWidth;
    private int ScreenHeight;
    private AudioEffect audioEffects;
    private List<String> eq_names;
    private TypedArray eq_active_off;


    public EqualizerDialog(Activity activity) {
        mActivity = activity;
        audioEffects = AudioEffect.getInstance(mActivity);

        Point point = new Point();
        mActivity.getWindowManager().getDefaultDisplay().getSize(point);
        ScreenWidth = point.x;
        ScreenHeight = point.y;

        eq_names = Arrays.asList(mActivity.getResources().getStringArray(R.array.eq_names));
        eq_active_off = mActivity.getResources().obtainTypedArray(R.array.eq_active_off);

    }

    public void show() {
        final EqualizerDialogAdapter adapter = new EqualizerDialogAdapter(mActivity, audioEffects.getSelectedEqualizerPosition(), eq_names, eq_active_off, this);
        RecyclerView recyclerView = (RecyclerView)mActivity.getLayoutInflater()
                .inflate(R.layout.recycler_view_layout, null);
        recyclerView.setLayoutManager(new LinearLayoutManager(mActivity));
        recyclerView.scrollToPosition(audioEffects.getSelectedEqualizerPosition());
        recyclerView.setAdapter(adapter);

        MaterialDialog dialog = Utils.createDialogBuilder(mActivity)
                .title(R.string.eq_dialog_title)
                .customView(recyclerView, false)
                .positiveText(R.string.done)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                })
                .autoDismiss(false)
                .canceledOnTouchOutside(false)
                .show();
        dialog.getWindow().setLayout((ScreenWidth *80)/100, (ScreenHeight *70)/100);
    }

    @Override
    public void onChangeEqualizerValue(final int position) {
        audioEffects.setSelectedEqualizerPosition(position);
    }
}
