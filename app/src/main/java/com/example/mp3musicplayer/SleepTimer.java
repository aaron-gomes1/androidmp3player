package com.example.mp3musicplayer;

import android.content.Context;
import android.os.Handler;

public class SleepTimer
{
    private final ScreenShowing screenShowing;
    private final MusicPlayer musicPlayer;
    private final Context context;

    private SettingsUpdater settingsUpdater;
    private Thread timer;

    public SleepTimer(Context context, MusicPlayer musicPlayer, ScreenShowing screenShowing)
    {
        this.context = context;
        this.musicPlayer = musicPlayer;
        this.screenShowing = screenShowing;
    }

    /**
     * Sets the SettingsUpdater
     * @param settingsUpdater The SettingsUpdater
     */
    public void setSettingsUpdater(SettingsUpdater settingsUpdater)
    {
        this.settingsUpdater = settingsUpdater;
    }

    /**
     * Sleeps the timer for the selected amount of timer
     * @param time The length of time to sleep as a string
     * @param position The position in the list
     */
    public void selected(String time, int position)
    {
        if (timer != null) {
            timer.interrupt();
        }

        if (!time.equals("Off")) {
            sleep(Integer.parseInt(time) * 60);
        }
    }

    /**
     * Sets the player to sleep after a certain time
     * @param seconds The number of seconds until sleep
     */
    private void sleep(int seconds)
    {
        if(timer != null) {
            timer.interrupt();
        }
        timer = new Thread(new Runnable() {
            @Override
            public void run() {

                for (int min = seconds / 60; min > 0; min--) {
                    try {
                        if (screenShowing.isSettingsScreenShowing()) {
                            Runnable run = new Runnable() {
                                @Override
                                public void run() {
                                    settingsUpdater.updateSleepText();
                                }
                            };
                            Handler mainHandler = new Handler(context.getMainLooper());
                            mainHandler.post(run);
                        }
                        Thread.sleep(60 * 1000);
                    } catch (InterruptedException e) {
                        min = 0;
                    }
                }
                musicPlayer.pause();
                if (screenShowing.isSettingsScreenShowing()) {
                    Runnable run = new Runnable() {
                        @Override
                        public void run() {
                            //sleepText.setText("Sleep: Off");
                            settingsUpdater.updateSleepText();
                            settingsUpdater.resetTimerSelector();
                            settingsUpdater.setUpTimer();
                        }
                    };
                    Handler mainHandler = new Handler(context.getMainLooper());
                    mainHandler.post(run);
                }
            }
        });
        timer.start();
    }
}