package com.globaldelight.boom.analytics;

import android.content.Context;

import com.flurry.android.FlurryAgent;
import com.globaldelight.boom.data.MediaCollection.IMediaItemBase;
import com.globaldelight.boomplayer.AudioEffect;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by nidhin on 17/11/16.
 */

public class AnalyticsHelper {
    /*  app common*/
    public static final String EVENT_APP_OPEN = "app_open";
    public static final String EVENT_FIRST_VISIT = "first_visit";

    public static final String EVENT_LAST_APP_OPEN = "last_app_open";
    public static final String EVENT_NUMBER_OF_TIMES_APP_OPEN = "number_of_times_app_open";
    public static final String PARAM_STATUS = "status";//active or inactive
    public static final String PARAM_STATUS_OFF = "on";//active or
    public static final String PARAM_STATUS_ON = "off";//active or inactive


    /* Effect screen*/
    public static final String EVENT_OPEN_EFFECTS = "open_effects";
    public static final String EVENT_FRONT_LEFT_SPEAKER_ON = "front_left_speaker_on";
    public static final String EVENT_FRONT_LEFT_SPEAKER_OFF = "front_left_speaker_off";
    public static final String EVENT_FRONT_RIGHT_SPEAKER_ON = "front_right_speaker_on";
    public static final String EVENT_FRONT_RIGHT_SPEAKER_OFF = "front_right_speaker_off";

    public static final String EVENT_REAR_LEFT_SPEAKER_ON = "rear_left_speaker_on";
    public static final String EVENT_REAR_LEFT_SPEAKER_OFF = "rear_left_speaker_off";
    public static final String EVENT_REAR_RIGHT_SPEAKER_ON = "rear_right_speaker_on";
    public static final String EVENT_REAR_RIGHT_SPEAKER_OFF = "rear_right_speaker_off";

    public static final String EVENT_SUBWOOFER_ON = "subwoofer_on";
    public static final String EVENT_SUBWOOFER_OFF = "subwoofer_off";

    public static final String EVENT_TWEETER_ON = "tweeter_on";
    public static final String EVENT_TWEETER_OFF = "tweeter_off";

    public static final String EVENT_FULL_BASS_ENABLED = "full_bass_enabled";
    public static final String EVENT_FULL_BASS_DISABLED = "full_bass_disabled";

    public static final String EVENT_EFFECT_STATE_CHANGED = "effect_state_changed";
    public static final String EVENT_3D_STATE_CHANGED = "3d_state_changed";
    public static final String EVENT_EQ_STATE_CHANGED = "eq_state_changed";
    public static final String EVENT_INTENSITY_STATE_CHANGED = "intensity_state_changed";
    public static final String EVENT_EFFECTS_BACK_BUTTON_TAPPED = "effects_back_button_tapped";
    //public static final String EVENT_PREVIOUS_BUTTON_TAPPED_FROM_EFFECTS= "previous_button_tapped_from_effect";

    /*about*/

    public static final String EVENT_ABOUT_RATE_BUTTON_TAPPED = "about:rate_button_tapped";
    public static final String EVENT_ABOUT_SHARE_BUTTON_TAPPED = "about:share_button_tapped";
    public static final String EVENT_ABOUT_CONTACT_US_BUTTON_TAPPED = "about:contact_us_button_tapped";


    /*settings*/
    public static final String EVENT_SORT_BY_ARTIST = "sort_by_artist";
    public static final String EVENT_SORT_BY_ALBUM = "sort_by_album";
    public static final String EVENT_HEADPHONE_TYPE_CHANGED = "headphone_type_changed";

    public static final String PARAM_SELECTED_HEADPHONE_TYPE = "selected_headphone_type";


    /*library*/
    public static final String EVENT_SONG_COUNT = "song_count";
    public static final String EVENT_CREATED_NEW_PLAYLIST = "created_new_playlist";
    public static final String EVENT_LIBRARY_CLOSE_BUTTON_TAPPED = "library_close_button_tapped";
    public static final String EVENT_MUSIC_PLAYED_FROM_SONG_SECTION = "music_played_from_song_section";
    public static final String EVENT_MUSIC_PLAYED_FROM_ARTIST_SECTION = "music_played_from_artist_section";
    public static final String EVENT_MUSIC_PLAYED_FROM_FAVOURITE_SECTION = "music_played_from_favourite_section";
    public static final String EVENT_ADD_ITEMS_TO_PLAYLIST_FROM_LIBRARY = "add_items_to_playlist_from_library";

    /*player*/
    public static final String EVENT_TRACK_SELECTION_CHANGED = "track_selection_changed";
    public static final String EVENT_MUSIC_SESSION_DURATION = " music_session_duration";
    public static final String EVENT_TOTAL_USAGE_IN_MINUTES = "total_usage_in_minutes";
    public static final String EVENT_QUEUE_BUTTON_FROM_PLAYER_SCREEN = "queue_button_from_player_screen";


    //Mixpanel

    public static final String EVENT_EFFECTS_TURNED_ON = "effects_turned_on";
    public static final String EVENT_EFFECTS_TURNED_OFF = "effects_turned_off";

