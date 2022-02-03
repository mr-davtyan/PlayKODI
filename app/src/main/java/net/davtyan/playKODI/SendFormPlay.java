package net.davtyan.playKODI;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Base64;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Objects;

import static net.davtyan.playKODI.Settings.APP_PREFERENCES;
import static net.davtyan.playKODI.Settings.APP_PREFERENCES_COPY_LINKS;
import static net.davtyan.playKODI.Settings.APP_PREFERENCES_FIRST_RUN;
//import static net.davtyan.playKODI.Settings.APP_PREFERENCES_HOST;
//import static net.davtyan.playKODI.Settings.APP_PREFERENCES_LOGIN;
//import static net.davtyan.playKODI.Settings.APP_PREFERENCES_PASS;
//import static net.davtyan.playKODI.Settings.APP_PREFERENCES_PORT;
import static net.davtyan.playKODI.Settings.APP_PREFERENCES_PREVIEW_LINKS;
//import static net.davtyan.playKODI.Settings.basicAuth;

public class SendFormPlay extends Activity implements AsyncResponse {

    private final String[] uri = new String[2];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();

        SharedPreferences mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);

        if (mSettings.getBoolean(APP_PREFERENCES_FIRST_RUN, true)) {  //if preference are not set - exit
            Toast.makeText(getApplicationContext(),
                    getResources().getString(R.string.messageSettingsWasntSetup), Toast.LENGTH_SHORT).show();
            finish();
        }

//        String userPass = mSettings.getString(APP_PREFERENCES_LOGIN, "Login") +
//                ":" +
//                mSettings.getString(APP_PREFERENCES_PASS, "Pass");
//        basicAuth = "Basic " + Base64.encodeToString(userPass.getBytes(), Base64.NO_WRAP);

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

        uri[0] = "http://" +
                mSettings.getString(APP_PREFERENCES_HOST, "") +
                ":" +
                mSettings.getString(APP_PREFERENCES_PORT, "") +
                "/jsonrpc";

        uri[1] = "{\"jsonrpc\":\"2.0\",\"method\":\"Player.Open\",\"params\":{\"item\":{\"file\":\"" +
                        textToPaste +
                        "\"}},\"id\":0}";

        String userPass = mSettings.getString(APP_PREFERENCES_LOGIN, "Login") +
                ":" +
                mSettings.getString(APP_PREFERENCES_PASS, "Pass");
        uri[2] = "Basic " + Base64.encodeToString(userPass.getBytes(), Base64.NO_WRAP);

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
        myMakeRequest.execute(uri);
        myMakeRequest.delegate = this;

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