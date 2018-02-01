package com.lolimanzgame.flappybird;

/**
 * Created by PC on 2018/1/16.
 */

class GameConfigurationsClass {

    //bird colors
    static final int BLUE = 0x00000000;
    static final int RED = 0x00000001;
    static final int YELLOW = 0x00000002;

    //bird status
    static final int FLYING = 0x00000001;
    static final int STOP = 0x00000002;
    static final int DEAD = 0x00000003;

    //sound resource
    static final int DIE = 0x00000000;
    static final int HIT = 0x00000001;
    static final int POINT = 0x00000002;
    static final int SWOOSH = 0x00000003;
    static final int WING = 0x00000004;

    //bird flying states
    static final int[][] flying_states = {{1,2,1,0}, {0,1,2,1}, {2,1,0,1}}; //3 birds, 4 flying states.

    static final int[][] bird_resource = {{ R.mipmap.bluebird_downflap,
                                            R.mipmap.bluebird_midflap,
                                            R.mipmap.bluebird_upflap },
                                        {   R.mipmap.redbird_downflap,
                                            R.mipmap.redbird_midflap,
                                            R.mipmap.redbird_upflap },
                                        {   R.mipmap.yellowbird_downflap,
                                            R.mipmap.yellowbird_midflap,
                                            R.mipmap.yellowbird_upflap }};

    static final int[] score_resource = {   R.mipmap.n0,
                                            R.mipmap.n1,
                                            R.mipmap.n2,
                                            R.mipmap.n3,
                                            R.mipmap.n4,
                                            R.mipmap.n5,
                                            R.mipmap.n6,
                                            R.mipmap.n7,
                                            R.mipmap.n8,
                                            R.mipmap.n9 };

    static int[] pipes_array = {1,1,1};  //the length of pipes, the range is 1~6.

    static final int[] pipes_resource = {0,
                                        R.mipmap.pipe_green_1_6,
                                        R.mipmap.pipe_green_2_5,
                                        R.mipmap.pipe_green_3_4,
                                        R.mipmap.pipe_green_4_3,
                                        R.mipmap.pipe_green_5_2,
                                        R.mipmap.pipe_green_6_1,};

    static final double[][] pipes_gap_range = { {0, 0},
                                                {0.140, 0.609},  //0: upper; 1: lower
                                                {0.233, 0.514},
                                                {0.327, 0.422},
                                                {0.422, 0.327},
                                                {0.514, 0.233},
                                                {0.609, 0.140}};

    static class Bird {
        int mColor;
        int mStatus;

        Bird(int color, int status) {
            mColor = color;
            mStatus = status;
        }

        int BirdFlying(int flying_state) {

            return bird_resource[mColor][flying_states[mColor][flying_state]];
        }

        void setBirdStatus(int status) {
            mStatus = status;
        }

        int getBirdStatus() {
            return mStatus;
        }

        int getBirdFlyingState(int flying_state) {

            return flying_states[mColor][flying_state];
        }

        int whichBird(){
            return mColor;
        }

    }


}
