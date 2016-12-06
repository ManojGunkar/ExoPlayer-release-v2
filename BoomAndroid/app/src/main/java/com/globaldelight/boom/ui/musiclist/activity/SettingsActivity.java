package com.globaldelight.boom.ui.musiclist.activity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TimePicker;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.globaldelight.boom.App;
import com.globaldelight.boom.R;
import com.globaldelight.boom.analytics.AnalyticsHelper;
import com.globaldelight.boom.analytics.FlurryAnalyticHelper;
import com.globaldelight.boom.purchase.InAppPurchaseActivity;
import com.globaldelight.boom.ui.musiclist.adapter.HeadSetListAdapter;
import com.globaldelight.boom.ui.widgets.RegularButton;
import com.globaldelight.boom.ui.widgets.RegularTextView;
import com.globaldelight.boom.utils.HeadSetType;
import com.globaldelight.boom.utils.PlayerSettings;
import com.globaldelight.boom.utils.SleepAlarm;
import com.globaldelight.boom.utils.handlers.Preferences;
import com.globaldelight.boom.utils.handlers.UserPreferenceHandler;
import com.globaldelight.boom.utils.helpers.ISwipeRefresh;
import com.globaldelight.boom.utils.helpers.OnScrollTouchListenerControl;
import com.globaldelight.boomplayer.AudioEffect;

import org.json.JSONException;

