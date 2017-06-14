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
public class StreamSender {

    private static final String TAG = "AoA-" + StreamSender.class.getSimpleName();

    private final BlockingQueue<byte[]> jpegQueue = new ArrayBlockingQueue<byte[]>(2);

    private Thread fetchThread;
    private Thread drawerThread;
    private int prevWidth = 0;
    private int prevHeight = 0;
    private StreamErrorListener mErrorListener;

    private boolean isFetch;

    /**
     * Constructor
     *
     * @param context
     */
    public StreamSender(Context context) {

    }

    /**
     * Start retrieving and drawing liveview frame data by new threads.
     *
     * @return true if the starting is completed successfully, false otherwise.
     * @see StreamSurfaceView #bindRemoteApi(SimpleRemoteApi)
     */
    public boolean startFetch(final String streamInputUrl, final String streamOutputUrl, StreamErrorListener listener) {
        mErrorListener = listener;

        if (streamInputUrl == null) {
            Log.e(TAG, "start() streamInputUrl is null.");

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
                    slicer.open(streamInputUrl);

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

    }

    public interface StreamErrorListener {

        enum StreamErrorReason {
            IO_EXCEPTION,
            OPEN_ERROR,
        }

        void onError(StreamErrorReason reason);
    }
}
