package com.example.mp3musicplayer;

import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaMetadataRetriever;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaPlayer;

import android.content.Context;
import android.net.Uri;

import java.io.FileInputStream;
import java.io.FileDescriptor;
import java.io.IOException;

/**
 * A player for mp3 songs
 */
public class Player {

    private final MusicPlayer musicPlayer;
    private final Context context;

    private MediaPlayer player;

    //  Used for deciding if a song needs to play or pause based on the previous songs playing
    private boolean isPlaying;
    private boolean autoplay;

    // Used for volume
    private final int maxVolume;
    private int currentVolume;

    //  Updates the time labels and the progress bar
    private final ProgressUpdater progressUpdater;


    public Player(Context context, MusicPlayer musicPlayer, ScreenShowing screenShowing)
    {

        this.context = context;
        this.musicPlayer = musicPlayer;

        isPlaying = false;
        autoplay = false;
        maxVolume = 100;
        currentVolume = 100;

        progressUpdater = new ProgressUpdater(context, screenShowing, this);
        progressUpdater.setUpVolumeControl();

        resetPlayer();
    }

    public void setupProgressUpdater()
    {
        progressUpdater.setup();
    }

    /**
     * Plays the song that has already been cued up
     */
    public void play() {
        if (! player.isPlaying()) {
            player.start();
            progressUpdater.runProgressBar();
            musicPlayer.animateLabels();
        }
    }

    /**
     * Gets whether the player is playing or not
     * @return Whether the player is playing
     */
    public boolean isPlaying()
    {
        return player.isPlaying();
    }

    /**
     * Gets the current length of the song
     * @return The length of the song
     */
    public long getCurrentSongLength()
    {
        return player.getDuration();
    }

    /**
     * Gets the players progress through the song
     * @return The progress
     */
    public long getCurrentPosition()
    {
        return player.getCurrentPosition();
    }

    /**
     * Plays a new song
     * @param song The song to be played
     * @param autoplay Whether the song should be played based on the previous song
     */
    public void play(Song song, boolean autoplay)
    {
        this.autoplay = autoplay;
        isPlaying = player.isPlaying();

        if (player.isPlaying()) {
            player.stop();
        }

        try {
            resetPlayer();

            FileInputStream fis = new FileInputStream(song.getFile().getAbsolutePath());
            FileDescriptor fd = fis.getFD();

            player.setDataSource(fd);
            player.prepare();

            progressUpdater.setUpLabels(player.getDuration());
            musicPlayer.animateLabels();

        } catch (IllegalStateException | IOException ignored) {
        }
    }

    /**
     * Pauses the player
     */
    public void pause()
    {
        if (player.isPlaying()) {
            player.pause();
            musicPlayer.animateLabels();
        }

    }

    /**
     * Used when unsure whether to play or pause
     * Pauses if player is playing and plays if player is paused
     */
    public void playpause()
    {
        if (player.isPlaying()) {
            pause();
        }
        else {
            play();
        }
    }

    /**
     * Sets the media player up
     */
    private void resetPlayer()
    {
        if (player != null) {
            player.pause();
            player.stop();
            player.release();
        }
        player = new MediaPlayer();

        player.setOnErrorListener(new OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                return true;
            }
        });

        //  Sets a listener for when the player is prepared

        player.setOnPreparedListener(new OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer p) {
                if (! autoplay) {
                    if (isPlaying) {
                        p.start();
                        progressUpdater.runProgressBar();
                        musicPlayer.animateLabels();
                    }
                }
                else {
                    p.start();
                    progressUpdater.runProgressBar();
                    musicPlayer.animateLabels();
                }
                isPlaying = true;
            }
        });

        //  Sets a listener for when the song ends to rollover to the next song

        player.setOnCompletionListener(new OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {
                player.pause();
                player.stop();
                resetPlayer();
                musicPlayer.skip(true);
            }
        });
    }

    /**
     * Skips the song to a particular point
     * @param sec The time to be skipped from the beginning of the song in seconds
     */
    public void skip(int sec)
    {

        // Acts when the user drags the slider to go back or forward
        isPlaying = player.isPlaying();
        autoplay = false;
        try {

            FileInputStream fis = new FileInputStream(musicPlayer.getCurrentSong().getFile().getAbsolutePath());
            FileDescriptor fd = fis.getFD();

            MediaExtractor mex = new MediaExtractor();
            try {
                mex.setDataSource(fd);
            } catch (IOException ignored) {
            }

            //  Gets the bitrate of the song so that the number of bytes to skip can be determined

            MediaFormat mf = mex.getTrackFormat(0);

            int bitRate = mf.getInteger(MediaFormat.KEY_BIT_RATE);

            resetPlayer();

            try {
                player.setDataSource(fd, (long) sec * (bitRate / 8), Long.MAX_VALUE);
                player.prepare();

            } catch(IllegalStateException | IOException ignored) {
            }
        } catch (IOException ignored) {
        }
    }

    public void setVolumeProgress(int progress)
    {
        currentVolume = progress;
        float volumeLog = (float) (((currentVolume / (maxVolume/10)) * 0.1) + ((currentVolume % (maxVolume/10)) * 0.01));
        player.setVolume(volumeLog, volumeLog);
    }

    public int getMaxVolume()
    {
        return maxVolume;
    }

    public int getCurrentVolume()
    {
        return maxVolume;
    }

    /**
     * Saves the state of the player when exited out
     * @param currentPlaylist   The current playlist
     * @param currentSong   The current song
     */
    public void savestate(PlayList currentPlaylist, Song currentSong)
    {
        long length = progressUpdater.getOffset() + player.getCurrentPosition();
        ((MainActivity) context).saveState(currentPlaylist, currentSong, length, currentVolume);
    }

     /**
     * Loads up the saved state into the player
     * @param time  The time
     */
    public void getSavedState(long time, int volume)
    {
        currentVolume = volume;
        //updateVolumeBar();
        progressUpdater.updateVolumeBar();
        skip((int) time / 1000);

        Uri uri = Uri.fromFile(musicPlayer.getCurrentSong().getFile());

        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(context, uri);

        long duration = Long.parseLong(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));

        progressUpdater.update(time, duration);
    }
}
