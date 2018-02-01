package com.lolimanzgame.flappybird;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import com.lolimanzgame.flappybird.GameConfigurationsClass.Bird;
import static com.lolimanzgame.flappybird.GameConfigurationsClass.pipes_array;
import static com.lolimanzgame.flappybird.GameConfigurationsClass.pipes_gap_range;
import static com.lolimanzgame.flappybird.GameConfigurationsClass.pipes_resource;
import static com.lolimanzgame.flappybird.GameConfigurationsClass.score_resource;

import java.util.HashMap;
import java.util.Random;


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class GameMainActivity extends Activity
{
    private Bird mBird;
    private static int[] mDifficulty = {75, 50, 25}; // 75 ~ 25, less difficulty, more difficult!
    private final static int mResolution = 1;

    private MediaPlayer mPlayer;
    private SoundPool mSound;
    private HashMap<Integer, Integer> mSoundPoolMap;

    protected static BirdHandler mHandler;

    private boolean mMoveUp = false;

    private ImageView mBirdView;
    private ImageView mGroundView;
    private ImageView mGroundNextView;
    private ImageView mCityView;
    private ImageView mCityNextView;

    private final int mPipeCounts = 3;
    private ImageView[] mPipes = new ImageView[mPipeCounts];

    private int mCurrentPipe = 0;

    WindowManager wm;
    int windowWidth;
    int windowHeight;

    private ImageView[] mScore = new ImageView[4];
    private Button mReplayButton;
    private Button mBackButton;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            startActivity(new Intent(GameMainActivity.this, StartGameActivity.class));
            overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_game_main);

        View mGameMainFrame = findViewById(R.id.id_game_main_frame);

        wm = this.getWindowManager();
        windowWidth = wm.getDefaultDisplay().getWidth();
        windowHeight = wm.getDefaultDisplay().getHeight();

        Intent intent = getIntent();
        int birdColor = Integer.valueOf(intent.getExtras().getString("which_bird"));
        mBird = new Bird(birdColor, GameConfigurationsClass.FLYING);
        mBirdView = findViewById(R.id.id_bird);
        mBirdView.setImageResource(mBird.BirdFlying(0));
        mBirdView.bringToFront();

        //Initialize the pipe array
        Random random = new Random();
        for (int m=0;m<mPipeCounts;m++)
        {
            pipes_array[m] = random.nextInt(7);
            if ( pipes_array[m] < 1 )
            {
                pipes_array[m] = 1;
            }
        }

        //Initialize the main game animation
        mGroundView = findViewById(R.id.id_ground);
        mGroundNextView = findViewById(R.id.id_ground_next);
        mCityView = findViewById(R.id.id_city);
        mCityNextView = findViewById(R.id.id_city_next);
        mPipes[0] = findViewById(R.id.id_pipe_1);
        mPipes[1] = findViewById(R.id.id_pipe_2);
        mPipes[2] = findViewById(R.id.id_pipe_3);
        mReplayButton = findViewById(R.id.id_button_replay);
        mBackButton = findViewById(R.id.id_button_back);

        // initialize the pipes positions
        mPipes[0].setX(windowWidth*6/5 + mPipes[0].getWidth());
        mPipes[1].setX(mPipes[0].getX()+mPipes[0].getWidth() + windowWidth*3/5);
        mPipes[2].setX(mPipes[1].getX()+mPipes[1].getWidth() + windowWidth*3/5);

        //initialize the score board
        findViewById(R.id.id_score).bringToFront();
        mScore[0] = findViewById(R.id.id_score_thousand_place);
        mScore[1] = findViewById(R.id.id_score_hundred_place);
        mScore[2] = findViewById(R.id.id_score_decade_place);
        mScore[3] = findViewById(R.id.id_score_unit_place);
        mScore[3].setImageResource(R.mipmap.n0);

        //initialize sound player
        InitSounds();

        //set the touch listener
        mGameMainFrame.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                switch (event.getAction())
                {
                    case MotionEvent.ACTION_DOWN:
                        mMoveUp = true;
                        break;
                    case MotionEvent.ACTION_UP:
                        mMoveUp = false;
                        break;
                }
                return true;
            }
        });

        mReplayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                Intent replayIntent = new Intent(GameMainActivity.this, GameMainActivity.class);
                replayIntent.putExtra("which_bird", String.valueOf(mBird.whichBird()));
                startActivity(replayIntent);
                overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
            }
        });

        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                startActivity(new Intent(GameMainActivity.this, StartGameActivity.class));
                overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
            }
        });

        //register the  main thread handler
        BirdRuns BirdFlying = new BirdRuns(new BirdHandler(mBirdView, mBird));      //main thread handler.

        // start sub-thread message machine.
        StartThreed(BirdFlying);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPlayer.stop();
        mBird.setBirdStatus(GameConfigurationsClass.STOP);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPlayer.stop();
        mBird.setBirdStatus(GameConfigurationsClass.STOP);
    }

    //
    // main handler class
    //
    class BirdHandler extends Handler
    {
        ImageView view;
        Bird bird;
        int wingState = 0;
        int score = 0;
        boolean scoreUpdate = true;
        boolean hitPipe = false;

        private BirdHandler(ImageView birdView, Bird bird)
        {
            super();
            this.view = birdView;
            this.bird = bird;
        }

        @Override
        public void handleMessage(Message msg)
        {
            switch(msg.what)
            {
                case 0x0002:
                    //bird is flying up
                    float bird_y_u = view.getY();
                    view.setY(bird_y_u - mResolution); //flying up step is 3
                    break;

                case 0x0003:
                    //bird is falling down
                    float bird_y_d = view.getY();
                    view.setY(bird_y_d + mResolution); //falling down step is 3
                    if (bird_y_d >= (windowHeight-mGroundView.getHeight()-mBirdView.getHeight()) &&
                        bird.getBirdStatus()!= GameConfigurationsClass.DEAD)
                    {
                        //fall down to die!!!
                        PlaySound(GameConfigurationsClass.HIT,0);
                        bird.setBirdStatus(GameConfigurationsClass.DEAD);
                        mPlayer.stop();

                        try {Thread.sleep(500);} catch (InterruptedException e) {e.printStackTrace();}
                        mReplayButton.setVisibility(View.VISIBLE);
                        mBackButton.setVisibility(View.VISIBLE);
                        findViewById(R.id.id_button_group).bringToFront();

                    }
                    break;

                case 0x0004:
                    // bird is flying
                    view.setImageResource(bird.BirdFlying(wingState));
                    if ( ++wingState == 4 ){ wingState = 0; }
                    break;

                case 0x0005:
                    //draw the first three pipes
                    for(int i=0;i<mPipeCounts;i++)
                    {
                        mPipes[i].setImageResource(pipes_resource[pipes_array[i]]);
                    }

                    //draw the score
                    mScore[3].setImageResource(score_resource[score]);
                    break;

                case 0x0006:
                    //animate the ground and pipes
                    mGroundView.setX(mGroundView.getX() - mResolution);
                    mGroundNextView.setX(mGroundView.getX() + windowWidth - mResolution);
                    if (mGroundView.getX() + windowWidth <= 0)
                    {
                        mGroundView.setX(0);
                        mGroundNextView.setX(windowWidth);
                    }

                    // shift the pipes
                    for (int i=0; i<mPipeCounts; i++)
                    {
                        mPipes[i].setX(mPipes[i].getX() - mResolution);
                    }

                    // who is the current pipe?
                    // bird and pipe are inter-crossing...
                    if (!hitPipe) {
                        if ((mPipes[mCurrentPipe].getX() - mBirdView.getX()) <= mBirdView.getWidth() &&
                                mPipes[mCurrentPipe].getX() - mBirdView.getX() >= (0 - mPipes[mCurrentPipe].getWidth()))
                        {
                            // judge the gap range.
                            float upperEnd = (float) ((windowHeight - mGroundView.getHeight()) * pipes_gap_range[pipes_array[mCurrentPipe]][0]);
                            float lowerEnd = (float) (windowHeight - (windowHeight - mGroundView.getHeight()) * pipes_gap_range[pipes_array[mCurrentPipe]][1]);

                            if ((mBirdView.getY() + 2 * mBirdView.getPaddingTop()) < upperEnd ||
                                    (mBirdView.getY() + mBirdView.getHeight() - 2 * mBirdView.getPaddingBottom()) > lowerEnd)
                            {
                                hitPipe = true;
                                PlaySound(GameConfigurationsClass.HIT, 0);
                                bird.setBirdStatus(GameConfigurationsClass.DEAD);

                                mPlayer.stop();
                                try {Thread.sleep(500);} catch (InterruptedException e) {e.printStackTrace();}
                                mReplayButton.setVisibility(View.VISIBLE);
                                mBackButton.setVisibility(View.VISIBLE);
                                findViewById(R.id.id_button_group).bringToFront();

/*
                                Animation deadFallAnim = new TranslateAnimation(mBirdView.getX(),
                                                                                mBirdView.getX(),
                                                                                mBirdView.getY(),
                                                                                windowHeight - mGroundView.getHeight() - mBirdView.getHeight());
                                deadFallAnim.setDuration(500);
                                deadFallAnim.setRepeatCount(1);
                                deadFallAnim.setInterpolator(new LinearInterpolator());
                                mBirdView.setAnimation(deadFallAnim);
                                deadFallAnim.start();
                                PlaySound(GameConfigurationsClass.DIE, 0);*/
                            }
                        }
                    }

                    //update the score
                    if (scoreUpdate)
                    {
                        if (mPipes[mCurrentPipe].getX() + mPipes[mCurrentPipe].getWidth() < mBirdView.getX())
                        {
                            scoreUpdate = false;
                            score++;
                            if (score < 10)
                            {
                                mScore[3].setImageResource(score_resource[score]);
                            }
                            else if (score >= 10 && score < 100)
                            {
                                mScore[2].setVisibility(View.VISIBLE);
                                mScore[2].setImageResource(score_resource[score / 10]);
                                mScore[3].setImageResource(score_resource[score % 10]);
                            }
                            else if (score >= 100 && score < 1000)
                            {
                                mScore[1].setVisibility(View.VISIBLE);
                                mScore[2].setVisibility(View.VISIBLE);
                                mScore[1].setImageResource(score_resource[score / 100]);
                                mScore[2].setImageResource(score_resource[(score % 100) / 10]);
                                mScore[3].setImageResource(score_resource[(score % 100) % 10]);
                            }
                            else if (score >= 1000 && score < 10000)
                            {
                                mScore[0].setVisibility(View.VISIBLE);
                                mScore[1].setVisibility(View.VISIBLE);
                                mScore[2].setVisibility(View.VISIBLE);
                                mScore[0].setImageResource(score_resource[score / 1000]);
                                mScore[1].setImageResource(score_resource[(score % 1000) / 100]);
                                mScore[2].setImageResource(score_resource[((score % 1000) % 100) / 10]);
                                mScore[3].setImageResource(score_resource[((score % 1000) % 100) % 10]);
                            }

                            PlaySound(GameConfigurationsClass.POINT, 0);
                        }
                    }


                    /* while the pipe shift out of the left side of screen, let service
                     * THREAD produce another one pipe on the other side of the screen.
                     *       |               screen              |
                     *       |                                   |
                     *       |                                   |
                     *       |                      ____         |
                     *       |                      |  |         |
                     *       |                      |  |         |      ____
                     *       |  ____                |  |         |      |  |
                     *       |  |  |                |  |         |      |  |
                     *       |  |  |                |  |         |      |  |
                     *       |  |  |                |  |         |      |  |
                     *          <------------------><------------------>
                     *              3/5 screenWidth     3/5 screenWidth
                     *
                     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
                    if ((mPipes[mCurrentPipe].getX() + mPipes[mCurrentPipe].getWidth()) <= 0)
                    {
                        Random random = new Random();
                        pipes_array[mCurrentPipe] = random.nextInt(7);
                        if ( pipes_array[mCurrentPipe] < 1 )
                        {
                            pipes_array[mCurrentPipe] = 1;
                        }

                        //draw the new pipe
                        mPipes[mCurrentPipe].setImageResource(pipes_resource[pipes_array[mCurrentPipe]]);
                        mPipes[mCurrentPipe].setX(mPipes[(mCurrentPipe+2)%mPipeCounts].getX()+mPipes[(mCurrentPipe+2)%mPipeCounts].getWidth()+windowWidth*3/5);

                        mCurrentPipe = (mCurrentPipe+1) % mPipeCounts;
                        scoreUpdate = true;
                    }
                    break;
                case 0x0007:
                    //animate the city
                    mCityView.setX(mCityView.getX() - mResolution);
                    mCityNextView.setX(mCityView.getX() + windowWidth - mResolution);
                    if (mCityView.getX() + windowWidth <= 0)
                    {
                        mCityView.setX(0);
                        mCityNextView.setX(windowWidth);
                    }
                    break;
                default:
                    break;
            }

            super.handleMessage(msg);
        }
    }

    // sub-thread, message machine class!
    class BirdRuns implements Runnable
    {

        private BirdRuns(BirdHandler handler)
        {
            super();
            mHandler = handler;
        }

        @Override
        public void run()
        {
            // TODO Auto-generated method stub
            int wingTime = 20;
            int citySpeed = 5;
            int getReadyTime = 500; //x seconds to prepare...
            boolean firstPipe = false;

            try
            {
                while (mBird.getBirdStatus() == GameConfigurationsClass.FLYING)
                {
                    //main clock
                    Thread.sleep(mDifficulty[mBird.whichBird()]/15);

                    //bird action
                    if (mMoveUp)
                    {
                        Message messageFU = new Message();
                        messageFU.what = 0x0002;
                        messageFU.obj = "flying_up";
                        mHandler.sendMessage(messageFU);
                    }
                    else
                    {
                        getReadyTime--;
                        if (getReadyTime == 400)
                        {
                            firstPipe = true;
                        }
                        if (getReadyTime==0)
                        {
                            getReadyTime++;
                            Message messageFD = new Message();
                            messageFD.what = 0x0003;
                            messageFD.obj = "fall_down";
                            mHandler.sendMessage(messageFD);
                        }
                    }

                    //bird's wing action
                    wingTime--;
                    if (wingTime == 0)
                    {
                        wingTime = 20;
                        Message messageFLY = new Message();
                        messageFLY.what = 0x0004;
                        messageFLY.obj = "flying";
                        mHandler.sendMessage(messageFLY);
                    }

                    //
                    if (firstPipe)
                    {
                        firstPipe = false;
                        Message messagePP = new Message();
                        messagePP.what = 0x0005;
                        messagePP.obj = "produce_pipes";
                        mHandler.sendMessage(messagePP);
                    }

                    //animate the ground and the pipes
                    {
                        Message messageGS = new Message();
                        messageGS.what = 0x0006;
                        messageGS.obj = "ground_speed";
                        mHandler.sendMessage(messageGS);
                    }

                    //animate the city
                    citySpeed--;
                    if (citySpeed==0)
                    {
                        citySpeed = 5;
                        Message messageCS = new Message();
                        messageCS.what = 0x0007;
                        messageCS.obj = "city_speed";
                        mHandler.sendMessage(messageCS);
                    }
                }
            }
            catch (Exception e)
            {
                // TODO: handle exception
            }
        }
    }

    //开始运动
    long StartThreed(BirdRuns runs)
    {
        Thread thread = new Thread(runs);
        long threadID = thread.getId();
        thread.start();
        return threadID;
    }

    // 初始化声音
    private void InitSounds()
    {
        // 设置播放音效
        mPlayer = MediaPlayer.create(GameMainActivity.this, R.raw.bg_music);
        mPlayer.setLooping(true);
        mPlayer.start();

        // 第一个参数为同时播放数据流的最大个数，第二数据流类型，第三为声音质量
        mSound = new SoundPool(6, AudioManager.STREAM_MUSIC, 100);
        mSoundPoolMap = new HashMap<>();
        mSoundPoolMap.put(GameConfigurationsClass.DIE, mSound.load(this, R.raw.die, 1));
        mSoundPoolMap.put(GameConfigurationsClass.HIT, mSound.load(this, R.raw.hit, 1));
        mSoundPoolMap.put(GameConfigurationsClass.POINT, mSound.load(this, R.raw.point, 1));
        mSoundPoolMap.put(GameConfigurationsClass.SWOOSH, mSound.load(this, R.raw.swoosh, 1));
        mSoundPoolMap.put(GameConfigurationsClass.WING, mSound.load(this, R.raw.wing, 1));
    }

    //soundPool播放
    //@param sound 播放第一个
    //@param loop 是否循环
    private void PlaySound(int sound, int loop)
    {
        AudioManager mgr = (AudioManager) this
                .getSystemService(Context.AUDIO_SERVICE);
        // 获取系统声音的当前音量
        float currentVolume = mgr.getStreamVolume(AudioManager.STREAM_MUSIC);
        // 获取系统声音的最大音量
        float maxVolume = mgr.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        // 获取当前音量的百分比
        float volume = currentVolume / maxVolume;

        // 第一个参数是声效ID,第二个是左声道音量，第三个是右声道音量，第四个是流的优先级，最低为0，
        // 第五个是是否循环播放，第六个播放速度(1.0 =正常播放,范围0.5 - 2.0)
        mSound.play(mSoundPoolMap.get(sound), volume, volume, 1, loop, 1f);
    }
}
