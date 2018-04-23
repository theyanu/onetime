package me.yanu.onetime;

import android.util.Base64;
import java.util.Formatter;

/**
 * Created by yannickpulver on 08.05.17.
 */

class ConnectionService {
  private String appCredentials = "588b8586ac814a5db61484c1e5582c97:afbcf0478e724b7a9b5565b11e4026d6";
  private String tokenUrl = "https://accounts.spotify.com/api/token";

  void getSpotifySong(MainActivity context, String accessToken) {
    String auth = "Bearer " + accessToken;
    String url = getRandomSpotifyUrl();
    new JsonTask(context).execute(url, auth);
  }

  void getAccessToken(MainActivity context) {
    String basicAuth = "Basic " + new String(android.util.Base64.encode(appCredentials.getBytes(), Base64.NO_WRAP));
    new JsonTask(context).execute(tokenUrl, basicAuth, "client_credentials", "POST");
  }

  private String getRandomSpotifyUrl() {
    String query = "jazz";
    int offset = (int) (Math.random() * 10000);
    StringBuilder builder = new StringBuilder();
    Formatter formatter = new Formatter(builder);
    return formatter.format(
        "https://api.spotify.com/v1/search?query=%s&offset=%s&limit=1&type=track", query, offset)
        .toString();
  }
}
