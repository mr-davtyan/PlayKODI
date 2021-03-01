package net.davtyan.playKODI;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Base64;
import android.widget.Toast;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.content.ClipData.newPlainText;
import static net.davtyan.playKODI.Settings.APP_PREFERENCES;
import static net.davtyan.playKODI.Settings.APP_PREFERENCES_COPY_LINKS;
import static net.davtyan.playKODI.Settings.APP_PREFERENCES_FIRST_RUN;
import static net.davtyan.playKODI.Settings.APP_PREFERENCES_HOST;
import static net.davtyan.playKODI.Settings.APP_PREFERENCES_LOGIN;
import static net.davtyan.playKODI.Settings.APP_PREFERENCES_PASS;
import static net.davtyan.playKODI.Settings.APP_PREFERENCES_PORT;
import static net.davtyan.playKODI.Settings.basicAuth;

public class SendFormPlayYoutube extends Activity implements AsyncResponse {

    private final String[] uri = new String[2];

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

        String userPass = mSettings.getString(APP_PREFERENCES_LOGIN, "Login") +
                ":" +
                mSettings.getString(APP_PREFERENCES_PASS, "Pass");
        basicAuth = "Basic " + Base64.encodeToString(userPass.getBytes(), Base64.NO_WRAP);

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

            uri[0] = "http://" +
                    mSettings.getString(APP_PREFERENCES_HOST, "") +
                    ":" +
                    mSettings.getString(APP_PREFERENCES_PORT, "") +
                    "/jsonrpc";

            uri[1] =
                    "{\"jsonrpc\":\"2.0\",\"method\":\"Player.Open\",\"params\":{\"item\":{\"file\":\"" +
                            textToPastePlugin +
                            "\"}},\"id\":0}";

            //send request to play
            MakeRequest myMakeRequest = new MakeRequest();
            myMakeRequest.execute(uri);
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
//                        getResources().getString(R.string.messageLinkSuccess) + "\n" + output, Toast.LENGTH_SHORT).show();
                        getResources().getString(R.string.messageLinkSuccess), Toast.LENGTH_SHORT).show();
                break;
        }
    }
}