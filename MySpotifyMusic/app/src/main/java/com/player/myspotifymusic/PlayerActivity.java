package com.player.myspotifymusic;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.openslplayer.OpenSLPlayer;
import com.player.music.AudioListActivity;
import com.player.music.MediaItem;
import com.example.openslplayer.PlayerEvents;

/**
 * Created by Rahul Agarwal on 13-09-16.
 */
public class PlayerActivity extends Activity {

    ToggleButton play_track, reverb, equalizer;
    Button stop_track;
    SeekBar seekTrack;
    ImageButton audioList, audioeffectbtn;
    final int REQUEST_CODE =123;
    OpenSLPlayer mPlayer;
    private static MediaItem item;
    static boolean isStopped = true;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        audioList = (ImageButton) findViewById(R.id.list);

        audioList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(PlayerActivity.this, AudioListActivity.class);
                PlayerActivity.this.startActivityForResult(i, REQUEST_CODE);
            }
        });

        audioeffectbtn = (ImageButton)findViewById(R.id.audioeffectbtn);
        audioeffectbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPlayer.SupportedCodec();
            }
        });
        mPlayer = new OpenSLPlayer(events);
        play_track = (ToggleButton)findViewById(R.id.playTrack);
        stop_track = (Button)findViewById(R.id.stopTrack);
        reverb = (ToggleButton)findViewById(R.id.reverb);
        equalizer = (ToggleButton)findViewById(R.id.equalizer);
        seekTrack = (SeekBar)findViewById(R.id.trackSeek);

        AssetManager mgr = getAssets();
        mPlayer.readAsset(mgr);

        seekTrack.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser) {
                    mPlayer.seek(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        play_track.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(item == null){
                    play_track.post(new Runnable() {
                        @Override
                        public void run() {
                            play_track.setChecked(false);
                        }
                    });
                    Toast.makeText(PlayerActivity.this, "Select a file to play music...!", Toast.LENGTH_LONG).show();
                    return;
                }
                    if (isChecked) {
                        mPlayer.setDataSource(item.getItemUrl());
                        mPlayer.play();
                        isStopped =false;
                    } else if(!isChecked && !isStopped){
                        mPlayer.pause();
                    }
            }
        });

        stop_track.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPlayer.stop();
                isStopped = true;
                play_track.setChecked(false);
            }
        });

        reverb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    mPlayer.enableReverb(isChecked);
            }
        });

        equalizer.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mPlayer.enableEq(isChecked);
            }
        });
    }

    PlayerEvents events = new PlayerEvents() {
        @Override public void onStop() {
//            Log.d("PlayerActivity" , "onStop called");
            isStopped = true;
            play_track.setChecked(false);
            seekTrack.setProgress(0);
        }
        @Override public void onStart(String mime, int sampleRate, int channels, long duration) {
//            Log.d("PlayerActivity" , "onStart called: " + mime + " sampleRate:" + sampleRate + " channels:"+ channels);
            if (duration == 0)
                Toast.makeText(PlayerActivity.this, "This is a LIVE Stream!", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(PlayerActivity.this, "This is a RECORDING!", Toast.LENGTH_SHORT).show();
        }
        @Override public void onPlayUpdate(int percent, long currentms, long totalms) {
            seekTrack.setProgress(percent);
        }
        @Override public void onPlay() {
        }
        @Override public void onError() {
            seekTrack.setProgress(0);
            Toast.makeText(PlayerActivity.this, "Error!",  Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && requestCode == REQUEST_CODE){
            item = data.getParcelableExtra("mediaItem");
//            Log.e("Result Item ", item.toString());
        }
    }
}
