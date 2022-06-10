package com.example.mp3musicplayer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.gridlayout.widget.GridLayout;

import java.util.ArrayList;

public class PlayerUpdater extends Updater
{
    private MusicPlayer musicPlayer;

    public PlayerUpdater(Context context, ScreenShowing screenShowing)
    {
        super(context, screenShowing);
    }

    /**
     * Sets the current music player
     * @param musicPlayer The music player to be updated
     */
    public void setMusicPlayer(MusicPlayer musicPlayer)
    {
        this.musicPlayer = musicPlayer;
    }

    /**
     * Sets up the initial screen state
     */
    public void setup()
    {
        setScreenSizes();
        updateLabels();
        resetSpinner();
        displayPLaylistSongs();
    }

    /**
     * Updates the labels
     */
    public void updateLabels()
    {
        if (getScreenShowing().isPlayerScreenShowing() && musicPlayer.getCurrentPlayList().play() != null) {

            PlayList currentPlayList = musicPlayer.getCurrentPlayList();
            Player player = musicPlayer.getPlayer();

            Activity ac = (Activity) getContext();
            TextView songT = (TextView) ac.findViewById(R.id.songText);
            TextView artistT = (TextView) ac.findViewById(R.id.artistText);

            artistT.setClipToOutline(false);
            songT.setClipToOutline(false);

            songT.setText(currentPlayList.play().getTitle());
            artistT.setText(currentPlayList.play().getArtist());

            artistT.clearAnimation();
            songT.clearAnimation();

            artistT.setWidth(artistT.getMaxWidth());
            songT.setWidth(songT.getMaxWidth());

            songT.setText(currentPlayList.play().getTitle());
            artistT.setText(currentPlayList.play().getArtist());

            if (player.isPlaying()) {
                TranslateAnimation ani = new TranslateAnimation((int) (artistT.getWidth()), (int) (-artistT.getWidth()), 0, 0);
                ani.setRepeatCount(Animation.INFINITE);
                ani.setDuration(11000);
                artistT.startAnimation(ani);

                TranslateAnimation songAni = new TranslateAnimation((int) (songT.getWidth()), (int) (-songT.getWidth()), 0, 0);
                songAni.setRepeatCount(Animation.INFINITE);
                songAni.setDuration(11000);
                songT.startAnimation(songAni);
            }
        }
    }

