package com.globaldelight.boomplayer;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.ConditionVariable;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by adarsh on 20/01/17.
 * AudioTrackReader reads the decoded samples from an audio file
 */
class AudioTrackReader extends MediaCodec.Callback {
    static final int STATE_NOT_STARTED = 0;
    static final int STATE_READING = 1;
    static final int STATE_WAITING = 2;
    static final int STATE_FINISHED = 3;
    static final int STATE_FORMAT_CHANGED = 4;
    static final int STATE_ERROR = 5;

    static class SampleBuffer {
        ByteBuffer   byteBuffer;
        long         size;
        long         timeStamp; // presentation time in micro seconds
        int          index;

        void invalidate() {
            byteBuffer = null;
            size = 0;
            index = -1;
        }
    }

    private MediaExtractor mExtractor;
    private MediaCodec mCodec;
    private String mSourcePath;
    private int state = STATE_NOT_STARTED;
    private long mDuration = 0;
    private MediaFormat mInputFormat;
    private MediaFormat mOutputFormat;

    private MediaCodec.BufferInfo mBufferInfo = new MediaCodec.BufferInfo();
    private SampleBuffer mSampleBuffer = new SampleBuffer();
    private ConditionVariable sync = new ConditionVariable();

    class ExtractorThread extends Thread {

        private boolean quit = false;

        // stops the execution of thread and waits until it finishes
        void finish() {
            try {
                quit = true;
                this.interrupt();
                this.join();
            }
            catch (InterruptedException e) {

            }
        }


        @Override
        public void run() {
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO);
            final long kTimeOutUs = 1000;
            try {
                while ( !quit && !this.isInterrupted() ) {
                    synchronized (mCodec) {
                        int inputBufIndex = mCodec.dequeueInputBuffer(kTimeOutUs);
                        if (inputBufIndex >= 0) {
                            this.onInputBufferAvailable(mCodec, inputBufIndex);
                        }
                        else {
                            mCodec.wait();
                        }
                    }
                    Thread.yield();
                }
            }
            catch (Exception e) {

            }
        }

