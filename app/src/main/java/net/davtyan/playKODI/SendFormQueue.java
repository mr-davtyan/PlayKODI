package net.davtyan.playKODI;

import static android.content.ClipData.newPlainText;
import static net.davtyan.playKODI.Settings.APP_PREFERENCES;
import static net.davtyan.playKODI.Settings.APP_PREFERENCES_COPY_LINKS;
import static net.davtyan.playKODI.Settings.APP_PREFERENCES_DEFAULT_HOST;
import static net.davtyan.playKODI.Settings.APP_PREFERENCES_PREVIEW_LINKS;
import static net.davtyan.playKODI.Settings.APP_PREFERENCES_USE_DEFAULT_HOST;
import static net.davtyan.playKODI.Util.extractYTId;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class SendFormQueue extends Activity implements AsyncResponse {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();

        SharedPreferences mSettings = getSharedPreferences(APP_PREFERENCES, MODE_PRIVATE);
        MyActivity.convertHostToList(mSettings);

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

        //            prepare smb link
        if (textToPaste.contains("%")) {
            try {
                textToPaste = URLDecoder.decode(textToPaste, "UTF-8");
                if (textToPaste.contains("%")) {
                    textToPaste = URLDecoder.decode(textToPaste, "UTF-8");
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        if (textToPaste.contains("smb:")) {
            textToPaste = textToPaste.substring(textToPaste.indexOf("smb:"));
            if (textToPaste.contains("?")) {
                textToPaste = textToPaste.substring(0, textToPaste.indexOf("?"));
            }
        }

        if (textToPaste.contains("youtu")) { //если ссылка из ютуба- продолжаем

            String youtubeId = extractYTId(textToPaste);    // парсим youtube id

            String textToPastePlugin = "plugin://plugin.video.youtube/play/?video_id=" + youtubeId;

            Gson gson = new Gson();
            String json = mSettings.getString("hosts", "");
            List<Host> hosts = new ArrayList<>();
            if (!json.equalsIgnoreCase("")) {
                hosts.addAll(Arrays.asList(gson.fromJson(json, Host[].class)));
            }
            if (hosts.size() == 0) {
                Toast.makeText(this, R.string.please_add_one_host, Toast.LENGTH_LONG).show();
                startActivity(new Intent(SendFormQueue.this, Hosts.class));
                finish();
            }

            String[] requestParams = new String[10];

            List<String> hostFullAddress = new ArrayList<>();
            for (Host host : hosts) {
                hostFullAddress.add(host.host + ":" + host.port);
            }
            int hostId = hostFullAddress.indexOf(mSettings.getString(APP_PREFERENCES_DEFAULT_HOST, ""));
            boolean useDefaultHost = mSettings.getBoolean(APP_PREFERENCES_USE_DEFAULT_HOST, false);

            if (!useDefaultHost || hostId < 0) {
                Intent intentHostsList = new Intent(SendFormQueue.this, HostsListDialog.class);
                intentHostsList.putExtra("link", textToPastePlugin);
                intentHostsList.putExtra("event", "YOUTUBEADD");
                startActivity(intentHostsList);
            } else {
                requestParams[0] = hosts.get(hostId).host;
                requestParams[1] = hosts.get(hostId).port;
                requestParams[2] = hosts.get(hostId).login;
                requestParams[3] = hosts.get(hostId).password;
                requestParams[4] = textToPastePlugin;
                requestParams[5] = "YOUTUBEADD";

                String fullName = hosts.get(hostId).host + ":" + hosts.get(hostId).port;
                if (!hosts.get(hostId).nickName.equals("")) fullName = hosts.get(hostId).nickName;
                Toast.makeText(getApplicationContext(),
                        getResources().getString(R.string.messageSendingLink) + " " + fullName,
                        Toast.LENGTH_SHORT).show();

                //send request to play
                MakeRequest myMakeRequest = new MakeRequest();
                myMakeRequest.execute(requestParams);
                myMakeRequest.delegate = this;
            }
            finish();
        }

        Gson gson = new Gson();
        String json = mSettings.getString("hosts", "");
        List<Host> hosts = new ArrayList<>();
        if (!json.equalsIgnoreCase("")) {
            hosts.addAll(Arrays.asList(gson.fromJson(json, Host[].class)));
        }
        if (hosts.size() == 0) {
            Toast.makeText(this, R.string.please_add_one_host, Toast.LENGTH_LONG).show();
            startActivity(new Intent(SendFormQueue.this, Hosts.class));
            finish();
        }

        String[] requestParams = new String[10];

        List<String> hostFullAddress = new ArrayList<>();
        for (Host host : hosts) {
            hostFullAddress.add(host.host + ":" + host.port);
        }
        int hostId = hostFullAddress.indexOf(mSettings.getString(APP_PREFERENCES_DEFAULT_HOST, ""));
        boolean useDefaultHost = mSettings.getBoolean(APP_PREFERENCES_USE_DEFAULT_HOST, false);

        if (!useDefaultHost || hostId < 0) {
            Intent intentHostsList = new Intent(SendFormQueue.this, HostsListDialog.class);
            intentHostsList.putExtra("link", textToPaste);
            intentHostsList.putExtra("event", "ADD");
            startActivity(intentHostsList);
        } else {
            requestParams[0] = hosts.get(hostId).host;
            requestParams[1] = hosts.get(hostId).port;
            requestParams[2] = hosts.get(hostId).login;
            requestParams[3] = hosts.get(hostId).password;
            requestParams[4] = textToPaste;
            requestParams[5] = "ADD";

            //coping to clipboard
            if (mSettings.getBoolean(APP_PREFERENCES_COPY_LINKS, false)) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                ClipData clip = newPlainText("label", textToPaste);
                Objects.requireNonNull(clipboard).setPrimaryClip(clip);
            }

            String fullName = hosts.get(hostId).host + ":" + hosts.get(hostId).port;
            if (!hosts.get(hostId).nickName.equals("")) fullName = hosts.get(hostId).nickName;
            String toastMessage = getResources().getString(R.string.messageSendingLink) + " " + fullName;
            //preview the link
            if (mSettings.getBoolean(APP_PREFERENCES_PREVIEW_LINKS, false)) {
                toastMessage = toastMessage + "\n" + textToPaste;
            }
            Toast.makeText(getApplicationContext(), toastMessage, Toast.LENGTH_SHORT).show();

            //send request to play
            MakeRequest myMakeRequest = new MakeRequest();
            myMakeRequest.execute(requestParams);
            myMakeRequest.delegate = this;
        }
        finish();
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