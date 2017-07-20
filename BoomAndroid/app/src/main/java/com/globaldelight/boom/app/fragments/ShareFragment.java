package com.globaldelight.boom.app.fragments;

import android.content.BroadcastReceiver;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.applinks.AppLinkData;
import com.facebook.share.Sharer;
import com.facebook.share.model.AppInviteContent;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.AppInviteDialog;
import com.facebook.share.widget.ShareDialog;
import com.globaldelight.boom.R;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;
import com.twitter.sdk.android.core.services.StatusesService;
import com.twitter.sdk.android.tweetcomposer.Card;
import com.twitter.sdk.android.tweetcomposer.ComposerActivity;
import com.twitter.sdk.android.tweetcomposer.TweetComposer;
import com.twitter.sdk.android.tweetcomposer.TweetUploadService;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import io.fabric.sdk.android.DefaultLogger;
import io.fabric.sdk.android.Fabric;

/**
 * Created by Manoj Kumar on 5/17/2017.
 */

public class ShareFragment extends Fragment {

    public static final String ACTION_SHARE_SUCCESS = "com.globaldelight.boom.SHARE_SUCCESS";
    public static final String ACTION_SHARE_FAILED = "com.globaldelight.boom.SHARE_FAILED";


    private View view;

    private CallbackManager callbackManager;
    private ShareDialog shareDialog;
    private TwitterAuthClient mAuthClient;
    private Callback<TwitterSession> mTwitterAuthCallback = new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                tweetShare();
            }

            @Override
            public void failure(TwitterException exception) {
                ShareFragment.this.onCancel();
            }
    };


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.fragment_share, container, false);

        Button shareButton = (Button)view.findViewById(R.id.facebook_share_button);
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fbShare();
            }
        });

        Button inviteButton = (Button)view.findViewById(R.id.facebook_invite_button);
        inviteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fbInvite();
            }
        });

        Button tweetButton = (Button)view.findViewById(R.id.tweet_button);
        tweetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( TwitterCore.getInstance().getSessionManager().getActiveSession() != null ) {
                    new Handler().post(new Runnable() {
                        @Override
                        public void run() {
                            tweetShare();
                        }
                    });
                }
                else {
                    getTwitterAuthClient().authorize(getActivity(),mTwitterAuthCallback);
                }
            }
        });


        return view;
    }


    @Override
    public void onStart() {
        super.onStart();

        IntentFilter filter = new IntentFilter();
        filter.addAction(TweetUploadService.UPLOAD_SUCCESS);
        filter.addAction(TweetUploadService.UPLOAD_FAILURE);
        getActivity().registerReceiver(mTweetStatusReceiver, filter);

    }

    @Override
    public void onStop() {
        super.onStop();
        getActivity().unregisterReceiver(mTweetStatusReceiver);
    }

    private void tweetShare(){
        final TwitterSession session = TwitterCore.getInstance().getSessionManager()
                .getActiveSession();
        final Intent intent = new ComposerActivity.Builder(getActivity())
                .session(session)
                .hashtags("#FeelYourMusic")
                .createIntent();
        startActivity(intent);
    }

    private void fbShare(){
        callbackManager = CallbackManager.Factory.create();
        shareDialog = new ShareDialog(this);
        if (ShareDialog.canShow(ShareLinkContent.class)) {


            ShareLinkContent linkContent = new ShareLinkContent.Builder()
                    .setContentUrl(Uri.parse("http://developers.facebook.com/android"))
                    .build();
            shareDialog.show(linkContent, ShareDialog.Mode.FEED);
        }
        shareDialog.registerCallback(callbackManager, new FacebookCallback<Sharer.Result>() {
            @Override
            public void onSuccess(Sharer.Result result) {
                ShareFragment.this.onSucess();
                Toast.makeText(getActivity(),"OnSuccess",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancel() {
                ShareFragment.this.onCancel();
                getActivity().sendBroadcast(new Intent(ACTION_SHARE_FAILED));
                Toast.makeText(getActivity(),"OnCancel",Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onError(FacebookException error) {
                ShareFragment.this.onCancel();
                getActivity().sendBroadcast(new Intent(ACTION_SHARE_FAILED));
                Toast.makeText(getActivity(),"OnError"+error.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fbInvite(){
        String AppURl = "https://fb.me/218364201905576";
        String previewImageUrl = "http://someurl/13_dp.png";

        callbackManager = CallbackManager.Factory.create();

        if (AppInviteDialog.canShow()) {
            AppInviteContent content = new AppInviteContent.Builder()
                    .setApplinkUrl(AppURl)//.setPreviewImageUrl(previewImageUrl)
                    .build();

            AppInviteDialog appInviteDialog = new AppInviteDialog(this);
            appInviteDialog.registerCallback(callbackManager,
                    new FacebookCallback<AppInviteDialog.Result>() {
                        @Override
                        public void onSuccess(AppInviteDialog.Result result) {
                            ShareFragment.this.onSucess();
                            Log.d("Invitation", "Invitation Sent Successfully");
                            Toast.makeText(getActivity(),"Invitation Sent Successfully",Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onCancel() {
                            ShareFragment.this.onCancel();
                            Toast.makeText(getActivity(),"Invitation has been canceled.",Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError(FacebookException e) {
                            ShareFragment.this.onCancel();
                            Log.d("Invitation", "Error Occured");
                            Toast.makeText(getActivity(),"Invitation-Error Occured "+e.getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    });

            appInviteDialog.show(content);
        }
    }

    private void onSucess() {
        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(new Intent(ACTION_SHARE_SUCCESS));
    }

    private void onCancel() {
        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(new Intent(ACTION_SHARE_FAILED));
    }

    private void bindView(){
        ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Share");
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        getTwitterAuthClient().onActivityResult(requestCode, resultCode, data);
        if ( callbackManager != null ) {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    private BroadcastReceiver mTweetStatusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (TweetUploadService.UPLOAD_SUCCESS.equals(intent.getAction())) {
                ShareFragment.this.onSucess();
                final Long tweetId = intent.getExtras().getLong(TweetUploadService.EXTRA_TWEET_ID);
                Toast.makeText(context,"Success Tweet"+tweetId,Toast.LENGTH_SHORT).show();
            } else {
                ShareFragment.this.onCancel();
                final Intent retryIntent = intent.getExtras().getParcelable(TweetUploadService.EXTRA_RETRY_INTENT);
                Toast.makeText(context,"Failure "+retryIntent,Toast.LENGTH_SHORT).show();
            }
        }
    };

    private TwitterAuthClient getTwitterAuthClient() {
        if ( mAuthClient == null ) {
            mAuthClient = new TwitterAuthClient();
        }

        return mAuthClient;
    };
}
