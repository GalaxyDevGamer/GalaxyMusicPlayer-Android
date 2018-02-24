package galaxysoftware.galaxymusicplayer_android;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

public class PlayerforExport extends AppCompatActivity {

    Intent MusicService;
    MusicServiceforExport MS;
    ImageButton PlayPause;
    TextView SongTitle, PlayTime, Duration;
    SeekBar PlayTimeBar, Volume;
    Handler ShowPlayTime;
    ImageView Thumbnail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.exported_player);
        MusicService = new Intent(PlayerforExport.this, MusicServiceforExport.class);
        startService(MusicService);
        bindService(MusicService, MusicServiceConnection, Context.BIND_AUTO_CREATE);
        PlayPause = (ImageButton) findViewById(R.id.PlayPause);
        SongTitle = (TextView) findViewById(R.id.SongTitle);
        PlayTime = (TextView) findViewById(R.id.PlayTime);
        Duration = (TextView) findViewById(R.id.Duration);
        Thumbnail = (ImageView) findViewById(R.id.Thumbnail);
        //237140 3:57
        PlayTimeBar = (SeekBar) findViewById(R.id.PlayTimeBar);
        PlayTimeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    MS.Player.seekTo(progress);
                    PlayTimeBar.setProgress(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        Volume = (SeekBar) findViewById(R.id.VolumeBar);
        Volume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            //つまみがドラッグされた時
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                float Volume = progress / 100f;
                MS.Player.setVolume(Volume, Volume);
            }

            //つまみがタッチされた時
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            //つまみが離された時
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        findViewById(R.id.FastRewind).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MS.Player.seekTo(0);
                PlayTimeBar.setProgress(0);
                PlayTime.setText("00:00");
            }
        });
        PlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (MS.Player.isPlaying()) {
                    MS.Player.pause();
                    PlayPause.setImageResource(R.mipmap.ic_play_circle_filled_black_48dp);
                    ShowPlayTime.removeCallbacksAndMessages(null);
                } else {
                    MS.Player.start();
                    PlayPause.setImageResource(R.mipmap.ic_pause_circle_filled_black_48dp);
                    StartShowing();
                }
            }
        });
        findViewById(R.id.FastForward).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MS.Player.seekTo(0);
                PlayTimeBar.setProgress(0);
                PlayTime.setText("00:00");
            }
        });
        ShowPlayTime = new Handler();
    }

    public void UpdateSongInfo() {
        PlayPause.setImageResource(R.mipmap.ic_pause_circle_filled_black_48dp);
        PlayTimeBar.setMax(MS.Player.getDuration());
        Duration.setText(getTimeString(MS.Player.getDuration()));
        Thumbnail.setImageResource(R.mipmap.ic_music_video_black_48dp);
        SongTitle.setText(getIntent().getData().getPath());
    }

    ServiceConnection MusicServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            MusicServiceforExport.MusicServiceBinder binder = (MusicServiceforExport.MusicServiceBinder) iBinder;
            MS = binder.getService();
            if (Intent.ACTION_VIEW.equals(getIntent().getAction())) {
                MS.PlayFromFile(getIntent().getData().getPath());
                Volume.setProgress(50);
                UpdateSongInfo();
                StartShowing();
                // do what you want with the file...
            }
            MS.Player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    MS.Player.seekTo(0);
                    PlayTimeBar.setProgress(0);
                    PlayTime.setText("00:00");
                    PlayPause.setImageResource(R.mipmap.ic_play_circle_filled_black_48dp);
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
        }
    };

    private String getTimeString(long millis) {
        StringBuffer buf = new StringBuffer();
        if (millis >= 6000000) {
            int hours = (int) (millis / (1000 * 60 * 60));
            int minutes = (int) ((millis % (1000 * 60 * 60)) / (1000 * 60));
            int seconds = (int) (((millis % (1000 * 60 * 60)) % (1000 * 60)) / 1000);
            buf.append(String.format("%02d", hours)).append(":").append(String.format("%02d", minutes)).append(":").append(String.format("%02d", seconds));
        } else {
            int minutes = (int) ((millis % (1000 * 60 * 60)) / (1000 * 60));
            int seconds = (int) (((millis % (1000 * 60 * 60)) % (1000 * 60)) / 1000);
            buf.append(String.format("%02d", minutes)).append(":").append(String.format("%02d", seconds));
        }
        return buf.toString();
    }

    public void StartShowing() {
        if (MS.Player.isPlaying()) {
            ShowPlayTime.postDelayed(new Runnable() {
                public void run() {
                    PlayTimeBar.setProgress(MS.Player.getCurrentPosition());
                    PlayTime.setText(getTimeString(MS.Player.getCurrentPosition()));
                    ShowPlayTime.postDelayed(this, 1000);
                }
            }, 1000);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(MS != null) {
            StartShowing();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        ShowPlayTime.removeCallbacksAndMessages(null);
    }

    public void onDestroy() {
        super.onDestroy();
        unbindService(MusicServiceConnection);
        stopService(MusicService);
    }
}