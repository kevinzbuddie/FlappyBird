package com.lolimanzgame.flappybird;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class WelcomeActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_welcome);

        View mContentView = findViewById(R.id.welcome_content);

        final MediaPlayer mPlayer = MediaPlayer.create(WelcomeActivity.this, R.raw.bg_logo);
        mPlayer.start();

        // Set up the user interaction to manually show or hide the system UI.
        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //todo
                if (mPlayer.isPlaying()) {
                    return;
                }
                finish();
                startActivity(new Intent(WelcomeActivity.this, StartGameActivity.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });
    }
}
