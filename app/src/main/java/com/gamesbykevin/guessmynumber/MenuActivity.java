package com.gamesbykevin.guessmynumber;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MenuActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
    }

    public void onClickStartGame(View view) {
        //create our intent, where we want to go
        Intent intent = new Intent(MenuActivity.this, MainActivity.class);

        //start the activity
        startActivity(intent);
    }

    public void onClickExit(View view) {
        //this will complete and close the activity
        finish();
    }

    @Override
    public void onBackPressed() {
        //don't allow the user to press the back button
        return;
    }
}