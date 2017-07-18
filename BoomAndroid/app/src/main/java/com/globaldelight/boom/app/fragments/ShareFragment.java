package com.globaldelight.boom.app.fragments;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import com.facebook.share.Sharer;
import com.facebook.share.model.AppInviteContent;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.AppInviteDialog;
import com.facebook.share.widget.ShareDialog;
import com.globaldelight.boom.R;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.tweetcomposer.Card;
import com.twitter.sdk.android.tweetcomposer.ComposerActivity;
import com.twitter.sdk.android.tweetcomposer.TweetComposer;
import com.twitter.sdk.android.tweetcomposer.TweetUploadService;

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
                tweetShare();
            }
        });

        return view;
    }

    private void tweetShare(){
        TwitterAuthConfig authConfig =  new TwitterAuthConfig(KEY.TWEET_CONSUMER, KEY.TWEET_SECRET);

        final Fabric fabric = new Fabric.Builder(getActivity())
                .kits(new TwitterCore(authConfig))
                .logger(new DefaultLogger(Log.DEBUG))
                .debuggable(true)
                .build();

        Fabric.with(fabric);


        Fabric.with(getActivity(), new TwitterCore(authConfig), new TweetComposer());
       TweetComposer.Builder builder = new TweetComposer.Builder(getActivity())
                .text("just setting up my Twitter Kit.");
        builder.show();

//        final TwitterSession session = TwitterCore.getInstance().getSessionManager()
//                .getActiveSession();
//        final Intent intent = new ComposerActivity.Builder(getActivity())
//                .session(session)
//                .createIntent();
//        intent.putExtra("EXTRA_TWEET_TEXT","Hello World Testing");
//        startActivity(intent);
/*
        Uri uri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                "://" + getResources().getResourcePackageName(R.drawable.ic_boom_about_icon)
                + '/' + getResources().getResourceTypeName(R.drawable.ic_boom_about_icon) + '/' + getResources().getResourceEntryName(R.drawable.ic_boom_about_icon) );

        final Card card = new Card.AppCardBuilder(getActivity())
               // .imageUri(uri)
             //   .googlePlayId("com.globaldelight.boom")
                .build();
        final Intent intent = new ComposerActivity.Builder(getActivity())
                .session(session)
                .card(card)
                .hashtags("#helloworld", "#twitter")
                .createIntent();
        startActivity(intent);*/
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
        getActivity().finish();
    }

    private void onCancel() {
        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(new Intent(ACTION_SHARE_FAILED));
        getActivity().finish();
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
        callbackManager.onActivityResult(requestCode,resultCode,data);
    }

    public class TweetResultReceiver extends BroadcastReceiver {
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
    }

     interface KEY{
        String TWEET_CONSUMER =" OJlXW2Wyy7LYuNVPlKhqTlvNa";
        String TWEET_SECRET ="3CfR84QEsJH3VLv9kacF9gz0qg86bUEKQsrjdLDG0DRhoREtJd";
    }
}
