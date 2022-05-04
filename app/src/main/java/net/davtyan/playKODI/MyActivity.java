package net.davtyan.playKODI;

import static net.davtyan.playKODI.Settings.APP_PREFERENCES;
import static net.davtyan.playKODI.Settings.APP_PREFERENCES_DEFAULT_HOST;
import static net.davtyan.playKODI.Settings.APP_PREFERENCES_THEME_DARK;
import static net.davtyan.playKODI.Settings.APP_PREFERENCES_THEME_DARK_AUTO;
import static net.davtyan.playKODI.Settings.APP_PREFERENCES_USE_DEFAULT_HOST;
import static net.davtyan.playKODI.Util.prepareLink;
import static net.davtyan.playKODI.Util.prepareRequestParams;

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
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class MyActivity extends AppCompatActivity implements AsyncResponse {

    Snackbar mySnackbar;
    private Boolean exit = false;
    private Boolean darkMode = false;
    private EditText appPreferencesLinkText;
    Spinner spinnerDefaultHost;
    List<Host> hosts = new ArrayList<>();

    @Override
    public void onBackPressed() {
        if (exit) {
            finish();
        } else {
            mySnackbar.setText(R.string.press_back_again_to_exit).show();
            exit = true;
            new Handler().postDelayed(() -> exit = false, 2 * 1000);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        convertHostToList(mSettings);

        //checking the current theme
        if (mSettings.getBoolean(APP_PREFERENCES_THEME_DARK_AUTO, true)) {
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
    }

    public void onClick(View view) {
        int id = view.getId();

        String textToPaste = appPreferencesLinkText.getText().toString();

        if (textToPaste.equals("")) {
            mySnackbar.setText(getResources().getString(R.string.messageLinkHaveWhitespace)).show();
            return;
        }

        textToPaste = prepareLink(textToPaste);

        String[] requestParams = prepareRequestParams(textToPaste, hosts, spinnerDefaultHost.getSelectedItemPosition());

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

        SharedPreferences mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        convertHostToList(mSettings);

        //checking the current theme
        if (mSettings.getBoolean(APP_PREFERENCES_THEME_DARK_AUTO, true)) {
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

        Gson gson = new Gson();
        String json = mSettings.getString("hosts", "");

        hosts.clear();
        if (!json.equalsIgnoreCase("")) {
            hosts.addAll(Arrays.asList(gson.fromJson(json, Host[].class)));
        }

        if (hosts.size() == 0) {
            Toast.makeText(this, R.string.please_add_one_host, Toast.LENGTH_LONG).show();
            startActivity(new Intent(MyActivity.this, Hosts.class));
            finish();
        }

        appPreferencesLinkText = findViewById(R.id.editTextLink);
        spinnerDefaultHost = findViewById(R.id.spinnerDefaultHost);

        List<String> spinnerItems = new ArrayList<>();
        List<String> colorCodes = new ArrayList<>();
        List<String> hostFullAddress = new ArrayList<>();

        for (Host host : hosts) {
            String fullName = host.host + ":" + host.port;
            if (!host.nickName.equals("")) fullName = host.nickName;
            spinnerItems.add(fullName);
            colorCodes.add(host.color);
            hostFullAddress.add(host.host + ":" + host.port);
        }
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, spinnerItems);
        spinnerDefaultHost.setAdapter(spinnerAdapter);

        if (mSettings.getBoolean(APP_PREFERENCES_USE_DEFAULT_HOST, false)){
            int hostId = hostFullAddress.indexOf(mSettings.getString(APP_PREFERENCES_DEFAULT_HOST, ""));
            spinnerDefaultHost.setSelection(Math.max(hostId, 0));
        }else{
            spinnerDefaultHost.setSelection(0);
        }

        spinnerDefaultHost.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                ImageView spinnerIcon = findViewById(R.id.spinnerIcon);
                spinnerIcon.setColorFilter(Integer.decode(colorCodes.get(position)));
            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });
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
