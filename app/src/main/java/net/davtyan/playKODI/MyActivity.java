package net.davtyan.playKODI;

import static net.davtyan.playKODI.Settings.APP_PREFERENCES;
import static net.davtyan.playKODI.Settings.APP_PREFERENCES_FIRST_RUN;
import static net.davtyan.playKODI.Settings.APP_PREFERENCES_THEME_DARK;
import static net.davtyan.playKODI.Settings.APP_PREFERENCES_THEME_DARK_AUTO;

import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
//import static net.davtyan.playKODI.Settings.basicAuth;


public class MyActivity extends AppCompatActivity implements AsyncResponse {

    static boolean haveToCloseApp = false;
    Snackbar mySnackbar;
    private Boolean exit = false;
    private Boolean darkMode = false;
    private EditText appPreferencesLinkText;
    private String textToPaste = "";
    private SharedPreferences mSettings;

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
    public void onBackPressed() {
        if (exit) {
            finish();
        } else {
            mySnackbar.setText("Press Back again for Exit").show();
            exit = true;
            new Handler().postDelayed(() -> exit = false, 2 * 1000);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);

        //checking the current theme
        if (mSettings.getBoolean(APP_PREFERENCES_THEME_DARK_AUTO, false)) {
            switch (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) {
                case Configuration.UI_MODE_NIGHT_YES:
                    setTheme(R.style.AppThemeDark);
                    darkMode = true;
                    break;
                case Configuration.UI_MODE_NIGHT_NO:
                    setTheme(R.style.AppTheme);
                    darkMode = false;
                    break;
            }
        } else {
            if (mSettings.getBoolean(APP_PREFERENCES_THEME_DARK, true)) {
                setTheme(R.style.AppTheme);
                darkMode = false;
            } else {
                setTheme(R.style.AppThemeDark);
                darkMode = true;
            }
        }

        setContentView(R.layout.main);

        mySnackbar = Snackbar.make(findViewById(R.id.mainLayout), "", Snackbar.LENGTH_SHORT);
        if (darkMode) { //check the theme
            mySnackbar.getView().setBackgroundColor(ContextCompat.getColor(this, R.color.black_alpha));
        } else {
            mySnackbar.getView().setBackgroundColor(ContextCompat.getColor(this, R.color.navigationBarColor));
        }

        if (mSettings.getBoolean(APP_PREFERENCES_FIRST_RUN, true) && !haveToCloseApp) {
            Intent intent = new Intent(MyActivity.this, Settings.class);
            startActivity(intent);
        } else if (mSettings.getBoolean(APP_PREFERENCES_FIRST_RUN, true) && haveToCloseApp) {
            finish();
        }

        //initializing clipboard
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (Objects.requireNonNull(clipboard).hasPrimaryClip()) {
            textToPaste = Objects.requireNonNull(clipboard.getPrimaryClip()).getItemAt(0).coerceToText(this).toString();
        }

        appPreferencesLinkText = findViewById(R.id.editTextLink);
        appPreferencesLinkText.setText(textToPaste); // paste text from clipboard


    }

    public void onClick(View view) {
        String[] uri = new String[3];

        int id = view.getId();
        if (id == R.id.buttonSendToKodi) {
            textToPaste = appPreferencesLinkText.getText().toString();
            if (textToPaste.equals("")) {
                mySnackbar.setText(getResources().getString(R.string.messageLinkHaveWhitespace)).show();
                return;
            }
            if (textToPaste.contains("youtu")) {
                String youtubeId = extractYTId(textToPaste);
                textToPaste = "plugin://plugin.video.youtube/play/?video_id=" + youtubeId;
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

            MakeRequest myMakeRequest = new MakeRequest();
            myMakeRequest.execute(uri);
            myMakeRequest.delegate = this;
        } else if (id == R.id.buttonAddToKodi) { // button Send to KODI
            MakeRequest myMakeRequest;
            textToPaste = appPreferencesLinkText.getText().toString(); //getting text
            if (textToPaste.equals("")) { //check empty string
                mySnackbar.setText(getResources().getString(R.string.messageLinkHaveWhitespace)).show();
                return;
            }
            if (textToPaste.contains("youtu")) {
                String youtubeId = extractYTId(textToPaste);    // parse youtube id
                textToPaste = "plugin://plugin.video.youtube/play/?video_id=" + youtubeId;
            }
            uri[0] = "http://" +
                    mSettings.getString(APP_PREFERENCES_HOST, "") +
                    ":" +
                    mSettings.getString(APP_PREFERENCES_PORT, "") +
                    "/jsonrpc";

            uri[1] = "{\"jsonrpc\":\"2.0\",\"method\":\"Playlist.Add\",\"params\":{\"playlistid\":1,\"item\":{\"file\":\"" +
                    textToPaste +
                    "\"}},\"id\":0}";

            String userPass = mSettings.getString(APP_PREFERENCES_LOGIN, "Login") +
                    ":" +
                    mSettings.getString(APP_PREFERENCES_PASS, "Pass");
            uri[2] = "Basic " + Base64.encodeToString(userPass.getBytes(), Base64.NO_WRAP);

            myMakeRequest = new MakeRequest(); //send request queue
            myMakeRequest.execute(uri);
            myMakeRequest.delegate = this;
        }
    }

    protected void onResume() {
        super.onResume();

        if (mSettings.getBoolean(APP_PREFERENCES_FIRST_RUN, true) & haveToCloseApp) {
            haveToCloseApp = false;
            finish();
        }

        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (Objects.requireNonNull(clipboard).hasPrimaryClip()) {
            textToPaste = Objects.requireNonNull(clipboard.getPrimaryClip()).getItemAt(0).coerceToText(this).toString();
        }

        appPreferencesLinkText = findViewById(R.id.editTextLink);
        appPreferencesLinkText.setText(textToPaste);// вставить текст из буфера
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intentSettings = new Intent(MyActivity.this, Settings.class);
            startActivity(intentSettings);
            finish();
            return true;
        } else if (id == R.id.action_about) {
            Intent intentAbout = new Intent(MyActivity.this, About.class);
            startActivity(intentAbout);
            return true;
        } else if (id == R.id.action_exit) {
            System.exit(0);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void processFinish(String output) {
        switch (output) {
            case "Illegal Argument ERROR":
                mySnackbar.setText(getResources().getString(R.string.messageLinkHaveWhitespace)).show();
                break;
            case "Network ERROR":
                mySnackbar.setText(getResources().getString(R.string.messageNetworkError)).show();
                break;
            default:
                mySnackbar.setText(getResources().getString(R.string.messageLinkSuccess) + "\n" + output).show();
                break;
        }
    }


}
