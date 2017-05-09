package me.yanu.onetime;

import android.Manifest;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.Prediction;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.audiofx.Visualizer;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.animation.ViewPropertyAnimation;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements GestureOverlayView.OnGesturePerformedListener, MediaPlayer.OnCompletionListener, IAsyncResponse, Visualizer.OnDataCaptureListener {
    private static final int UI_ANIMATION_DELAY = 300;
    private static final int CAPTURE_SIZE = 256;

    @BindView(R.id.artist) TextView mArtist;
    @BindView(R.id.songTitle) TextView mSongTitle;
    @BindView(R.id.songCover) ImageView mSongCover;
    @BindView(R.id.innerLayout) RelativeLayout mContentView;
    @BindView(R.id.mainLayout) RelativeLayout mMainLayout;
    @BindView(R.id.animation_view) LottieAnimationView mAnimationView;
    @BindView(R.id.waveform_view) WaveformView mWaveformView;

    private MediaPlayer mPlayer;
    private ConnectionService mConnectionService;

    private int prevRed = 230;
    private int prevGreen = 124;
    private int prevBlue = 178;

    private int currRed = 245;
    private int currGreen = 228;
    private int currBlue = 94;

    private boolean currUp;
    private boolean prevUp;

    Handler mHandler = new Handler();
    int mDelay = 60;

    ViewPropertyAnimation.Animator animationText = new ViewPropertyAnimation.Animator() {
        @Override
        public void animate(View view) {
            view.setAlpha( 0f );

            ObjectAnimator fadeAnim = ObjectAnimator.ofFloat( view, "alpha", 0f, 1f );
            AnimatorSet animSetXY = new AnimatorSet();
            animSetXY.playTogether(fadeAnim);
            animSetXY.setDuration( 1500 );
            animSetXY.start();
        }
    };

    ViewPropertyAnimation.Animator animationObject = new ViewPropertyAnimation.Animator() {
        @Override
        public void animate(View view) {
            view.setAlpha( 0f );

            ObjectAnimator animX = ObjectAnimator.ofFloat(view, "scaleX", 0.9f, 1f);
            ObjectAnimator animY = ObjectAnimator.ofFloat(view, "scaleY", 0.9f, 1f);
            ObjectAnimator fadeAnim = ObjectAnimator.ofFloat( view, "alpha", 0f, 1f );
            AnimatorSet animSetXY = new AnimatorSet();
            animSetXY.playTogether(animX, animY, fadeAnim);
            animSetXY.setDuration( 1500 );
            animSetXY.start();
        }
    };

    ViewPropertyAnimation.Animator animationWave = new ViewPropertyAnimation.Animator() {
        @Override
        public void animate(View view) {
            view.setAlpha(0f);
            view.setPivotY(view.getMeasuredHeight());
            ObjectAnimator animY = ObjectAnimator.ofFloat(view, "scaleY", 0f, 1f);
            ObjectAnimator fadeAnim = ObjectAnimator.ofFloat( view, "alpha", 0f, 1f );
            AnimatorSet animSetXY = new AnimatorSet();
            animSetXY.playTogether(animY, fadeAnim);
            animSetXY.setDuration( 1500 );
            animSetXY.start();
        }
    };


    private GestureLibrary mLibrary;
    private boolean mIsPlaying = false;
    private JSONObject currSong;
   // private AudioService mAudioService;
    private final Handler mHideHandler = new Handler();
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
        }
    };
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        RendererFactory rendererFactory = new RendererFactory();
        mWaveformView.setRenderer(rendererFactory.createSimpleWaveformRenderer(Color.WHITE));

        clearSongInfo();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 1239);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, 1240);
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.MODIFY_AUDIO_SETTINGS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.MODIFY_AUDIO_SETTINGS}, 1241);
        }

        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hide();
            }
        });

        //Gestures
        mLibrary = GestureLibraries.fromRawResource(this, R.raw.gesture);
        if (!mLibrary.load()) finish();
        GestureOverlayView gestures = (GestureOverlayView) findViewById(R.id.gestureOverlay);
        gestures.addOnGesturePerformedListener(this);


        //Fanciness
        mAnimationView.setAnimation("data.json");
        mAnimationView.loop(true);
        mAnimationView.playAnimation();

        mHandler.postDelayed(new Runnable(){
            public void run(){
                //do something
                changeGradient();
                mHandler.postDelayed(this, mDelay);
            }
        }, mDelay);

        mConnectionService = new ConnectionService();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        delayedHide(100);
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    @Override
    public void onGesturePerformed(GestureOverlayView overlay, Gesture gesture) {
        ArrayList<Prediction> predictions = mLibrary.recognize(gesture);

        if (predictions.size() > 0 && predictions.get(0).score > 0.8) {
            String result = predictions.get(0).name;

            if ("play".equalsIgnoreCase(result) && !mIsPlaying) {
                Toast.makeText(this, "WAIT FOR IT...", Toast.LENGTH_SHORT).show();
                requestSong();
            } else if ("stop".equalsIgnoreCase(result) && mIsPlaying) {
                Toast.makeText(this, "FORVER GONE", Toast.LENGTH_LONG).show();
                mIsPlaying = false;
                stopStream();
                clearSongInfo();
                mWaveformView.setVisibility(View.GONE);
                mAnimationView.setVisibility(View.VISIBLE);
            } else if ("next".equalsIgnoreCase(result) && mIsPlaying) {
                requestSong();
                stopStream();
            }
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp)
    {
        requestSong();
    }

    private void setSongInfo() {
        try {
            JSONArray artists = (JSONArray) currSong.get("artists");
            final JSONObject artist = (JSONObject) artists.get(0);

            JSONObject album = (JSONObject) currSong.get("album");
            JSONArray images = (JSONArray) album.get("images");
            JSONObject image = (JSONObject) images.get(0);

            Glide.with(this)
                    .load(image.get("url"))
                    .animate(animationObject)
                    .into(new GlideDrawableImageViewTarget(mSongCover) {
                        @Override
                        public void onResourceReady(GlideDrawable drawable, GlideAnimation anim) {
                            super.onResourceReady(drawable, anim);
                            try {
                                animationText.animate(mArtist);
                                mArtist.setText(artist.get("name").toString());
                                animationText.animate(mSongTitle);
                                mSongTitle.setText(currSong.get("name").toString());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            Log.d("hello", "Sometimes the image is not loaded and this text is not displayed");
                        }
                    });



        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void clearSongInfo() {
        mArtist.setText("");
        mSongTitle.setText("");
        mSongCover.setImageResource(0);
    }

    @Override
    public void processFinish(String output) {
        JSONObject json, res;
        try {
            json = new JSONObject(output);
            JSONObject tracks = (JSONObject) json.get("tracks");
            JSONArray items = (JSONArray) tracks.get("items");
            res = (JSONObject) items.get(0);
            streamSong(res.get("preview_url").toString());
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "SHIT, SOMETHING WENT WRONG. RETRYING.", Toast.LENGTH_LONG).show();
            requestSong();
            return;
        }
        currSong = res;
        mIsPlaying = true;
        setSongInfo();
        mAnimationView.setVisibility(View.GONE);
    }

    public void changeGradient() {
        GradientDrawable gd = new GradientDrawable();

        gd.setOrientation(GradientDrawable.Orientation.TL_BR);
        gd.setShape(GradientDrawable.RECTANGLE);

        prevBlue = prevUp ? prevBlue+1 : prevBlue-1;
        if ((prevBlue < 100 && !prevUp) || (prevBlue > 200 && prevUp)) {
            prevUp = !prevUp;
        }

        currBlue = currUp ? currBlue+1 : currBlue-1;
        if ((currBlue < 100 && !currUp) || (currBlue > 200 && currUp)) {
            currUp = !currUp;
        }

        gd.setColors(new int[]{
                Color.argb(255, prevRed, prevGreen, prevBlue), Color.argb(255, currRed, currGreen, currBlue)
        });

        mMainLayout.setBackground(gd);
    }

    void requestSong() {
        mConnectionService.getSpotifySong(this);
    }

    void stopStream() {
        mPlayer.stop();
    }

    boolean streamSong(String url) {
        mPlayer = new MediaPlayer();
        mPlayer.setOnCompletionListener(this);
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mPlayer.setDataSource(url);
            mPlayer.prepare();
            startVisualiser();
            mPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private void startVisualiser() {
        Visualizer mVisualizer = new Visualizer(mPlayer.getAudioSessionId());
        mVisualizer.setDataCaptureListener(this, Visualizer.getMaxCaptureRate(), true, false);
        mVisualizer.setCaptureSize(CAPTURE_SIZE);
        mVisualizer.setEnabled(true);

        mWaveformView.setVisibility(View.VISIBLE);
        animationWave.animate(mWaveformView);
    }

    @Override
    public void onWaveFormDataCapture(Visualizer visualizer, byte[] waveform, int samplingRate) {
        if (mWaveformView != null) {
            mWaveformView.setWaveform(waveform);
        }
    }

    @Override
    public void onFftDataCapture(Visualizer visualizer, byte[] fft, int samplingRate) {
        //Nothing to do here.
    }
}
