package net.davtyan.playKODI;

import static android.content.ClipData.newPlainText;
import static net.davtyan.playKODI.Hosts.APP_PREFERENCES_FIRST_RUN;
import static net.davtyan.playKODI.Settings.APP_PREFERENCES;
import static net.davtyan.playKODI.Settings.APP_PREFERENCES_COPY_LINKS;
import static net.davtyan.playKODI.Settings.APP_PREFERENCES_DEFAULT_HOST;
import static net.davtyan.playKODI.Settings.APP_PREFERENCES_USE_DEFAULT_HOST;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SendFormPlayYoutube extends Activity implements AsyncResponse {

    private static String extractYTId(String ytUrl) {
        String vId = "";
        Pattern pattern = Pattern.compile(".*(?:youtu.be/|v/|u/\\w/|embed/|watch\\?v=)([^#&?]*).*");
        Matcher matcher = pattern.matcher(ytUrl);
        if (matcher.matches()) {
            vId = matcher.group(1);
        }
        return vId;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();

        SharedPreferences mSettings = getSharedPreferences(APP_PREFERENCES, MODE_PRIVATE);
        MyActivity.convertHostToList(mSettings);

        if (mSettings.getBoolean(APP_PREFERENCES_FIRST_RUN, true)) {  //if preference are not set - exit
            Toast.makeText(getApplicationContext(),
                    getResources().getString(R.string.messageSettingsWasntSetup), Toast.LENGTH_SHORT).show();
            finish();
        }

        String textToPaste = "";

        try {           //extract the link
            textToPaste = Objects.requireNonNull(intent.getData()).toString();

            if (mSettings.getBoolean(APP_PREFERENCES_COPY_LINKS, false)) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                ClipData clip = newPlainText("label", textToPaste);
                Objects.requireNonNull(clipboard).setPrimaryClip(clip);
            }

        } catch (Exception e) {
            try {

                ClipData clip = intent.getClipData();
                //copy the link from the clipboard
                textToPaste = Objects.requireNonNull(clip).getItemAt(0).coerceToText(this).toString();

                if (mSettings.getBoolean(APP_PREFERENCES_COPY_LINKS, false)) {
                    //coping to clipboard
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                    Objects.requireNonNull(clipboard).setPrimaryClip(clip);
                }

            } catch (Exception e1) {
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.messageLinkHaveWhitespace), Toast.LENGTH_SHORT).show();
                e1.printStackTrace();
                finish();
            }
        }

        if (textToPaste.contains("youtu")) { //если ссылка из ютуба- продолжаем

            String youtubeId = extractYTId(textToPaste);    // парсим youtube id

            String textToPastePlugin = "plugin://plugin.video.youtube/play/?video_id=" + youtubeId;

            Gson gson = new Gson();
            String json = mSettings.getString("hosts", "");
            List<Host> hosts = new ArrayList<>(Arrays.asList(gson.fromJson(json, Host[].class)));

            String[] requestParams = new String[10];

            List<String> hostFullAddress = new ArrayList<>();
            for (Host host : hosts) {
                hostFullAddress.add(host.host + ":" + host.port);
            }
            int hostId = hostFullAddress.indexOf(mSettings.getString(APP_PREFERENCES_DEFAULT_HOST, ""));
            boolean useDefaultHost = mSettings.getBoolean(APP_PREFERENCES_USE_DEFAULT_HOST, false);

            if (!useDefaultHost || hostId < 0) {
                Intent intentHostsList = new Intent(SendFormPlayYoutube.this, HostsListDialog.class);
                intentHostsList.putExtra("link", textToPastePlugin);
                intentHostsList.putExtra("event", "YOUTUBE");
                startActivity(intentHostsList);
            } else {
                requestParams[0] = hosts.get(hostId).host;
                requestParams[1] = hosts.get(hostId).port;
                requestParams[2] = hosts.get(hostId).login;
                requestParams[3] = hosts.get(hostId).password;
                requestParams[4] = textToPastePlugin;
                requestParams[5] = "OPEN";

                //send request to play
                MakeRequest myMakeRequest = new MakeRequest();
                myMakeRequest.execute(requestParams);
                myMakeRequest.delegate = this;
            }
            finish();

        } else { //exit if not a youtube link
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.messageCantRecognizeYoutubeLink), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public void processFinish(String output) {
        //toast messages about the result
        switch (output) {
            case "Illegal Argument ERROR":
                Toast.makeText(getApplicationContext(),
                        getResources().getString(R.string.messageLinkHaveWhitespace), Toast.LENGTH_SHORT).show();
                break;
            case "Network ERROR":
                Toast.makeText(getApplicationContext(),
                        getResources().getString(R.string.messageNetworkError), Toast.LENGTH_SHORT).show();
                break;
            default:
                Toast.makeText(getApplicationContext(),
                        getResources().getString(R.string.messageLinkSuccess), Toast.LENGTH_SHORT).show();
                break;
        }
    }
}