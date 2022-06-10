package com.example.mp3musicplayer;

import java.util.ArrayList;

public class MergedPlayList extends PlayList
{
    private ArrayList<PlayList> playlists;
    private PlayListType extraType;

    public MergedPlayList(String name, PlayListType type)
    {
        super(name, type);
        playlists = new ArrayList<>();
    }

    @Override
    public PlayListType getType()
    {
        return extraType;
    }

    public void removePlayList(PlayList playList)
    {
        for (PlayList play : playlists) {
            if (play == playList) {
                playlists.remove(play);
                addAllSongs();
            }
        }
    }

    private boolean checkSong(Song song)
    {
        for (Song s : playlist) {
            if (s == song) {
                return true;
            }
        }
        return false;
    }

    private void addAllSongs()
    {
        playlist = new ArrayList<>();
        for (PlayList p : playlists) {
            playlist.addAll(p.getPlayList());
        }
    }

    public void add(PlayList playList)
    {
        if (playList != null) {
            playlists.add(playList);
            for (Song song : playList.getPlayList()) {
                if (!checkSong(song)) {
                    playlist.add(song);
                }
            }
            randomise();
        }
    }

    private boolean getIsHidden(Song song)
    {
        for (PlayList playList : playlists) {
            if (playList.getType() == PlayListType.HIDDEN && playList.isSongInPlayList(song)) {
                return true;
            }
        }
        if (extraType == PlayListType.HIDDEN) {
            return true;
        }
        return false;
    }

    @Override
    public void next()
    {
        Song song = currentSong;
        while (song == currentSong) {
            index = (index + 1) % playlist.size();
            Song song1 = playlist.get(index);
            if (! getIsHidden(song1)) {
                song = song1;
            }
        }
        currentSong = song;
    }

    @Override
    public void back()
    {
        Song song = currentSong;
        while (song == currentSong) {
            index = index - 1;
            if (index < 0) {
                index = playlist.size() - 1;
            }
            Song song1 = playlist.get(index);
            if (! getIsHidden(song1)) {
                song = song1;
            }
        }
        currentSong = song;
    }

    @Override
    public void changeType(PlayListType type)
    {
        extraType = type;
    }

    @Override
    public void add(Song song)
    {
    }
}
