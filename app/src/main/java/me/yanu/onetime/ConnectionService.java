package me.yanu.onetime;

import java.util.Formatter;
import java.util.List;
import java.util.Random;

/**
 * Created by yannickpulver on 08.05.17.
 */

public class ConnectionService {

    public void getSpotifySong(MainActivity context) {
            String url = getRandomSpotifyUrl();
            new JsonTask(context).execute(url);
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
