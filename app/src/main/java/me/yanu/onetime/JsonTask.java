package me.yanu.onetime;

import android.app.ProgressDialog;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Base64;

/**
 * Created by yannickpulver on 08.05.17.
 */

public class JsonTask extends AsyncTask<String, String, String> {
  public IAsyncResponse delegate;

  public JsonTask(IAsyncResponse delegate) {
    this.delegate = delegate;
  }

  @Override protected void onPostExecute(String result) {
    delegate.processFinish(result);
  }

  protected String doInBackground(String... params) {
    HttpURLConnection connection = null;
    BufferedReader reader = null;

    try {
      URL url = new URL(params[0]);
      connection = (HttpURLConnection) url.openConnection();

      if (params.length >= 2) {
        connection.setRequestProperty("Authorization", params[1]);
      }

      if (params.length >= 3) {
        connection.setDoInput(true);
        connection.setDoOutput(true);
        appendBodyParameter(connection,"grant_type", params[2]);
      }

      if (params.length >= 4) {
        connection.setRequestMethod(params[3]);
      }

      connection.connect();

      InputStream stream = connection.getInputStream();

      reader = new BufferedReader(new InputStreamReader(stream));

      StringBuffer buffer = new StringBuffer();
      String line = "";

      while ((line = reader.readLine()) != null) {
        buffer.append(line + "\n");
        Log.d("Response: ", "> " + line);   //here u ll get whole response...... :-)
      }

      return buffer.toString();
    } catch (MalformedURLException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (connection != null) {
        connection.disconnect();
      }
      try {
        if (reader != null) {
          reader.close();
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return null;
  }

  private void appendBodyParameter(HttpURLConnection connection, String key, String param) throws IOException {
    Uri.Builder builder = new Uri.Builder().appendQueryParameter(key, param);
    String query = builder.build().getEncodedQuery();
    OutputStream os = connection.getOutputStream();
    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
    writer.write(query);
    writer.flush();
    writer.close();
    os.close();
  }
}
