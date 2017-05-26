package com.globaldelight.boom.playbackEvent.controller.callbacks;

import android.app.Activity;

/**
 * Created by Rahul Agarwal on 24-01-17.
 */

public interface IPlayerUIController {

    void OnPlayPause();

    void OnPlayerSeekChange(int progress);

    void OnRepeatClick();

    void OnShuffleClick();

    void OnNextTrackClick();

    void OnPreviousTrackClick();

    void OnUpNextClick(Activity activity);

    void OnPlayerTitleClick(Activity activity);


}
