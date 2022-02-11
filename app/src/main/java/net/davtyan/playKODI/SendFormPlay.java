package net.davtyan.playKODI;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static net.davtyan.playKODI.Settings.APP_PREFERENCES;
import static net.davtyan.playKODI.Settings.APP_PREFERENCES_COPY_LINKS;
import static net.davtyan.playKODI.Hosts.APP_PREFERENCES_FIRST_RUN;
import static net.davtyan.playKODI.Settings.APP_PREFERENCES_DEFAULT_HOST;
import static net.davtyan.playKODI.Settings.APP_PREFERENCES_PREVIEW_LINKS;
import static net.davtyan.playKODI.Settings.APP_PREFERENCES_USE_DEFAULT_HOST;

import com.google.gson.Gson;

public class SendFormPlay extends Activity implements AsyncResponse {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();

        SharedPreferences mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        MyActivity.convertHostToList(mSettings);

        if (mSettings.getBoolean(APP_PREFERENCES_FIRST_RUN, true)) {  //if preference are not set - exit
            Toast.makeText(getApplicationContext(),
                    getResources().getString(R.string.messageSettingsWasntSetup), Toast.LENGTH_SHORT).show();
            finish();
        }

        String textToPaste = Objects.requireNonNull(intent.getData()).toString();

//            prepare smb link
        if (textToPaste.contains("%")){
            try {
                textToPaste = URLDecoder.decode(textToPaste, "UTF-8");
                if (textToPaste.contains("%")){
                    textToPaste = URLDecoder.decode(textToPaste, "UTF-8");
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        if (textToPaste.contains("smb:")){
            textToPaste = textToPaste.substring(textToPaste.indexOf("smb:"));
            if (textToPaste.contains("?")){
                textToPaste = textToPaste.substring(0, textToPaste.indexOf("?"));
            }
        }







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
        if (!useDefaultHost || hostId < 0){
            Intent intentHostsList = new Intent(SendFormPlay.this, HostsListDialog.class);
            intentHostsList.putExtra("link", textToPaste);
            intentHostsList.putExtra("host", hosts.get(hostId).host);
            intentHostsList.putExtra("port", hosts.get(hostId).port);
            intentHostsList.putExtra("login", hosts.get(hostId).login);
            intentHostsList.putExtra("password", hosts.get(hostId).password);
            intentHostsList.putExtra("host", hosts.get(hostId).host);
            intentHostsList.putExtra("event", "OPEN");
            startActivity(intentHostsList);
            finish();
        }else{

            requestParams[0] = hosts.get(hostId).host;
            requestParams[1] = hosts.get(hostId).port;
            requestParams[2] = hosts.get(hostId).login;
            requestParams[3] = hosts.get(hostId).password;
            requestParams[4] = textToPaste;






            requestParams[5] = "OPEN";

            //coping to clipboard
            if (mSettings.getBoolean(APP_PREFERENCES_COPY_LINKS, false)) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("label", textToPaste);
                Objects.requireNonNull(clipboard).setPrimaryClip(clip);
            }
            //preview the link
            if (mSettings.getBoolean(APP_PREFERENCES_PREVIEW_LINKS, false)) {
                Toast.makeText(getApplicationContext(),
                        getResources().getString(R.string.messageSendingLink) + ":\n" + textToPaste, Toast.LENGTH_SHORT).show();
            }

            //send request to play
            MakeRequest myMakeRequest = new MakeRequest();
            myMakeRequest.execute(requestParams);
            myMakeRequest.delegate = this;

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