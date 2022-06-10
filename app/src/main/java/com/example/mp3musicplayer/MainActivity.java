package com.example.mp3musicplayer;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.media.session.MediaSession;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Menu;
import android.view.inputmethod.InputMethodManager;

import com.google.android.material.navigation.NavigationView;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.gridlayout.widget.GridLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mp3musicplayer.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private static final String msg = "MusicPlayer : ";

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private MediaSession mediaSession;
    private MusicPlayer musicPlayer;

    private Notification notification;
    private NotificationManager notificationManager;

    private SleepTimer timer;
    private int num = 1;

    private Settings settings;
    private ScreenShowing screenShowing;
    private SettingsUpdater settingsUpdater;
    private PlayerUpdater playerUpdater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
                NavigationUI.setupWithNavController(navigationView, navController);

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }

        notif();
        createHeadPhoneListener();

        sharedPreferences = getSharedPreferences("dataInfo", MODE_PRIVATE);
        editor = sharedPreferences.edit();

        screenShowing = new ScreenShowing();
        playerUpdater = new PlayerUpdater(this, screenShowing);

        settings = new Settings(this);

        musicPlayer = new MusicPlayer(this, playerUpdater, screenShowing);
        timer = new SleepTimer(this, musicPlayer, screenShowing);

        settingsUpdater = new SettingsUpdater(this, screenShowing, settings, musicPlayer, timer);
    }

    public void deleteMerged(View view)
    {
        settingsUpdater.deleteMergedPlayLists();
    }

    public void showSoftKeyboard(View view) {
        if (view.requestFocus()) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    public void showMergePlayListEditor(View view)
    {
        settingsUpdater.showMergePlayListEditor();
    }

    public void addMergedPlayList(View view)
    {
        settingsUpdater.addMergedPlayList();
    }


    public void changeScreen(boolean screen)
    {
        screenShowing.setScreenShowing(screen);
    }

    /**
     * Sets the visual elements of the main screen
     */
    public void setPlayerScreen()
    {
        screenShowing.setScreenShowing(true);
        playerUpdater.setup();
        musicPlayer.setupProgressUpdater();
    }

    /**
     * Sets the visual elements of the settings screen
     */
    public void setSettingsScreen()
    {
        screenShowing.setScreenShowing(false);
        settingsUpdater.setup();
    }

    private void notif()
    {
        /*Intent snoozeIntent = new Intent(this, PauseBroadcastReceiver.class);
        snoozeIntent.setAction(KeyEvent.KEYCODE_MEDIA_PAUSE + "");
        snoozeIntent.putExtra(Notification.EXTRA_NOTIFICATION_ID, 0);
        PendingIntent snoozePendingIntent =
                PendingIntent.getBroadcast(this, 0, snoozeIntent, 0);

        notification = new NotificationCompat.Builder(this, "default")
                .setSmallIcon(R.drawable.ic_notif_icon)
                .setContentTitle("My notification")
                .setContentText("Hello World!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(snoozePendingIntent)
                .setAutoCancel(false)
                .addAction(R.drawable.ic_pause, "Pause", snoozePendingIntent);


        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        NotificationChannel notificationChannel = new NotificationChannel( "100010011101" , "NOTIFICATION_CHANNEL_NAME" , NotificationManager.IMPORTANCE_HIGH) ;
        mNotificationManager.createNotificationChannel(notificationChannel) ;
        mNotificationManager.notify(100, notification.build());*/

        CharSequence name = "My app";
        String description = "Music";
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel channel = new NotificationChannel("My app", name, importance);
        channel.setDescription(description);
        // Register the channel with the system; you can't change the importance
        // or other notification behaviors after this

        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        notificationManager.createNotificationChannel(channel);

        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);


        notification = new NotificationCompat.Builder(this, NotificationChannel.DEFAULT_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notif_icon)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_notif_icon))
                .setContentTitle("My notification")
                .setContentIntent(pi)
                .addAction(R.drawable.ic_pause,"Delete", pi)
                .setContentText("Much longer text that cannot fit one line...")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Much longer text that cannot fit one line..."))
                .setPriority(NotificationCompat.PRIORITY_HIGH).build();

        notificationManager.notify(1, notification);
    }

    /**
     * Clears all of the saved information from the file
     */
    public void clear()
    {
        editor.clear();
        editor.apply();
    }

    /**
     * Saves the state of the player
     * @param currentPlaylist The current playlist
     * @param currentSong The current song
     * @param currentTime The current time
     * @param currentVolume The current volume level
     */
    public void saveState(PlayList currentPlaylist, Song currentSong, long currentTime, int currentVolume)
    {
        editor.putString("CurrentPlaylist", currentPlaylist.getName());
        editor.putString("CurrentSong", currentSong.getTitle());
        editor.putString("CurrentArtist", currentSong.getArtist());
        editor.putLong("CurrentTime", currentTime);
        editor.putInt("Volume", currentVolume);
        editor.apply();
    }

    /**
     * Creates a receiver for headphone button commands
     */
    private void createHeadPhoneListener()
    {
        mediaSession = new MediaSession(getApplicationContext(), getPackageName());
        mediaSession.setActive(true);

        mediaSession.setCallback(new MediaSession.Callback() {
            @Override
            public boolean onMediaButtonEvent(Intent mediaButtonIntent) {
                num = (num + 1) % 2;
                KeyEvent event = (KeyEvent) mediaButtonIntent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);

                int keyCode = event.getKeyCode();
                if (num == 1) {
                    if(keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) {

                        musicPlayer.playpause();
                    }
                    else if (keyCode == KeyEvent.KEYCODE_MEDIA_NEXT)
                    {
                        musicPlayer.skip(false);
                    }
                    else if (keyCode == KeyEvent.KEYCODE_MEDIA_PREVIOUS)
                    {
                        musicPlayer.back();
                    }
                    return true;
                }

                return super.onMediaButtonEvent(mediaButtonIntent);
            }
        });
    }

    /**
     * Plays the player
     * @param view
     */
    public void play(View view)
    {
        musicPlayer.play(false);
    }

    /**
     * Pauses the player
     * @param view
     */
    public void pause(View view)
    {
        musicPlayer.pause();
    }

    /**
     * Plays or pauses the player depending on the current state
     * @param view
     */
    public void playpause(View view)
    {
        musicPlayer.playpause();
    }

    /**
     * Sets the player to the next song
     * @param view
     */
    public void next(View view)
    {
        musicPlayer.skip(false);
    }

    /**
     * Set the player to the previous song
     * @param view a view
     */
    public void back(View view)
    {
        musicPlayer.back();
    }

    /**
     * Shuffles the songs in the playlist
     * @param view a view
     */
    public void shuffle(View view)
    {
        musicPlayer.shuffle();
    }


    /** Called when the activity is about to become visible. */
    @Override
    protected void onStart() {
        super.onStart();
        Log.d(msg, "The onStart() event");
    }

    /** Called when the activity has become visible. */
    @Override
    protected void onResume() {
        super.onResume();
    }

    /** Called when another activity is taking focus. */
    @Override
    protected void onPause() {
        super.onPause();
        Log.d(msg, "The onPause() event");
    }

    /** Called when the activity is no longer visible. */
    @Override
    protected void onStop() {
        super.onStop();
        Log.d(msg, "The onStop() event");
        musicPlayer.triggerSave();
    }

    /** Called just before the activity is destroyed. */
    @Override
    public void onDestroy() {
        super.onDestroy();
        notificationManager.deleteNotificationChannel("My app");
        Log.d(msg, "The onDestroy() event");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}