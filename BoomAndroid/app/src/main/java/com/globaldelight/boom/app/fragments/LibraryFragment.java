package com.globaldelight.boom.app.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.TextView;

import com.globaldelight.boom.BuildConfig;
import com.globaldelight.boom.R;
import com.globaldelight.boom.app.activities.MainActivity;
import com.globaldelight.boom.app.adapters.search.SearchSuggestionAdapter;
import com.globaldelight.boom.app.receivers.actions.PlayerEvents;
import com.globaldelight.boom.app.service.HeadPhonePlugReceiver;
import com.globaldelight.boom.app.adapters.utils.SectionsPagerAdapter;
import com.globaldelight.boom.view.CoachMarkerWindow;
import com.globaldelight.boom.utils.Utils;
import com.globaldelight.boom.app.sharedPreferences.Preferences;

import static android.content.Context.SEARCH_SERVICE;
import static com.globaldelight.boom.app.receivers.actions.PlayerEvents.ACTION_HEADSET_PLUGGED;
import static com.globaldelight.boom.app.sharedPreferences.Preferences.LIBRARY_CURRENT_TAB;
import static com.globaldelight.boom.app.sharedPreferences.Preferences.TOOLTIP_SWITCH_EFFECT_SCREEN_EFFECT;
import static com.globaldelight.boom.view.CoachMarkerWindow.DRAW_NORMAL_BOTTOM;
import static com.globaldelight.boom.app.sharedPreferences.Preferences.HEADPHONE_CONNECTED;
import static com.globaldelight.boom.app.sharedPreferences.Preferences.TOOLTIP_CHOOSE_HEADPHONE_LIBRARY;
import static com.globaldelight.boom.app.sharedPreferences.Preferences.TOOLTIP_USE_24_HEADPHONE_LIBRARY;
import static com.globaldelight.boom.app.sharedPreferences.Preferences.TOOLTIP_USE_HEADPHONE_LIBRARY;

/**
 * Created by Rahul Agarwal on 27-02-17.
 */

public class LibraryFragment extends Fragment {
    private Activity mActivity;
    private View rootView;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private TabLayout mTabBar;
    private ViewPager mViewPager;
    private CoachMarkerWindow coachMarkUseHeadPhone, coachMarkChooseHeadPhone;


