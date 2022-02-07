package net.davtyan.playKODI;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.skydoves.colorpickerview.ColorPickerDialog;
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HostEditor extends AppCompatActivity {

    static final String APP_PREFERENCES = "MySettings";
    static final String APP_PREFERENCES_THEME_DARK = "Theme"; //
    static final String APP_PREFERENCES_THEME_DARK_AUTO = "AutoTheme"; //

    private static SharedPreferences mSettings;
    List<Host> hosts = new ArrayList<>();
    private int hostId;
    private EditText editTextNickname;
    private EditText editTextHost;
    private EditText editTextPort;
    private EditText editTextLogin;
    private EditText editTextPass;
    private Button buttonHostColor;
    private EditText editTextColorCode;
    private Button buttonHostDelete;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);

        if (mSettings.getBoolean(APP_PREFERENCES_THEME_DARK_AUTO, false)) {
            switch (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) {
                case Configuration.UI_MODE_NIGHT_YES:
                    setTheme(android.R.style.Theme_DeviceDefault_Dialog_NoActionBar_MinWidth);
                    break;
                case Configuration.UI_MODE_NIGHT_NO:
                    setTheme(android.R.style.Theme_DeviceDefault_Light_Dialog_MinWidth);
                    break;
            }
        } else {
            if (mSettings.getBoolean(APP_PREFERENCES_THEME_DARK, true)) { //checking the theme
                setTheme(android.R.style.Theme_DeviceDefault_Light_Dialog_MinWidth);
            } else {
                setTheme(android.R.style.Theme_DeviceDefault_Dialog_NoActionBar_MinWidth);
            }
        }

        setContentView(R.layout.host_editor);

        Gson gson = new Gson();
        String json = mSettings.getString("hosts", "");
        hosts.addAll(Arrays.asList(gson.fromJson(json, Host[].class)));

        editTextNickname = findViewById(R.id.editTextName);
        editTextHost = findViewById(R.id.editTextHost);
        editTextPort = findViewById(R.id.editTextPort);
        editTextLogin = findViewById(R.id.editTextLogin);
        editTextPass = findViewById(R.id.editTextPass);
        editTextColorCode = findViewById(R.id.editTextColorCode);
        buttonHostColor = findViewById(R.id.buttonColorPicker);
        buttonHostDelete = findViewById(R.id.buttonHostDelete);

        Intent intent = getIntent();
        hostId = intent.getExtras().getInt("ID");

        if (hostId < hosts.size()) {
            editTextNickname.setText(hosts.get(hostId).nickName);
            editTextHost.setText(hosts.get(hostId).host);
            editTextPort.setText(hosts.get(hostId).port);
            editTextLogin.setText(hosts.get(hostId).login);
            editTextPass.setText(hosts.get(hostId).password);
            editTextColorCode.setText(hosts.get(hostId).color);
            try {
                buttonHostColor.setBackgroundColor(Integer.decode(editTextColorCode.getText().toString()));
            } catch (NumberFormatException e) {
                buttonHostColor.setBackgroundColor(-11889757);
            }
        }else{
            buttonHostDelete.setVisibility(View.GONE);
        }
    }

    public void onClick(View v) {
        Intent intentHosts = new Intent(HostEditor.this, Hosts.class);

        int id = v.getId();
        if (id == R.id.buttonHostSave) {  // save button
            if(editTextHost.getText().toString().equalsIgnoreCase("")){
                Toast.makeText(getApplicationContext(), "Please enter Host IP and Port first.", Toast.LENGTH_SHORT).show();
                return;
            }
            Host newHost = new Host(
                    editTextNickname.getText().toString(),
                    editTextHost.getText().toString(),
                    editTextPort.getText().toString(),
                    editTextLogin.getText().toString(),
                    editTextPass.getText().toString(),
                    editTextColorCode.getText().toString(),
                    hostId);

            if (hostId < hosts.size()) {
                hosts.remove(hostId);
            }
            hosts.add(hostId, newHost);

            SharedPreferences.Editor editor = mSettings.edit();
            Gson gson = new Gson();
            String json = gson.toJson(hosts);
            editor.putString("hosts", json);
            editor.apply();
            startActivity(intentHosts);
            finish();
        } else if (id == R.id.buttonHostDelete) {  // exit button
            if (hostId < hosts.size()) {
                hosts.remove(hostId);
            }
            SharedPreferences.Editor editor = mSettings.edit();
            Gson gson = new Gson();
            String json = gson.toJson(hosts);
            editor.putString("hosts", json);
            editor.apply();
            startActivity(intentHosts);
            finish();
        } else if (id == R.id.buttonHostCancel) {  // exit button
            startActivity(intentHosts);
            finish();
        } else if (id == R.id.buttonColorPicker) {  // exit button
            new ColorPickerDialog.Builder(this)
                    .setTitle(editTextNickname.getText().toString())
                    .setPositiveButton(getString(R.string.confirm),
                            (ColorEnvelopeListener) (envelope, fromUser) -> {
                                editTextColorCode.setText(String.valueOf(envelope.getColor()));
                                buttonHostColor.setBackgroundColor(Integer.decode(editTextColorCode.getText().toString()));
                            })
                    .setNegativeButton(getString(R.string.cancel),
                            (dialogInterface, i) -> dialogInterface.dismiss())
                    .attachAlphaSlideBar(false) // the default value is true.
                    .attachBrightnessSlideBar(false)  // the default value is true.
                    .setBottomSpace(12) // set a bottom space between the last slide bar and buttons.
                    .show();
        }
    }
}