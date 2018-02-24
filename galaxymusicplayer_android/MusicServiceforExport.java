package galaxysoftware.galaxymusicplayer_android;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;

public class MusicServiceforExport extends Service {
    MediaPlayer Player;

    private final IBinder Binder = new MusicServiceBinder();

    public class MusicServiceBinder extends Binder {
        MusicServiceforExport getService() {
            return MusicServiceforExport.this;
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
        float Volume = 50 / 100f;
        Player.setVolume(Volume, Volume);
    }

    public void PlayFromFile(String FilePath){
        try {
            Player.setDataSource(FilePath);
            Player.prepare();
            Player.start();
        } catch (Exception e) {
            Toast.makeText(this, "Failed to play: " + FilePath + "\n\nError: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroy() {
        Player.stop();
        Player.release();
        Toast.makeText(this, "MediaPlayer is stopped...", Toast.LENGTH_SHORT).show();
    }
}