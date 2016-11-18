package world.picpic.www.bbac.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import java.io.InputStream;

/**
 * Created by Wonseob on 2016. 7. 27..
 */
public class GifView extends ImageView implements View.OnClickListener {
    private Context context;
    private boolean isPlayingGif = false;
    private GifDecoder gifDecoder = new GifDecoder();
    private Bitmap frame = null;
    final Handler handler = new Handler();

    final Runnable updateDisplayedImage = new Runnable() {
        public void run() {
            if(frame != null && !frame.isRecycled())
                setImageBitmap(frame);
        }
    };

    public GifView(Context context) {
        super(context);
        this.context = context;
    }

    public GifView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public GifView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
    }

    public void initGifView(InputStream gifStream) {
        playGif(gifStream);
        setOnClickListener(this);
    }

    private void playGif(InputStream gifStream) {;
        gifDecoder.read(gifStream);
        isPlayingGif = true;

        new Thread(new Runnable() {
            public void run() {
                int numOfFrame = gifDecoder.getFrameCount();

                while(true) {
                    for(int i = 0; i < numOfFrame; i++) {
                        // get current frame and update displayed image
                        frame = gifDecoder.getFrame(i);

                        // the runnable will be run on the thread
                        // to which this handler is attached (main thread)
                        handler.post(updateDisplayedImage);

                        // break time up to the next frame
                        int breakTime = gifDecoder.getDelay(i);
                        try {
                            Thread.sleep(breakTime);
                            while(!isPlayingGif)
                                Thread.sleep(50);
                        } catch (InterruptedException e) { }
                    }
                }
            }
        }).start();
    }

    @Override
    public void onClick(View v) {
        isPlayingGif = !isPlayingGif;
    }

}
