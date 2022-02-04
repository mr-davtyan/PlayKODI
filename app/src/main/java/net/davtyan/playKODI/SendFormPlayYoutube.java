package net.davtyan.playKODI;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.content.ClipData.newPlainText;
import static net.davtyan.playKODI.Settings.APP_PREFERENCES;
import static net.davtyan.playKODI.Settings.APP_PREFERENCES_COPY_LINKS;
import static net.davtyan.playKODI.Settings.APP_PREFERENCES_FIRST_RUN;

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

            String[] requestParams = new String[10];
//            requestParams[0] = APP_PREFERENCES_HOST;
//            requestParams[1] = APP_PREFERENCES_PORT;
//            requestParams[2] = APP_PREFERENCES_LOGIN;
//            requestParams[3] = APP_PREFERENCES_PASS;
//            requestParams[4] = textToPastePlugin;
//            requestParams[5] = "OPEN";

            //send request to play
            MakeRequest myMakeRequest = new MakeRequest();
            myMakeRequest.execute(requestParams);
            myMakeRequest.delegate = this;

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