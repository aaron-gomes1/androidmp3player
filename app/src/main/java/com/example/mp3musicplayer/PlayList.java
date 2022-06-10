package com.example.mp3musicplayer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

/**
 * A playlist of songs with commands
 */
public class PlayList {

    private final String name;

    protected ArrayList<Song> playlist;
    protected PlayListType type;
    protected Song currentSong;
    protected int index;

    public PlayList(String name, PlayListType type)
    {
        this.name = name;
        this.type = type;
        playlist = new ArrayList<>();
        index = 0;
    }

    /**
     * Gets the song being played
     * @return The current Song
     */
    public Song play()
    {
        return currentSong;
    }

    /**
     * Goes to the previous song
     */
    public void back()
    {
        Song song = currentSong;
        while (song == currentSong) {
            index = index - 1;
            if (index < 0) {
                index = playlist.size() - 1;
            }
            song = playlist.get(index);
        }
        currentSong = song;
    }

    /**
     * Goes to the next song
     */
    public void next()
    {
        Song song = currentSong;
        while (song == currentSong) {
            index = (index + 1) % playlist.size();
            song = playlist.get(index);
        }
        currentSong = song;
    }

    /**
     * Adds a song to the playlist
     * @param song The song being added
     */
    public void add(Song song) {
        playlist.add(song);
        if (playlist.size() == 1) {
            currentSong = song;
        }

    }

    /**
     * Randomises the order of the songs in the playlist
     * @return the new current song
     */
    public Song randomise()
    {
        ArrayList<Song> temp = new ArrayList<Song>();
        Random rand = new Random();

        while(playlist.size() != 0) {
            temp.add(playlist.remove(rand.nextInt(playlist.size())));
        }

        playlist = temp;
        reset();
        return currentSong;
    }

    /**
     * Resets the playlist back to the beginning
     */
    public void reset()
    {
        index = 0;
        if (!isEmpty()) {
            currentSong = playlist.get(index);
        }
    }

    /**
     * Gets the playlist
     *
     * @return The list of songs
     */
    public ArrayList<Song> getPlayList() {
        return playlist;
    }

    /**
     * Gets the name of the playlist
     *
     * @return The name of the playlist
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the current song
     *
     * @return The current song
     */
    public Song getCurrentSong()
    {
        return currentSong;
    }

    /**
     * Checks whether the file is in the playlist
     *
     * @param filename The name of the file
     * @return Whether the file is in the playlist or not
     */
    public boolean checkName(String filename)
    {
        Iterator it = this.playlist.iterator();
        Song song;
        do {
            if (!it.hasNext()) {
                return true;
            }

            song = (Song) it.next();
        } while(!filename.equals(song.getFilename()));
        return false;
    }

    /**
     * Gets the type of playlist
     * @return The type of playlist
     */
    public PlayListType getType()
    {
        return type;
    }

    public boolean isSongInPlayList(Song song)
    {
        for (Song s : playlist) {
            if (s == song) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks whether the playlist is empty
     *
     * @return Whether the playlist is empty
     */
    public boolean isEmpty()
    {
        return playlist.size() == 0;
    }

    /**
     * Sets the initial song of the playlist
     * @param song The initial song
     */
    public void setInitialSong(Song song)
    {
        int num = 0;
        for (Song s : playlist) {
            if (s.getArtist().equals(song.getArtist()) && s.getTitle().equals(song.getTitle())) {
                index = num;
            }
            num++;
        }
        currentSong = song;
    }

    /**
     * Change the type of the playlist
     * @param type The playlist type
     */
    public void changeType(PlayListType type)
    {
        this.type = type;
    }
}
