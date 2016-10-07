package com.player.ui.musiclist.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;

import com.player.data.PlayingQueue.PlayerEventHandler;
import com.player.myspotifymusic.R;
import com.player.ui.UIEvent;
import com.player.ui.widgets.CircleImageView;
import com.player.ui.widgets.CircularSeekBar;
import com.squareup.picasso.Picasso;

import java.io.File;

/**
 * Created by Rahul Agarwal on 30-09-16.
 */

public class BoomPlayerActivity extends AppCompatActivity implements View.OnClickListener{

    CircularSeekBar seekTrack;
    ImageButton prevTrack, stopTrack, nextTrack, listbtn, audioeffect_btn, paly_pause_btn;
    CircleImageView albumart;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_player);

        PlayerEventHandler.getQueueEventInstance(this).setUIEvent(event);

        paly_pause_btn = (ImageButton)findViewById(R.id.play_pause_btn);
        audioeffect_btn = (ImageButton)findViewById(R.id.audioeffectbtn);
        albumart = (CircleImageView)findViewById(R.id.albumart);
        listbtn = (ImageButton)findViewById(R.id.list);
        nextTrack = (ImageButton)findViewById(R.id.nextTrack);
        stopTrack = (ImageButton)findViewById(R.id.stopTrack);
        prevTrack = (ImageButton)findViewById(R.id.prevTrack);
        seekTrack = (CircularSeekBar)findViewById(R.id.trackSeek);

        paly_pause_btn.setOnClickListener(this);
        audioeffect_btn.setOnClickListener(this);
        listbtn.setOnClickListener(this);
        nextTrack.setOnClickListener(this);
        stopTrack.setOnClickListener(this);
        prevTrack.setOnClickListener(this);

        seekTrack.setOnCircularSeekBarChangeListener(new CircularSeekBar.OnCircularSeekBarChangeListener() {
            @Override
            public void onProgressChanged(CircularSeekBar circularSeekBar, int progress, boolean fromUser) {
                if(fromUser && PlayerEventHandler.getQueueEventInstance(BoomPlayerActivity.this).isPlaying()) {
                    circularSeekBar.setProgress(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(CircularSeekBar circularSeekBar) {

            }

            @Override
            public void onStopTrackingTouch(CircularSeekBar circularSeekBar) {
                if(PlayerEventHandler.getQueueEventInstance(BoomPlayerActivity.this).isPlaying())
                    PlayerEventHandler.getQueueEventInstance(BoomPlayerActivity.this).seek(circularSeekBar.getProgress());
            }
        });
    }


    UIEvent event = new UIEvent() {
        @Override
        public void updateSeek(int percent, long currentms, long totalms) {
            seekTrack.setProgress(percent);
        }

        @Override
        public void updateUI() {
            if(PlayerEventHandler.getQueueEventInstance(BoomPlayerActivity.this).isPlaying()){
                paly_pause_btn.setImageDrawable(getResources().getDrawable(R.drawable.ic_play, null));
            }else{
                paly_pause_btn.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause, null));
            }
            if(PlayerEventHandler.getQueueEventInstance(BoomPlayerActivity.this).getPlayingItem() != null){
                Picasso.with(BoomPlayerActivity.this).load(new File(PlayerEventHandler.getQueueEventInstance(BoomPlayerActivity.this).getPlayingItem().getItemArtUrl()))
                    /*.centerCrop().resize(size, size)*//*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(albumart);
            }else{
                albumart.setImageDrawable(getResources().getDrawable(R.drawable.default_album_art_home));
            }
        }

        @Override
        public void stop() {
            paly_pause_btn.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause, null));
            seekTrack. setProgress(0);
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.play_pause_btn:
                int play = PlayerEventHandler.getQueueEventInstance(this).Play();
                if(play == 0){
                    paly_pause_btn.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause, null));
                }else if(play == 1){
                    paly_pause_btn.setImageDrawable(getResources().getDrawable(R.drawable.ic_play, null));
                }
                break;
            case R.id.audioeffectbtn:
                Intent queueIntent = new Intent(BoomPlayerActivity.this, PlayingQueueActivity.class);
                startActivity(queueIntent);
                break;
            case R.id.list:
                Intent listIntent = new Intent(BoomPlayerActivity.this, DeviceMusicActivity.class);
                startActivity(listIntent);
                break;
            case R.id.nextTrack:
                PlayerEventHandler.getQueueEventInstance(this).next();
                break;
            case R.id.stopTrack:
                PlayerEventHandler.getQueueEventInstance(this).stop();
                break;
            case R.id.prevTrack:
                PlayerEventHandler.getQueueEventInstance(this).previous();
                break;
            default:

                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(PlayerEventHandler.getQueueEventInstance(this).isPlaying()){
            paly_pause_btn.setImageDrawable(getResources().getDrawable(R.drawable.ic_play, null));
            seekTrack.setVisibility(View.VISIBLE);
        }else{
            paly_pause_btn.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause, null));
            seekTrack.setVisibility(View.INVISIBLE);
        }
        if(PlayerEventHandler.getQueueEventInstance(this).getPlayingItem() != null){
            Picasso.with(this).load(new File(PlayerEventHandler.getQueueEventInstance(this).getPlayingItem().getItemArtUrl()))
                    /*.centerCrop().resize(size, size)*//*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(albumart);
        }else{
            albumart.setImageDrawable(getResources().getDrawable(R.drawable.default_album_art_home));
        }
    }
}
