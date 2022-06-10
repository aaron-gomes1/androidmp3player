package com.example.mp3musicplayer;

import android.content.Context;
import android.view.Gravity;
import android.view.View;

import android.widget.CompoundButton;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Spinner;
import android.widget.Switch;


import androidx.gridlayout.widget.GridLayout;

import java.util.ArrayList;

public class SettingsUpdater extends Updater
{
    private final Settings settings;
    private final MusicPlayer musicPlayer;
    private final SleepTimer sleepTimer;

    private String timeSelected;

    public SettingsUpdater(Context context, ScreenShowing screenShowing, Settings settings, MusicPlayer musicPlayer, SleepTimer sleepTimer)
    {
        super(context, screenShowing);
        this.settings = settings;
        this.musicPlayer = musicPlayer;
        this.sleepTimer = sleepTimer;
        timeSelected = "Off";
        musicPlayer.setSettingsUpdater(this);
        sleepTimer.setSettingsUpdater(this);
    }

    private void setUpHidden()
    {
        Switch hidden = (Switch) (((MainActivity) getContext()).findViewById(R.id.hidden));
        hidden.setChecked(settings.getHidden());
        hidden.setOnCheckedChangeListener((buttonView, isChecked) -> settings.setHidden(isChecked));
    }

    public void setup()
    {
        setUpHidden();
        setUpHiddenPlaylists();
        setUpTimer();
        updateSleepText();
        setMerged();
        setMergedPlayLists(null);
    }

    public void showMergePlayListEditor()
    {
        GridLayout gridLayout = ((MainActivity) getContext()).findViewById(R.id.mergeNamer);
        gridLayout.setVisibility(View.VISIBLE);
    }

    public void addMergedPlayList()
    {
        EditText edit = ((MainActivity) getContext()).findViewById(R.id.showText);
        String text = edit.getText().toString();

        if (!text.equals("")) {
            musicPlayer.createNewMergedPlayList(text);
            setMergedPlayLists(null);
            displayMergedPlayListSelected(text);

            GridLayout gridLayout = ((MainActivity) getContext()).findViewById(R.id.mergeNamer);
            gridLayout.setVisibility(View.INVISIBLE);
        }

    }

    public void deleteMergedPlayLists()
    {
        musicPlayer.deleteMergedPlayLists();
        settings.deletedMergedPlayLists();
        setMergedPlayLists(null);
    }

