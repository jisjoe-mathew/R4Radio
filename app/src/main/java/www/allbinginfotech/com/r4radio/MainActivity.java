package www.allbinginfotech.com.r4radio;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatDelegate;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;


public class MainActivity extends Activity  {

    private ImageButton btnPlay;
    private ImageButton btnForward;
    private ImageButton btnBackward;
    private ImageButton btnNext;
    private ImageButton btnPrevious;
    private ImageButton btnPlaylist;
    private ImageButton btnRepeat;
    private ImageButton btnShuffle;
    private SeekBar songProgressBar;
    private TextView songTitleLabel;
    private TextView songCurrentDurationLabel;
    private TextView songTotalDurationLabel;
    private ProgressDialog progressDialog;
    private AudioManager audioManager = null;

    MediaMetadataRetriever metaRetriver;
    byte[] art;

    // Media Player
    private MediaPlayer mp;
    // Handler to update UI timer, progress bar etc,.
    private Handler mHandler = new Handler();;
    private int seekForwardTime = 5000; // 5000 milliseconds
    private int seekBackwardTime = 5000; // 5000 milliseconds
    private int currentSongIndex = 0;
    private boolean isShuffle = false;
    private boolean isRepeat = false;
    private ArrayList<HashMap<String, String>> songsList = new ArrayList<HashMap<String, String>>();


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.player);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
       if(isOnline()==true)
       {
        //do whatever you want to do

        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        initControls();
        // All player buttons
        btnPlay = (ImageButton) findViewById(R.id.btnPlay);
      btnRepeat = (ImageButton) findViewById(R.id.btnRepeat);
        songTitleLabel = (TextView) findViewById(R.id.songTitle);

        // Mediaplayer
        mp = new MediaPlayer();
        mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
        progressDialog = new ProgressDialog(this);
        //songManager = new SongsManager();
       // utils = new Utilities();

        // Listeners
      //  songProgressBar.setOnSeekBarChangeListener(this); // Important
        //mp.setOnCompletionListener(this); // Important

        // Getting all songs list
      //  songsList = songManager.getPlayList();

        // By default play first song
       // playSong(0);

        new Player().execute("http://www.r4radio.com:8000/live");
        btnPlay.setImageResource(R.drawable.btn_pause);
        MyTimerTask myTask = new MyTimerTask();
        Timer myTimer = new Timer();
        //        public void schedule (TimerTask task, long delay, long period)
        //        Schedule a task for repeated fixed-delay execution after a specific delay.
        //
        //        Parameters
        //        task  the task to schedule.
        //        delay  amount of time in milliseconds before first execution.
        //        period  amount of time in milliseconds between subsequent executions.

        myTimer.schedule(myTask, 10000, 7500);
        /**
         * Play button click event
         * plays a song and changes button to pause image
         * pauses a song and changes button to play image
         * */
        btnPlay.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // check for already playing
                if(mp.isPlaying()){
                    if(mp!=null){
                            mp.pause();

                        // Changing button image to play button
                        btnPlay.setImageResource(R.drawable.btn_play);
                    }
                }else{
                    // Resume song
                    if(mp!=null){
                        mp.start();
                        // Changing button image to pause button
                        btnPlay.setImageResource(R.drawable.btn_pause);
                    }
                }

            }
        });

        /**
         * Forward button click event
         * Forwards song specified seconds
         *
        btnForward.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // get current song position				
                int currentPosition = mp.getCurrentPosition();
                // check if seekForward time is lesser than song duration
                if(currentPosition + seekForwardTime <= mp.getDuration()){
                    // forward song
                    mp.seekTo(currentPosition + seekForwardTime);
                }else{
                    // forward to end position
                    mp.seekTo(mp.getDuration());
                }
            }
        });

        /**
         * Backward button click event
         * Backward song to specified seconds
         *
        btnBackward.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // get current song position				
                int currentPosition = mp.getCurrentPosition();
                // check if seekBackward time is greater than 0 sec
                if(currentPosition - seekBackwardTime >= 0){
                    // forward song
                    mp.seekTo(currentPosition - seekBackwardTime);
                }else{
                    // backward to starting position
                    mp.seekTo(0);
                }

            }
        });

        /**
         * Next button click event
         * Plays next song by taking currentSongIndex + 1
         *
        btnNext.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // check if next song is there or not
                if(currentSongIndex < (songsList.size() - 1)){
                    playSong(currentSongIndex + 1);
                    currentSongIndex = currentSongIndex + 1;
                }else{
                    // play first song
                    playSong(0);
                    currentSongIndex = 0;
                }

            }
        });

        /**
         * Back button click event
         * Plays previous song by currentSongIndex - 1
         *
        btnPrevious.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if(currentSongIndex > 0){
                    playSong(currentSongIndex - 1);
                    currentSongIndex = currentSongIndex - 1;
                }else{
                    // play last song
                    playSong(songsList.size() - 1);
                    currentSongIndex = songsList.size() - 1;
                }

            }
        });

        /**
         * Button Click event for Repeat button
         * Enables repeat flag to true
         */
        btnRepeat.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if(mp!=null)
                mp.release();
                MainActivity.this.finish();
                Intent i=new Intent(getApplicationContext(),MainActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(new Intent(getApplicationContext(),MainActivity.class));


            }
        });

        /**
         * Button Click event for Shuffle button
         * Enables shuffle flag to true
         *
        btnShuffle.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if(isShuffle){
                    isShuffle = false;
                    Toast.makeText(getApplicationContext(), "Shuffle is OFF", Toast.LENGTH_SHORT).show();
                    btnShuffle.setImageResource(R.drawable.btn_shuffle);
                }else{
                    // make repeat to true
                    isShuffle= true;
                    Toast.makeText(getApplicationContext(), "Shuffle is ON", Toast.LENGTH_SHORT).show();
                    // make shuffle to false
                    isRepeat = false;
                    btnShuffle.setImageResource(R.drawable.btn_shuffle_focused);
                    btnRepeat.setImageResource(R.drawable.btn_repeat);
                }
            }
        });

        /**
         * Button Click event for Play list click event
         * Launches list activity which displays list of songs
         *
        btnPlaylist.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Intent i = new Intent(getApplicationContext(), PlayListActivity.class);
                startActivityForResult(i, 100);
            }
        });

    }

    /**
     * Receiving song index from playlist view
     * and play the song
     * */
    }
    }
    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == 100){
            currentSongIndex = data.getExtras().getInt("songIndex");
            // play selected song
           // playSong(currentSongIndex);
        }

    }


    /*
     * Function to play a song
     * @param songIndex - index of song
     *
    public void  playSong(int songIndex){
        // Play song
        try {
            mp.reset();
            mp.setDataSource(songsList.get(songIndex).get("songPath"));
            mp.prepare();
            mp.start();
            // Displaying Song title
            String songTitle = songsList.get(songIndex).get("songTitle");
            songTitleLabel.setText(songTitle);

            // Changing Button Image to pause image
            btnPlay.setImageResource(R.drawable.btn_pause);

            // set Progress bar values
            songProgressBar.setProgress(0);
            songProgressBar.setMax(100);

            // Updating progress bar
            updateProgressBar();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Update timer on seekbar
     *
    public void updateProgressBar() {
        mHandler.postDelayed(mUpdateTimeTask, 100);
    }

    /*
     * Background Runnable thread
     *
    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            long totalDuration = mp.getDuration();
            long currentDuration = mp.getCurrentPosition();

            // Displaying Total Duration time
            songTotalDurationLabel.setText(""+utils.milliSecondsToTimer(totalDuration));
            // Displaying time completed playing
            songCurrentDurationLabel.setText(""+utils.milliSecondsToTimer(currentDuration));

            // Updating progress bar
            int progress = (int)(utils.getProgressPercentage(currentDuration, totalDuration));
            //Log.d("Progress", ""+progress);
            songProgressBar.setProgress(progress);

            // Running this thread after 100 milliseconds
            mHandler.postDelayed(this, 100);
        }
    };

    /**
     *
     * */


    /**
     * When user starts moving the progress handler
     *
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // remove message Handler from updating progress bar
        mHandler.removeCallbacks(mUpdateTimeTask);
    }

    /**
     * When user stops moving the progress hanlder
     *
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        mHandler.removeCallbacks(mUpdateTimeTask);
        int totalDuration = mp.getDuration();
        int currentPosition = utils.progressToTimer(seekBar.getProgress(), totalDuration);

        // forward or backward to certain seconds
        mp.seekTo(currentPosition);

        // update timer progress again
        updateProgressBar();
    }

    /**
     * On Song Playing completed
     * if repeat is ON play same song again
     * if shuffle is ON play random song
     *
    @Override
    public void onCompletion(MediaPlayer arg0) {

        // check for repeat is ON or OFF
        if(isRepeat){
            // repeat is on play same song again
            playSong(currentSongIndex);
        } else if(isShuffle){
            // shuffle is on - play a random song
            Random rand = new Random();
            currentSongIndex = rand.nextInt((songsList.size() - 1) - 0 + 1) + 0;
            playSong(currentSongIndex);
        } else{
            // no repeat or shuffle ON - play next song
            if(currentSongIndex < (songsList.size() - 1)){
                playSong(currentSongIndex + 1);
                currentSongIndex = currentSongIndex + 1;
            }else{
                // play first song
                playSong(0);
                currentSongIndex = 0;
            }
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        mp.release();
    }

*/
class Player extends AsyncTask<String, Void, Boolean> {
    @Override
    protected Boolean doInBackground(String... strings) {
        Boolean prepared = false;

        try {
            mp.setDataSource(strings[0]);
            mp.setOnCompletionListener(new OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    if(mp!=null){
                        mp.start();
                        // Changing button image to pause button
                        btnPlay.setImageResource(R.drawable.btn_pause);
                     mp.stop();
                    mp.reset();}
                }
            });