        private void onInputBufferAvailable(MediaCodec mediaCodec, int inputBufIndex) {
            try {
                ByteBuffer dstBuf = mediaCodec.getInputBuffer(inputBufIndex);
                int sampleSize = mExtractor.readSampleData(dstBuf, 0);

                if (sampleSize < 0) {
                    quit = true;
                    sampleSize = 0;
                }

                mCodec.queueInputBuffer(inputBufIndex, 0, sampleSize, mExtractor.getSampleTime(), quit ? MediaCodec.BUFFER_FLAG_END_OF_STREAM : 0);
                if (!quit) {
                    mExtractor.advance();
                }
            }
            catch (IllegalStateException e) {

            }
        }
    }

    private ExtractorThread mExtractorThread;


    AudioTrackReader(String sourcePath) {
        mSourcePath = sourcePath;
    }


    void startReading()
            throws InterruptedException, ExecutionException, TimeoutException, IOException {
        mExtractor = createMediaExtractorWithPath(mSourcePath);
        mInputFormat = mExtractor.getTrackFormat(0);

        // check we have audio content we know
        String mime = mInputFormat.getString(MediaFormat.KEY_MIME);
        if ( !mime.startsWith("audio/")) {
            throw new ExecutionException("Unsupported mime type", new Throwable());
        }

        mDuration = mInputFormat.getLong(MediaFormat.KEY_DURATION);
        mExtractor.selectTrack(0);

        mCodec = MediaCodec.createDecoderByType(mime);

        mCodec.configure(mInputFormat, null, null, 0);
        mOutputFormat = mCodec.getOutputFormat();
        mCodec.start();
        state = STATE_READING;

        mExtractorThread = new ExtractorThread();
        mExtractorThread.start();
    }

    void stopReading() {
        try {
            mExtractorThread.finish();

            mCodec.stop();
            mCodec.release();
            mExtractor.release();
        }
        catch (Exception e) {

        }
    }

    // Can return an empty buffer if  buffers are not ready.
    SampleBuffer readNextBuffer() {
        final long kTimeOutUs = 1000;

        state = STATE_WAITING; // initialize state for waiting
        synchronized (mCodec) {
            try {
                int outputBufIndex = mCodec.dequeueOutputBuffer(mBufferInfo, kTimeOutUs);
                if (outputBufIndex >= 0) {
                    state = STATE_READING;
                    this.onOutputBufferAvailable(mCodec, outputBufIndex, mBufferInfo);
                    if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        state = STATE_FINISHED;
                    }
                } else if (outputBufIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    onOutputFormatChanged(mCodec, mCodec.getOutputFormat());
                    mSampleBuffer.invalidate();
                }
                else {
                    mSampleBuffer.invalidate();
                }
            }
            catch ( MediaCodec.CodecException e) {
                this.onError(mCodec, e);
            }
        }

        return mSampleBuffer;
    }

    void releaseBuffer(SampleBuffer buffer) {
        synchronized (mCodec) {
            if ( buffer.index >= 0 ) {
                mCodec.releaseOutputBuffer(buffer.index,false);
            }

            mCodec.notify();
        }

        mSampleBuffer.invalidate();
    }

    void seekTo(long time) {
        mExtractor.seekTo(time, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
    }

    MediaFormat getInputFormat() {
        return mInputFormat;
    }

    int getState() {
        return state;
    }

    MediaFormat getOutputFormat() {
        return mOutputFormat;
    }

    long getDuration() {
        return mDuration;
    }

    @Override
    public void onInputBufferAvailable(MediaCodec mediaCodec, int inputBufIndex) {
        try {
            ByteBuffer dstBuf = mediaCodec.getInputBuffer(inputBufIndex);
            int sampleSize = mExtractor.readSampleData(dstBuf, 0);

            Boolean sawInputEOS = false;
            if (sampleSize < 0) {
                sawInputEOS = true;
                sampleSize = 0;
            }

            mCodec.queueInputBuffer(inputBufIndex, 0, sampleSize, mExtractor.getSampleTime(), sawInputEOS ? MediaCodec.BUFFER_FLAG_END_OF_STREAM : 0);
            if (!sawInputEOS) {
                mExtractor.advance();
            }
        }
        catch (IllegalStateException e) {

        }
    }

    @Override
    public void onOutputBufferAvailable(MediaCodec mediaCodec, int outputBufIndex, MediaCodec.BufferInfo bufferInfo) {
        mSampleBuffer.byteBuffer = mediaCodec.getOutputBuffer(outputBufIndex);
        mSampleBuffer.size = bufferInfo.size;
        mSampleBuffer.timeStamp = bufferInfo.presentationTimeUs;
        mSampleBuffer.index = outputBufIndex;
    }

    @Override
    public void onOutputFormatChanged(MediaCodec mediaCodec, MediaFormat mediaFormat) {
        if ( !isSameFormat(mediaFormat, mOutputFormat) ) {
            mOutputFormat = mediaFormat;
            state = STATE_FORMAT_CHANGED;
        }
    }

    @Override
    public void onError(MediaCodec mediaCodec, MediaCodec.CodecException e) {
        state = STATE_ERROR;
    }

    private boolean isSameFormat(MediaFormat f1, MediaFormat f2) {
        return (f1.getInteger(MediaFormat.KEY_SAMPLE_RATE) == f2.getInteger(MediaFormat.KEY_SAMPLE_RATE)
                && f1.getInteger(MediaFormat.KEY_CHANNEL_COUNT) == f2.getInteger(MediaFormat.KEY_CHANNEL_COUNT));
    }
    
    private static MediaExtractor createMediaExtractorWithPath(final String path)
            throws InterruptedException, ExecutionException, TimeoutException {
        FutureTask<MediaExtractor> future = new FutureTask<MediaExtractor>(new Callable<MediaExtractor>() {
            @Override
            public MediaExtractor call() throws Exception {
                try {
                    MediaExtractor extractor = new MediaExtractor();
                    extractor.setDataSource(path);
                    return extractor;
                }
                catch (Exception e) {
                    return null;
                }
            }
        });

        new Thread(future).start();

        MediaExtractor extractor = future.get(60, TimeUnit.SECONDS);
        if ( extractor == null ) {
            throw new ExecutionException("Failed to create extractor", new Throwable());
        }

        return extractor;
    }

}