import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener, ISwipeRefresh, RadioGroup.OnCheckedChangeListener {


    protected View view;
    Resources system;
    RadioButton rbNone, rbNext, rbPlay, rbArtist, rbAlbum;
    AlertDialog alertDialog;
    private RadioGroup radioButtonSortGroup;
    private RadioGroup radioButtonShakeGroup;
    private Switch crossfadeSwich;
    private TimePicker timePicker;
    private RegularTextView titleSupport, titleAbout, titleShare;
    private CountDownTimer mCountDownTimer;
    private long sleepTime;
    private SeekBar slideseekbar;
    private boolean seekbarfocus;
    private ArrayList<HeadSetType> headSetTypeList;
    private Context mContext;
    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;
    private int MIN_PROGRESS = 3;
    private int progress;
    private RegularTextView txtSeekText;
    private RegularTextView txtDescTimer;
    private Toolbar toolbar;
    private RecyclerView mRecyclerView;
    private HeadSetListAdapter headSetAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private AudioEffect audioEffectPreferenceHandler;
    private Typeface font;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        overridePendingTransition(R.anim.push_up_in, R.anim.stay_out);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        mContext = this;
        toolbar = (Toolbar) findViewById(R.id.settings_toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        initPagerItems();
        audioEffectPreferenceHandler = AudioEffect.getAudioEffectInstance(this);
        radioButtonSortGroup = (RadioGroup) findViewById(R.id.radio_group_sort);
        radioButtonSortGroup.setOnCheckedChangeListener(this);
        //section shake
        radioButtonShakeGroup = (RadioGroup) findViewById(R.id.radio_group_shake);
        radioButtonShakeGroup.setOnCheckedChangeListener(this);
        //section crossfade
        crossfadeSwich = (Switch) findViewById(R.id.switch_cross_fade);
        slideseekbar = (SeekBar) findViewById(R.id.seekBar);
        slideseekbar.setEnabled(false);
        crossfadeSwich.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                Preferences.writeBoolean(mContext, Preferences.CROSS_FADE_ENABLE, isChecked);
                slideseekbar.setEnabled(isChecked);

            }
        });

        font = Typeface.createFromAsset(getAssets(), "fonts/TitilliumWeb-Regular.ttf");

        titleAbout = (RegularTextView) findViewById(R.id.title_about);
        titleSupport = (RegularTextView) findViewById(R.id.title_support);
        titleShare = (RegularTextView) findViewById(R.id.title_share);
        titleSupport.setOnClickListener(this);
        titleShare.setOnClickListener(this);
        titleAbout.setOnClickListener(this);

        txtDescTimer = (RegularTextView) findViewById(R.id.description_timer);
        txtDescTimer.setOnClickListener(this);
        txtSeekText = (RegularTextView) findViewById(R.id.seek_text);
        findViewById(R.id.mainContainer).setOnTouchListener(new OnScrollTouchListenerControl(this, this));
        slideseekbar.setOnSeekBarChangeListener(this);
        set_timepicker_text_colour();


        mRecyclerView = (RecyclerView) findViewById(R.id.head_recycler);
        headSetAdapter = new HeadSetListAdapter(headSetTypeList, this);
        headSetAdapter.setOnItemClickListener(new HeadSetListAdapter.HeadsetClickListener() {
            @Override
            public void onItemClick(int position, View v) {

                for (int i = 0; i < headSetTypeList.size(); i++) {
                    if (i == position) {
                        headSetTypeList.get(i).setActive(true);

                        int type;
                        if(position == 0){
                            type = 0;
                        }else if(position == 1){
                            type = 3;
                        }else{
                            type = 1;
                        }
                        audioEffectPreferenceHandler.setHeadPhoneType(AudioEffect.headphone.fromOrdinal(type));
                        try {
                            AnalyticsHelper.trackHeadPhoneUsed(SettingsActivity.this, AudioEffect.headphone.fromOrdinal(type).toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        App.getPlayerEventHandler().setHeadPhoneType(type);
                    } else
                        headSetTypeList.get(i).setActive(false);
                }
                headSetAdapter.notifyDataSetChanged();
            }
        });

        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(headSetAdapter);


        rbAlbum = (RadioButton) findViewById(R.id.radio_album);
        rbArtist = (RadioButton) findViewById(R.id.radio_artist);

        rbNone = (RadioButton) findViewById(R.id.radio_none);

        rbNext = (RadioButton) findViewById(R.id.radio_next_song);

        rbPlay = (RadioButton) findViewById(R.id.radio_play_pause);
    }

    public void initPagerItems() {
        headSetTypeList = new ArrayList<HeadSetType>();
        HeadSetType item = new HeadSetType();
        item.setPosition(0);
        item.setImageResource(R.drawable.over_ear_normal);
        item.setImageActiveResource(R.drawable.over_ear_active);
        item.setTitle(getResources().getString(R.string.over_ear));
        item.setTextColor(R.color.white_shade);
        item.setTextColoreActive(R.color.white);
        item.setActive(false);
        headSetTypeList.add(item);
        item = new HeadSetType();
        item.setPosition(1);
        item.setImageResource(R.drawable.in_canal_normal);
        item.setImageActiveResource(R.drawable.in_canal_active);
        item.setTitle(getResources().getString(R.string.in_canal));
        item.setTextColor(R.color.white_shade);
        item.setTextColoreActive(R.color.white);
        item.setActive(false);
        headSetTypeList.add(item);
        item = new HeadSetType();
        item.setPosition(2);
        item.setImageResource(R.drawable.on_ear_normal);
        item.setImageActiveResource(R.drawable.on_ear_active);
        item.setTitle(getResources().getString(R.string.on_ear));
        item.setTextColor(R.color.white_shade);
        item.setTextColoreActive(R.color.white);
        item.setActive(false);
        headSetTypeList.add(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadStoredSettings();
        resumeTimerState();

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus)
            showSeekValue(progress);
    }

    public void setTimer() {
        int hour = 0;
        int minute = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            hour = timePicker.getHour();
            minute = timePicker.getMinute();
        } else {
            hour = timePicker.getCurrentHour();
            minute = timePicker.getCurrentMinute();
        }
        if (hour == 0 && minute == 0) {
            Toast.makeText(mContext, mContext.getResources().getString(R.string.invalid_timer_entry), Toast.LENGTH_SHORT).show();
            setUiTimerEditMode();
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss aa");//yyyy:MM:dd:HH:mm

        String currentDateandTime = sdf.format(new Date(System.currentTimeMillis()));

        Date date = null;
        try {
            date = sdf.parse(currentDateandTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.HOUR, hour);
        calendar.add(Calendar.MINUTE, minute);
        Date endDate = calendar.getTime();

        SimpleDateFormat sdftime = new SimpleDateFormat("hh:mm:ss aa");//yyyy:MM:dd:HH:mm
        String endtime = sdftime.format(calendar.getTime());
        txtDescTimer.setText(getResources().getString(R.string.sleep_timer_description_active) + " " + endtime);
        txtDescTimer.setOnClickListener(this);
        //String dateString = sdf.format(new Date(System.currentTimeMillis()));
        sleepTime = endDate.getTime() - date.getTime();
        alarmMgr = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(mContext, SleepAlarm.class);
        alarmIntent = PendingIntent.getBroadcast(mContext, 0, i, 0);
        alarmMgr.set(AlarmManager.RTC_WAKEUP, endDate.getTime(), alarmIntent);
        Preferences.writeBoolean(mContext, Preferences.SLEEP_TIMER_ENABLED, true);
        Preferences.writeLong(mContext, Preferences.SLEEP_TIME, endDate.getTime());

        mCountDownTimer = new CountDownTimer(sleepTime, 1000) {

            public void onTick(long millisUntilFinished) {

                String hms = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(millisUntilFinished),
                        TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millisUntilFinished)),
                        TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)));
                txtDescTimer.setText(hms + " remaining.");
                
            }

            public void onFinish() {
                txtDescTimer.setText("00:00:00" + " remaining.");
                Preferences.writeBoolean(mContext, Preferences.SLEEP_TIMER_ENABLED, false);


            }

        }.start();

        setUiTimerMode();

    }


    public void resetTimer() {
        if (mCountDownTimer != null)
            mCountDownTimer.cancel();
        if (alarmMgr != null)
            alarmMgr.cancel(alarmIntent);

        Preferences.writeBoolean(mContext, Preferences.SLEEP_TIMER_ENABLED, false);
        setUiTimerEditMode();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            timePicker.setMinute(0);
            timePicker.setHour(0);
        } else {
            timePicker.setCurrentHour(0);
            timePicker.setCurrentMinute(1);
        }
    }

    public void cancelTimer() {
        // String TimerTime = txtTimer.getText().toString();
        String TimerTime = txtDescTimer.getText().toString().substring(0, 7);
        Toast.makeText(this, TimerTime, Toast.LENGTH_SHORT).show();
        DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        try {
            Date dt = formatter.parse(TimerTime);
            Calendar cal = Calendar.getInstance();
            cal.setTime(dt);
            int hour = cal.get(Calendar.HOUR);
            int minute = cal.get(Calendar.MINUTE);
            int second = cal.get(Calendar.SECOND);
            if (second > 0)
                minute = minute + 1;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                timePicker.setMinute(minute);
                timePicker.setHour(hour);
            } else {
                timePicker.setCurrentMinute(minute);
                timePicker.setCurrentHour(hour);
            }
        } catch (ParseException e) {
            // This can happen if you are trying to parse an invalid date, e.g., 25:19:12.
            // Here, you should log the error and decide what to do next
            e.printStackTrace();
        }
    }


    public void resumeTimerState() {

        boolean sleepTimerEnabled = Preferences.readBoolean(mContext, Preferences.SLEEP_TIMER_ENABLED, false);
        if (!sleepTimerEnabled) {
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss aa");//yyyy:MM:dd:HH:mm

        String currentDateandTime = sdf.format(new Date(System.currentTimeMillis()));

        Date runningDateTime = null;
        try {
            runningDateTime = sdf.parse(currentDateandTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        long savedEndDateTime = Preferences.readLong(mContext, Preferences.SLEEP_TIME, 0L);
        if (savedEndDateTime == 0L) {
            setUiTimerEditMode();
            return;
        }

        Date endDateTime = null;
        String strEndDateTime = sdf.format(new Date(savedEndDateTime));
        try {
            endDateTime = sdf.parse(strEndDateTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        SimpleDateFormat sdftime = new SimpleDateFormat("hh:mm:ss aa");//yyyy:MM:dd:HH:mm
        String endtime = sdftime.format(endDateTime);
        sleepTime = endDateTime.getTime() - runningDateTime.getTime();
        if (sleepTime > 0) {

            setUiTimerMode();
            // txtStopTime.setText("Music will stop playing at " + strEndDateTime);
            txtDescTimer.setText(getResources().getString(R.string.sleep_timer_description_active) + " " + endtime);
            mCountDownTimer = new CountDownTimer(sleepTime, 1000) {

                public void onTick(long millisUntilFinished) {


                    String hms = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(millisUntilFinished),
                            TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millisUntilFinished)),
                            TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)));
                    // txtTimer.setText(hms);
                    txtDescTimer.setText(hms + " remaining.");
                }
                public void onFinish() {
                    //txtTimer.setText("00:00:00");
                    txtDescTimer.setText("00:00:00" + " remaining.");
                    Preferences.writeBoolean(mContext, Preferences.SLEEP_TIMER_ENABLED, false);


                }
            }.start();
        } else {
            setUiTimerEditMode();
        }
    }

    public void setUiTimerEditMode() {
        // timePicker.setVisibility(View.VISIBLE);
        //txtTimer.setVisibility(View.GONE);
        //  btnTimerCancel.setVisibility(View.INVISIBLE);
        //  btnTimerReset.setVisibility(View.INVISIBLE);
        //  btnTimerStart.setVisibility(View.VISIBLE);
        txtDescTimer.setText(getResources().getString(R.string.sleep_timer_description));
        /// txtStopTime.setVisibility(View.GONE);
        //txtStopTime.setVisibility(View.VISIBLE);
        //txtStopTime.setText("Hour : Minute");
        //txtStopTime.setTextSize(25f);
    }

    public void setUiTimerMode() {
        // timePicker.setVisibility(View.GONE);
        //  txtTimer.setVisibility(View.VISIBLE);
        // btnTimerCancel.setVisibility(View.VISIBLE);
        // btnTimerReset.setVisibility(View.VISIBLE);
        // btnTimerStart.setVisibility(View.INVISIBLE);
        // txtStopTime.setVisibility(View.GONE);

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.description_timer:
                //setTimer();
                boolean sleepTimerEnabled = Preferences.readBoolean(mContext, Preferences.SLEEP_TIMER_ENABLED, false);
                if (sleepTimerEnabled) {
                    customMaterialTimepicker(true);
                    try {
                        cancelTimer();
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }

                } else {
                    customMaterialTimepicker(false);
                }
                break;

            case R.id.title_about:
                Intent abIntent = new Intent(SettingsActivity.this, AboutActivity.class);
                startActivity(abIntent);
                break;
            case R.id.title_share:
                try {
                    Intent i = new Intent(Intent.ACTION_SEND);
                    i.setType("text/plain");
                    i.putExtra(Intent.EXTRA_SUBJECT, mContext.getResources().getString(R.string.title_share));
                    String sAux = mContext.getResources().getString(R.string.desc_share);
                    sAux = sAux + mContext.getResources().getString(R.string.playstore_link);
                    i.putExtra(Intent.EXTRA_TEXT, sAux);
                    startActivity(Intent.createChooser(i, "choose one"));
                } catch (Exception e) {
                    //e.toString();
                }
                FlurryAnalyticHelper.logEvent(AnalyticsHelper.EVENT_ABOUT_SHARE_BUTTON_TAPPED);
                break;
            case R.id.title_support:
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                        "mailto", mContext.getResources().getString(R.string.mailto), null));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, mContext.getResources().getString(R.string.title_support));
                emailIntent.putExtra(Intent.EXTRA_TEXT, mContext.getResources().getString(R.string.desc_support));
                startActivity(Intent.createChooser(emailIntent, "Send email..."));
                FlurryAnalyticHelper.logEvent(AnalyticsHelper.EVENT_ABOUT_CONTACT_US_BUTTON_TAPPED);
                break;
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (mCountDownTimer != null)
            mCountDownTimer.cancel();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        if (seekbarfocus == false) {
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {

                    seekbarfocus = false;

                }
            }, PlayerSettings.SEEKBAR_GESTURE_ACTIVE_TIME);
        }

        seekbarfocus = true;
        int crossfadeValue = slideseekbar.getProgress();
        Preferences.writeInteger(mContext, Preferences.CROSS_FADE_VALUE, crossfadeValue);

        showSeekValue(progress);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void leftSwipe() {
        if (seekbarfocus && slideseekbar.isEnabled())
            slideseekbar.setProgress(slideseekbar.getProgress() - MIN_PROGRESS);
    }

    @Override
    public void rightSwipe() {
        if (seekbarfocus && slideseekbar.isEnabled())
            slideseekbar.setProgress(slideseekbar.getProgress() + MIN_PROGRESS);
    }

    @Override
    public void upSwipe() {

    }

    @Override
    public void downSwipe() {

    }

    private void showSeekValue(int seekValue) {
        progress = seekValue;
        float value = (float) seekValue / 10;


        String newvalue = String.format("%.2f", value);
        if (seekValue < 100)
            txtSeekText.setText("0" + newvalue);
        else
            txtSeekText.setText(newvalue);
        int seek_label_pos = (((slideseekbar.getRight() - slideseekbar.getLeft()) * slideseekbar.getProgress()) / slideseekbar.getMax()) + slideseekbar.getLeft();
        txtSeekText.setX(seek_label_pos - txtSeekText.getWidth() / 2);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // getMenuInflater().inflate(R.menu.menu_settings, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                overridePendingTransition(R.anim.stay_out, R.anim.push_up_out);
                break;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.stay_out, R.anim.push_up_out);
    }

    public void loadStoredSettings() {
        int selectedHeadType = audioEffectPreferenceHandler.getHeadPhoneType();
        switch (AudioEffect.headphone.fromOrdinal(selectedHeadType)) {
            case OVER_EAR:
                headSetTypeList.get(0).setActive(true);
                break;
            case IN_CANAL:
                headSetTypeList.get(1).setActive(true);
                break;
            case ON_EAR:
                headSetTypeList.get(2).setActive(true);
                break;
        }
        headSetAdapter.notifyDataSetChanged();

        int type = App.getUserPreferenceHandler().getSortedByAlbum();
        switch (type) {
            case UserPreferenceHandler.ALBUM_SORTED_BY_ARTIST:
                rbArtist.setChecked(true);
                break;
            case UserPreferenceHandler.ALBUM_SORTED_BY_TITLE:
                rbAlbum.setChecked(true);
                break;

        }
        String selectedShakeOption = Preferences.readString(mContext, Preferences.SHAKE_GESTURE_PREF, PlayerSettings.ShakeGesture.SHAKE_GESTURE_NONE.toString());
        PlayerSettings.ShakeGesture selGesture = PlayerSettings.ShakeGesture.toShakeGesture(selectedShakeOption);
        switch (selGesture) {
            case SHAKE_GESTURE_NONE:

                rbNone.setChecked(true);
                break;
            case SHAKE_GESTURE_NEXT:

                rbNext.setChecked(true);
                break;
            case SHAKE_GESTURE_PLAY:

                rbPlay.setChecked(true);
                break;


        }
        boolean crossFadeIsEnabled = Preferences.readBoolean(mContext, Preferences.CROSS_FADE_ENABLE, false);
        crossfadeSwich.setChecked(crossFadeIsEnabled);
        progress = Preferences.readInteger(mContext, Preferences.CROSS_FADE_VALUE, 0);
        slideseekbar.setProgress(progress);

        seekbarfocus = false;


    }

    private void set_timepicker_text_colour() {
        system = Resources.getSystem();
        int hour_numberpicker_id = system.getIdentifier("hour", "id", "android");
        int minute_numberpicker_id = system.getIdentifier("minute", "id", "android");
        int ampm_numberpicker_id = system.getIdentifier("amPm", "id", "android");

        //  NumberPicker hour_numberpicker = (NumberPicker) timePicker.findViewById(hour_numberpicker_id);
        //  NumberPicker minute_numberpicker = (NumberPicker) timePicker.findViewById(minute_numberpicker_id);
        //  NumberPicker ampm_numberpicker = (NumberPicker) timePicker.findViewById(ampm_numberpicker_id);

        //   set_numberpicker_text_colour(hour_numberpicker);
        //  set_numberpicker_text_colour(minute_numberpicker);
        // set_numberpicker_text_colour(ampm_numberpicker);
    }

    private void set_numberpicker_text_colour(NumberPicker number_picker) {
        final int count = number_picker.getChildCount();
        final int color = getResources().getColor(R.color.white_active);

        for (int i = 0; i < count; i++) {
            View child = number_picker.getChildAt(i);

            try {
                Field wheelpaint_field = number_picker.getClass().getDeclaredField("mSelectorWheelPaint");
                wheelpaint_field.setAccessible(true);

                ((Paint) wheelpaint_field.get(number_picker)).setColor(color);
                ((EditText) child).setTextColor(color);
                ((EditText) child).setTypeface(font);
                //((EditText)child).setTextSize(20f);
                number_picker.invalidate();
                break;
            } catch (NoSuchFieldException e) {
                Log.w("setNumberPickerTextColo", e);
            } catch (IllegalAccessException e) {
                Log.w("setNumberPickerTextColo", e);
            } catch (IllegalArgumentException e) {
                Log.w("setNumberPickerTextColo", e);
            }

        }

        Field[] pickerFields = NumberPicker.class.getDeclaredFields();
        for (Field pf : pickerFields) {
            if (pf.getName().equals("mSelectionDivider")) {
                pf.setAccessible(true);
                try {
                    ColorDrawable colorDrawable = new ColorDrawable(getResources().getColor(R.color.white));
                    pf.set(number_picker, colorDrawable);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (Resources.NotFoundException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                break;
            }
        }

    }


    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        if (group.getId() == R.id.radio_group_sort) {
            switch (checkedId) {
                case R.id.radio_artist:
                    App.getUserPreferenceHandler().setAlbumSorted(UserPreferenceHandler.ALBUM_SORTED_BY_ARTIST);
                    FlurryAnalyticHelper.logEvent(AnalyticsHelper.EVENT_SORT_BY_ARTIST);
                    break;
                case R.id.radio_album:
                    App.getUserPreferenceHandler().setAlbumSorted(UserPreferenceHandler.ALBUM_SORTED_BY_TITLE);
                    FlurryAnalyticHelper.logEvent(AnalyticsHelper.EVENT_SORT_BY_ALBUM);
                    break;

            }

        } else if (group.getId() == R.id.radio_group_shake) {
            int selectedShakeId = radioButtonShakeGroup.getCheckedRadioButtonId();

            switch (selectedShakeId) {
                case R.id.radio_none:
                    Preferences.writeString(mContext, Preferences.SHAKE_GESTURE_PREF, PlayerSettings.ShakeGesture.SHAKE_GESTURE_NONE.toString());
                    break;
                case R.id.radio_next_song:
                    Preferences.writeString(mContext, Preferences.SHAKE_GESTURE_PREF, PlayerSettings.ShakeGesture.SHAKE_GESTURE_NEXT.toString());
                    break;
                case R.id.radio_play_pause:
                    Preferences.writeString(mContext, Preferences.SHAKE_GESTURE_PREF, PlayerSettings.ShakeGesture.SHAKE_GESTURE_PLAY.toString());
                    break;


            }

        }

    }

    public void openStore(View v) {
        Intent intent = new Intent(SettingsActivity.this, InAppPurchaseActivity.class);
        startActivity(intent);
    }


    public void customMaterialTimepicker(boolean running) {

        MaterialDialog dialog = new MaterialDialog.Builder(this)
                .customView(R.layout.custom_time_picker, false)
                .backgroundColor(Color.parseColor("#171921"))
                .titleColor(Color.parseColor("#ffffff"))
                .positiveColor(this.getResources().getColor(R.color.colorPrimary))
                .typeface("TitilliumWeb-SemiBold.ttf", "TitilliumWeb-Regular.ttf")
                .widgetColor(Color.parseColor("#ffffff"))
                .contentColor(Color.parseColor("#ffffff"))
                .positiveText(R.string.timer_start)
                .negativeText(R.string.timer_cancel)
                .neutralText(R.string.timer_reset)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                        setTimer();
                        materialDialog.dismiss();

                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                        resetTimer();
                        materialDialog.dismiss();
                    }
                })
                .onNeutral(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                })

                .show();
        View dialogView = dialog.getCustomView();
        timePicker = (TimePicker) dialogView.findViewById(R.id.timePickerdialog);
        RegularButton timerStart = (RegularButton) dialogView.findViewById(R.id.tmstart);
        RegularButton timerReset = (RegularButton) dialogView.findViewById(R.id.tmreset);//close
        final RegularButton timerCancel = (RegularButton) dialogView.findViewById(R.id.tmcancel);
        timerStart.setTransformationMethod(null);
        timerReset.setTransformationMethod(null);
        timerCancel.setTransformationMethod(null);
        View negative = dialog.getActionButton(DialogAction.NEGATIVE);
        View positive = dialog.getActionButton(DialogAction.POSITIVE);
        if (running) {
            positive.setVisibility(View.GONE);
            negative.setVisibility(View.VISIBLE);
        } else {
            positive.setVisibility(View.VISIBLE);
            negative.setVisibility(View.GONE);
        }

        timePicker.setIs24HourView(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            timePicker.setHour(0);
            timePicker.setMinute(0);
        } else {
            timePicker.setCurrentHour(0);
            timePicker.setCurrentMinute(0);
        }
        system = Resources.getSystem();
        int hour_numberpicker_id = system.getIdentifier("hour", "id", "android");
        int minute_numberpicker_id = system.getIdentifier("minute", "id", "android");
        int ampm_numberpicker_id = system.getIdentifier("amPm", "id", "android");

        NumberPicker hour_numberpicker = (NumberPicker) timePicker.findViewById(hour_numberpicker_id);
        NumberPicker minute_numberpicker = (NumberPicker) timePicker.findViewById(minute_numberpicker_id);
        NumberPicker ampm_numberpicker = (NumberPicker) timePicker.findViewById(ampm_numberpicker_id);

        set_numberpicker_text_colour(hour_numberpicker);
        set_numberpicker_text_colour(minute_numberpicker);
        set_numberpicker_text_colour(ampm_numberpicker);

    }
}