            mp.prepare();
            prepared = true;

        } catch (Exception e) {
           // Log.e("MyAudioStreamingApp", e.getMessage());
            prepared = false;
        }

        return prepared;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);
     //   new RetrieveFeedTask().execute("http://www.r4radio.com:8000");
        (new ParseURL() ).execute(new String[]{"http://www.r4radio.com:8000"});
        if (progressDialog.isShowing()) {
            progressDialog.cancel();
        }


       /* metaRetriver = new MediaMetadataRetriever();
        metaRetriver.setDataSource("http://www.r4radio.com:8000/live");
        try {
            art = metaRetriver.getEmbeddedPicture();
            Bitmap songImage = BitmapFactory
                    .decodeByteArray(art, 0, art.length);
            Log.i("jisjoe",""+MediaMetadataRetriever.METADATA_KEY_ALBUM);
            //album_art.setImageBitmap(songImage);
           // album.setText(metaRetriver
             //       .extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM));
          //  artist.setText(metaRetriver
            //        .extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST));
          //  genre.setText(metaRetriver
            //        .extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE));
        } catch (Exception e) {
         //   album_art.setBackgroundColor(Color.GRAY);
           // album.setText("Unknown Album");
           // artist.setText("Unknown Artist");
            //genre.setText("Unknown Genre");
        }*/
        mp.start();
        //initialStage = false;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog.setMessage("Buffering...");
        progressDialog.show();

    }
}
    class RetrieveFeedTask extends AsyncTask<String, Void, String> {

        private Exception exception;

        protected String doInBackground(String... urls) {
            try {
                URL url = new URL(urls[0]);
                String fullString = "";
                try {
                    //url = new URL("http://www.r4radio.com:8000");
                    BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
                    String line;

                    while ((line = reader.readLine()) != null) {
                        fullString += line;
                    }
                    reader.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }

                return fullString;
            } catch (Exception e) {
                this.exception = e;

                return null;
            } finally {

            }
        }

        protected void onPostExecute(String feed) {
            // TODO: check this.exception
            // TODO: do something with the feed
            Log.i("jisjoe",feed);
        }
    }
    private class ParseURL extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            StringBuffer buffer = new StringBuffer();
            try {
                Log.d("jisjoe", "Connecting to [" + strings[0] + "]");
                Document doc = Jsoup.connect(strings[0]).get();
                Log.d("jisjoe", "Connected to [" + strings[0] + "]");
                // Get document (HTML page) title
                String title = doc.title();
                Log.d("jisjoe", "Title [" + title + "]");
                buffer.append("Title: " + title + "rn");
                Elements links = doc.select("td[class]");
                final ArrayList<String> list=new ArrayList<String>();
                list.clear();
                for (Element metaElem : links) {
                    String name = metaElem.attr("Currently Playing:");
                    String content = metaElem.attr("content");
                    list.add(""+Jsoup.parse(""+metaElem).text());

                }
                Log.i("jisjoe","name--"+list.get(6));
                runOnUiThread(new Runnable(){
                    @Override
                    public void run(){
                        songTitleLabel.setSelected(true);
                        songTitleLabel.setEllipsize(TextUtils.TruncateAt.MARQUEE);
                        songTitleLabel.setSingleLine(true);
                        songTitleLabel.setText(""+list.get(6));

                        // change UI elements here
                    }
                });


                // Get meta info
                Elements metaElems = doc.select("<td>");
                buffer.append("META DATArn");
                for (Element metaElem : metaElems) {
                    String name = metaElem.attr("Currently Playing:");
                    String content = metaElem.attr("content");
                    buffer.append("name [" + name + "] - content [" + content + "] rn");
                }

                Elements topicList = doc.select("h2.topic");
                buffer.append("Topic listrn");
                for (Element topic : topicList) {
                    String data = topic.text();
                    buffer.append("Data [" + data + "] rn");
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
            return buffer.toString();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            Log.i("jisjoe",s);
        }
}

    class MyTimerTask extends TimerTask

    {
        public void run () {
        // ERROR
            (new ParseURL() ).execute(new String[]{"http://www.r4radio.com:8000"});
        // how update TextView in link below
        // http://android.okhelp.cz/timer-task-timertask-run-cancel-android-    example/
        System.out.println("");
    }
    }
    private void initControls()
    {
        try
        {
            songProgressBar = (SeekBar)findViewById(R.id.songProgressBar);
            audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            songProgressBar.setMax(audioManager
                    .getStreamMaxVolume(AudioManager.STREAM_MUSIC));
            songProgressBar.setProgress(audioManager
                    .getStreamVolume(AudioManager.STREAM_MUSIC));


            songProgressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
            {
                @Override
                public void onStopTrackingTouch(SeekBar arg0)
                {
                }

                @Override
                public void onStartTrackingTouch(SeekBar arg0)
                {
                }

                @Override
                public void onProgressChanged(SeekBar arg0, int progress, boolean arg2)
                {
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                            progress, 0);
                }
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public boolean isOnline() {
        ConnectivityManager conMgr = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = conMgr.getActiveNetworkInfo();

        if (netInfo == null || !netInfo.isConnected() || !netInfo.isAvailable()) {
            Toast.makeText(getApplicationContext(), "No Internet connection!", Toast.LENGTH_LONG).show();
            AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();

            alertDialog.setTitle("Info");
            alertDialog.setMessage("Internet not available, Cross check your internet connectivity and try again!!");
            alertDialog.setIcon(android.R.drawable.ic_dialog_alert);
            alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    MainActivity.this.finish();

                }
            });
alertDialog.setCancelable(false);
            alertDialog.show();

        }
        else {
            return true;
        }
        return false;
    }
}