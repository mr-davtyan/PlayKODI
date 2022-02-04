package net.davtyan.playKODI;

import static net.davtyan.playKODI.MyActivity.haveToCloseApp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class HostEditor extends AppCompatActivity {

    static final String APP_PREFERENCES = "MySettings";

    static final String APP_PREFERENCES_FIRST_RUN = "Run"; //
    static final String APP_PREFERENCES_THEME_DARK = "Theme"; //
    static final String APP_PREFERENCES_THEME_DARK_AUTO = "AutoTheme"; //

    private static SharedPreferences mSettings;

    private int hostId;

    private EditText editTextNickname;
    private EditText editTextHost;
    private EditText editTextPort;
    private EditText editTextLogin;
    private EditText editTextPass;
    private Button buttonHostColor;
    private Button buttonHostSave;
    private Button buttonHostDelete;
    private Button buttonHostExit;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);

        if (mSettings.getBoolean(APP_PREFERENCES_THEME_DARK_AUTO, false)) {
            switch (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) {
                case Configuration.UI_MODE_NIGHT_YES:
                    setTheme(R.style.AppThemeDark);
                    break;
                case Configuration.UI_MODE_NIGHT_NO:
                    setTheme(R.style.AppTheme);
                    break;
            }
        } else {
            if (mSettings.getBoolean(APP_PREFERENCES_THEME_DARK, true)) { //checking the theme
                setTheme(R.style.AppTheme);
            } else {
                setTheme(R.style.AppThemeDark);
            }
        }

        setContentView(R.layout.host_editor);

        editTextNickname = findViewById(R.id.editTextName);
        editTextHost = findViewById(R.id.editTextHost);
        editTextPort = findViewById(R.id.editTextPort);
        editTextLogin = findViewById(R.id.editTextLogin);
        editTextPass = findViewById(R.id.editTextPass);
        buttonHostColor = findViewById(R.id.buttonColorPicker);
        buttonHostSave = findViewById(R.id.buttonHostSave);
        buttonHostDelete = findViewById(R.id.buttonHostDelete);
        buttonHostExit = findViewById(R.id.buttonHostCancel);

        Intent intent = getIntent();
        hostId = intent.getExtras().getInt("ID");

    }

    public void onClick(View v) {
        Intent intentHosts = new Intent(HostEditor.this, Hosts.class);

        int id = v.getId();
        if (id == R.id.buttonHostSave) {  // save button
            SharedPreferences.Editor editor = mSettings.edit();

            editor.apply();
            startActivity(intentHosts);
            finish();
        } else if (id == R.id.buttonHostDelete) {  // exit button
            SharedPreferences.Editor editor = mSettings.edit();

            editor.apply();
            startActivity(intentHosts);
            finish();
        } else if (id == R.id.buttonHostCancel) {  // exit button
            startActivity(intentHosts);
            finish();
        }
    }

//    public void onBackPressed() { // Back button
//        finish();
//    }

}