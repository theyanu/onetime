package me.yanu.onetime;

import android.Manifest;
import android.annotation.SuppressLint;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.Prediction;
import android.graphics.drawable.TransitionDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements GestureOverlayView.OnGesturePerformedListener, MediaPlayer.OnCompletionListener, IAsyncResponse {
    public static int INTERNET_PERMISSION = 0;
    private static final int UI_ANIMATION_DELAY = 300;

    @BindView(R.id.artist) TextView mArtist;
    @BindView(R.id.songTitle) TextView mSongTitle;
    @BindView(R.id.songCover) ImageView mSongCover;
    @BindView(R.id.innerLayout) RelativeLayout mContentView;
    @BindView(R.id.play_indicator) TextView mPlayIndicator;
    @BindView(R.id.mainLayout) RelativeLayout mMainLayout;
    @BindView(R.id.animation_view) LottieAnimationView mAnimationView;


    private GestureLibrary mLibrary;
    private boolean mIsPlaying = false;
    private JSONObject currSong;
    private AudioService mAudioService;
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
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        clearSongInfo();

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

        //Background Transition
        TransitionDrawable trans = (TransitionDrawable) mMainLayout.getBackground();
        trans.startTransition(20000);

        //Spotify - AudioService
        mAudioService = new AudioService(this);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, INTERNET_PERMISSION);


        //Fanciness
        mAnimationView.setAnimation("data.json");
        mAnimationView.loop(true);
        mAnimationView.playAnimation();
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

        // Schedule a runnable to remove the status and navigation bar after a delay
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

        if (predictions.size() > 0 && predictions.get(0).score > 1.0) {
            String result = predictions.get(0).name;

            if ("play".equalsIgnoreCase(result) && !mIsPlaying) {
                Toast.makeText(this, "WAIT FOR IT...", Toast.LENGTH_LONG).show();
                streamSong();
            } else if ("stop".equalsIgnoreCase(result) && mIsPlaying) {
                Toast.makeText(this, "FORVER GONE", Toast.LENGTH_LONG).show();
                mIsPlaying = false;
                mPlayIndicator.setText(R.string.play);
                mAudioService.stopStream();
                clearSongInfo();
                mAnimationView.setVisibility(View.VISIBLE);
            }
        }
    }


    private void streamSong() {
        mAudioService.requestSong();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        streamSong();
    }

    private void setSongInfo() {
        try {
            JSONArray artists = (JSONArray) currSong.get("artists");
            JSONObject artist = (JSONObject) artists.get(0);
            mArtist.setText(artist.get("name").toString());
            mSongTitle.setText(currSong.get("name").toString());

            JSONObject album = (JSONObject) currSong.get("album");
            JSONArray images = (JSONArray) album.get("images");
            JSONObject image = (JSONObject) images.get(0);

            Glide.with(this).load(image.get("url")).into(mSongCover);

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

        JSONObject json;
        JSONObject res = null;
        try {
            json = new JSONObject(output);

            JSONObject tracks = (JSONObject) json.get("tracks");
            JSONArray items = (JSONArray) tracks.get("items");

            res = (JSONObject) items.get(0);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mAudioService.streamSong(res);
        currSong = res;

        mIsPlaying = currSong != null;
        if (!mIsPlaying) {
            Toast.makeText(this, "SHIT, SOMETHING WENT WRONG. RETRYING", Toast.LENGTH_LONG).show();
            streamSong();
        } else {
            mPlayIndicator.setText(R.string.stop);
            Toast.makeText(this, "PLAYING FANCY TUNE", Toast.LENGTH_LONG).show();
            setSongInfo();
            mAnimationView.setVisibility(View.GONE);
        }

    }
}
