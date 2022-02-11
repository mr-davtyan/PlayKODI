package net.davtyan.playKODI;

import static net.davtyan.playKODI.Settings.APP_PREFERENCES;
import static net.davtyan.playKODI.Hosts.APP_PREFERENCES_FIRST_RUN;
import static net.davtyan.playKODI.Settings.APP_PREFERENCES_THEME_DARK;
import static net.davtyan.playKODI.Settings.APP_PREFERENCES_THEME_DARK_AUTO;
import static net.davtyan.playKODI.Settings.APP_PREFERENCES_DEFAULT_HOST;


import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MyActivity extends AppCompatActivity implements AsyncResponse {

    static boolean haveToCloseApp = false;
    Snackbar mySnackbar;
    private Boolean exit = false;
    private Boolean darkMode = false;
    private EditText appPreferencesLinkText;
    private String textToPaste = "";
    private SharedPreferences mSettings;
    private View spinnerView;
    Spinner spinnerDefaultHost;
    List<Host> hosts = new ArrayList<>();


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
        convertHostToList(mSettings);

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
            Toast.makeText(this, "Please add at least one KODI host", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(MyActivity.this, Hosts.class);
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

        spinnerDefaultHost = findViewById(R.id.spinnerDefaultHost);
        spinnerView = findViewById(R.id.spinnerView);

        Gson gson = new Gson();
        String json = mSettings.getString("hosts", "");

        hosts.clear();
        hosts.addAll(Arrays.asList(gson.fromJson(json, Host[].class)));

        List<String> spinnerItems = new ArrayList<>();
        List<String> colorCodes = new ArrayList<>();
        List<String> hostFullAddress = new ArrayList<>();

        for (Host host : hosts) {
            String fullName = host.host + ":" + host.port;
            if (!host.nickName.equals("")) fullName = host.nickName + " (" + fullName + " )";
            spinnerItems.add(fullName);
            colorCodes.add(host.color);
            hostFullAddress.add(host.host + ":" + host.port);
        }
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, spinnerItems);
        spinnerDefaultHost.setAdapter(spinnerAdapter);

        int hostId = hostFullAddress.indexOf(mSettings.getString(APP_PREFERENCES_DEFAULT_HOST, ""));
        spinnerDefaultHost.setSelection(Math.max(hostId, 0));

        spinnerDefaultHost.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                spinnerView.setBackgroundColor(Integer.decode(colorCodes.get(position)));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });


    }

    public void onClick(View view) {
        int id = view.getId();

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

        String[] requestParams = new String[10];
        requestParams[0] = hosts.get(spinnerDefaultHost.getSelectedItemPosition()).host;
        requestParams[1] = hosts.get(spinnerDefaultHost.getSelectedItemPosition()).port;
        requestParams[2] = hosts.get(spinnerDefaultHost.getSelectedItemPosition()).login;
        requestParams[3] = hosts.get(spinnerDefaultHost.getSelectedItemPosition()).password;
        requestParams[4] = textToPaste;

        if (id == R.id.buttonSendToKodi) {
            requestParams[5] = "OPEN";
        } else if (id == R.id.buttonAddToKodi) { // button Send to KODI
            requestParams[5] = "ADD";
        }

        MakeRequest myMakeRequest = new MakeRequest();
        myMakeRequest.execute(requestParams);
        myMakeRequest.delegate = this;

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
        } else if (id == R.id.action_hosts) {
            Intent intentEditHosts = new Intent(MyActivity.this, Hosts.class);
            startActivity(intentEditHosts);
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

    public static void convertHostToList(SharedPreferences prefs){
        final String APP_PREFERENCES_HOST = "Host"; //
        final String APP_PREFERENCES_PORT = "Port"; //
        final String APP_PREFERENCES_LOGIN = "Login"; //
        final String APP_PREFERENCES_PASS = "Pass"; //

        if (!prefs.getString(APP_PREFERENCES_HOST, "convertedtolist156187132417").equalsIgnoreCase("convertedtolist156187132417")){
            SharedPreferences.Editor editor = prefs.edit();
            Gson gson = new Gson();
            List<Host> hosts = new ArrayList<>();

            Host newHost = new Host(
                    "",
                    prefs.getString(APP_PREFERENCES_HOST, ""),
                    prefs.getString(APP_PREFERENCES_PORT, ""),
                    prefs.getString(APP_PREFERENCES_LOGIN, ""),
                    prefs.getString(APP_PREFERENCES_PASS, ""),
                    "-11889757",
                    0);
            hosts.add(newHost);

            String json = gson.toJson(hosts);
            editor.putString("hosts", json);

            editor.putString(APP_PREFERENCES_HOST, "convertedtolist156187132417");
            editor.putString(APP_PREFERENCES_PORT, "convertedtolist156187132417");
            editor.putString(APP_PREFERENCES_LOGIN, "convertedtolist156187132417");
            editor.putString(APP_PREFERENCES_PASS, "convertedtolist156187132417");

            editor.apply();
        }

    }

}
