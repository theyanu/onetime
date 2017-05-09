package me.yanu.onetime;

import java.util.Formatter;

/**
 * Created by yannickpulver on 08.05.17.
 */

class ConnectionService {

    void getSpotifySong(MainActivity context) {
            String url = getRandomSpotifyUrl();
            new JsonTask(context).execute(url);
    }

    private String getRandomSpotifyUrl() {
        String query = "jazz";
        int offset = (int)(Math.random() * 10000);
        StringBuilder builder = new StringBuilder();
        Formatter formatter = new Formatter(builder);
        return formatter.format("https://api.spotify.com/v1/search?query=%s&offset=%s&limit=1&type=track", query, offset).toString();
    }
}
