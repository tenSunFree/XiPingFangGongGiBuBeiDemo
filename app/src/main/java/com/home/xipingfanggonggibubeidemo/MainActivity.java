package com.home.xipingfanggonggibubeidemo;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;
import com.warkiz.widget.IndicatorSeekBar;
import com.warkiz.widget.OnSeekChangeListener;
import com.warkiz.widget.SeekParams;

public class MainActivity extends YouTubeBaseActivity {

    private final String YOUTUBE_API_KEY = "輸入API金鑰";
    private final String YOUTUBE_VIDEO_CODE = "VW5bBIA8AHY";
    private int currentSeekBarProgress, currentDurationMillis, currentTimeMillis,
            conversationPart = 0, conversationPartBackground = 1, conversationPartPosition,
            conversationPart1Begin = 4000, conversationPart1End = 6500,
            conversationPart2Begin = 7100, conversationPart2End = 10800;
    private boolean onPlaying = false, isConversationPartBackground = true, isCycle = false;

    private YouTubePlayerView youTubePlayerView;
    private LinearLayout playLinearLayout, previousLinearLayout, cycleLinearLayout, nextLinearLayout;
    private ImageView playImageView, previousImageView, cycleImageView, nextImageView;
    private YouTubeAsyncTask youTubeAsyncTask;
    private IndicatorSeekBar scheduleIndicatorSeekBar;
    private TextView englishContentTextView, twContentTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializationBottomButtonAndDisplayText();
        initializationYouTubePlayer();
    }

    private void initializationBottomButtonAndDisplayText() {
        englishContentTextView =  findViewById(R.id.englishContentTextView);
        twContentTextView =  findViewById(R.id.twContentTextView);
        previousLinearLayout = findViewById(R.id.previousLinearLayout);
        previousLinearLayout.setClickable(false);
        previousImageView = findViewById(R.id.previousImageView);
        previousLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isConversationPartBackground = false;
                cycleImageView.setImageResource(R.drawable.icon_cycle_enabled);
                if (conversationPartBackground > 2) {
                    conversationPartBackground = 0;
                    conversationPart = 3;
                }
                if (conversationPart > 1) {
                    conversationPart = conversationPart - 1;
                }
                if (conversationPart == 1) {
                    previousImageView.setImageResource(R.drawable.icon_previous_not_clickable);
                    previousLinearLayout.setClickable(false);
                    nextImageView.setImageResource(R.drawable.icon_next_clickable);
                    nextLinearLayout.setClickable(true);
                }
            }
        });
        nextLinearLayout = findViewById(R.id.nextLinearLayout);
        nextImageView = findViewById(R.id.nextImageView);
        nextLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cycleImageView.setImageResource(R.drawable.icon_cycle_enabled);
                conversationPart = conversationPart + 1;
                if (conversationPart > 0) {
                    previousImageView.setImageResource(R.drawable.icon_previous_clickable);
                    playLinearLayout.setClickable(true);
                }
                if (conversationPart > 1) {
                    nextImageView.setImageResource(R.drawable.icon_next_not_clickable);
                    nextLinearLayout.setClickable(false);
                }
            }
        });
        cycleLinearLayout = findViewById(R.id.cycleLinearLayout);
        cycleImageView = findViewById(R.id.cycleImageView);
        cycleLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (conversationPart == 0) {
                    englishContentTextView.setVisibility(View.VISIBLE);
                    twContentTextView.setVisibility(View.VISIBLE);
                    if (currentTimeMillis <= conversationPart1End) {
                        conversationPart = 1;
                        cycleImageView.setImageResource(R.drawable.icon_cycle_enabled);
                    } else if (currentTimeMillis >= conversationPart2Begin && currentTimeMillis <= conversationPart2End) {
                        conversationPart = 2;
                        cycleImageView.setImageResource(R.drawable.icon_cycle_enabled);
                    } else {
                        Toast.makeText(MainActivity.this, "剩下的時間沒有製作重播XD", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    englishContentTextView.setVisibility(View.INVISIBLE);
                    twContentTextView.setVisibility(View.INVISIBLE);
                    isConversationPartBackground = true;
                    conversationPart = 0;
                    cycleImageView.setImageResource(R.drawable.icon_cycle_not_enabled);
                }
            }
        });
    }

    /**
     * 初始化YouTubePlayer 相關設定
     */
    private void initializationYouTubePlayer() {
        youTubePlayerView = findViewById(R.id.youTubePlayerView);
        youTubePlayerView.initialize(YOUTUBE_API_KEY, new YouTubePlayer.OnInitializedListener() {

            @Override
            public void onInitializationSuccess(
                    YouTubePlayer.Provider provider, final YouTubePlayer youTubePlayer, boolean b) {
                if (!b) {
                    currentDurationMillis = youTubePlayer.getDurationMillis();
                    youTubePlayer.loadVideo(YOUTUBE_VIDEO_CODE);
                    initializationYoutubPlayLinearLayout(youTubePlayer);
                    initializationScheduleIndicatorSeekBar(youTubePlayer);
                    youTubeAsyncTask = new YouTubeAsyncTask();
                    youTubeAsyncTask.execute();
                    youTubePlayer.setPlayerStyle(YouTubePlayer.PlayerStyle.CHROMELESS);
                    youTubePlayer.setPlaybackEventListener(new YouTubePlayer.PlaybackEventListener() {
                        @Override
                        public void onPlaying() {
                            playImageView.setImageResource(R.drawable.icon_pause);
                            currentDurationMillis = youTubePlayer.getDurationMillis();
                            currentTimeMillis = youTubePlayer.getCurrentTimeMillis();
                            onPlaying = true;
                        }

                        @Override
                        public void onPaused() {
                            playImageView.setImageResource(R.drawable.icon_play);
                            onPlaying = false;
                        }

                        @Override
                        public void onStopped() {
                        }

                        @Override
                        public void onBuffering(boolean b) {
                        }

                        @Override
                        public void onSeekTo(int i) {
                            currentTimeMillis = i;
                        }
                    });
                }
            }

            @Override
            public void onInitializationFailure(
                    YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
                Toast.makeText(MainActivity.this,
                        youTubeInitializationResult.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initializationScheduleIndicatorSeekBar(final YouTubePlayer youTubePlayer) {
        scheduleIndicatorSeekBar = findViewById(R.id.scheduleIndicatorSeekBar);
        scheduleIndicatorSeekBar.setOnSeekChangeListener(new OnSeekChangeListener() {
            @Override
            public void onSeeking(SeekParams seekParams) {
                currentSeekBarProgress = seekParams.progress;
                if (isCycle) {
                    youTubePlayer.seekToMillis(youTubePlayer.getDurationMillis() * currentSeekBarProgress / 10000);
                }
            }

            @Override
            public void onStartTrackingTouch(IndicatorSeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(IndicatorSeekBar seekBar) {
            }
        });
    }

    /**
     * 初始化 播放/暫停的按鈕
     */
    private void initializationYoutubPlayLinearLayout(final YouTubePlayer youTubePlayer) {
        playLinearLayout = findViewById(R.id.playLinearLayout);
        playImageView = findViewById(R.id.playImageView);
        playLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (youTubePlayer.isPlaying()) {
                    youTubePlayer.pause();
                    playImageView.setImageResource(R.drawable.icon_play);
                } else {
                    youTubePlayer.play();
                    playImageView.setImageResource(R.drawable.icon_pause);
                }
            }
        });
    }

    private class YouTubeAsyncTask extends AsyncTask<String, Integer, String> {

        /**
         * 这个方法会在后台任务开始执行之间调用, 在主线程执行, 用于进行一些界面上的初始化操作, 比如显示一个进度条对话框等
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        /**
         * 更新UI
         */
        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        /**
         * 执行耗时操作
         */
        @Override
        protected String doInBackground(String... params) {
            while (true) {
                if (isCancelled()) {
                    break;
                }
                if (onPlaying) {
                    currentSeekBarProgress = (int) (((float) currentTimeMillis / currentDurationMillis) * 10000);
                    scheduleIndicatorSeekBar.setProgress(currentSeekBarProgress);

                    if (isConversationPartBackground) {
                        if (currentTimeMillis > conversationPart2End) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    conversationPartBackground = 3;
                                    nextImageView.setImageResource(R.drawable.icon_next_not_clickable);
                                    nextLinearLayout.setClickable(false);
                                }
                            });
                        } else if (currentTimeMillis > conversationPart1End) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    conversationPartBackground = 2;
                                    previousImageView.setImageResource(R.drawable.icon_previous_clickable);
                                    previousLinearLayout.setClickable(true);
                                }
                            });
                        }
                    }

                    switch (conversationPart) {
                        case 1:
                            if (currentTimeMillis > conversationPart1End) {
                                Log.d("more", "01");
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        englishContentTextView.setVisibility(View.VISIBLE);
                                        twContentTextView.setVisibility(View.VISIBLE);
                                        englishContentTextView.setText("New york, 1922");
                                        twContentTextView.setText("紐約 1992年");
                                        onPlaying = false;
                                        isCycle = true;
                                        conversationPartPosition = (int) (((float) conversationPart1Begin / currentDurationMillis) * 10000);
                                        Log.d("more", "02, currentSeekBarProgress: " + currentSeekBarProgress);
                                        scheduleIndicatorSeekBar.setProgress(conversationPartPosition);
                                    }
                                });
                            }
                            break;
                        case 2:
                            if (currentTimeMillis > conversationPart2End) {
                                Log.d("more", "02");
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        englishContentTextView.setVisibility(View.VISIBLE);
                                        twContentTextView.setVisibility(View.VISIBLE);
                                        englishContentTextView.setText("這城市的節奏已經整個變調\n大樓比以前更高");
                                        twContentTextView.setText("The tempo of the city had changed sharply.\nthe buildings were higher.");
                                        onPlaying = false;
                                        isCycle = true;
                                        conversationPartPosition = (int) (((float) conversationPart2Begin / currentDurationMillis) * 10000);
                                        scheduleIndicatorSeekBar.setProgress(conversationPartPosition);
                                    }
                                });
                            }
                            break;
                    }

                    isCycle = false;
                    if (currentTimeMillis <= currentDurationMillis) {
                        currentTimeMillis = currentTimeMillis + 50;
                    }
                }
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            return "finished";
        }

        /**
         * doInBackground结束后执行本方法, result是doInBackground方法返回的数据
         */
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        onPlaying = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        onPlaying = false;
        youTubeAsyncTask.cancel(true);
    }
}
