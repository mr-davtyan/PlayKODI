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
import java.util.Objects;

import static net.davtyan.playKODI.Settings.APP_PREFERENCES;
import static net.davtyan.playKODI.Settings.APP_PREFERENCES_COPY_LINKS;
import static net.davtyan.playKODI.Settings.APP_PREFERENCES_FIRST_RUN;
import static net.davtyan.playKODI.Settings.APP_PREFERENCES_PREVIEW_LINKS;

public class SendFormPlay extends Activity implements AsyncResponse {

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

        String[] requestParams = new String[10];
//        requestParams[0] = APP_PREFERENCES_HOST;
//        requestParams[1] = APP_PREFERENCES_PORT;
//        requestParams[2] = APP_PREFERENCES_LOGIN;
//        requestParams[3] = APP_PREFERENCES_PASS;
//        requestParams[4] = textToPaste;
//        requestParams[5] = "OPEN";

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