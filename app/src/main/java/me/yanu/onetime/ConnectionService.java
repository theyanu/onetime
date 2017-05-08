package me.yanu.onetime;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Formatter;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;

/**
 * Created by yannickpulver on 08.05.17.
 */

public class ConnectionService {

    public JSONObject getSpotifySong(MainActivity context) {
            JSONObject res = null;
            String url = getRandomSpotifyUrl();

        try {
            JSONObject json = new JSONObject(new JsonTask(context).execute(url).get());

            JSONObject tracks = (JSONObject) json.get("tracks");
            JSONArray items = (JSONArray) tracks.get("items");

            res = (JSONObject) items.get(0);
        } catch (InterruptedException | ExecutionException | JSONException e) {
            e.printStackTrace();
        }
        return res;
    }

    public String getRandomSpotifyUrl() {
        //List<String> songs = new ArrayList<>();
        //songs.addAll(Arrays.asList("%25a%25", "a%25", "%25e%25", "e%25", "%25i%25", "i%25", "%25o%25", "o%25"));
        //String query = getRandomItem(songs);

        String query = "fancy";
        int offset = (int)(Math.random() * 10000);
        StringBuilder builder = new StringBuilder();
        Formatter formatter = new Formatter(builder);
        return formatter.format("https://api.spotify.com/v1/search?query=%s&offset=%s&limit=1&type=track", query, offset).toString();
    }

    private <T> T getRandomItem(List<T> list)
    {
        Random random = new Random();
        int listSize = list.size();
        int randomIndex = random.nextInt(listSize);
        return list.get(randomIndex);
    }
}
