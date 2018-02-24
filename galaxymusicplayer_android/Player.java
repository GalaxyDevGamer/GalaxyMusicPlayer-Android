package galaxysoftware.galaxymusicplayer_android;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class Player extends AppCompatActivity {

    Manager Manager;
    ImageButton PlayPause, Shuffle, Repeat;
    TextView SongTitle, PlayTime, Duration;
    SeekBar PlayTimeBar, Volume;
    Handler ShowPlayTime;
    ImageView Thumbnail;
    ListView lv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.playerlayout);
        Manager = (Manager) getApplication();
        PlayPause = (ImageButton) findViewById(R.id.PlayPause);
        if (Manager.MS.Player.isPlaying()) {
            PlayPause.setImageResource(R.mipmap.ic_pause_circle_filled_black_48dp);
        }
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
                    Manager.MS.Player.seekTo(progress);
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
                Manager.MS.Player.setVolume(Volume, Volume);
                Manager.editor.putInt("Volume", progress);
            }

            //つまみがタッチされた時
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            //つまみが離された時
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Manager.editor.apply();
            }
        });
        Volume.setProgress(Manager.preference.getInt("Volume", 50));
        findViewById(R.id.FastRewind).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Manager.MS.PlayPreviousSong();
                UpdateSongInfo();
            }
        });
        findViewById(R.id.PlayPause).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Manager.MS.PlayingSong.isEmpty()) {
                    Manager.MS.PlayPause(PlayPause);
                    UpdateSongInfo();
                } else {
                    Manager.MS.PlayPause(PlayPause);
                }
                if (Manager.MS.Player.isPlaying()) {
                    StartShowing();
                } else {
                    ShowPlayTime.removeCallbacksAndMessages(null);
                }
            }
        });
        findViewById(R.id.FastForward).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Manager.MS.PlayNextSong();
                UpdateSongInfo();
            }
        });
        Shuffle = (ImageButton) findViewById(R.id.Shuffle);
        Shuffle.setImageResource(Manager.preference.getBoolean("Shuffle", false) ? R.mipmap.ic_shuffle_red_48dp : R.mipmap.ic_shuffle_black_48dp);
        Shuffle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!Manager.MS.Shuffle) {
                    Manager.MS.Shuffle = true;
                    Shuffle.setImageResource(R.mipmap.ic_shuffle_red_48dp);
                    Manager.editor.putBoolean("Shuffle", true).apply();
                } else {
                    Manager.MS.Shuffle = false;
                    Shuffle.setImageResource(R.mipmap.ic_shuffle_black_48dp);
                    Manager.editor.putBoolean("Shuffle", false).apply();
                }
                Manager.MS.CreateUpNext(Manager.MS.PlayingIndex);
            }
        });
        Repeat = (ImageButton) findViewById(R.id.Repeat);
        switch (Manager.preference.getInt("Repeat", 0)) {
            case 0:
                Repeat.setImageResource(R.mipmap.ic_repeat_black_48dp);
                break;
            case 1:
                Repeat.setImageResource(R.mipmap.ic_repeat_red_48dp);
                break;
            case 2:
                Repeat.setImageResource(R.mipmap.ic_repeat_one_red_48dp);
                break;
        }
        Repeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (Manager.MS.Repeat) {
                    case 0:
                        Manager.MS.Repeat = 1;
                        Repeat.setImageResource(R.mipmap.ic_repeat_red_48dp);
                        Manager.editor.putInt("Repeat", 1).apply();
                        break;
                    case 1:
                        Manager.MS.Repeat = 2;
                        Manager.MS.Player.setLooping(true);
                        Repeat.setImageResource(R.mipmap.ic_repeat_one_red_48dp);
                        Manager.editor.putInt("Repeat", 2).apply();
                        break;
                    case 2:
                        Manager.MS.Repeat = 0;
                        Manager.MS.Player.setLooping(false);
                        Repeat.setImageResource(R.mipmap.ic_repeat_black_48dp);
                        Manager.editor.putInt("Repeat", 0).apply();
                        break;
                }
            }
        });
        try {
            Manager.MS.Player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    Manager.MS.PlayComplete();
                    UpdateSongInfo();
                }
            });
        } catch (Exception e) {
            Toast.makeText(this, "Failed to set onCompletionListener\n" + e.getMessage(), Toast.LENGTH_LONG).show();
        }
        //Cursor cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, new String[]{MediaStore.Audio.Media.ALBUM_ID}, MediaStore.Audio.Media.IS_MUSIC + " != 0", null, null);
        UpdateSongInfo();
        ShowPlayTime = new Handler();
//        lv = (ListView)findViewById(R.id.UpNext);
//        try {
//            lv.setAdapter(Manager.UpNextAdapter);
//        } catch (Exception e){
//            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
//        }
    }

    public void UpdateSongInfo() {
        SongTitle.setText(Manager.MS.PlayingSong);
        if (Manager.MS.PlayingSong.isEmpty()) {
            PlayPause.setImageResource(R.mipmap.ic_play_circle_filled_black_48dp);
            Thumbnail.setImageResource(R.mipmap.ic_music_video_black_48dp);
            Duration.setText("");
            PlayTime.setText("00:00");
        } else {
            PlayPause.setImageResource(R.mipmap.ic_pause_circle_filled_black_48dp);
            PlayTimeBar.setMax(Manager.MS.Player.getDuration());
            Duration.setText(getTimeString(Manager.MS.Player.getDuration()));
            Thumbnail.setImageBitmap(Manager.MS.PlayingSongCover);
            SongTitle.setText(Manager.MS.PlayingSong);
        }
    }

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
        if (Manager.MS.Player.isPlaying()) {
            ShowPlayTime.postDelayed(new Runnable() {
                public void run() {
                    PlayTimeBar.setProgress(Manager.MS.Player.getCurrentPosition());
                    PlayTime.setText(getTimeString(Manager.MS.Player.getCurrentPosition()));
                    ShowPlayTime.postDelayed(this, 1000);
                }
            }, 1000);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        StartShowing();
    }

    @Override
    public void onPause() {
        super.onPause();
        ShowPlayTime.removeCallbacksAndMessages(null);
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)) {
            int CurrentVolume = Manager.preference.getInt("Volume", 50)-10;
            float volume = 0;
            if(CurrentVolume > 0){
                volume = (Manager.preference.getInt("Volume", 50)-10) / 100f;
            } else {
                CurrentVolume = 0;
            }
            Manager.MS.Player.setVolume(volume, volume);
            Volume.setProgress(CurrentVolume);
            Manager.editor.putInt("Volume", CurrentVolume).apply();
            return true;
        }else if ((keyCode == KeyEvent.KEYCODE_VOLUME_UP)){
            int CurrentVolume = Manager.preference.getInt("Volume", 50)+10;
            float volume;
            if(CurrentVolume < 100){
                volume = (Manager.preference.getInt("Volume", 50)+10) / 100f;
            } else {
                CurrentVolume = 100;
                volume = 100;
            }
            Manager.MS.Player.setVolume(volume, volume);
            Volume.setProgress(CurrentVolume);
            Manager.editor.putInt("Volume", CurrentVolume).apply();
            return true;
        }else
            return super.onKeyDown(keyCode, event);
    }
}