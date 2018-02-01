package com.lolimanzgame.flappybird;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.lolimanzgame.flappybird.GameConfigurationsClass.Bird;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class StartGameActivity extends Activity {

    static final Bird BlueBird = new Bird(GameConfigurationsClass.BLUE, GameConfigurationsClass.FLYING);
    static final Bird RedBird = new Bird(GameConfigurationsClass.RED, GameConfigurationsClass.FLYING);
    static final Bird YellowBird = new Bird(GameConfigurationsClass.YELLOW, GameConfigurationsClass.FLYING);

    BirdRuns BlueBirdFlying;
    BirdRuns RedBirdFlying;
    BirdRuns YellowBirdFlying;

    private static boolean isExit = false;

    static Handler mExit_Handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            isExit = false;
        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exit();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void exit() {
        if (!isExit) {
            isExit = true;
            Toast.makeText(getApplicationContext(), "再按一次退出程序",
                    Toast.LENGTH_SHORT).show();
            // 利用handler延迟发送更改状态信息
            mExit_Handler.sendEmptyMessageDelayed(0, 2000);
        } else {
            finish();
            System.exit(0);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_start_game);

        View mStartGameFrame = findViewById(R.id.id_start_game_frame);
        final ImageView mBlueBird = findViewById(R.id.id_blue_bird);
        final ImageView mRedBird = findViewById(R.id.id_red_bird);
        final ImageView mYellowBird = findViewById(R.id.id_yellow_bird);

        final Intent EntryIntent = new Intent(StartGameActivity.this, GameMainActivity.class);

        // Set up the user interaction to manually show or hide the system UI.
        mStartGameFrame.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        float click_X = event.getX();
                        float click_Y = event.getY();
                        int[] blueLocation = new int[2];
                        mBlueBird.getLocationOnScreen(blueLocation);
                        int[] redLocation = new int[2];
                        mRedBird.getLocationOnScreen(redLocation);
                        int[] yellowLocation = new int[2];
                        mYellowBird.getLocationOnScreen(yellowLocation);

                        if (click_X > yellowLocation[0] && click_Y > yellowLocation[1]) {
                            EntryIntent.putExtra("which_bird", String.valueOf(GameConfigurationsClass.YELLOW));
                        }
                        else if (click_X > redLocation[0] && click_Y > redLocation[1]) {
                            EntryIntent.putExtra("which_bird", String.valueOf(GameConfigurationsClass.RED));
                        }
                        else if (click_X > blueLocation[0] && click_Y > blueLocation[1]) {
                            EntryIntent.putExtra("which_bird", String.valueOf(GameConfigurationsClass.BLUE));
                        }
                        else {
                            EntryIntent.putExtra("which_bird", String.valueOf(GameConfigurationsClass.BLUE));
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        finish();
                        startActivity(EntryIntent);
                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        break;
                }
                return true;
            }
        });

        BlueBirdFlying = new BirdRuns(new BirdHandler(mBlueBird, BlueBird));
        RedBirdFlying = new BirdRuns(new BirdHandler(mRedBird, RedBird));
        YellowBirdFlying = new BirdRuns(new BirdHandler(mYellowBird, YellowBird));

    }

    @Override
    protected void onPause() {
        super.onPause();
        BlueBird.setBirdStatus(GameConfigurationsClass.STOP); //stop all three bird flying threads..
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BlueBird.setBirdStatus(GameConfigurationsClass.STOP); //stop all three bird flying threads..
    }

    @Override
    protected void onResume() {
        super.onResume();
        BlueBird.setBirdStatus(GameConfigurationsClass.FLYING); //resume all three bird flying threads..
        StartThreed(BlueBirdFlying);
        StartThreed(RedBirdFlying);
        StartThreed(YellowBirdFlying);
    }

    static class BirdHandler extends Handler {

        ImageView mView;
        Bird mBird;
        int i = 0;

        private BirdHandler(ImageView imageView, Bird bird) {
            super();
            this.mView = imageView;
            this.mBird = bird;
        }

        @Override
        public void handleMessage(Message msg) {
            mView.setImageResource(mBird.BirdFlying(i));
            if (++i == 4 ){ i =0; }
            super.handleMessage(msg);
        }
    }

    // 子线程
    class BirdRuns implements Runnable {

        BirdHandler mHandler;

        private BirdRuns(BirdHandler handler) {
            super();
            this.mHandler = handler;
        }

        @Override
        public void run() {
            // TODO Auto-generated method stub
            try {
                while (BlueBird.getBirdStatus() == GameConfigurationsClass.FLYING) {
                    Thread.sleep(200);
                    Message message = new Message();
                    message.what = 3;
                    message.obj = "";
                    mHandler.sendMessage(message);
                }
            } catch (Exception e) {
                // TODO: handle exception
            }
        }
    }

    //开始运动
    void StartThreed(BirdRuns runs) {
        Thread thread = new Thread(runs);
        thread.start();
    }
}
