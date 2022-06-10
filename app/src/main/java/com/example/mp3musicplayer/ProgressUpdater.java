package com.example.mp3musicplayer;

import android.content.Context;
import android.app.Activity;
import android.os.Handler;

import android.widget.TextView;
import android.widget.SeekBar;

public class ProgressUpdater extends Updater
{
    private final Player player;

    private TextView currentPosition;
    private TextView duration;
    private SeekBar timeBar;

    private Runnable timer;

    private boolean canUpdate;
    private long offset;
    private long currentSongLength;

    public ProgressUpdater(Context context, ScreenShowing screenShowing, Player player) {
        super(context, screenShowing);

        this.currentSongLength = 0;
        this.player = player;

        canUpdate = true;
        offset = 0;

        timeBar = ((Activity) context) .findViewById(R.id.timeBar);
        currentPosition = ((Activity) context) .findViewById(R.id.currentPosition);
        duration = ((Activity) context) .findViewById(R.id.duration);
        setProgressBar();
    }

    /**
     * Sets up the controls for volume control
     */
    public void updateVolumeBar()
    {
        SeekBar volumeBar = ((Activity) getContext()).findViewById(R.id.volumeBar);
        volumeBar.setMin(0);
        volumeBar.setMax(player.getMaxVolume());
        volumeBar.setProgress(player.getCurrentVolume());
    }

    public void setUpVolumeControl()
    {
        SeekBar volume = ((Activity) getContext()).findViewById(R.id.volumeBar);
        updateVolumeBar();
        volume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                player.setVolumeProgress(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }



    /**
     * Sets up the progress labels and bar
     * @param time
     */
    public void setUpLabels(long time)
    {
        if (getScreenShowing().isPlayerScreenShowing()) {
            currentSongLength = time;
            offset = 0;
            currentPosition.setText(getStringTime(0));
            duration.setText(getStringTime((int) time / 1000));
            progressTimeBar(0, (int) time / 1000);
        }
    }

    /**
     * Gets the offset
     * @return  The offset
     */
    public long getOffset()
    {
        return offset;
    }

    /**
     * Starts the listener for the progress bar
     */
    public void runProgressBar()
    {
        new Thread(timer).start();
    }

    /**
     * Sets the progress bar to a particular value
     * @param progress The progress of the song in seconds
     * @param max   The length of the song in seconds
     */
    private void progressTimeBar(int progress, int max)
    {
        if (getScreenShowing().isPlayerScreenShowing()) {
            timeBar.setMin(0);
            timeBar.setMax(max);
            timeBar.setProgress(progress);
        }
    }

    /**
     * Updates the progress
     * @param progress  The time from start of the song
     * @param time  The length of the song
     */
    public void update(long progress, long time)
    {
        if (getScreenShowing().isPlayerScreenShowing()) {
            offset = progress;
            currentSongLength = time;
            updateLabels();
        }
    }

    /**
     * Updates the labels
     */
    private void updateLabels()
    {
        timeBar.setMin(0);
        timeBar.setMax((int) (currentSongLength/1000));
        timeBar.setProgress((int) (offset/1000));
        currentPosition.setText(getStringTime((int) offset / 1000));
        duration.setText(getStringTime((int) (currentSongLength/1000)));
    }

    /**
     * Sets the progress stuff when the screen is shown
     */
    public void setup()
    {
        timeBar = ((Activity) getContext()).findViewById(R.id.timeBar);
        currentPosition = ((Activity) getContext()).findViewById(R.id.currentPosition);
        duration = ((Activity) getContext()).findViewById(R.id.duration);

        updateLabels();
        setUpVolumeControl();
        setProgressBar();
        runProgressBar();
    }

    /**
     * Initialises the listeners for the progress bar
     */
    private void setProgressBar()
    {
        if (getScreenShowing().isPlayerScreenShowing()) {
            timeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    canUpdate = false;
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    offset = seekBar.getProgress() * 1000L;
                    currentPosition.setText(getStringTime((int) offset / 1000));
                    duration.setText(getStringTime((int) currentSongLength / 1000));
                    progressTimeBar((int) offset / 1000, (int) currentSongLength / 1000);
                    player.skip((int) offset / 1000);
                }
            });
        }

        timer = new Runnable() {

            @Override
            public void run() {

                canUpdate = true;
                int progress = 0;

                TextView currentPosition = ((Activity) getContext()).findViewById(R.id.currentPosition);

                Runnable run = new Runnable() {
                    @Override
                    public void run() {
                        int progress = (int) player.getCurrentPosition() / 1000;

                        if (getScreenShowing().isPlayerScreenShowing()) {
                            progressTimeBar((int) (offset + player.getCurrentPosition()) / 1000, (int) currentSongLength / 1000);

                            currentPosition.setText(getStringTime((int) (offset + player.getCurrentPosition()) / 1000));
                        }
                    }
                };
                Handler mainHandler = new Handler(getContext().getMainLooper());

                boolean playing = true;
                int time = 0;

                try {
                    playing = player.isPlaying();
                    time = (int) player.getCurrentSongLength();
                } catch (IllegalStateException e) {

                }

                while(canUpdate && playing && progress < time) {

                    mainHandler.post(run);
                    try {
                        playing = player.isPlaying();
                        time = (int) player.getCurrentSongLength();
                        Thread.sleep(1000);

                    } catch (InterruptedException ex) { }
                    catch (IllegalStateException e) { }
                }
            }
        };
    }

    /**
     *
     * @param time The time to be converted
     * @return The time as a string
     */
    private static String getStringTime(int time)
    {
        String s = "";

        int min = time / 60;
        int sec = time % 60;

        if (min < 10) {
            s+="0";
        }
        s = s + min + ":";
        if (sec < 10) {
            s+="0";
        }
        s+=sec;
        return s;
    }
}
