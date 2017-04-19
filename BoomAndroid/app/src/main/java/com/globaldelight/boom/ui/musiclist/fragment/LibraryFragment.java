package com.globaldelight.boom.ui.musiclist.fragment;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.globaldelight.boom.R;
import com.globaldelight.boom.analytics.FlurryAnalyticHelper;
import com.globaldelight.boom.business.BusinessUtils;
import com.globaldelight.boom.manager.HeadPhonePlugReceiver;
import com.globaldelight.boom.ui.musiclist.adapter.utils.SectionsPagerAdapter;
import com.globaldelight.boom.ui.widgets.CoachMarkerWindow;
import com.globaldelight.boom.utils.Utils;
import com.globaldelight.boom.utils.handlers.Preferences;

import static com.globaldelight.boom.ui.widgets.CoachMarkerWindow.DRAW_NORMAL_BOTTOM;
import static com.globaldelight.boom.utils.handlers.Preferences.HEADPHONE_CONNECTED;
import static com.globaldelight.boom.utils.handlers.Preferences.TOOLTIP_CHOOSE_HEADPHONE_LIBRARY;
import static com.globaldelight.boom.utils.handlers.Preferences.TOOLTIP_OPEN_EFFECT_MINI_PLAYER;
import static com.globaldelight.boom.utils.handlers.Preferences.TOOLTIP_SWITCH_EFFECT_SCREEN_EFFECT;
import static com.globaldelight.boom.utils.handlers.Preferences.TOOLTIP_USE_24_HEADPHONE_LIBRARY;
import static com.globaldelight.boom.utils.handlers.Preferences.TOOLTIP_USE_HEADPHONE_LIBRARY;

/**
 * Created by Rahul Agarwal on 27-02-17.
 */

