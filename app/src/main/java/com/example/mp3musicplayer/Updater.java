package com.example.mp3musicplayer;

import android.content.Context;

public abstract class Updater
{
    private Context context;
    private ScreenShowing screenShowing;

    public Updater(Context context, ScreenShowing screenShowing)
    {
        this.context = context;
        this.screenShowing = screenShowing;
    }

    protected Context getContext()
    {
        return context;
    }

    protected  ScreenShowing getScreenShowing()
    {
        return screenShowing;
    }

    public abstract void setup();
}
