package net.davtyan.playKODI;

import static net.davtyan.playKODI.Settings.APP_PREFERENCES;
import static net.davtyan.playKODI.Settings.APP_PREFERENCES_DEFAULT_HOST;
import static net.davtyan.playKODI.Settings.APP_PREFERENCES_USE_DEFAULT_HOST;
import static net.davtyan.playKODI.Util.preExecuteMessage;
import static net.davtyan.playKODI.Util.prepareLink;
import static net.davtyan.playKODI.Util.prepareRequestParams;

import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class SendFormPlay extends Activity implements AsyncResponse {

    public String setupEvent() {
        return "OPEN";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();

        SharedPreferences mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        MyActivity.convertHostToList(mSettings);

        Gson gson = new Gson();
        String json = mSettings.getString("hosts", "");
        List<Host> hosts = new ArrayList<>();
        if (!json.equalsIgnoreCase("")) {
            hosts.addAll(Arrays.asList(gson.fromJson(json, Host[].class)));
        }
        if (hosts.size() == 0) {
            Toast.makeText(this, R.string.please_add_one_host, Toast.LENGTH_LONG).show();
            startActivity(new Intent(SendFormPlay.this, Hosts.class));
            finish();
        }
        List<String> hostFullAddress = new ArrayList<>();
        for (Host host : hosts) {
            hostFullAddress.add(host.host + ":" + host.port);
        }
        int hostId = hostFullAddress.indexOf(mSettings.getString(APP_PREFERENCES_DEFAULT_HOST, ""));
        boolean useDefaultHost = mSettings.getBoolean(APP_PREFERENCES_USE_DEFAULT_HOST, false);

        String event = setupEvent();

        String textToPaste = "";

        //extract the link
        try {
            textToPaste = Objects.requireNonNull(intent.getData()).toString();
        } catch (Exception e1) {
            try {
                ClipData clip = intent.getClipData();
                textToPaste = Objects.requireNonNull(clip).getItemAt(0).coerceToText(this).toString();
            }catch (Exception e2) {
                e2.printStackTrace();
            }
        }

        if (textToPaste.equals("")) {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.messageLinkHaveWhitespace), Toast.LENGTH_SHORT).show();
            finish();
        }

        textToPaste = prepareLink(textToPaste);

        if (!useDefaultHost || hostId < 0) {
            Intent intentHostsList = new Intent(SendFormPlay.this, HostsListDialog.class);
            intentHostsList.putExtra("link", textToPaste);
            intentHostsList.putExtra("event", event);
            startActivity(intentHostsList);
        } else {
            String[] requestParams = prepareRequestParams(textToPaste, hosts, hostId);
            requestParams[5] = event;

            preExecuteMessage(mSettings, getSystemService(CLIPBOARD_SERVICE), textToPaste, hosts,
                    hostId, getResources(), getApplicationContext());

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