public class LibraryFragment extends Fragment {
    private Activity mActivity;
    View rootView;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private TabLayout mTabBar;
    private ViewPager mViewPager;
    private LinearLayout mAddsContainer;
    private CoachMarkerWindow coachMarkUseHeadPhone, coachMarkChooseHeadPhone;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Activity){
            mActivity = (Activity) context;
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_music_library, container, false);
        if(null == mActivity)
            mActivity = getActivity();
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initViews();
        FlurryAnalyticHelper.init(mActivity);
    }

    private void initViews() {
        mAddsContainer = (LinearLayout) rootView.findViewById(R.id.lib_add_container);
        mTabBar= (TabLayout)  rootView.findViewById(R.id.tabLayout);
        mViewPager = (ViewPager) rootView.findViewById(R.id.container);

        setupViewPager(mViewPager);

        Typeface font = Typeface.createFromAsset(mActivity.getAssets(), "fonts/TitilliumWeb-SemiBold.ttf");
        for (int i = 0; i < mTabBar.getChildCount(); i++) {
            final View view = mTabBar.getChildAt(i);
            if (view instanceof TextView) {
                ((TextView) view).setTypeface(font);
                ((TextView) view).setTextSize(getResources().getDimension(R.dimen.music_tab_txt_size));
            }
        }
    }

    private void setupViewPager(ViewPager viewPager) {
        int[] items = {R.string.artists, R.string.albums, R.string.songs, R.string.playlists, R.string.genres};
        mSectionsPagerAdapter = new SectionsPagerAdapter(getChildFragmentManager());
        mSectionsPagerAdapter.addFragment(mActivity, new ArtistsListFragment(), items[0]);
        mSectionsPagerAdapter.addFragment(mActivity, new AlbumsListFragment(), items[1]);
        mSectionsPagerAdapter.addFragment(mActivity, new SongsListFragment(), items[2]);
        mSectionsPagerAdapter.addFragment(mActivity, new PlayListsFragment(), items[3]);
        mSectionsPagerAdapter.addFragment(mActivity, new GenresListFragment(), items[4]);
        viewPager.setAdapter(mSectionsPagerAdapter);
        viewPager.setOffscreenPageLimit(items.length);
        mTabBar.setupWithViewPager(mViewPager);
    }

    public void updateAdds(BusinessUtils.AddSource addSources, boolean isAddEnable, View addContainer) {
        if(null != getActivity()) {
            mAddsContainer.removeAllViews();
            mAddsContainer.addView(addContainer);
            mAddsContainer.setVisibility(isAddEnable ? View.VISIBLE : View.GONE);
        }
    }

    public void useCoachMarkWindow(){
        if(HeadPhonePlugReceiver.isHeadsetConnected()){
            Preferences.writeBoolean(mActivity, HEADPHONE_CONNECTED, false);
        }
        if (null != getActivity() && (Preferences.readBoolean(mActivity, TOOLTIP_USE_HEADPHONE_LIBRARY, true) || Preferences.readBoolean(mActivity, TOOLTIP_USE_24_HEADPHONE_LIBRARY, true))
                && Preferences.readBoolean(mActivity, HEADPHONE_CONNECTED, true) && !Preferences.readBoolean(mActivity, TOOLTIP_SWITCH_EFFECT_SCREEN_EFFECT, true)
                && !Preferences.readBoolean(mActivity, TOOLTIP_OPEN_EFFECT_MINI_PLAYER, true)) {

            if(Utils.isMoreThan24Hour() || Preferences.readBoolean(mActivity, TOOLTIP_USE_HEADPHONE_LIBRARY, true)) {
                coachMarkUseHeadPhone = new CoachMarkerWindow(mActivity, DRAW_NORMAL_BOTTOM, getResources().getString(R.string.use_headphone_tooltip));
                coachMarkUseHeadPhone.setAutoDismissBahaviour(true);
                coachMarkUseHeadPhone.showCoachMark(mViewPager);

                if(Utils.isMoreThan24Hour())
                    Preferences.writeBoolean(mActivity, TOOLTIP_USE_24_HEADPHONE_LIBRARY, false);
            }

            Preferences.writeBoolean(mActivity, TOOLTIP_USE_HEADPHONE_LIBRARY, false);
        }
    }

    public void setDismissHeadphoneCoachmark(){
        if(null != getActivity()) {
            if (null != coachMarkUseHeadPhone)
                coachMarkUseHeadPhone.dismissTooltip();

            if (null != coachMarkChooseHeadPhone)
                coachMarkChooseHeadPhone.dismissTooltip();
        }
    }

    public void setAutoDismissBahaviour(){
        if(null != getActivity()) {
            if (null != coachMarkUseHeadPhone) {
                coachMarkUseHeadPhone.setAutoDismissBahaviour(true);
            }
            if (null != coachMarkChooseHeadPhone) {
                coachMarkChooseHeadPhone.setAutoDismissBahaviour(true);
            }
        }
    }

    public void chooseCoachMarkWindow(boolean isPlayerExpended, boolean isLibraryRendered) {
        if (null != getActivity() &&  Preferences.readBoolean(mActivity, TOOLTIP_CHOOSE_HEADPHONE_LIBRARY, true) && !isPlayerExpended && HeadPhonePlugReceiver.isHeadsetConnected() && isLibraryRendered ) {
            if(null != coachMarkUseHeadPhone)
                coachMarkUseHeadPhone.dismissTooltip();
            coachMarkChooseHeadPhone = new CoachMarkerWindow(mActivity, DRAW_NORMAL_BOTTOM, getResources().getString(R.string.choose_headphone_tooltip));
            coachMarkChooseHeadPhone.setAutoDismissBahaviour(true);
            coachMarkChooseHeadPhone.showCoachMark(mViewPager);
            Preferences.writeBoolean(mActivity, TOOLTIP_CHOOSE_HEADPHONE_LIBRARY, false);
        }
    }
    @Override
    public  void onStart() {
        super.onStart();
        FlurryAnalyticHelper.flurryStartSession(mActivity);
    }

    @Override
    public void onStop() {
        super.onStop();
        FlurryAnalyticHelper.flurryStopSession(mActivity);
    }

}
