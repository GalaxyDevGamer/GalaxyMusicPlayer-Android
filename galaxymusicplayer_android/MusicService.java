package galaxysoftware.galaxymusicplayer_android;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.widget.ImageButton;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MusicService extends Service {
    MediaPlayer Player;
    Manager Manager;

    private final IBinder Binder = new MusicServiceBinder();

    int PlayFrom, Repeat, UpNextIndex, PlayingIndex;
    String PlayingSong = "", PlayingSongPath = "";
    Bitmap PlayingSongCover;
    boolean Playing, Shuffle;

    public class MusicServiceBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return Binder;
    }

    @Override
    public void onRebind(Intent intent) {
        Toast.makeText(this, "Rebinding Service: " + intent, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Toast.makeText(this, "Unbinding Service: " + intent, Toast.LENGTH_SHORT).show();
        //onUnbindをreturn trueでoverrideすると次回バインド時にonRebildが呼ばれる
        return true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        Player = new MediaPlayer();
        Manager = (Manager) getApplication();
        float Volume = Manager.preference.getInt("Volume", 50) / 100f;
        Shuffle = Manager.preference.getBoolean("Shuffle", false);
        Repeat = Manager.preference.getInt("Repeat", 0);
        Player.setVolume(Volume, Volume);
        Player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                PlayComplete();
            }
        });
    }
    public void PlaySelectedSong(int PlayFrom, ListViewAdapter adapter, int position) {
        //ここはライブラリとプレイリストで共に利用されるので、ライブラリとプレイリストの切り替えもここですることになる
        //Recently Playedみたいに再生履歴リストは用意してもいいかも？
        this.PlayFrom = PlayFrom;
        setNextSong(adapter, position);
        try {
            Player.setDataSource(PlayingSongPath);
            if (Repeat == 2) {
                Player.setLooping(true);
            }
            Player.prepare();
            Player.start();
            Playing = true;
        } catch (Exception e) {
            Playing = false;
            PlayingSong = "";
            PlayingSongPath = "";
            PlayingSongCover = null;
            Toast.makeText(this, "Failed to play: " + PlayingSong + "\n\nError: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        if (Playing) {
            try {
                CreateUpNext(position);
            } catch (Exception e) {
                Toast.makeText(this, "Failed to create Up Next\n\nError: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void Play() {
        //UpNextから再生する場合: getUpNext()
        //UpNextが終わって次を待つ時: setNextSong()
        try {
            Player.setDataSource(PlayingSongPath);
            if (Repeat == 2) {
                Player.setLooping(true);
            }
            Player.prepare();
            Player.start();
            Playing = true;
        } catch (Exception e) {
            Playing = false;
            Toast.makeText(this, "Failed to play: " + PlayingSong, Toast.LENGTH_SHORT).show();
            PlayNextSong();
        }
        if (Playing) {
            if (Manager.RecentlyPlayed.contains(PlayingSong)) {
                Manager.RecentlyPlayed.remove(PlayingSong);
            }
            Manager.RecentlyPlayed.add(PlayingSong);
        }
    }

    public void getUpNext() {
        PlayingSong = Manager.UpNextAdapter.getTitle(UpNextIndex);
        PlayingSongPath = Manager.UpNextAdapter.getPath(UpNextIndex);
        if (Manager.UpNextAdapter.getAlbumArt(UpNextIndex) != null) {
            PlayingSongCover = BitmapFactory.decodeFile(Manager.UpNextAdapter.getAlbumArt(UpNextIndex));
        } else {
            PlayingSongCover = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_music_video_black_48dp);
        }
    }

    public void setNextSong(ListViewAdapter adapter, int index) {
        PlayingIndex = index;
        PlayingSong = adapter.getTitle(index);
        PlayingSongPath = adapter.getPath(index);
        if (adapter.getAlbumArt(index) != null) {
            PlayingSongCover = BitmapFactory.decodeFile(adapter.getAlbumArt(index));
        } else {
            PlayingSongCover = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_music_video_black_48dp);
        }
    }
    public void setIndexforNextSong(ListViewAdapter adapter) {
        int index = 0;
        if(Shuffle){
            if(adapter.getCount() > 1){
                Random random = new Random();
                index = random.nextInt(adapter.getCount()-1);
            }
        }
        setNextSong(adapter, index);
    }

    public void WaitforNextSong() {
        if (Shuffle) {
            //これだと1曲しかない場合に落ちる
            if (PlayFrom == 1) {
                setIndexforNextSong(Manager.SongAdapter);
            } else if (PlayFrom == 2) {
                setIndexforNextSong(Manager.PlayListContentAdapter);
            } else if (PlayFrom == 3) {
                setIndexforNextSong(Manager.ACAdapter);
            } else if (PlayFrom == 4) {
                setIndexforNextSong(Manager.ArtistC);
            }
        } else {
            if (PlayFrom == 1) {
                setIndexforNextSong(Manager.SongAdapter);
            } else if (PlayFrom == 2) {
                setIndexforNextSong(Manager.PlayListContentAdapter);
            } else if (PlayFrom == 3) {
                setIndexforNextSong(Manager.ACAdapter);
            } else if (PlayFrom == 4) {
                setIndexforNextSong(Manager.ArtistC);
            }
        }
        Play();
        CreateUpNext(PlayingIndex);
    }

    public void PlayPause(ImageButton PlayPause) {
        if (Player.isPlaying()) {
            Player.pause();
            Playing = false;
            PlayPause.setImageResource(R.mipmap.ic_play_circle_filled_black_48dp);
        } else {
            //ここの曲のプレイ状況とかはこのService内で管理したほうがいいかも
            if (!PlayingSong.isEmpty()) {
                Player.start();
                Playing = true;
                PlayPause.setImageResource(R.mipmap.ic_pause_circle_filled_black_48dp);
            } else {
                WaitforNextSong();
            }
        }
    }

    public void PlayNextSong() {
        if (!PlayingSong.isEmpty()) {
            ResetPlayer();
        }
        boolean isNextSongAvailable;
        //これだと、UpNextで曲がずれる。Createで作るUpNextの順番は合っている。ただUpNextはこれから再生する曲だから
        //選択したindexで再生しようとするとUpNext内(次に再生する曲の)の何番目を選んでしまうことになりずれる
        try {
            PlayingSong = Manager.UpNextAdapter.getTitle(UpNextIndex);
            PlayingSongPath = Manager.UpNextAdapter.getPath(UpNextIndex);
            isNextSongAvailable = true;
        } catch (Exception e) {
            isNextSongAvailable = false;
        }
        if (isNextSongAvailable) {
            getUpNext();
            Play();
            UpNextIndex++;
        } else {
            if (Repeat == 1) {
                WaitforNextSong();
            } else if (Repeat == 0) {
                NoSongs();
            }
        }
    }

    public void PlayPreviousSong() {
        if (!PlayingSong.isEmpty()) {
            if (Player.getCurrentPosition() <= 500) {
                ResetPlayer();
                boolean isPreviousSongAvailable;
                try {
                    PlayingSong = Manager.UpNextAdapter.getTitle(UpNextIndex - 1);
                    isPreviousSongAvailable = true;
                } catch (Exception e) {
                    isPreviousSongAvailable = false;
                    NoSongs();
                }
                if (isPreviousSongAvailable) {
                    UpNextIndex--;
                    Play();
                }
            }
        } else {
            Player.seekTo(0);
        }
    }

    public void CreateUpNext(int PlayingIndex) {
        UpNextIndex = 0;
        Manager.UpNextAdapter.Clear();
        Manager.UpNextAdapter.notifyDataSetChanged();
        if (Shuffle) {
            //一時的に曲全体を別のリストに移して、そっから一つずつランダムに移動して、移動した奴を消せば競合による時間ロスは少ないのでは？
            if (PlayFrom == 1) {
                CreateUpNextforShuffle(Manager.SongAdapter, PlayingIndex);
            } else if (PlayFrom == 2) {
                //プレイリストとしての再生でも違うプレイリストからの再生の場合は作り直さなきゃいけない
                CreateUpNextforShuffle(Manager.PlayListContentAdapter, PlayingIndex);
            } else if (PlayFrom == 3) {
                CreateUpNextforShuffle(Manager.ACAdapter, PlayingIndex);
            } else if (PlayFrom == 4) {
                CreateUpNextforShuffle(Manager.ArtistC, PlayingIndex);
            }
        } else {
            if (PlayFrom == 1) {
                CreateUpNextbyOrder(Manager.SongAdapter, PlayingIndex);
            } else if (PlayFrom == 2) {
                CreateUpNextbyOrder(Manager.PlayListContentAdapter, PlayingIndex);
            } else if (PlayFrom == 3) {
                CreateUpNextbyOrder(Manager.ACAdapter, PlayingIndex);
            } else if (PlayFrom == 4) {
                CreateUpNextbyOrder(Manager.ArtistC, PlayingIndex);
            }
        }
        if (Manager.UpNextAdapter.getCount() > 0) {
            Manager.UpNextAdapter.CreateList(getApplicationContext());
        }
    }

    public void CreateUpNextforShuffle(ListViewAdapter adapter, int PlayingIndex) {
        if(adapter.getCount() > 1) {
            List<String> temporarylist = new ArrayList<>(), Cover = new ArrayList<>(), Path = new ArrayList<>();
            Random random = new Random();
            int temp1, index;
            for (int i = 0; i < adapter.getCount(); i++) {
                temporarylist.add(adapter.getTitle(i));
                Cover.add(adapter.getAlbumArt(i));
                Path.add(adapter.getPath(i));
            }
            //ここで今再生してる曲を削除するが、曲を選択してつくる場合ならindexでいいが、プレイリスト終わりで作り直す場合だとずれる
            temporarylist.remove(PlayingIndex);
            Cover.remove(PlayingIndex);
            Path.remove(PlayingIndex);
            temp1 = temporarylist.size();
            for (int i = 0; i < temp1; i++) {
                if (temporarylist.size() > 1) {
                    index = random.nextInt(temporarylist.size() - 1);
                } else {
                    index = 0;
                }
                Manager.UpNextAdapter.addTitle(temporarylist.get(index));
                Manager.UpNextAdapter.addAlbumArt(Cover.get(index));
                Manager.UpNextAdapter.addPath(Path.get(index));
                temporarylist.remove(temporarylist.get(index));
                Cover.remove(Cover.get(index));
                Path.remove(Path.get(index));
            }
        } else {
            Manager.UpNextAdapter.addTitle(adapter.getTitle(0));
            Manager.UpNextAdapter.addAlbumArt(adapter.getAlbumArt(0));
            Manager.UpNextAdapter.addPath(adapter.getPath(0));
        }
    }

    public void CreateUpNextbyOrder(ListViewAdapter adapter, int index) {
        if(adapter.getCount() > 1) {
            for (int i = (index + 1); i < adapter.getCount(); i++) {
                Manager.UpNextAdapter.addTitle(adapter.getTitle(i));
                Manager.UpNextAdapter.addAlbumArt(adapter.getAlbumArt(i));
                Manager.UpNextAdapter.addPath(adapter.getPath(i));
            }
        } else {
            Manager.UpNextAdapter.addTitle(adapter.getTitle(0));
            Manager.UpNextAdapter.addAlbumArt(adapter.getAlbumArt(0));
            Manager.UpNextAdapter.addPath(adapter.getPath(0));
        }
    }

    public void PlayComplete() {
        PlayNextSong();
    }

    public void NoSongs() {
        Playing = false;
        PlayingSong = "";
        PlayingSongPath = "";
        PlayingSongCover = null;
        Toast.makeText(this, getString(R.string.PlaylistEmpty), Toast.LENGTH_SHORT).show();
    }

    public void ResetPlayer() {
        Player.stop();
        Player.reset();
    }

    @Override
    public void onDestroy() {
        Player.stop();
        Player.release();
        if (PlayingSongCover != null) {
            PlayingSongCover.recycle();
        }
        Toast.makeText(this, "MediaPlayer is stopped...", Toast.LENGTH_SHORT).show();
    }
}