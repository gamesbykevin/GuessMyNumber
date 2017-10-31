package com.gamesbykevin.guessmynumber;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ToggleButton;

public class OptionsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);

        //get our shared preferences so we can pre-populate
        SharedPreferences preferences = getSharedPreferences(getString(R.string.key_shared_preferences), MODE_PRIVATE);

        //pre-populate the options (if exist)
        if (preferences != null) {

            //access values so we can save to shared preferences
            ToggleButton button1 = (ToggleButton)findViewById(R.id.buttonVibrate);
            ToggleButton button2 = (ToggleButton)findViewById(R.id.buttonInput);
            EditText editText = (EditText)findViewById(R.id.textRandom);

            //update the button settings
            button1.setChecked(preferences.getBoolean(getString(R.string.key_vibrate), true));
            button2.setChecked(preferences.getBoolean(getString(R.string.key_input), true));

            //update the text box
            editText.setText(preferences.getInt(getString(R.string.key_random), 100) + "");
        }
    }

    @Override
    public void onBackPressed() {

        //get our shared preferences
        SharedPreferences preferences = getSharedPreferences(getString(R.string.key_shared_preferences), MODE_PRIVATE);

        //ontain our editor object so we can make changes to the shared preferences
        SharedPreferences.Editor edit = preferences.edit();

        //access values so we can save to shared preferences
        ToggleButton button1 = (ToggleButton)findViewById(R.id.buttonVibrate);
        ToggleButton button2 = (ToggleButton)findViewById(R.id.buttonInput);
        EditText editText = (EditText)findViewById(R.id.textRandom);

        //store the boolean value
        edit.putBoolean(getString(R.string.key_vibrate), button1.isChecked());
        edit.putBoolean(getString(R.string.key_input), button2.isChecked());

        //default 100
        int number = 100;

        //parse string to int and save
        if (editText.getText() != null && editText.getText().toString().trim().length() > 0)
            number = Integer.parseInt(editText.getText().toString());

        //store our value
        edit.putInt(getString(R.string.key_random), number);

        //apply our changes
        edit.apply();

        //call parent
        super.onBackPressed();
    }
}