    private void setUpHiddenPlaylists()
    {
        ArrayList<PlayList> playlists = musicPlayer.getPlaylists();
        GridLayout hiddenplaylists = (GridLayout) ((MainActivity) getContext()).findViewById(R.id.hidden_playlists);
        hiddenplaylists.setColumnCount(1);
        hiddenplaylists.setRowCount(playlists.size() - 1);
        int row = 0;
        for (PlayList playList: playlists) {
            if (playList.getType() != PlayListType.ALL_SONGS) {

                /*PlayList p = null;
                for (PlayList play : playlists) {
                    if (play.getType() == PlayListType.ALL_SONGS) {
                        play = p;
                    }
                }*/

                CheckBox checkBox = new CheckBox(getContext());
                checkBox.setText(playList.getName());
                checkBox.setChecked(!(playList.getType() == PlayListType.HIDDEN));
                checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            musicPlayer.getPlaylist((String) buttonView.getText()).changeType(PlayListType.ALL_SONGS);
                            settings.removeHiddenPlayList((String) buttonView.getText());
                        }
                        else {
                            settings.addHiddenPlayList((String) buttonView.getText());
                            musicPlayer.getPlaylist((String) buttonView.getText()).changeType(PlayListType.HIDDEN);
                        }
                    }
                });
                GridLayout.LayoutParams param = new GridLayout.LayoutParams();

                param.setGravity(Gravity.FILL);
                param.columnSpec = GridLayout.spec(0);
                param.rowSpec = GridLayout.spec(row);

                checkBox.setLayoutParams(param);
                checkBox.setGravity(Gravity.FILL);
                hiddenplaylists.addView(checkBox);
                row++;
            }
        }
    }

    private void displayMergedPlayListSelected(String playList)
    {
        ArrayList<PlayList> playlists = new ArrayList<>();

        GridLayout mergedPlayLists = ((MainActivity) getContext()).findViewById(R.id.merged_playlists);

        mergedPlayLists.removeAllViews();

        for (PlayList play : musicPlayer.getPlaylists()) {
            if (!(play instanceof MergedPlayList)) {
                if (play.getType() != PlayListType.ALL_SONGS) {
                    playlists.add(play);
                }
            }
        }

        int row = 0;
        for (PlayList p : playlists) {
            CheckBox checkBox = new CheckBox(getContext());
            checkBox.setText(p.getName());
            checkBox.setChecked(settings.isPlayListInMerged(playList, p.getName()));

            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                private final MergedPlayList mergedPlayList = (MergedPlayList) musicPlayer.getPlaylist(playList);
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (! isChecked) {
                        mergedPlayList.removePlayList(musicPlayer.getPlaylist((String) buttonView.getText()));
                        settings.removePlayListFromMerged(mergedPlayList.getName(), (String) buttonView.getText());
                    }
                    else {
                        mergedPlayList.add(musicPlayer.getPlaylist((String) buttonView.getText()));
                        settings.addPlayListToMerged(mergedPlayList.getName(), (String) buttonView.getText());

                    }
                }
            });

            GridLayout.LayoutParams param = new GridLayout.LayoutParams();

            param.setGravity(Gravity.FILL);
            param.columnSpec = GridLayout.spec(0);
            param.rowSpec = GridLayout.spec(row);

            checkBox.setLayoutParams(param);
            checkBox.setGravity(Gravity.FILL);

            mergedPlayLists.addView(checkBox);
            row++;
        }
    }

    private void setMergedPlayLists(String playlist)
    {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), R.layout.support_simple_spinner_dropdown_item){};

        Spinner playlists = ((MainActivity) getContext()).findViewById(R.id.mergedSelector);

        boolean isNull = playlist != null;
        if (isNull) {
            adapter.add(playlist);
        }
        adapter.add("");
        for (String play : settings.getMergedPlayLists()) {
            if (!play.equals(playlist)) {
                adapter.add(play);
            }
        }
        playlists.setAdapter(adapter);
        playlists.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                displayMergedPlayListSelected((String) parent.getItemAtPosition(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    public void setUpTimer()
    {
        Spinner sleep = (Spinner) ((MainActivity) getContext()).findViewById(R.id.sleep);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), R.layout.support_simple_spinner_dropdown_item){};

        ArrayList<String> times = new ArrayList<>();

        times.add("Off");
        times.add("120");
        times.add("90");
        times.add("60");
        times.add("50");
        times.add("40");
        times.add("30");
        times.add("20");
        times.add("10");
        times.add("5");

        adapter.add(timeSelected);
        for (String time : times) {
            if (!timeSelected.equals(time)) {
                adapter.add(time);
            }
        }

        sleep.setAdapter(adapter);
        sleep.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            private boolean hasChanged = false;
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (hasChanged) {
                    String time = (String) parent.getItemAtPosition(position);
                    sleepTimer.selected(time, position);
                    timeSelected = (String) parent.getItemAtPosition(position);
                }
                hasChanged = true;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    private void setMerged()
    {
        Switch merged = ((MainActivity) getContext()).findViewById(R.id.merged);
        merged.setChecked(settings.getMerged());
        merged.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                settings.setMerged(isChecked);
                musicPlayer.setAllMergedPlayListsHiddenOrVisible(isChecked);
            }
        });
    }

    public void updateSleepText()
    {
        TextView sleep = ((MainActivity) getContext()).findViewById(R.id.sleepText);
        if (timeSelected.equals("Off")) {
            sleep.setText("Sleep: " + timeSelected);
        }
        else {
            sleep.setText("Sleep: " + timeSelected + " min");
        }
    }

    public void resetTimerSelector()
    {
        timeSelected = "Off";
    }
}
