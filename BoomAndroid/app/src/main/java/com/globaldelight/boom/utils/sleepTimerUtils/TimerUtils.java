package com.globaldelight.boom.utils.sleepTimerUtils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TimePicker;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.globaldelight.boom.R;
import com.globaldelight.boom.ui.widgets.RegularButton;
import com.globaldelight.boom.ui.widgets.RegularTextView;
import com.globaldelight.boom.utils.handlers.Preferences;

import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by Rahul Agarwal on 03-02-17.
 */

public class TimerUtils {
    private static TimePicker timePicker;
    private static Resources system;
    private static Typeface font;
    private static PendingIntent alarmIntent;
    private static AlarmManager alarmMgr;
    private static Long sleepTime;
    private static CountDownTimer mCountDownTimer;;

    public static void customMaterialTimepicker(final Context mContext, final RegularTextView txtDescTimer, boolean running) {
        MaterialDialog dialog = new MaterialDialog.Builder(mContext)
                .customView(R.layout.custom_time_picker, false)
                .backgroundColor(ContextCompat.getColor(mContext, R.color.dialog_background))
                .titleColor(ContextCompat.getColor(mContext, R.color.dialog_title))
                .positiveColor(ContextCompat.getColor(mContext, R.color.dialog_submit_positive))
                .negativeColor(ContextCompat.getColor(mContext, R.color.dialog_submit_positive))
                .neutralColor(ContextCompat.getColor(mContext, R.color.dialog_submit_positive))
                .widgetColor(ContextCompat.getColor(mContext, R.color.dialog_widget))
                .contentColor(ContextCompat.getColor(mContext, R.color.dialog_content))
                .typeface("TitilliumWeb-SemiBold.ttf", "TitilliumWeb-Regular.ttf")
                .positiveText(R.string.timer_start)
                .negativeText(R.string.timer_cancel)
                .neutralText(R.string.timer_reset)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                        setTimer(mContext, txtDescTimer);
                        materialDialog.dismiss();
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                        resetTimer(mContext, txtDescTimer);
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

        set_numberpicker_text_colour(mContext, hour_numberpicker);
        set_numberpicker_text_colour(mContext, minute_numberpicker);
        set_numberpicker_text_colour(mContext, ampm_numberpicker);

    }


    private static void set_numberpicker_text_colour(Context mContext, NumberPicker number_picker) {
        final int count = number_picker.getChildCount();
        final int color = ContextCompat.getColor(mContext, R.color.effect_active);
        font = Typeface.createFromAsset(mContext.getAssets(), "fonts/TitilliumWeb-Regular.ttf");

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
                    ColorDrawable colorDrawable = new ColorDrawable(ContextCompat.getColor(mContext, R.color.effect_active));
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

    public static void setTimer(final Context mContext, final RegularTextView txtDescTimer) {
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
            setUiTimerEditMode(mContext, txtDescTimer);
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
        txtDescTimer.setText(mContext.getResources().getString(R.string.sleep_timer_description_active) + " " + endtime);
//        txtDescTimer.setOnClickListener(new );
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
                txtDescTimer.setText(hms + mContext.getResources().getString(R.string.remaning));
            }

            public void onFinish() {
                txtDescTimer.setText("00:00:00" + mContext.getResources().getString(R.string.remaning));
                Preferences.writeBoolean(mContext, Preferences.SLEEP_TIMER_ENABLED, false);
                txtDescTimer.setText(mContext.getResources().getString(R.string.sleep_timer_description));
            }
        }.start();
    }


    public static void resetTimer(Context mContext, RegularTextView txtDescTimer) {
        if (mCountDownTimer != null)
            mCountDownTimer.cancel();
        if (alarmMgr != null)
            alarmMgr.cancel(alarmIntent);

        Preferences.writeBoolean(mContext, Preferences.SLEEP_TIMER_ENABLED, false);
        setUiTimerEditMode(mContext, txtDescTimer);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            timePicker.setMinute(0);
            timePicker.setHour(0);
        } else {
            timePicker.setCurrentHour(0);
            timePicker.setCurrentMinute(1);
        }
    }

    public static void cancelTimer(RegularTextView txtDescTimer) {
        String TimerTime = txtDescTimer.getText().toString().substring(0, 7);
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

    public static void resumeTimerState(final Context mContext, final RegularTextView txtDescTimer) {

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
            setUiTimerEditMode(mContext, txtDescTimer);
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
            txtDescTimer.setText(mContext.getResources().getString(R.string.sleep_timer_description_active) + " " + endtime);
            mCountDownTimer = new CountDownTimer(sleepTime, 1000) {

                public void onTick(long millisUntilFinished) {

                    String hms = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(millisUntilFinished),
                            TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millisUntilFinished)),
                            TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)));
                    // txtTimer.setText(hms);
                    txtDescTimer.setText(hms + mContext.getResources().getString(R.string.remaning));
                }
                public void onFinish() {
                    //txtTimer.setText("00:00:00");
                    txtDescTimer.setText("00:00:00" + mContext.getResources().getString(R.string.remaning));
                    Preferences.writeBoolean(mContext, Preferences.SLEEP_TIMER_ENABLED, false);
                    txtDescTimer.setText(mContext.getResources().getString(R.string.sleep_timer_description));

                }
            }.start();
        } else {
            setUiTimerEditMode(mContext, txtDescTimer);
        }
    }

    public static void setUiTimerEditMode(Context context, RegularTextView txtDescTimer) {
        txtDescTimer.setText(context.getResources().getString(R.string.sleep_timer_description));
    }
}
