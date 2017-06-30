package com.globaldelight.boom;

import android.content.Context;
import android.os.Handler;

import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.Renderer;
import com.google.android.exoplayer2.audio.AudioProcessor;
import com.google.android.exoplayer2.audio.AudioRendererEventListener;
import com.google.android.exoplayer2.drm.DrmSessionManager;
import com.google.android.exoplayer2.drm.FrameworkMediaCrypto;
import com.google.android.exoplayer2.metadata.MetadataRenderer;
import com.google.android.exoplayer2.text.TextRenderer;
import com.google.android.exoplayer2.video.VideoRendererEventListener;

/**
 * Created by adarsh on 27/06/17.
 */

public class BoomRenderersFactory extends DefaultRenderersFactory {

    private BoomAudioProcessor mBoomAudioProcessor;

    public BoomRenderersFactory(Context context,
                                   DrmSessionManager<FrameworkMediaCrypto> drmSessionManager,
                                   @ExtensionRendererMode int extensionRendererMode, long allowedVideoJoiningTimeMs) {
        super(context, drmSessionManager, extensionRendererMode, allowedVideoJoiningTimeMs);
        BoomEngine.init(context);
        mBoomAudioProcessor = new BoomAudioProcessor();
    }

    public BoomRenderersFactory(Context context,
                                   DrmSessionManager<FrameworkMediaCrypto> drmSessionManager,
                                   @ExtensionRendererMode int extensionRendererMode) {
        this(context, drmSessionManager, extensionRendererMode,
                DEFAULT_ALLOWED_VIDEO_JOINING_TIME_MS);
    }


    @Override
    protected AudioProcessor[] buildAudioProcessors() {
        AudioProcessor[] processors = new AudioProcessor[1];
        processors[0] = mBoomAudioProcessor;
        return processors;
    }

    public BoomAudioProcessor getBoomAudioProcessor() {
        return mBoomAudioProcessor;
    }
}
