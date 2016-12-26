package com.globaldelight.boom.analytics;

import android.app.Application;
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

    public static final String EVENT_OPEN_STORE = "store_opened";
    public static final String PARAM_DATE = "date";
    public static final String EVENT_PURCHASE_FAILED = "purchase_failed";
    public static final String EVENT_PURCHASE_CANCELLED = "purchase_cancelled";
    public static final String EVENT_PURCHASE_RESTORED = "purchase_restored";
    public static final String EVENT_PURCHASE_SUCCESS = "purchase_success";
    public static final String PARAM_PURCHASED_ITEM = "purchased_item";
    public static final String PARAM_REMAINING_DAYS = "remiaining_days";
    public static final String EVENT_EFFECT_PACK_PURCHASE = "effect_pack_purchase";

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

    public void storeOpened(Context context, String event) {
        FlurryAnalyticHelper.logEvent(EVENT_OPEN_STORE);
        JSONObject properties = new JSONObject();
        //properties.put(PARAM_DATE, headPhoneType);
    }
}

/*

import UIKit

@objc class BMAnalyticsHandler: NSObject {


        let flurryAnalyticsHandler:BMFlurryAnalyticsHandler = BMFlurryAnalyticsHandler()

static let sharedInstance = BMAnalyticsHandler()


        var productionBuild:Bool = false


        var appsFlyerHandler = DZAppsFlyerAnalyticsHandler()


        var mixpanelHandler = BMMixPanelHandler()


@objc func surveyAlertShown() {
        flurryAnalyticsHandler.trackEvent(eventName: "Survey Alert Shown", withInfo: nil)
        mixpanelHandler.trackEvent("Survey Alert Shown")
        }


@objc func optedForSurvey(){
        flurryAnalyticsHandler.trackEvent(eventName: "Opted For Survey From Alert", withInfo: nil)
        mixpanelHandler.trackEvent("Opted For Survey From Alert")
        }


@objc func surveyIgnored(){
        flurryAnalyticsHandler.trackEvent(eventName: "Survey ignored from alert", withInfo: nil)
        mixpanelHandler.trackEvent("Survey ignored from alert")
        }


@objc func surveyCaptured(){
        flurryAnalyticsHandler.trackEvent(eventName: "Survey Captured Successfuly", withInfo: nil)
        mixpanelHandler.trackEvent("Survey Captured Successfuly")
        }


        func musicSourceAnalytics(info:Dictionary<String,AnyObject>){


        mixpanelHandler.trackEvents(withInfo: [kEventName:"Song Music Source",kProperties:info])
        flurryAnalyticsHandler.trackEvent(eventName: "Song Music Source", withInfo: info)


        }


        func playSongFromWidget(){
        flurryAnalyticsHandler.trackEvent(eventName: "song play from widget", withInfo: nil)

        }


        func openLibraryFromSlide(){
        flurryAnalyticsHandler.trackEvent(eventName: "open Library From Slide", withInfo: nil)
        }


        func openQueueFromSlide(){
        flurryAnalyticsHandler.trackEvent(eventName: "open Queue From Slide", withInfo: nil)

        }


        func openEffectsFromSlide(){
        flurryAnalyticsHandler.trackEvent(eventName: "open Effects From Slide", withInfo: nil)
        }


        func queueFrom3D(){
        flurryAnalyticsHandler.trackEvent(eventName: "Open Queue from 3D Touch", withInfo: nil)

        }


        func libraryFrom3D(){
        flurryAnalyticsHandler.trackEvent(eventName: "Open Library from 3D Touch", withInfo: nil)

        }


        func didTapOnMoreOptionButton(){
        flurryAnalyticsHandler.trackEvent(eventName: "Did Tap On More Option Button", withInfo: nil)

        }




        func pushNotificationVideoPlayed(){
        mixpanelHandler.incrementPeopleProperty("Push Notification Video Played", byValue: 1)
        flurryAnalyticsHandler.trackEvent(eventName: "Push Notification Video Played", withInfo: nil)
        }


        func pushNotificationImageLoaded(){
        mixpanelHandler.incrementPeopleProperty("Push Notification Image Loaded", byValue: 1)
        flurryAnalyticsHandler.trackEvent(eventName: "Push Notification Image Loaded", withInfo: nil)

        }


        func pushNotificationImageDoneButtonTapped(){
        mixpanelHandler.incrementPeopleProperty("Push Notification Image Done Button Tapped", byValue: 1)
        flurryAnalyticsHandler.trackEvent(eventName: "Push Notification Image Done Button Tapped", withInfo: nil)

        }


        func pushNotificationImageCancelButtonTapped(){
        mixpanelHandler.incrementPeopleProperty("Push Notification Image Cancel Button Tapped", byValue: 1)
        flurryAnalyticsHandler.trackEvent(eventName: "Push Notification Image Cancel Button Tapped", withInfo: nil)

        }


        func pushNotificationVideoDoneButtonTapped(){
        mixpanelHandler.incrementPeopleProperty("Push Notification Video Done Button Tapped", byValue: 1)
        flurryAnalyticsHandler.trackEvent(eventName: "Push Notification Video Done Button Tapped", withInfo: nil)

        }


        func pushNotificationVideoCancelButtonTapped(){
        mixpanelHandler.incrementPeopleProperty("Push Notification Video Cancel Button Tapped", byValue: 1)
        flurryAnalyticsHandler.trackEvent(eventName: "Push Notification Video Cancel Button Tapped", withInfo: nil)

        }




        func trackEvent(_ inEvent:String, withInfo inInfo:Dictionary<String,AnyObject>?) -> Void {


        let properties:Dictionary<String,AnyObject> = [kEventAction:inEvent as AnyObject]


        flurryAnalyticsHandler.trackEventWithInfo(properties)


//        mixpanelHandler.trackEventsWithInfo([kEventName:inEvent])

        }


        func trackAirDropAnalytics(){
        mixpanelHandler.incrementPeopleProperty("Import Files With Boom", byValue: 1)
        mixpanelHandler.trackEvents(withInfo: [kEventName:"Import Files With Boom"])
        }


        func trackFirstLaunch() {


        var firstLaunchDate = UserDefaults.standard.object(forKey: kApplicationFirstLaunchDate)


        if firstLaunchDate == nil {


        firstLaunchDate = Date()


        appsFlyerHandler.appFirstLaunch()
        mixpanelHandler.trackAppFirstLaunch()
        UserDefaults.standard.set(firstLaunchDate, forKey: kApplicationFirstLaunchDate)
        }
        }


        func startLaunchTracking(){
        appsFlyerHandler.trackAppLaunch()
        trackFirstLaunch()
        appsFlyerHandler.appOpenTrack()
        mixpanelHandler.trackApplicationLaunchDate()
        startFacebookLaunchTracking()
        }


        func  startTracking() -> Void {


        NotificationCenter.default.addObserver(self, selector: #selector(bm_appExpired), name: NSNotification.Name.tempPassEnded, object: nil)


        flurryAnalyticsHandler.productionBuild = productionBuild
        appsFlyerHandler.startTracking()
        flurryAnalyticsHandler.startTracking()
        mixpanelHandler.startTracking()


        }


        func bm_appExpired(_ inNotification:Notification){


        BMAnalyticsHandler.sharedInstance.trackEvent("Trial Expired", withInfo: nil)
        mixpanelHandler.setPeopleProperty([kEventName:"Trial Expired"])
        mixpanelHandler.trackEvents(withInfo: [kEventName:"Trial Expired"])


        }




        //MARK:- Push notification


        func registerForRemoteNotificationsWithDeviceToken(_ deviceToken:Data){
        mixpanelHandler.trackEvents(withInfo: [kEventName:kPushNotificationTokenReceived])
        mixpanelHandler.registerForMixpanelRemoteNotifications(withDeviceToken: deviceToken)
        appsFlyerHandler.registerPushNotificationToken(deviceToken)
        }
        func trackPushNotificationWithInfo(_ info:Dictionary<String,AnyObject>){


        var eventName = "PushNotificationReceived";
        if(info["campaign"] != nil){


        eventName =  eventName + "_" + (info["campaign"] as! String);

        }
        trackEvent(eventName, withInfo:nil)
        mixpanelHandler.trackPushNotification(withInfo: info)
        }


        //MARK:- Tutorial


        func  trackTutorialLaunch(){
        trackEvent(kStartedOnBoarding, withInfo:nil)
        bm_trackTutorialLaunch()
        }


        func  trackTutorialClose(_ isFirstAttempt:Bool){
        trackEvent(kEndedOnBoarding, withInfo:nil)
        bm_trackTutorialClose(isFirstAttempt)
        }


        func bm_trackTutorialLaunch(){


        mixpanelHandler.trackEvents(withInfo: [kEventName:kStartedOnBoarding])


        }


        func  bm_trackTutorialClose(_ isFirstAttempt:Bool){
        mixpanelHandler.trackEvents(withInfo: [kEventName:kEndedOnBoarding])
        mixpanelHandler.setSuperProperties([kOnboardingCompletedDate:Date(),
        kOnboardingCompletedOnFirstAttempt:isFirstAttempt])
        mixpanelHandler.setPeopleProperty([kOnboardingCompletedDate:Date(),
        kOnboardingCompletedOnFirstAttempt:isFirstAttempt])




        }






        //MARK:- Store


        func trackInAppPurchaseWithInfoWithApsFlyer(_ inInfo:Dictionary<String,AnyObject>) -> Void {


        appsFlyerHandler.trackInAppPurchase(withInfo: inInfo)


        }


        func trackStoreOpen(_ ScreenName:String){


        let remainingDays = BMGateKeeper.shared().passHandler().passRemainingDays()
        mixpanelHandler.trackEvents(withInfo: [kEventName:kStoreOpened,kProperties:[kOpenedFrom:ScreenName,kRemaingDays:remainingDays]])
        mixpanelHandler.setPeopleProperty([kTappedOnStoreOnDay:remainingDays])

        }


        func trackTapOnBuy(){


        let remainingDays = BMGateKeeper.shared().passHandler().passRemainingDays()
        mixpanelHandler.trackEvents(withInfo: [kEventName:kTappedOnBuy,kProperties:[kRemaingDays:remainingDays]])
        tapOnBuyButton()

        }


        func trackTapOnRestore(){
        let remainingDays = BMGateKeeper.shared().passHandler().passRemainingDays()
        mixpanelHandler.trackEvents(withInfo: [kEventName:kTappedOnRestore,kProperties:[kRemaingDays:remainingDays]])

        }


        func createdNewPlaylist() {
        mixpanelHandler.trackEvent(kCreatedNewPlaylist)
        }


        func trackPurchaseCompleted(_ info:NSDictionary,isRestore:Bool){


        let purchasedItemList = info[kPurchasedProductsList];
        if(isRestore){
        mixpanelHandler.trackEvents(withInfo: [kEventName:kRestoreCompleted,kProperties:[kPurchasedProductsList:purchasedItemList!] as NSDictionary])
        flurryAnalyticsHandler.trackEventWithInfo([kEventAction:(kRestoreCompleted as String) as AnyObject])
        appsFlyerHandler.trackEvent(withInfo: [kEventAction:(kRestoreCompleted as String) as AnyObject])


        }else{


        purchaseSuccessful()
        mixpanelHandler.trackEvents(withInfo: [kEventName:kPurchaseCompleted,kProperties:[kPurchasedProductsList:purchasedItemList!] as NSDictionary])
        mixpanelHandler.setPeopleProperty([kPurchasedProductsList:purchasedItemList!])
        mixpanelHandler.setSuperProperties([kEffectspackPurchased:true])
        mixpanelHandler.setPeopleProperty([kEffectspackPurchased:true])
        flurryAnalyticsHandler.trackEventWithInfo([kEventAction:(kPurchaseCompleted as String) as AnyObject])
        appsFlyerHandler.trackEvent(withInfo: [kEventAction:(kPurchaseCompleted as String) as AnyObject])

        }
        }


        func trackPurchaseFailed(){
        let remainingDays = BMGateKeeper.shared().passHandler().passRemainingDays()
        mixpanelHandler.trackEvents(withInfo: [kEventName:kPurchaseFailed,kProperties:[kRemaingDays:remainingDays]])
        flurryAnalyticsHandler.trackEventWithInfo([kEventAction:(kPurchaseFailed as String) as AnyObject]);

        }


        func trackPurchaseCancelled(){
        let remainingDays = BMGateKeeper.shared().passHandler().passRemainingDays()
        mixpanelHandler.trackEvents(withInfo: [kEventName:kPurchaseCancelled,kProperties:[kRemaingDays:remainingDays]])
        flurryAnalyticsHandler.trackEventWithInfo([kEventAction:(kPurchaseCancelled as String) as AnyObject] );

        }


        //MARK:- Effects Screen
        func trackEffectsState(_ state:Bool){


        mixpanelHandler.trackEvents(withInfo: [kEventName:kEffectStateChanged,kProperties:[kState:state]])

        }
        func track3DState(_ state:Bool){


        mixpanelHandler.trackEvents(withInfo: [kEventName:k3DStateChanged,kProperties:[kState:state]])

        }
        func trackEQState(_ state:Bool){
        mixpanelHandler.trackEvents(withInfo: [kEventName:kEQStateChanged,kProperties:[kState:state]])

        }
        func trackIntensityState(_ state:Bool){
        mixpanelHandler.trackEvents(withInfo: [kEventName:kIntensityStateChanged,kProperties:[kState:state]])

        }


        //MARK:Songs


        func updatePlaybackSessionDuration(_ duration:Float32){


        mixpanelHandler.incrementPeopleProperty(kMusicSessionDuration,byValue:duration as NSNumber!)
        mixpanelHandler.trackEvents(withInfo: [kEventName:kMusicSessionDuration,kProperties:[kMusicSessionDuration:duration]])

        }


        func songSelectionChanged(_ songsInfo:NSDictionary)  {


        let properties  = songsInfo;


        mixpanelHandler.incrementPeopleProperty(kSongCount, byValue: 1)


        let audioState:BMAudioSystemState = BMDefaults.shared().audioSystemState()
        if(audioState == BMAudioSystemState.on && BMDefaults.shared().effectStateforEffect(BMAudioEffectType.effect3D) == 1){
        mixpanelHandler.incrementPeopleProperty(kEffectsWith3D, byValue: 1)
        properties.setValue(true, forKey: kEffectsWith3D)
        }
        else if(audioState == BMAudioSystemState.on){
        mixpanelHandler.incrementPeopleProperty(kEffectsWith2D, byValue: 1)
        properties.setValue(true, forKey: kEffectsWith2D)

        }


        if(audioState == BMAudioSystemState.on && BMDefaults.shared().effectStateforEffect(BMAudioEffectType.effectFidelity) == 1){
        mixpanelHandler.incrementPeopleProperty(kEffectsWithIntensity, byValue: 1)
        properties.setValue(true, forKey: kEffectsWithIntensity)

        }


        if(audioState == BMAudioSystemState.on && BMDefaults.shared().effectStateforEffect(BMAudioEffectType.effectEqualizer) == 1){
        mixpanelHandler.incrementPeopleProperty(kEffectsWithEQ, byValue: 1)
        properties.setValue(true, forKey: kEffectsWithEQ)

        }
        if(songsInfo[kTrendingArtist] != nil){
        let artistName = songsInfo[kTrendingArtist]
        mixpanelHandler.setPeopleProperty([kArtistName:artistName!])
        }


        if(songsInfo[kTrendingGenre] != nil){
        let genre = songsInfo[kTrendingGenre]
        mixpanelHandler.setPeopleProperty([kGenre:genre!])
        }
        mixpanelHandler.trackEvents(withInfo: [kEventName:kTrackSelectionChanged,kProperties:properties])

        }
        func emailIdReceived(_ email:String){
        mixpanelHandler.createAlias(withUserId: email)


        }
        //MARK:Headphone Types
        func trackHeadPhonetypeUsed(_ headPhone:String){


        mixpanelHandler.trackEvents(withInfo: [kEventName:kHeadphoneTypeChanged,kProperties:[kHeadphoneType:headPhone]])
        mixpanelHandler.setSuperProperties([kHeadphoneType:headPhone])
        mixpanelHandler.setPeopleProperty([kHeadphoneType:headPhone])


        }


        func silentNotificationReceived() {


        let kEventName = "Silent Notification Received"
        let properties:Dictionary<String,AnyObject> = [kEventAction:kEventName as AnyObject]


        mixpanelHandler.trackEvent(kEventName)
        flurryAnalyticsHandler.trackEventWithInfo(properties)
        }


        }





*/
