package com.example.mp3musicplayer;

import java.io.File;

public class Song
{
    private File file;
    private String title;
    private String artist;

    public Song(File file, String title, String artist)
    {
        this.file = file;
        this.title = title;
        this.artist = convertArtist(artist);
    }

    private static String convertArtist(String artist)
    {
        String artists = "";
        String[] art = artist.split("/");
        artists = art[0];

        for (int num = 1; num < art.length; num++) {
            artists = artists + ", " + art[num];
        }
        return artists;
    }

    public File getFile()
    {
        return file;
    }

    public String getFilename()
    {
        return file.getName();
    }

    public String getArtist()
    {
        return artist;
    }

    public String getTitle()
    {
        return title;
    }
}
