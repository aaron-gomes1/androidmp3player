package com.example.mp3musicplayer;

import android.content.SharedPreferences;
import android.content.Context;

import java.util.ArrayList;
import java.util.Map;

public class Settings
{
    private SharedPreferences hiddenSharedPreferences;
    private SharedPreferences mergedSharedPreferences;
    private SharedPreferences.Editor hiddenEditor;
    private SharedPreferences.Editor mergedEditor;
    private final Context context;
    private Map<String, ?> hiddenValues;
    private Map<String, ?> mergedValues;

    public Settings(Context context)
    {
        this.context = context;
        getSettings();
    }

    /**
     * Loads the settings
     */
    private void getSettings()
    {
        hiddenSharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
        hiddenEditor = hiddenSharedPreferences.edit();

        mergedSharedPreferences = context.getSharedPreferences("merged", Context.MODE_PRIVATE);
        mergedEditor = mergedSharedPreferences.edit();

        hiddenValues = hiddenSharedPreferences.getAll();
        mergedValues = mergedSharedPreferences.getAll();
    }

    public boolean isPlayListInMerged(String merged, String playlist)
    {
        String[] playLists = getPlayListsFromMerged(merged);
        for (String play : playLists) {
            if (play.equals(playlist)) {
                return true;
            }
        }
        return false;
    }

    public void setMerged(boolean value)
    {
        mergedEditor.putBoolean("merged", value);
        mergedEditor.apply();
    }

    public boolean getMerged()
    {
        Object m = mergedValues.get("merged");
        if (m == null) {
            return true;
        }
        else {
            return (boolean) m;
        }
    }

    public String[] getMergedPlayLists()
    {
        mergedValues = mergedSharedPreferences.getAll();
        ArrayList<String> playlists = new ArrayList<>();

        for (String key : mergedValues.keySet()) {
            if (! key.equals("merged")) {
                playlists.add(key);
            }
        }

        String[] p = new String[playlists.size()];
        int index = 0;
        for(String s : playlists) {
            p[index] = s;
            index++;
        }

        return p;
    }

    public void deletedMergedPlayLists()
    {
        mergedEditor.clear();
        mergedEditor.apply();
    }

    private String convertToString(String[] playlists)
    {
        if (playlists.length != 0) {
            String text = playlists[0];
            for (int index = 1; index < playlists.length; index++) {
                if (playlists[index] != null && playlists[index] != "null") {
                    text = text + "," + playlists[index];
                }
            }
            return text;
        }
        else {
            return "";
        }
    }

    public void addMergedPlayList(String playlist, String[] playlists)
    {
        mergedEditor.putString(playlist, convertToString(playlists));
        mergedEditor.apply();
        updateMergedValues();
    }

    private void updateMergedValues()
    {
        mergedValues = mergedSharedPreferences.getAll();
    }

    public void removeMergedPlayList(String playlist)
    {
        mergedEditor.remove(playlist);
        mergedEditor.apply();
        updateMergedValues();
    }

    public void removePlayListFromMerged(String merged, String playlist) {

        String[] playlists = getPlayListsFromMerged(merged);
        boolean check = false;
        for (int i = 0; i < playlists.length; i++) {
            if (playlists[i].equals(playlist)) {
                check = true;
                playlists[i] = "";
            }
        }
        if (! check) {
            String[] play = new String[playlists.length - 1];
            int count = 0;
            for (int i = 0; i < playlists.length; i++) {
                if (!playlists[i].equals("")) {
                    if (count < playlists.length - 1) {
                        play[count] = playlists[i];
                    }
                    count+=1;
                }
            }
            addMergedPlayList(merged, play);
            updateMergedValues();
        }
    }

    public void addPlayListToMerged(String merged, String playlist) {

        String[] playlists = getPlayListsFromMerged(merged);
        boolean check = false;
        for (int i = 0; i < playlists.length; i++) {
            if (playlists[i].equals(playlist)) {
                check = true;
            }
        }
        if (! check) {
            String[] play = new String[playlists.length + 1];
            for (int i = 0; i < playlists.length; i++) {
                play[i] = playlists[i];
            }
            play[playlists.length] = playlist;
            addMergedPlayList(merged, play);
            updateMergedValues();
        }
    }

    public String[] getPlayListsFromMerged(String playlist)
    {
        String[] plays = new String[]{};
        Object playlists = mergedValues.get(playlist);
        if (playlists != null) {
            plays =  ((String)playlists).split(",");
        }
        return plays;
    }

    /**
     * Sets whether the hidden playlists are visible or not
     * @param value The visibilty of hidden playlists
     */
    public void setHidden(boolean value)
    {
        hiddenEditor.putBoolean("hidden", value);
        hiddenEditor.apply();
    }

    /**
     * Gets the visiblity of hidden playlists
     * @return The visiblity of hidden playlists
     */
    public boolean getHidden()
    {
        hiddenValues = hiddenSharedPreferences.getAll();
        Object hidden = hiddenValues.get("hidden");
        if (hidden == null) {
            return false;
        }
        else {
            return (boolean) hidden;
        }
    }

    public boolean isMergedPlayList(String playlist)
    {
        String[] mergedPlaylists = getMergedPlayLists();
        if (mergedPlaylists.length != 0) {
            for (String play : mergedPlaylists) {
                if (play.equals(playlist)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void setPlayListsType(PlayList playlist)
    {
        for (String s : getHiddenPlayLists()) {
            if(s.equals(playlist.getName())) {
                playlist.changeType(PlayListType.HIDDEN);
            }
        }
    }

    /**
     * Gets whether a playlist is hidden or not
     * @param playlist  The name of the playlist
     * @return Whether the playlist is hidden or not
     */
    public boolean isHiddenPlaylist(String playlist)
    {
        String[] playlists = getHiddenPlayLists();
        if (playlists.length != 0) {
            for (String play : playlists) {
                if (play.equals(playlist)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Removes a playlist from being hidden
     * @param playlist  The playlist to be removed
     */
    public void removeHiddenPlayList(String playlist)
    {
        String[] playlists = getHiddenPlayLists();
        String value = "";
        for (int index = 0; index < playlists.length; index++) {
            if(playlists[index].equals(playlist)) {
                playlists[index] = "";
            }
        }
        if (playlists.length != 0) {
            value = playlists[0];
            for (int index = 1; index < playlists.length; index++) {
                if (!playlists[index].equals("")) {
                    value = value + "," + playlists[index];
                }
            }
        }

        hiddenEditor.putString("hidden_playlists", value);
        hiddenEditor.apply();
        hiddenValues = hiddenSharedPreferences.getAll();
    }

    /**
     * Adds a playlist to be hidden
     * @param playlist The playlist to be added
     */
    public void addHiddenPlayList(String playlist)
    {
        String[] playlists = getHiddenPlayLists();
        String value = "";
        boolean isIn = false;
        if (playlists.length != 0) {
            for (String play : playlists) {
                if (play.equals(playlist)) {
                    isIn = true;
                    break;
                }
            }
        }
        else {
            value = playlist;
        }

        if (! isIn) {
            value = hiddenValues.get("hidden_playlists") + "," + playlist;
        }
        hiddenEditor.putString("hidden_playlists", value);
        hiddenEditor.apply();
        hiddenValues = hiddenSharedPreferences.getAll();
    }

    /**
     * Gets the hidden playlists
     * @return  The hidden playlists
     */
    public String[] getHiddenPlayLists()
    {
        Object hidden = hiddenValues.get("hidden_playlists");
        if (hidden == null) {
            return new String[]{};
        }
        else {
            return ((String) hidden).split(",");
        }
    }
}
