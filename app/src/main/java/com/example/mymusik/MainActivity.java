package com.example.mymusik;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    TextView tvAudioName, tvAudioPosition, tvAudioDuration;
    SeekBar seekBarAudio;
    ImageButton btnPrevious, btnNext, btnPlay, btnPause, btnStop;

    /* Media player properties */
    final MediaPlayer mediaPlayer = new MediaPlayer();
    final Handler handler = new Handler();
    Runnable runnable;

    /* initialize the music playlist */
    ArrayList<Musik> musicList = new ArrayList<>();

    /* initialize the nowPlayingMusic */
    int nowPlaying = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* Assign view properties */
        tvAudioName = findViewById(R.id.tvAudioName);
        tvAudioPosition = findViewById(R.id.tvAudioPosition);
        tvAudioDuration = findViewById(R.id.tvAudioDuration);
        seekBarAudio = findViewById(R.id.seekBarAudio);
        btnPlay = findViewById(R.id.btnPlay);
        btnPause = findViewById(R.id.btnPause);
        btnStop = findViewById(R.id.btnStop);
        btnPrevious = findViewById(R.id.btnPrevious);
        btnNext = findViewById(R.id.btnNext);


        /* load the music playlist */
        musicList.add(new Musik(getRawUri(R.raw.jb), "Justin Bieber - Favorite Girl"));



        /* Initialize the audio player */
        init();

        /* load the first music */
        try {
            loadMusic(musicList.get(nowPlaying));
        } catch (IOException e) {
            e.printStackTrace();
        }


        /* When Play Button is clicked */
        btnPlay.setOnClickListener(view -> playMusic());


        /* When Pause Button is clicked */
        btnPause.setOnClickListener(view -> pauseMusic());


        /* When Stop Button is clicked */
        btnStop.setOnClickListener(view -> {
            /* Stop the media player & handler */
            stopMusic();

            /* Preparing the media player */
            try {
                mediaPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }

            /* Back to start */
            mediaPlayer.seekTo(0);

            /* Handler post delay for 0.5s */
            handler.postDelayed(runnable, 500);
        });


        /* When Previous Button is clicked */
        btnPrevious.setOnClickListener(view -> {
            /* Get the previous music in playlist */
            try {
                goToPreviousMusic();
            } catch (IndexOutOfBoundsException e) {
                showToast("This is the first music.");
            }
        });


        /* When Next Button is clicked */
        btnNext.setOnClickListener(view -> {
            /* Get the next music in playlist */
            try {
                goToNextMusic();
            } catch (IndexOutOfBoundsException e) {
                showToast("This is the last music.");
            }
        });


        /* When Seek Bar is scrolled */
        seekBarAudio.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (b) {
                    /* When drag on the seek bar, set progress to the seek bar */
                    mediaPlayer.seekTo(i);
                }

                /* Update the current position on display */
                tvAudioPosition.setText(seekBarTimeFormat(mediaPlayer.getCurrentPosition()));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }


    @Override
    protected void onDestroy() {
        /* Stop the media player */
        stopMusic();

        /* Destroy the media player. */
        mediaPlayer.release();

        /* Destroy the activity */
        super.onDestroy();
    }

    /**
     * Initialize the audio player.
     */
    private void init() {
        /* Initialize media player */
        mediaPlayer.setAudioAttributes(
                new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build());

        /* Set autoplay to next music after the music is finished. */
        mediaPlayer.setOnCompletionListener(mediaPlayer -> {
            try {
                goToNextMusic();
            } catch (IndexOutOfBoundsException e) {
                /* Hide the Pause Button & show the Play Button  */
                showPlayButton();

                /* Reset media player position */
                mediaPlayer.seekTo(0);
            }
        });
    }

    /**
     * Load a music to audio player.
     * @param music The music.
     */
    private void loadMusic(Musik music) throws IOException {
        /* Reset the media player to idle. */
        mediaPlayer.reset();

        /* Set the music to media player */
        mediaPlayer.setDataSource(getApplicationContext(), music.getUri());

        /* Preparing the media player */
        mediaPlayer.prepare();

        /* Initialize runnable */
        runnable = new Runnable() {
            @Override
            public void run() {
                /* Set progress on seek bar */
                seekBarAudio.setProgress(mediaPlayer.getCurrentPosition());

                /* Handler post delay for 0.5s */
                handler.postDelayed(this, 500);
            }
        };

        /* Set seek bar max */
        seekBarAudio.setMax(mediaPlayer.getDuration());

        /* Get duration of media player, convert it to Seek Bar time format, then displaying it. */
        tvAudioDuration.setText(seekBarTimeFormat(mediaPlayer.getDuration()));

        /* Update the music name */
        tvAudioName.setText(music.getTitle());
    }


    /**
     * Play the previous music
     */
    private void goToPreviousMusic() {
        if (nowPlaying == 0) {
            throw new IndexOutOfBoundsException();
        }

        /* Stop the media player & handler */
        stopMusic();

        /* Preparing the new music */
        nowPlaying -= 1;

        try {
            loadMusic(musicList.get(nowPlaying));
        } catch (IOException e) {
            e.printStackTrace();
        }

        /* Playing the new music */
        playMusic();
    }


    /**
     * Play the next music.
     */
    private void goToNextMusic() {
        if (nowPlaying == musicList.size()-1) {
            throw new IndexOutOfBoundsException();
        }

        /* Stop the media player & handler */
        stopMusic();

        /* Preparing the new music */
        nowPlaying += 1;

        try {
            loadMusic(musicList.get(nowPlaying));
        } catch (IOException e) {
            e.printStackTrace();
        }

        /* Playing the new music */
        playMusic();
    }


    /**
     * Play the audio player.
     */
    private void playMusic() {
        /* Hide the Play Button & show the Pause Button */
        showPauseButton();

        /* Start the media player */
        mediaPlayer.start();

        /* Start handler */
        handler.postDelayed(runnable, 0);
    }


    /**
     * Pause the audio player.
     */
    private void pauseMusic() {
        /* Hide the Pause Button & show the Play Button */
        showPlayButton();

        /* Pause the media player */
        mediaPlayer.pause();

        /* Stop handler */
        handler.removeCallbacks(runnable);
    }


    /**
     * Stop the audio player.
     */
    private void stopMusic() {
        /* Hide the Pause Button & show the Play Button */
        showPlayButton();

        /* Stop the media player */
        mediaPlayer.stop();

        /* Stop handler */
        handler.removeCallbacks(runnable);
    }


    /**
     * Convert time in milliseconds to seek bar format (mm:ss).
     * @param durationInMs The time in milliseconds.
     * @return The time in string with seek bar format.
     */
    @SuppressLint("DefaultLocale")
    private String seekBarTimeFormat(int durationInMs) {
        long minutesDuration = TimeUnit.MILLISECONDS.toMinutes(durationInMs);
        long secondsDuration = TimeUnit.MILLISECONDS.toSeconds(durationInMs);

        return String.format("%02d:%02d",
                minutesDuration,
                secondsDuration - TimeUnit.MINUTES.toSeconds(minutesDuration));
    }


    /**
     * Display the Play Button and hide the Pause Button.
     */
    private void showPlayButton() {
        btnPlay.setVisibility(View.VISIBLE);
        btnPause.setVisibility(View.GONE);
    }


    /**
     * Display the Pause Button and hide the Play Button.
     */
    private void showPauseButton() {
        btnPause.setVisibility(View.VISIBLE);
        btnPlay.setVisibility(View.GONE);
    }


    /**
     * Display a message with toast.
     * @param message The message to displayed.
     */
    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT)
                .show();
    }


    private Uri getRawUri(int rawId) {
        return Uri.parse("android.resource://" + getPackageName() + "/" + rawId);
    }
}
