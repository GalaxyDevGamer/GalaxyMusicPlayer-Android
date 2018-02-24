package galaxysoftware.galaxymusicplayer_android;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.MediaStore;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Galaxy on 2017/01/27.
 */

public class Manager extends Application {
    SharedPreferences preference;
    SharedPreferences.Editor editor;
    ListViewAdapter SongAdapter = null, PlayListContentAdapter = null, PlayListAdapter = null, UpNextAdapter = null, AlbumAdapter = null, ACAdapter = null, ArtistAdapter = null, ArtistC = null;
    List<String> RecentlyPlayed = new ArrayList<>();
    DatabaseHelper dbHelper;
    SQLiteDatabase db;
    MusicService MS;

    String CurrentPlayList = "", CurrentAlbum = "", CurrentArtist = "";

    public Manager() {
        dbHelper = new DatabaseHelper(this);
    }

    public void LoadSongs() {
        String[] STAR = {"*"}; //Queryの2番目
        Cursor cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, STAR, MediaStore.Audio.Media.IS_MUSIC + " != 0", null, "title");
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    String song_title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                    String song_name = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
                    int song_id = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
                    String fullpath = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                    String album_name = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                    int album_id = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
                    String artist_name = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                    int artist_id = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST_ID));
                    String Cover = getCoverArtPath(cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)), this);
                    SongAdapter.addTitle(song_title);
                    SongAdapter.addArtist(artist_name);
                    SongAdapter.addAlbum(album_name);
                    SongAdapter.addAlbumArt(Cover);
                    SongAdapter.addPath(fullpath);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        if (SongAdapter.getCount() > 0) {
            SongAdapter.CreateList(this.getApplicationContext());
        }
        LoadPlaylists();
        LoadAlbums();
        LoadArtists();
    }

    public void LoadPlaylists() {
        PlayListAdapter.Clear();
        PlayListAdapter.notifyDataSetChanged();
        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name!='android_metadata' order by name", null);
        while (c.moveToNext()) {
            PlayListAdapter.addTitle(c.getString(0));
        }
        c.close();
        if (PlayListAdapter.getCount() > 0) {
            for (int i = 0; i < PlayListAdapter.getCount(); i++) {
                boolean isAvailable = false;
                c = db.rawQuery("SELECT ALBUM_ART FROM " + PlayListAdapter.getTitle(i) + " where id=1", null);
                while (c.moveToNext()) {
                    isAvailable = true;
                    PlayListAdapter.addAlbumArt(c.getString(0));
                }
                c.close();
                if (!isAvailable) {
                    PlayListAdapter.addAlbumArt(null);
                }
            }
            PlayListAdapter.CreateList(this.getApplicationContext());
        }
    }

    public void LoadAlbums() {
        Cursor cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, new String[]{"*"}, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    if (!AlbumAdapter.TitleContains(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)))) {
                        AlbumAdapter.addTitle(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)));
                        AlbumAdapter.addAlbumArt(getCoverArtPath(cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)), this));
                    }
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        if (AlbumAdapter.getCount() > 0) {
            AlbumAdapter.CreateList(this.getApplicationContext());
        }
    }

    public void LoadArtists() {
        Cursor cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, new String[]{"*"}, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    if (!ArtistAdapter.TitleContains(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)))) {
                        ArtistAdapter.addTitle(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)));
                        ArtistAdapter.addAlbumArt(getCoverArtPath(cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)), this));
                    }
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        if (ArtistAdapter.getCount() > 0) {
            ArtistAdapter.CreateList(this.getApplicationContext());
        }
    }

    public String getCoverArtPath(long albumId, Context context) {
        Cursor albumCursor = context.getContentResolver().query(
                MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Audio.Albums.ALBUM_ART},
                MediaStore.Audio.Albums._ID + " = ?",
                new String[]{Long.toString(albumId)},
                null
        );
        boolean queryResult = albumCursor.moveToFirst();
        String result = null;
        if (queryResult) {
            result = albumCursor.getString(0);
        }
        albumCursor.close();
        return result;
    }

    public String ApostropheToGraveAccent(String text) {
        return text.replace("'", "`");
    }

    public String GraveAccentToApostrophe(String text) {
        return text.replace("`", "'");
    }

    public void ShowUpNext() {
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < UpNextAdapter.getCount(); i++) {
            str.append(UpNextAdapter.getTitle(i) + "\n");
        }
        Toast.makeText(this, "Up Next songs:\n" + str.toString(), Toast.LENGTH_SHORT).show();
//        Toast.makeText(this, "Up Next size: " + UpNextAdapter.getCount() + "\n\nTemp1 size: " + temp1 + "\n\nAll: " + songList.size(), Toast.LENGTH_LONG).show();
    }
}