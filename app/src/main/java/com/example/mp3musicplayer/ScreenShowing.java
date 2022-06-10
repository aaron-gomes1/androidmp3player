package com.example.mp3musicplayer;

public class ScreenShowing
{
    private boolean screen;

    public ScreenShowing()
    {
        screen = true;
    }

    public void setScreenShowing(boolean screen)
    {
        this.screen = screen;
    }

    public boolean isPlayerScreenShowing()
    {
        return screen == true;
    }

    public boolean isSettingsScreenShowing()
    {
        return screen == false;
    }
}
