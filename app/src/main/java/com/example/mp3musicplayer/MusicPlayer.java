package com.example.mp3musicplayer;

import android.media.MediaMetadataRetriever;
import android.content.SharedPreferences;
import android.content.Context;
import android.os.Environment;
import android.net.Uri;

import java.util.ArrayList;
import java.io.File;
import java.util.Map;
import java.util.Objects;

/**
 * Main class of the system
 */
public class MusicPlayer
{
    private final ArrayList<PlayList> playlists;
    private final PlayerUpdater playerUpdater;
    private final Settings settings;
    private final Context context;
    private final Player player;

    private SettingsUpdater settingsUpdater;
    private PlayList currentPlayList;
    protected boolean startAgain;

    public MusicPlayer(Context context, PlayerUpdater playerUpdater, ScreenShowing screenShowing)
    {
        this.context = context;
        this.playerUpdater = playerUpdater;
        this.settingsUpdater = settingsUpdater;

        playerUpdater.setMusicPlayer(this);

        player = new Player(context, this, screenShowing);
        startAgain = false;

        settings = new Settings(context);

        PlayList playlist = new PlayList("All songs", PlayListType.ALL_SONGS);
        currentPlayList = playlist;

        playlists = new ArrayList<PlayList>();
        playlists.add(playlist);

        addPlayListSongs(playlist);
        addMergedPlaylists();
        currentPlayList.randomise();

        PlayList play = getSavedState();

        playerUpdater.setMusicPlayer(this);
        playerUpdater.setSpinner(play);
    }

    public void setSettingsUpdater(SettingsUpdater settingsUpdater)
    {
        this.settingsUpdater = settingsUpdater;
    }

    /**
     * Plays or pauses the player depending on the current state
     */
    public void playpause()
    {
        if (! startAgain) {
            play(true);
        }
        else {
            player.playpause();
        }

    }

    public void animateLabels()
    {
        playerUpdater.updateLabels();
    }

    /**
     * Plays a song
     * @param autoplay whether the player should play the song depending on the last song or not
     */
    public void play(boolean autoplay)
    {
        if (currentPlayList == null) {
            currentPlayList = playlists.get(0);
        }
        Song song = currentPlayList.play();
        if (startAgain) {
            //  When the song has already been loaded in the player
            player.play();
        }
        else {
            //  When a new song needs to be played
            //updateLabels();
            playerUpdater.updateLabels();
            //colourCurrentSongInPlaylist();
            playerUpdater.colourCurrentSongInPlaylist();
            player.play(song, autoplay);
        }
        startAgain = true;
    }

    /**
     * Pauses the player
     */
    public void pause()
    {
        player.pause();
    }


    /**
     * Sets the player to the last song
     */
    public void back()
    {
        startAgain = false;
        currentPlayList.back();
        play(false);
    }

    /**
     * Sets the song to the next song
     * @param autoplay Whether the player plays the song depending on the previous song or not
     */
    public void skip(boolean autoplay)
    {
        startAgain = false;
        currentPlayList.next();
        play(autoplay);
    }

    /**
     * Shuffles the playlist
     */
    public void shuffle()
    {
        currentPlayList.randomise();
        startAgain = false;
        //displayPLaylistSongs();
        playerUpdater.displayPLaylistSongs();
        play(false);
    }

    public PlayerUpdater getPlayerUpdater()
    {
        return playerUpdater;
    }

    public void setupProgressUpdater()
    {
        player.setupProgressUpdater();
    }

    public ArrayList<PlayList> getPlaylists()
    {
        return playlists;
    }

    public boolean getHidden()
    {
        return settings.getHidden();
    }

    /**
     * Gets the current playlist
     * @return The current playlist
     */
    public PlayList getCurrentPlayList()
    {
        return currentPlayList;
    }

    /**
     * Gets the current song
     * @return The current song
     */
    public Song getCurrentSong()
    {
        return currentPlayList.getCurrentSong();
    }

    /**
     * Gets a playlist by name
     * @param name The name of the playlist
     * @return The playlist whose name matches
     */
    public PlayList getPlaylist(String name)
    {
        for (PlayList p : playlists) {
            if (p.getName().equals(name)) {
                return p;
            }
        }
        return null;
    }

