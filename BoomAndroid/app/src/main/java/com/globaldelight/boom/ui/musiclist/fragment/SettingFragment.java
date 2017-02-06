package com.globaldelight.boom.ui.musiclist.fragment;

import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import com.globaldelight.boom.R;
import com.globaldelight.boom.ui.musiclist.adapter.HeadPhoneItemAdapter;
import com.globaldelight.boom.ui.widgets.RegularTextView;
import com.globaldelight.boom.utils.handlers.Preferences;
import com.globaldelight.boom.utils.sleepTimerUtils.TimerUtils;

/**
 * Created by Rahul Agarwal on 03-02-17.
 */

public class SettingFragment extends Fragment implements View.OnClickListener{
    ScrollView rootView;
    RegularTextView sleepTimerTxt;

    public SettingFragment(){}

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = (ScrollView) inflater.inflate(R.layout.fragment_settings, container, false);
        initViews();
        return rootView;
    }

    private void initViews() {
        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.headset_recycler) ;
        LinearLayout dropbox = (LinearLayout) rootView.findViewById(R.id.setting_dropbox_panel);
        dropbox.setOnClickListener(this);
        LinearLayout googleDrive = (LinearLayout) rootView.findViewById(R.id.setting_google_drive_panel);
        googleDrive.setOnClickListener(this);
        sleepTimerTxt = (RegularTextView) rootView.findViewById(R.id.seeting_sleep_timer);
        LinearLayout sleepTimerPanel = (LinearLayout) rootView.findViewById(R.id.seeting_sleep_timer_panel);
        sleepTimerPanel.setOnClickListener(this);

        setHeadsetList(recyclerView);
    }

    private void setHeadsetList(RecyclerView recyclerView) {
        TypedArray activeHeadPhoneList = getResources().obtainTypedArray(R.array.headphone_active);
        TypedArray inactiveHeadPhoneList = getResources().obtainTypedArray(R.array.headphone_inactive);
        TypedArray HeadPhoneList = getResources().obtainTypedArray(R.array.headphone_list);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setAdapter(new HeadPhoneItemAdapter(activeHeadPhoneList, inactiveHeadPhoneList, HeadPhoneList, getContext(), recyclerView));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.setting_dropbox_panel:

                break;
            case R.id.setting_google_drive_panel:

                break;
            case R.id.seeting_sleep_timer_panel:
                boolean sleepTimerEnabled = Preferences.readBoolean(getContext(), Preferences.SLEEP_TIMER_ENABLED, false);
                TimerUtils.customMaterialTimepicker(getContext(), sleepTimerTxt, sleepTimerEnabled);
                if (sleepTimerEnabled) {
                    try {
                        TimerUtils.cancelTimer(sleepTimerTxt);
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }
}
