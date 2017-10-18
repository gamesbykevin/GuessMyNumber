package com.gamesbykevin.guessmynumber;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //required call to parent
        super.onCreate(savedInstanceState);

        //set our xml ui view for this activity
        setContentView(R.layout.activity_main);

        //reset our attempts to 0
        attempts = 0;

        //generate our random number between 1 - 100
        random = new Random().nextInt(100) + 1;

        //display our random number generated
        System.out.println("Random number generated: " + random);

        //start the speech input
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
            if (!success)
                promptSpeechInput();

        } else {

            //no guess was detected, prompt again for input
            promptSpeechInput();
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
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.speech_prompt));

        try {
            //start the activity for speech recognition
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {

            //if the activity was not found on the phone, display error message on  the phone
            Toast.makeText(getApplicationContext(), getString(R.string.speech_not_supported), Toast.LENGTH_SHORT).show();
        }
    }
}