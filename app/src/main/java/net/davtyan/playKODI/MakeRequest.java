package net.davtyan.playKODI;

import android.os.AsyncTask;
import android.util.Base64;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;


class MakeRequest extends AsyncTask<String, Integer, String> {

    AsyncResponse delegate = null;

    private static String convertStreamToString(java.io.InputStream is) {  //convert Stream to String
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    @Override
    protected String doInBackground(String... params) {
        String response;
        String APP_PREFERENCES_HOST = params[0];
        String APP_PREFERENCES_PORT = params[1];
        String APP_PREFERENCES_LOGIN = params[2];
        String APP_PREFERENCES_PASS = params[3];
        String link = params[4];
        String operation = params[5];

        String data;
        if (operation.equalsIgnoreCase("add")) {
            data = "{\"jsonrpc\":\"2.0\",\"method\":\"Playlist.Add\",\"params\":{\"playlistid\":1,\"item\":{\"file\":\"" +
                    link + "\"}},\"id\":0}";
        } else {
            data = "{\"jsonrpc\":\"2.0\",\"method\":\"Player.Open\",\"params\":{\"item\":{\"file\":\"" +
                    link + "\"}},\"id\":0}";
        }

        String urlString = "http://" + APP_PREFERENCES_HOST + ":" + APP_PREFERENCES_PORT + "/jsonrpc";
        String userPass = APP_PREFERENCES_LOGIN + ":" + APP_PREFERENCES_PASS;
        String basicAuth = "Basic " + Base64.encodeToString(userPass.getBytes(), Base64.NO_WRAP);

        if (urlString.contains(" ")) {
            response = "Illegal Argument ERROR";
            return response;
        }
        try {
            URL urlUrl = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) urlUrl.openConnection();
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(10000);
            connection.setRequestProperty("Authorization", basicAuth);
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            connection.setRequestMethod("POST");
            try {
                OutputStream os = connection.getOutputStream();
                os.write(data.getBytes(StandardCharsets.UTF_8));
                os.flush();
                os.close();
                InputStream connectionStream = new BufferedInputStream(connection.getInputStream());
                response = convertStreamToString(connectionStream);
                connectionStream.close();
            } catch (IOException e) {
                e.printStackTrace();
                response = "Network ERROR";
            } finally {
                connection.disconnect();
            }
            connection.disconnect();

        } catch (IllegalArgumentException | MalformedURLException exc) {
            exc.printStackTrace();
            response = "Illegal Argument ERROR";

        } catch (Exception e) {
            e.printStackTrace();
            response = "Network ERROR";
        }
        return response;
    }

    protected void onProgressUpdate(Integer... progress) {
    }

    @Override
    protected void onPostExecute(String result) {
        delegate.processFinish(result);
    }

}