    /**
     * Checks if there is data from a previously played song.
     * If there is then it will try and load the data
     * @return The playlist of which that song is apart of
     */
    public PlayList getSavedState()
    {
        MainActivity mainActivity = (MainActivity) context;
        SharedPreferences sharedPreferences = mainActivity.getSharedPreferences("dataInfo", Context.MODE_PRIVATE);
        Map<String, ?> values = sharedPreferences.getAll();
        PlayList playlist = getPlaylist((String) values.get("CurrentPlaylist"));
        Song song = getSongByName((String) values.get("CurrentSong"), (String) values.get("CurrentArtist"));
        long time = 0;
        if (values.get("CurrentTime") != null) {
            time = (Long) values.get("CurrentTime");
        }
        int volume = 100;
        if (values.get("Volume") != null) {
            volume = (int) values.get("Volume");
        }

        if (playlist != null && song != null) {
            startAgain = true;
            currentPlayList = playlist;
            currentPlayList.setInitialSong(song);
            //updateLabels();
            playerUpdater.updateLabels();
            player.getSavedState(time, volume);
            return playlist;
        }
        mainActivity.clear();
        return null;
    }

    /**
     * Saves information like the current playlist and the current song and how far along it is
     */
    public void triggerSave()
    {
        player.savestate(currentPlayList, currentPlayList.getCurrentSong());
    }

    /**
     * Plays a new playlist
     * @param playlistName The name of the playlist to be played
     */
    public void playPlayList(String playlistName)
    {
        for (PlayList p : playlists) {
            if (p.getName().equals(playlistName)) {
                changePlaylist(p);
            }
        }
    }

    /**
     * Plays a new playlist
     * @param newPlaylist The playlist to be played
     */
    public void changePlaylist(PlayList newPlaylist)
    {
        if (newPlaylist != null) {
            currentPlayList = newPlaylist;
            currentPlayList.reset();
            startAgain = false;
            play(false);
        }
    }

    public void deleteMergedPlayLists()
    {
        PlayList play = null;
        boolean isCurrent = false;
        for (PlayList playList : playlists) {
            if (playList instanceof MergedPlayList) {
                if (playList == currentPlayList) {
                    isCurrent = true;
                    player.pause();
                }
                playlists.remove(playList);
            }
            else if (playList.getType() == PlayListType.ALL_SONGS) {
                play = playList;
            }
        }
        if (isCurrent && play != null) {
            currentPlayList = play;
            startAgain = false;
        }
    }

    /**
     * Gets a song based on a song name and the artist
     * @param songname  The song title
     * @param artistname    The artist name
     * @return  The song that matches the song title and artist name
     */
    private Song getSongByName(String songname, String artistname)
    {
        Song song = null;
        for (Song s : currentPlayList.getPlayList()) {
            if (s.getTitle().equals(songname) && s.getArtist().equals(artistname)) {
                song = s;
            }
        }
        return song;
    }

    public void createNewMergedPlayList(String name)
    {
        playlists.add(new MergedPlayList(name, PlayListType.MERGED));
        settings.addMergedPlayList(name, new String[]{});
    }

    public void setAllMergedPlayListsHiddenOrVisible(boolean isHidden)
    {
        for (PlayList playList : playlists) {
            if (playList instanceof MergedPlayList) {
                if (isHidden) {
                    playList.changeType(PlayListType.MERGED);
                }
                else {
                    playList.changeType(PlayListType.HIDDEN);
                }
            }
        }
    }

    private void addMergedPlaylists()
    {
        String[] merged = settings.getMergedPlayLists();
        for (String m : merged) {
            MergedPlayList mergedPlayList = new MergedPlayList(m, PlayListType.MERGED);
            String[] play = settings.getPlayListsFromMerged(m);
            for (String p : play) {
                mergedPlayList.add(getPlaylist(p));
            }
            settings.setPlayListsType(mergedPlayList);
            playlists.add(mergedPlayList);
        }
    }

    /**
     * Initialises all of the songs in all the playlists
     * @param playlist The All Songs Playlist
     */
    public void addPlayListSongs(PlayList playlist)
    {
        File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
        File[] files = file.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.listFiles() != null) {
                    PlayList play = new PlayList(f.getName(), PlayListType.PLAYLIST);
                    for (File p : f.listFiles()) {
                        if (p.getName().contains(".mp3")) {

                            Uri uri = (Uri) Uri.fromFile(p);

                            MediaMetadataRetriever mediaMetadataRetriever = (MediaMetadataRetriever) new MediaMetadataRetriever();
                            mediaMetadataRetriever.setDataSource(context, uri);

                            String title = (String) mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
                            String artist = (String) mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);

                            Song s = getSongByName(title, artist);
                            if (s == null) {
                                s = new Song(p, title, artist);
                            }

                            play.add(s);
                            playlist.add(s);
                        }
                    }
                    settings.setPlayListsType(play);

                    // Checks if the playlist has any songs
                    if (play.getPlayList().size() != 0) {
                        shuffle();
                        playlists.add(play);
                    }
                }
            }
            currentPlayList = playlist;
        }
    }

    public Player getPlayer()
    {
        return player;
    }

    public void setClickSong(String[] names)
    {
        Song song = getSongByName(names[0], names[1]);
        currentPlayList.setInitialSong(song);
        startAgain = false;
        play(false);
    }
}