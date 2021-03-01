package net.davtyan.playKODI;

import android.os.AsyncTask;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static net.davtyan.playKODI.Settings.basicAuth;


class MakeRequest extends AsyncTask<String, Integer, String> {

    AsyncResponse delegate = null;

    private static String convertStreamToString(java.io.InputStream is) {  //convert Stream to String
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }


    @Override
    protected String doInBackground(String... params) {
        String response;
        String urlString = params[0]; // URL to call
        String data = params[1]; //data to post
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