    private BroadcastReceiver headPhoneReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            MainActivity activity = (MainActivity)getActivity();
            switch ( intent.getAction() ) {
                case ACTION_HEADSET_PLUGGED:
                    setDismissHeadphoneCoachmark();
                    chooseCoachMarkWindow(activity.isPlayerExpended(), activity.isLibraryRendered);
                    break;

                case PlayerEvents.ACTION_PLAYER_STATE_CHANGED:
                    useCoachMarkWindow();
                    chooseCoachMarkWindow(activity.isPlayerExpended(), activity.isLibraryRendered);
                    break;
            }
        }
    };


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
        setHasOptionsMenu(true);
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initViews();
    }

    private void initViews() {
        mTabBar= rootView.findViewById(R.id.tabLayout);
        mViewPager = rootView.findViewById(R.id.container);

        setupViewPager(mViewPager);
        getActivity().setTitle(R.string.music_library);
    }


    private void registerHeadSetReceiver(){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_HEADSET_PLUGGED);
        intentFilter.addAction(PlayerEvents.ACTION_PLAYER_STATE_CHANGED);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(headPhoneReceiver, intentFilter);
    }


    private void setupViewPager(ViewPager viewPager) {
        mSectionsPagerAdapter = new SectionsPagerAdapter(getChildFragmentManager());
        mSectionsPagerAdapter.addFragment(mActivity, new ArtistsListFragment(), R.string.artists);
        mSectionsPagerAdapter.addFragment(mActivity, new AlbumsListFragment(), R.string.albums);
        mSectionsPagerAdapter.addFragment(mActivity, new SongsListFragment(), R.string.songs);
        mSectionsPagerAdapter.addFragment(mActivity, new PlayListsFragment(), R.string.playlists);
        mSectionsPagerAdapter.addFragment(mActivity, new GenresListFragment(), R.string.genres);
        viewPager.setAdapter(mSectionsPagerAdapter);
        viewPager.setOffscreenPageLimit(5);
        mTabBar.setupWithViewPager(mViewPager);
        viewPager.setCurrentItem(Preferences.readInteger(getActivity(), LIBRARY_CURRENT_TAB, 2));
    }

    public void useCoachMarkWindow(){
        if (BuildConfig.FLAVOR == "whitelabel" ) {
            autoOpenPlayer();
            return;
        }

        if( HeadPhonePlugReceiver.isHeadsetConnected() ){
            autoOpenPlayer();
            Preferences.writeBoolean(mActivity, HEADPHONE_CONNECTED, false);
        }
        if (null != getActivity() && (Preferences.readBoolean(mActivity, TOOLTIP_USE_HEADPHONE_LIBRARY, true) || Preferences.readBoolean(mActivity, TOOLTIP_USE_24_HEADPHONE_LIBRARY, true))
                && Preferences.readBoolean(mActivity, HEADPHONE_CONNECTED, true) ) {

            if(Utils.isMoreThan24Hour() || Preferences.readBoolean(mActivity, TOOLTIP_USE_HEADPHONE_LIBRARY, true)) {
                coachMarkUseHeadPhone = new CoachMarkerWindow(mActivity, DRAW_NORMAL_BOTTOM, getResources().getString(R.string.use_headphone_tooltip));
                coachMarkUseHeadPhone.setAutoDismissBahaviour(true);
                coachMarkUseHeadPhone.showCoachMark(mViewPager);
                coachMarkUseHeadPhone.setOnDismissListener(new CoachMarkerWindow.OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        autoOpenPlayer();
                    }
                });

                if(Utils.isMoreThan24Hour())
                    Preferences.writeBoolean(mActivity, TOOLTIP_USE_24_HEADPHONE_LIBRARY, false);
            }

            Preferences.writeBoolean(mActivity, TOOLTIP_USE_HEADPHONE_LIBRARY, false);
        }
    }

    private float mToolbarElevation;
    @Override
    public void onStart() {
        super.onStart();

        Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
        mToolbarElevation = toolbar.getElevation();
        mTabBar.setElevation(mToolbarElevation);
        toolbar.setElevation(0);
    }

    @Override
    public void onStop() {
        Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
        toolbar.setElevation(mToolbarElevation);
        Preferences.writeInteger(getActivity(), LIBRARY_CURRENT_TAB, mViewPager.getCurrentItem());

        super.onStop();
    }


    @Override
    public void onPause() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(headPhoneReceiver);
        super.onPause();
    }


    @Override
    public void onResume() {
        registerHeadSetReceiver();
        super.onResume();
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
        if (BuildConfig.FLAVOR == "whitelabel" ) {
            return;
        }

        if ( Preferences.readBoolean(mActivity, TOOLTIP_SWITCH_EFFECT_SCREEN_EFFECT, true) ) {
            return;
        }

        if (null != getActivity() &&  Preferences.readBoolean(mActivity, TOOLTIP_CHOOSE_HEADPHONE_LIBRARY, true) && !isPlayerExpended && HeadPhonePlugReceiver.isHeadsetConnected() && isLibraryRendered ) {
            if(null != coachMarkUseHeadPhone)
                coachMarkUseHeadPhone.dismissTooltip();
            coachMarkChooseHeadPhone = new CoachMarkerWindow(mActivity, DRAW_NORMAL_BOTTOM, getResources().getString(R.string.choose_headphone_tooltip));
            coachMarkChooseHeadPhone.setAutoDismissBahaviour(true);
            coachMarkChooseHeadPhone.showCoachMark(mViewPager);
            Preferences.writeBoolean(mActivity, TOOLTIP_CHOOSE_HEADPHONE_LIBRARY, false);
        }
    }

    private void autoOpenPlayer() {
        if ( Preferences.readBoolean(mActivity, TOOLTIP_SWITCH_EFFECT_SCREEN_EFFECT, true) ) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if ( !MainActivity.isPlayerExpended() ) {
                        ((MainActivity)getActivity()).toggleSlidingPanel();
                    }
                }
            }, 1000);
        }
    }

    private Fragment mSearchResult;
    private SearchSuggestionAdapter searchSuggestionAdapter;
    public static String[] columns = new String[]{"_id", "FEED_TITLE"};

    private SearchView searchView;

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflator) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflator.inflate(R.menu.library_menu, menu);
        MenuItem searchMenuItem = menu.findItem(R.id.action_search);
        searchView = (SearchView)searchMenuItem.getActionView();
        searchView.setQueryHint(getResources().getString(R.string.search_hint));
        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getActivity().getSystemService(SEARCH_SERVICE);

        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));

        ActionBar.LayoutParams params = new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT);
        searchView.setLayoutParams(params);
        searchView.setDrawingCacheBackgroundColor(ContextCompat.getColor(getActivity(), R.color.transparent));
        searchView.setMaxWidth(2000);
        searchView.setIconified(true);


        searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int position) {
                onSuggetionSelected(position);
                return true;
            }

            @Override
            public boolean onSuggestionClick(int position) {
                onSuggetionSelected(position);
                return true;
            }
        });

        searchSuggestionAdapter = new SearchSuggestionAdapter(getActivity(), R.layout.card_search_suggestion_item, null, columns,null, -1000);
        searchView.setSuggestionsAdapter(searchSuggestionAdapter);
        registerSearchListeners();

        searchMenuItem.setOnActionExpandListener( new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                onSearchExapnded();
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                onSearchCollapsed(item);
                return true;
            }
        });
       super.onCreateOptionsMenu(menu, inflator);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_search) {
            return true;
        }
        return false;
    }


    private void onSearchExapnded() {
        mSearchResult = new SearchViewFragment();
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .add(R.id.fragment_container, mSearchResult)
                .commitAllowingStateLoss();
        animateSearchToolbar(1, true, true);
    }

    private void onSearchCollapsed(MenuItem item) {
        searchSuggestionAdapter.changeCursor(null);
        if (item.isActionViewExpanded()) {
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .remove(mSearchResult)
                    .commitAllowingStateLoss();
            mSearchResult = null;
            animateSearchToolbar(1, false, false);
        }
    }

    private void onSuggetionSelected(int position) {
        Cursor cursor = (Cursor) searchView.getSuggestionsAdapter().getItem(position);
        String feedName = cursor.getString(1);
        searchView.setQuery(feedName, false);
        fetchAndUpdateSearchResult(feedName);
    }


    private MatrixCursor convertToCursor(Cursor feedlyResults) {
        MatrixCursor cursor = new MatrixCursor(columns);
        if (feedlyResults != null && feedlyResults.moveToFirst()) {
            do{
                String[] temp = new String[2];
                temp[0] = Integer.toString(feedlyResults.getInt(0));
                temp[1] = feedlyResults.getString(1);
                cursor.addRow(temp);
            } while (feedlyResults.moveToNext());
        }
        return cursor;
    }

    private void registerSearchListeners() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                fetchAndUpdateSearchResult(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                if(query.length() >= 2) {
                    MatrixCursor matrixCursor = convertToCursor(((MainActivity)getActivity()).musicSearchHelper.getSongList(query));
                    searchSuggestionAdapter.changeCursor(matrixCursor);
                }
                if(null == query || query.length() < 2){
                    searchSuggestionAdapter.changeCursor(null);
                }
                return false;
            }
        });
    }

    private void animateSearchToolbar(int numberOfMenuIcon, boolean containsOverflow, boolean show) {
        Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
        if ( toolbar == null ) {
            return;
        }

        toolbar.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.search_view_fade));

        if (show) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                int width = toolbar.getWidth() -
                        (containsOverflow ? getResources().
                                getDimensionPixelSize(R.dimen.abc_action_button_min_width_overflow_material) : 0) -
                        ((getResources().getDimensionPixelSize(R.dimen.abc_action_button_min_width_material)
                                * numberOfMenuIcon) / 2);
                Animator createCircularReveal = ViewAnimationUtils.createCircularReveal(toolbar,
                        isRtl(getResources())
                                ? toolbar.getWidth() - width : width, toolbar.getHeight()
                                / 2, 0.0f, (float) width);
                createCircularReveal.setDuration(250);
                createCircularReveal.start();
            } else {
                TranslateAnimation translateAnimation = new TranslateAnimation(0.0f, 0.0f, (float) (-toolbar.getHeight()), 0.0f);
                translateAnimation.setDuration(220);
                toolbar.clearAnimation();
                toolbar.startAnimation(translateAnimation);
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                int width = toolbar.getWidth() -
                        (containsOverflow ? getResources().getDimensionPixelSize(R.dimen.abc_action_button_min_width_overflow_material) : 0) -
                        ((getResources().getDimensionPixelSize(R.dimen.abc_action_button_min_width_material) * numberOfMenuIcon) / 2);
                Animator createCircularReveal = ViewAnimationUtils.createCircularReveal(toolbar,
                        isRtl(getResources()) ? toolbar.getWidth() - width : width, toolbar.getHeight() / 2, (float) width, 0.0f);
                createCircularReveal.setDuration(250);
                createCircularReveal.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                    }
                });
                createCircularReveal.start();
            } else {
                AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0.0f);
                Animation translateAnimation = new TranslateAnimation(0.0f, 0.0f, 0.0f, (float) (-toolbar.getHeight()));
                AnimationSet animationSet = new AnimationSet(true);
                animationSet.addAnimation(alphaAnimation);
                animationSet.addAnimation(translateAnimation);
                animationSet.setDuration(220);
                animationSet.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                toolbar.startAnimation(animationSet);
            }
        }
    }

    private boolean isRtl(Resources resources) {
        return resources.getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
    }

    private void fetchAndUpdateSearchResult(String query) {
        if(null != mSearchResult)
            ((SearchViewFragment) mSearchResult).updateSearchResult(query);
        searchView.clearFocus();
    }


}