    /**
     * Displays all of the playlist songs
     */
    public void displayPLaylistSongs() {
        if (getScreenShowing().isPlayerScreenShowing()) {
            ArrayList<Song> songs = musicPlayer.getCurrentPlayList().getPlayList();

            GridLayout gridLayout = ((Activity) getContext()).findViewById(R.id.playlists);
            gridLayout.removeAllViews();
            gridLayout.setRowCount(songs.size() + 1);
            gridLayout.setColumnCount(2);
            gridLayout.setPadding(0, 0, 0, 50);

            TextView playlistName = new TextView(getContext());
            playlistName.setText(musicPlayer.getCurrentPlayList().getName());
            playlistName.setTextSize(20);
            playlistName.setGravity(Gravity.CENTER);

            @SuppressLint("ResourceType")
            String currentColour = getContext().getResources().getString(R.color.currentSongPlaying);
            playlistName.setTextColor(Color.parseColor(currentColour));

            GridLayout.LayoutParams playlistparam = new GridLayout.LayoutParams();
            playlistparam.height = GridLayout.LayoutParams.WRAP_CONTENT;
            playlistparam.width = GridLayout.LayoutParams.WRAP_CONTENT;
            playlistparam.setGravity(Gravity.CENTER);
            playlistparam.bottomMargin = 20;
            playlistparam.leftMargin = 30;
            playlistparam.columnSpec = GridLayout.spec(0);
            playlistparam.rowSpec = GridLayout.spec(0);
            playlistName.setLayoutParams(playlistparam);
            gridLayout.addView(playlistName);


            for (int r = 0; r < songs.size(); r++) {
                Button text = new Button(getContext());
                text.setGravity(Gravity.FILL);
                text.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.setGravity(Gravity.CENTER);
                params.topMargin = 10;
                params.bottomMargin = 10;
                text.setLayoutParams(params);

                @SuppressLint("ResourceType")
                String colour = getContext().getResources().getString(R.color.playlistBackground);
                text.setBackgroundColor(Color.parseColor(colour));

                text.setText(songs.get(r).getTitle() + "\n" + songs.get(r).getArtist());

                DisplayMetrics displayMetrics = new DisplayMetrics();
                ((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                int width = displayMetrics.widthPixels;

                text.setWidth((int) (width * 0.7));
                text.setPadding(20, 10, 10, 20);

                text.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        TextView text = (TextView) v;
                        String[] names = ((String) text.getText()).split("\n");
                        musicPlayer.setClickSong(names);
                    }
                });

                GridLayout.LayoutParams param = new GridLayout.LayoutParams();
                param.height = GridLayout.LayoutParams.WRAP_CONTENT;
                param.width = GridLayout.LayoutParams.WRAP_CONTENT;
                param.setGravity(Gravity.FILL);
                //param.rightMargin = 5;
                //param.topMargin = 5;
                param.columnSpec = GridLayout.spec(0);
                param.rowSpec = GridLayout.spec(r + 1);
                text.setLayoutParams(param);
                text.setGravity(Gravity.FILL);
                gridLayout.addView(text);
            }
            colourCurrentSongInPlaylist();
        }
    }

    public void resetSpinner() {
        PlayList playList = musicPlayer.getCurrentPlayList();
        if (playList != null) {
            setSpinner(playList);
        }
    }

    public void setSpinner(PlayList playList)
    {
        Activity ac = (Activity) getContext();
        Spinner playlistSelector = (Spinner) ac.findViewById(R.id.playlistSelector);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), R.layout.support_simple_spinner_dropdown_item){};

        if (playList != null) {
            adapter.add(playList.getName());
            for (PlayList p : musicPlayer.getPlaylists()) {
                if (!p.getName().equals(playList.getName())) {
                    adapter.add(p.getName());
                }
            }
        }
        else {
            boolean hidden = musicPlayer.getHidden();
            for (PlayList p : musicPlayer.getPlaylists()) {
                if (hidden) {
                    adapter.add(p.getName());
                }
                else {
                    if (p.getType() != PlayListType.HIDDEN) {
                        adapter.add(p.getName());
                    }
                }
            }
        }
        playlistSelector.setAdapter(adapter);
        AdapterView.OnItemSelectedListener selected = new AdapterView.OnItemSelectedListener(){
            private boolean hasChanged = false;
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                if (hasChanged) {
                    musicPlayer.playPlayList((String) parent.getItemAtPosition(position));
                    displayPLaylistSongs();
                }
                hasChanged = true;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {

            }
        };
        playlistSelector.setOnItemSelectedListener(selected);
    }

    public void colourCurrentSongInPlaylist()
    {
        if (getScreenShowing().isPlayerScreenShowing() && musicPlayer.getCurrentSong() != null) {
            GridLayout gridLayout = ((Activity) getContext()).findViewById(R.id.playlists);
            for (int index = 0; index < gridLayout.getChildCount(); index++) {
                TextView textView = (TextView) gridLayout.getChildAt(index);
                String[] names = ((String) textView.getText()).split("\n");
                if (musicPlayer.getCurrentSong().getTitle().equals(names[0]) && musicPlayer.getCurrentSong().getArtist().equals(names[1])) {
                    @SuppressLint("ResourceType")
                    String colour = getContext().getResources().getString(R.color.currentSongPlaying);
                    textView.setTextColor(Color.parseColor(colour));
                } else {
                    textView.setTextColor(Color.BLACK);
                }
            }
        }
    }

    private void setScreenSizes() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((MainActivity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;

        SeekBar timeBar = (SeekBar) ((MainActivity) getContext()).findViewById(R.id.timeBar);
        SeekBar volumeBar = (SeekBar) ((MainActivity) getContext()).findViewById(R.id.volumeBar);
        GridLayout gridLayout = (GridLayout) ((MainActivity) getContext()).findViewById(R.id.playlists);
        TextView currentPosition = (TextView) ((MainActivity) getContext()).findViewById(R.id.currentPosition);
        TextView duration = (TextView) ((MainActivity) getContext()).findViewById(R.id.duration);
        GridLayout timeInfo = (GridLayout) ((MainActivity) getContext()).findViewById(R.id.timeInfo);

        LinearLayout.LayoutParams timeParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
        GridLayout.LayoutParams currentParams = new GridLayout.LayoutParams();
        GridLayout.LayoutParams durationParams = new GridLayout.LayoutParams();

        timeParams.width = (int) (width * 0.9);
        timeParams.topMargin = 20;
        timeParams.bottomMargin = 100;
        timeParams.leftMargin = (int) (width * 0.05);
        currentParams.rightMargin = (int) (width * 0.3);
        durationParams.leftMargin = (int) (width * 0.3);

        currentPosition.setLayoutParams(currentParams);
        duration.setLayoutParams(durationParams);
        timeInfo.setLayoutParams(timeParams);

        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        p.width = (int) (width * 0.7);
        p.gravity = Gravity.CENTER;
        gridLayout.setLayoutParams(p);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.width = (int) (width * 0.9);
        timeBar.setLayoutParams(params);

        LinearLayout.LayoutParams volumeparams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        volumeparams.width = (int) (width * 0.5);
        volumeBar.setLayoutParams(volumeparams);
    }
}
