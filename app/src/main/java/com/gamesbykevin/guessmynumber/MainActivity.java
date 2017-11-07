package com.gamesbykevin.guessmynumber;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.speech.RecognizerIntent;
import android.text.InputType;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;

public class MainActivity extends Activity {

    //unique code for activity so we know when onActivityResult fires, we know where it came from
    private final int REQ_CODE_SPEECH_INPUT = 100;

    //how many tries will it take to guess the number
    private int attempts;

    //our random number we are going to generate
    private int random;

    //do we vibrate the phone when the number is entered correctly
    private boolean vibrate = false;

    //do we play sound effects
    private boolean sound = false;

    //do we speak our guess?
    private boolean speak = false;

    //number range
    private int total = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //required call to parent
        super.onCreate(savedInstanceState);

        //set our xml ui view for this activity
        setContentView(R.layout.activity_main);

        //reset our attempts to 0
        attempts = 0;

        //number range
        total = 100;

        //get our shared preferences
        SharedPreferences preferences = getSharedPreferences(getString(R.string.key_shared_preferences), MODE_PRIVATE);

        //load the value
        if (preferences != null) {

            //get our saved value
            total = preferences.getInt(getString(R.string.key_random), 100);

            //update the number range displayed
            TextView textView = findViewById(R.id.myInstructions);
            textView.setText(getString(R.string.instructions) + " 1 - " + total);

            vibrate = preferences.getBoolean(getString(R.string.key_vibrate), true);
            speak = preferences.getBoolean(getString(R.string.key_input), true);
            sound = preferences.getBoolean(getString(R.string.key_sound), true);

            //if we are speaking, hide the ui
            if (speak) {

                EditText editText = findViewById(R.id.editText);
                editText.setVisibility(View.INVISIBLE);
                Button button = findViewById(R.id.buttonGuess);
                button.setVisibility(View.INVISIBLE);
            }
        }

        //generate our random number between 1 - 100
        random = new Random().nextInt(total) + 1;

        //display our random number generated
        System.out.println("Random number generated: " + random);

        //start the speech input
        if (speak)
            promptSpeechInput();
    }

    /**
     * Receiving speech input
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        //call parent
        super.onActivityResult(requestCode, resultCode, data);

        //we'll use this as the number we tried to guess, default to -1
        int guess = -1;

        switch (requestCode) {

            //make sure that we came from the google speech recognition activity
            case REQ_CODE_SPEECH_INPUT: {

                if (resultCode == RESULT_OK && data != null) {

                    //google returns all variations of what you are saying (example "50", "fifty", "fif tee", etc....)
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

                    //print results so we can see what google is thinking :)
                    for (String tmp : result) {
                        System.out.println(tmp);
                    }

                    for (String tmp : result) {

                        //if we speak exit, go back to the previous page
                        if (tmp.trim().equalsIgnoreCase("exit"))
                            super.onBackPressed();
                    }

                    //loop through each result to find our number guess
                    for (String tmp : result) {

                        //our number can be inside a sentence so we will separate each word accordingly
                        String words[] = tmp.split(" ");

                        //loop through each word
                        for (String word : words) {

                            //if the string is a number
                            if (isNumber(word)) {

                                //parse the number
                                guess = Integer.parseInt(word);

                                //we found our number and can exit the loop
                                break;
                            }
                        }

                        //if we found our number, we can exit the loop
                        if (guess != -1)
                            break;
                    }
                }
                break;
            }
        }

        //if we made a guess
        if (guess != -1) {

            //make our guess and get the result
            boolean success = performGuess(guess);

            //if we didn't guess the correct number, continue with speech input
            if (!success) {
                promptSpeechInput();
            } else {
                win();
            }

        } else {

            //no guess was detected, prompt again for input
            promptSpeechInput();
        }
    }

    private void win() {

        //vibrate phone
        vibrate();

        //play sound effect
        playSound();

        //show fireworks
        WebView wv = (WebView)findViewById(R.id.webView);
        wv.setVisibility(View.VISIBLE);
        wv.getSettings().setUseWideViewPort(true);
        wv.loadUrl("http://bestanimations.com/Holidays/Fireworks/fireworks/ba-awesome-colorful-fireworks-animated-gif-image-s.gif");
    }

    private void playSound() {

        //only play sound if sound enabled
        if (sound) {

            //set up MediaPlayer with our sound effect
            MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.win);

            //start playing the sound
            mp.start();
        }
    }

    private void vibrate() {

        if (vibrate) {

            //obtain our vibrator object
            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

            //vibrate phone for 500 milliseconds
            v.vibrate(500);
        }
    }

    public void performGuess(View view) {

        //obtain our ui to get the guess
        EditText editText = findViewById(R.id.editText);

        //obtain our guess
        int guess = Integer.parseInt(editText.getText().toString());

        //perform the guess
        boolean result = performGuess(guess);

        //remove entry
        editText.setText("");

        //if successful, disable guess button
        if (result) {

            //hide keyboard
            if (getCurrentFocus() != null) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            }

            win();
            Button button = findViewById(R.id.buttonGuess);
            button.setEnabled(false);
        }
    }

    /**
     * Is the specified String a number?
     * @param number The string potentially representing a number
     * @return true = yes, false = otherwise
     */
    private boolean isNumber(String number) {

        try {

            //attempt to parse the number
            Integer.parseInt(number);

            //if we made it to this line of code, we didn't get an exception meaning this is a real number
            return true;

        } catch (Exception e) {

            //if an exception occurred, then the String is not a number
            return false;
        }
    }

    /**
     * Perform our guess
     * @param guess The number we are guessing
     * @return true if the number is the correct one, false otherwise
     */
    private boolean performGuess(int guess) {

        //every guess we increase the number of attempts
        attempts++;

        //we want our text view objects so we can update the text displayed
        TextView textViewStatus = findViewById(R.id.textViewStatus);
        TextView textViewGuess = findViewById(R.id.textViewGuess);

        //display the guess we made to ensure the app is working correctly
        textViewGuess.setText(String.valueOf(guess));

        if (guess < random) {

            //if our guess is less than the random number, display "higher"
            textViewStatus.setText(getString(R.string.guess_higher));

            //we didn't guess the number correct, return false
            return false;

        } else if (guess > random) {

            //if our guess is more than the random number, display "lower"
            textViewStatus.setText(getString(R.string.guess_lower));

            //we didn't guess the number correct, return false
            return false;

        } else {

            //if our guess is not more or less, it is correct!!!!
            textViewStatus.setText(getString(R.string.guess_correct) + attempts);

            //we got the right number, return true
            return true;
        }
    }

    /**
     * Display the google speech recognition dialog
     */
    private void promptSpeechInput() {

        //create intent for google speech recognition
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        //supply parameters that google's speech input allow us to customize
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.instructions));

        try {
            //start the activity for speech recognition
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {

            //if the activity was not found on the phone, display error message on  the phone
            Toast.makeText(getApplicationContext(), getString(R.string.speech_not_supported), Toast.LENGTH_SHORT).show();
        }
    }
}