    public static final String EVENT_3D_TURNED_ON = "3d_turned_on";
    public static final String EVENT_3D_TURNED_OFF = "3d_turned_on";


    public static final String EVENT_EQ_TURNED_ON = "eq_turned_on";
    public static final String EVENT_EQ_TURNED_OFF = "eq_turned_off";

    public static final String EVENT_INTENSITY_TURNED_ON = "intensity_turned_on";
    public static final String EVENT_INTENSITY_TURNED_OFF = "intensity_turned_off";
    public static final String EVENT_MOVE_TO_NEXT_SONG = "move_to_next_song";
    public static final String EVENT_MOVE_TO_PRE_SONG = "move_to_previous_song";
    public static final String PARAM_SONG_WITH_EQ = "Song_with_effect_eq";
    public static final String PARAM_SONG_WITH_3D = "Song_with_effect_3d";
    public static final String PARAM_SONG_WITH_INTENSITY = "Song_with_effect_intensity";
    public static final String PARAM_ARTIST_NAME = "artist_name";
    public static final String PARAM_GENRE = "song_genre";
    public static final String PARAM_SONG_SELECTED = "Song_selected";
    public static final String EVENT_UPDATE_PLAYBACK_SESSION = "update_playback_session";
   /* public static final String EVENT_HEAD_PHONE_PROFILE_NAME = "headphone_profile_name";
    public static final String PARAM_IN_CANAL = "in_canal";
    public static final String PARAM_OVER_EAR = "over_ear";
    public static final String PARAM_ON_EAR = "on_ear";*/
    //public static final String EVENT_CREATED_NEW_PLAYLIST = "created_new_playlist";


    public static void logCommonEventWithStatus(Context ctx, String eventName, boolean status) {
        //flurry

        Map<String, String> articleParams = new HashMap<>();
        // JSONObject props = new JSONObject();
        //param keys and values have to be of String type
        if (status) {
            articleParams.put(AnalyticsHelper.PARAM_STATUS, PARAM_STATUS_ON);
        } else {
            articleParams.put(AnalyticsHelper.PARAM_STATUS, PARAM_STATUS_OFF);
        }
        //up to 10 params can be logged with each event

        FlurryAgent.logEvent(eventName, articleParams);

        //Mixpanel

        JSONObject props = new JSONObject(articleParams);

        MixPanelAnalyticHelper.getInstance(ctx).registerSuperProperties(props);
    }

    public static void songSelectionChanged(Context context, IMediaItemBase songInfo) {
        AudioEffect audioEffectPreferenceHandler;
        audioEffectPreferenceHandler = AudioEffect.getAudioEffectInstance(context);
        FlurryAnalyticHelper.logEvent(AnalyticsHelper.EVENT_TRACK_SELECTION_CHANGED);
        MixpanelAPI mixpanel = MixPanelAnalyticHelper.getInstance(context);
        MixPanelAnalyticHelper.getInstance(context).getPeople().increment(EVENT_SONG_COUNT, 1);


        JSONObject properties = new JSONObject();


        // properties.put(AnalyticsHelper.EVENT_MOVE_TO_NEXT_SONG, 1);
        if (audioEffectPreferenceHandler.isAudioEffectOn()) {
            if (audioEffectPreferenceHandler.isEqualizerOn()) {
                mixpanel.getPeople().increment(PARAM_SONG_WITH_EQ, 1);
                try {
                    properties.put(AnalyticsHelper.PARAM_SONG_WITH_EQ, true);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            if (audioEffectPreferenceHandler.is3DSurroundOn()) {
                mixpanel.getPeople().increment(PARAM_SONG_WITH_3D, 1);
                try {
                    properties.put(AnalyticsHelper.PARAM_SONG_WITH_3D, true);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
            if (audioEffectPreferenceHandler.isIntensityOn()) {
                mixpanel.getPeople().increment(PARAM_SONG_WITH_INTENSITY, 1);
                try {
                    properties.put(AnalyticsHelper.PARAM_SONG_WITH_INTENSITY, true);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }

        mixpanel.track(EVENT_TRACK_SELECTION_CHANGED, properties);

    }

    public static void trackHeadPhoneUsed(Context context, String headPhoneType) throws JSONException {

        HashMap<String, String> articleParams = new HashMap<>();
        articleParams.put(AnalyticsHelper.PARAM_SELECTED_HEADPHONE_TYPE, "over_ear");
        FlurryAnalyticHelper.logEvent(AnalyticsHelper.EVENT_HEADPHONE_TYPE_CHANGED, articleParams);

        MixpanelAPI mixpanel = MixPanelAnalyticHelper.getInstance(context);
        JSONObject properties = new JSONObject();
        properties.put(PARAM_SELECTED_HEADPHONE_TYPE, headPhoneType);
        mixpanel.track(EVENT_HEADPHONE_TYPE_CHANGED, properties);
        mixpanel.registerSuperProperties(properties);
        mixpanel.getPeople().set(properties);
    }

    public void logCommonEvent(Context context, String event) {
       FlurryAgent.logEvent(event);
        MixPanelAnalyticHelper.getInstance(context).track(event);
    }
}
