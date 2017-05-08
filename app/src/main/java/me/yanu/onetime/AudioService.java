package me.yanu.onetime;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;

import com.h6ah4i.android.media.IBasicMediaPlayer;
import com.h6ah4i.android.media.IMediaPlayerFactory;
import com.h6ah4i.android.media.opensl.OpenSLMediaPlayerFactory;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by yannickpulver on 08.05.17.
 */

public class AudioService {
    //MediaPlayer mPlayer;
    MainActivity mContext;
    ConnectionService mConnectionService;
    IMediaPlayerFactory mFactory;
    IBasicMediaPlayer mPlayer;

    public AudioService(MainActivity context) {
        mContext = context;
        mConnectionService = new ConnectionService();
    }

    public void requestSong() {
        mConnectionService.getSpotifySong(mContext);
    }

    public JSONObject streamSong(JSONObject song) {
        try {
            streamSong(song.get("preview_url").toString());
            return song;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean streamSong(String url) {
        mFactory = new OpenSLMediaPlayerFactory(mContext);
        mPlayer = mFactory.createMediaPlayer();

        mPlayer.setOnCompletionListener(mContext);
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mPlayer.setDataSource(url);
            mPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        mPlayer.start();

        return true;
    }


    public void stopStream() {
        mPlayer.stop();
    }
}
