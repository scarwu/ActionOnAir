/**
 * Sony Remote Surface View
 *
 * @package     Action on Air
 * @author      ScarWu
 * @copyright   Copyright (c) 2017, ScarWu (http://scar.simcz.tw/)
 * @link        http://github.com/scarwu/ActionOnAir
 */

/**
 * Copyright 2014 Sony Corporation
 */

package scarwu.actiononair.cameras.sony;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import scarwu.actiononair.cameras.sony.StreamSlicer;
import scarwu.actiononair.cameras.sony.StreamSlicer.Payload;

/**
 * A SurfaceView based class to draw liveview frames serially.
 */
public class StreamSurfaceView extends SurfaceView implements SurfaceHolder.Callback {

    private static final String TAG = "AoA-" + StreamSurfaceView.class.getSimpleName();

    private final BlockingQueue<byte[]> jpegQueue = new ArrayBlockingQueue<byte[]>(2);

    private Thread fetchThread;
    private Thread drawerThread;
    private int prevWidth = 0;
    private int prevHeight = 0;
    private final Paint mFramePaint;
    private StreamErrorListener mErrorListener;

    private boolean isFetch;

    /**
     * Constructor
     *
     * @param context
     */
    public StreamSurfaceView(Context context) {
        super(context);

        getHolder().addCallback(this);

        mFramePaint = new Paint();
        mFramePaint.setDither(true);
    }

    /**
     * Constructor
     *
     * @param context
     * @param attrs
     */
    public StreamSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);

        getHolder().addCallback(this);

        mFramePaint = new Paint();
        mFramePaint.setDither(true);
    }

    /**
     * Constructor
     *
     * @param context
     * @param attrs
     * @param defStyle
     */
    public StreamSurfaceView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        getHolder().addCallback(this);

        mFramePaint = new Paint();
        mFramePaint.setDither(true);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // do nothing.
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // do nothing.
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        isFetch = false;
    }

    /**
     * Start retrieving and drawing liveview frame data by new threads.
     *
     * @return true if the starting is completed successfully, false otherwise.
     * @see StreamSurfaceView #bindRemoteApi(SimpleRemoteApi)
     */
    public boolean startFetch(final String streamUrl, StreamErrorListener listener) {
        mErrorListener = listener;

        if (streamUrl == null) {
            Log.e(TAG, "start() streamUrl is null.");

            isFetch = false;

            mErrorListener.onError(StreamErrorListener.StreamErrorReason.OPEN_ERROR);

            return false;
        }

        if (isFetch) {
            Log.w(TAG, "start() already starting.");

            return false;
        }

        isFetch = true;

        // A thread for retrieving liveview data from server.
        fetchThread = new Thread() {

            @Override
            public void run() {
                Log.d(TAG, "Starting retrieving streaming data from server.");

                StreamSlicer slicer = null;

                try {

                    // Create Slicer to open the stream and parse it.
                    slicer = new StreamSlicer();
                    slicer.open(streamUrl);

                    while (isFetch) {
                        final Payload payload = slicer.nextPayload();

                        if (payload == null) { // never occurs
                            Log.e(TAG, "Liveview Payload is null.");

                            continue;
                        }

                        if (jpegQueue.size() == 2) {
                            jpegQueue.remove();
                        }

                        jpegQueue.add(payload.jpegData);
                    }
                } catch (IOException e) {
                    Log.w(TAG, "IOException while fetching: " + e.getMessage());

                    mErrorListener.onError(StreamErrorListener.StreamErrorReason.IO_EXCEPTION);
                } finally {
                    if (slicer != null) {
                        slicer.close();
                    }

                    if (drawerThread != null) {
                        drawerThread.interrupt();
                    }

                    jpegQueue.clear();

                    isFetch = false;
                }
            }
        };

        fetchThread.start();

        // Set Fixed Width & Height
        int width = getWidth();
        int height = (width * 9) / 16;
        getHolder().setFixedSize(width, height);

        // A thread for drawing liveview frame fetched by above thread.
        drawerThread = new Thread() {

            @Override
            public void run() {
                Log.d(TAG, "Starting drawing stream frame.");

                Bitmap frameBitmap = null;

                BitmapFactory.Options factoryOptions = new BitmapFactory.Options();
                factoryOptions.inSampleSize = 1;
                factoryOptions.inBitmap = null;
                factoryOptions.inMutable = true;

                while (isFetch) {
                    try {
                        byte[] jpegData = jpegQueue.take();
                        frameBitmap = BitmapFactory.decodeByteArray(jpegData, 0, jpegData.length, factoryOptions);
                    } catch (IllegalArgumentException e) {
                        if (factoryOptions.inBitmap != null) {
                            factoryOptions.inBitmap.recycle();
                            factoryOptions.inBitmap = null;
                        }

                        continue;
                    } catch (InterruptedException e) {
                        Log.i(TAG, "Drawer thread is Interrupted.");

                        break;
                    }

                    factoryOptions.inBitmap = frameBitmap;
                    drawFrame(frameBitmap);
                }

                if (frameBitmap != null) {
                    frameBitmap.recycle();
                }

                isFetch = false;
            }
        };

        drawerThread.start();

        return true;
    }

    /**
     * Request to stop retrieving and drawing liveview data.
     */
    public void stopFetch() {
        isFetch = false;
    }

    /**
     * Check to see whether start() is already called.
     *
     * @return true if start() is already called, false otherwise.
     */
    public boolean isFetching() {
        return isFetch;
    }

    /**
     * Draw frame bitmap onto a canvas.
     *
     * @param frame
     */
    private void drawFrame(Bitmap frame) {
        if (frame.getWidth() != prevWidth || frame.getHeight() != prevHeight) {
            onDetectedFrameSizeChanged(frame.getWidth(), frame.getHeight());
            return;
        }

        Canvas canvas = getHolder().lockCanvas();

        if (canvas == null) {
            return;
        }

        int w = frame.getWidth();
        int h = frame.getHeight();
        Rect src = new Rect(0, 0, w, h);

        float by = Math.min((float) getWidth() / w, (float) getHeight() / h);

        int offsetX = (getWidth() - (int) (w * by)) / 2;
        int offsetY = (getHeight() - (int) (h * by)) / 2;
        Rect dst = new Rect(offsetX, offsetY, getWidth() - offsetX, getHeight() - offsetY);

        canvas.drawBitmap(frame, src, dst, mFramePaint);

        getHolder().unlockCanvasAndPost(canvas);
    }

    /**
     * Called when the width or height of liveview frame image is changed.
     *
     * @param width
     * @param height
     */
    private void onDetectedFrameSizeChanged(int width, int height) {
        Log.d(TAG, "Change of aspect ratio detected");
        prevWidth = width;
        prevHeight = height;
        drawBlackFrame();
        drawBlackFrame();
        drawBlackFrame(); // delete triple buffers
    }

    /**
     * Draw black screen.
     */
    private void drawBlackFrame() {
        Canvas canvas = getHolder().lockCanvas();

        if (canvas == null) {
            return;
        }

        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.FILL);

        canvas.drawRect(new Rect(0, 0, getWidth(), getHeight()), paint);
        getHolder().unlockCanvasAndPost(canvas);
    }

    public interface StreamErrorListener {

        enum StreamErrorReason {
            IO_EXCEPTION,
            OPEN_ERROR,
        }

        void onError(StreamErrorReason reason);
    }
}
