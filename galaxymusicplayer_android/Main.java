package galaxysoftware.galaxymusicplayer_android;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;

public class Main extends AppCompatActivity {

    Manager Manager;
    LinearLayout HomeContainer;
    Toolbar ToolbarTop;
    ListView lv;
    ImageButton PlayPause;
    TextView SongTitle;
    ImageView PLThumbnail, SCThumbnail;
    RelativeLayout SmallController, TopArea;
    List<Integer> TLIndex = new ArrayList<>();
    ArrayAdapter DeleteAdapter;
    Intent MusicService;

    int Layout, LibraryPos, PLPos, AlbumPos, ArtistPos, LibraryY, EditMode, REQUEST_CODE_READ_EXTERNAL_STORAGE = 0x01;
    boolean Editing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Manager = (Manager) getApplication();
        Manager.preference = getSharedPreferences("Setting", MODE_PRIVATE);
        Manager.editor = Manager.preference.edit();
        MusicService = new Intent(Main.this, MusicService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(MusicService);
        } else {
            startService(MusicService);
        }
        bindService(MusicService, MusicServiceConnection, Context.BIND_AUTO_CREATE);
        HomeContainer = findViewById(R.id.HomeContainer);
        SmallController = findViewById(R.id.SmallController);
        ToolbarTop = findViewById(R.id.toolbar_top);
        setSupportActionBar(ToolbarTop);
        ToolbarTop.setLogo(null);
        lv = findViewById(R.id.SongList);
        TopArea = findViewById(R.id.TopArea);
        Manager.db = Manager.dbHelper.getWritableDatabase();
        Manager.SongAdapter = new ListViewAdapter(R.layout.listitem);
        Manager.PlayListAdapter = new ListViewAdapter(R.layout.playlistitem);
        Manager.PlayListContentAdapter = new ListViewAdapter(R.layout.listitem);
        Manager.UpNextAdapter = new ListViewAdapter(R.layout.listitem);
        Manager.AlbumAdapter = new ListViewAdapter(R.layout.playlistitem);
        Manager.ACAdapter = new ListViewAdapter(R.layout.listitem);
        Manager.ArtistAdapter = new ListViewAdapter(R.layout.playlistitem);
        Manager.ArtistC = new ListViewAdapter(R.layout.listitem);
        BottomNavigationView NavigationView = findViewById(R.id.BottomNavigation);
        NavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.SongMenu:
                        SongsMenu();
                        return true;
                    case R.id.PlaylistMenu:
                        PlaylistsMenu();
                        return true;
                    case R.id.AlbumMenu:
                        AlbumMenu();
                        return true;
                    case R.id.ArtistMenu:
                        AlbumMenu();
                        return true;
                }
                return false;
            }
        });
        int permissionCheck = ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            // アクセス権がOFFならここでONにしてもらうようリクエストしてもらう
            boolean showRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, READ_EXTERNAL_STORAGE);
            if (showRationale) {
                // ダイアログを表示する場合。ここで一発アクセスする目的を説明してからリクエストするのが推奨されている。
                // 説明してOKならrequestPermissionsを呼ぶ。
                return;
            }
            //ダイアログを表示しない場合。ダイアログで「今後は確認しない」にチェックをした場合に実行される。
            ActivityCompat.requestPermissions(this, new String[]{READ_EXTERNAL_STORAGE}, REQUEST_CODE_READ_EXTERNAL_STORAGE);
        } else {
            // アクセス権がONならそのまま処理をすすめる
            Manager.LoadSongs();
            SongsMenu();
        }
    }

    public void UpdateSongInfo() {
        SongTitle.setText(Manager.MS.PlayingSong);
        if (Manager.MS.PlayingSong.isEmpty()) {
            SmallController.removeAllViews();
            PlayPause.setImageResource(R.mipmap.ic_play_circle_filled_black_48dp);
            SCThumbnail.setImageResource(R.mipmap.ic_music_video_black_48dp);
        } else {
            PlayPause.setImageResource(R.mipmap.ic_pause_circle_filled_black_48dp);
            SCThumbnail.setImageBitmap(Manager.MS.PlayingSongCover);
        }
    }

    public void InitializeSmallControler() {
        PlayPause = findViewById(R.id.PlayPause);
        SongTitle = findViewById(R.id.SongTitle);
        SCThumbnail = findViewById(R.id.SCThumbnail);
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
                    UpdateSongInfo();
                }
                Manager.MS.PlayPause(PlayPause);
            }
        });
        findViewById(R.id.FastForward).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Manager.MS.PlayNextSong();
                UpdateSongInfo();
            }
        });
        findViewById(R.id.Control).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (!Manager.MS.PlayingSong.isEmpty()) {
                    Intent intent = new Intent(Main.this, Player.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
                return false;
            }
        });
    }

    private void SetSongInfo(int position, int PlayFrom, ListViewAdapter adapter) {
        if (!Manager.MS.PlayingSong.isEmpty()) {
            Manager.MS.ResetPlayer();
        }
        Manager.MS.PlaySelectedSong(PlayFrom, adapter, position);
        if (Manager.MS.Playing) {
            if (!Manager.MS.PlayingSong.isEmpty()) {
                SmallController.removeAllViews();
                getLayoutInflater().inflate(R.layout.controllayout, SmallController);
                InitializeSmallControler();
            }
            PlayPause.setImageResource(R.mipmap.ic_pause_circle_filled_black_48dp);
            SongTitle.setText(Manager.MS.PlayingSong);
            SCThumbnail.setImageBitmap(Manager.MS.PlayingSongCover);
        }
    }

    public void ClearInfo(String current) {
        if (current.isEmpty()) {
            if (Layout == 2) {
                PLPos = lv.getFirstVisiblePosition();
            } else if (Layout == 3) {
                AlbumPos = lv.getFirstVisiblePosition();
            } else if (Layout == 4) {
                ArtistPos = lv.getFirstVisiblePosition();
            }
        }
        TopArea.removeAllViews();
    }

    private void SongsMenu() {
        if (Layout == 2) {
            ClearInfo(Manager.CurrentPlayList);
        }
        if (Layout == 3) {
            ClearInfo(Manager.CurrentAlbum);
        }
        if (Layout == 4) {
            ClearInfo(Manager.CurrentArtist);
        }
        if (Layout != 1) {
            Layout = 1;
            ToolbarTop.setNavigationIcon(null);
            ToolbarTop.setTitle(R.string.Songs);
            //Manager.SongAdapter.notifyDataSetChanged();
            View v = lv.getChildAt(0);
            LibraryY = (v == null) ? 0 : v.getTop();
            lv.setAdapter(Manager.SongAdapter);
            lv.setSelectionFromTop(LibraryPos, LibraryY);
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                    ListView listView = (ListView) parent;
//                    String item = (String) listView.getItemAtPosition(position);
                    SetSongInfo(position, 1, Manager.SongAdapter);
                }
            });
            lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int SongIndex, long l) {
                    String[] str = {getString(R.string.Play), getString(R.string.AddToPlaylist)};
                    new AlertDialog.Builder(Main.this).setItems(str, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int which) {
                            switch (which) {
                                case 0:
                                    SetSongInfo(SongIndex, 1, Manager.SongAdapter);
                                    break;
                                case 1:
                                    //追加先を選択
                                    String[] List = Manager.PlayListAdapter.Title.toArray(new String[Manager.PlayListAdapter.getCount()]);
                                    new AlertDialog.Builder(Main.this).setItems(List, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int which) {
                                            String Title = Manager.ApostropheToGraveAccent(Manager.SongAdapter.getTitle(SongIndex)), Artist = Manager.ApostropheToGraveAccent(Manager.SongAdapter.getArtist(SongIndex)), Album = Manager.ApostropheToGraveAccent(Manager.SongAdapter.getAlbum(SongIndex)), AlbumArt = Manager.ApostropheToGraveAccent(Manager.SongAdapter.getAlbumArt(SongIndex)), Path = Manager.ApostropheToGraveAccent(Manager.SongAdapter.getPath(SongIndex));
                                            int cnt = 0;
                                            Cursor c = Manager.db.rawQuery("SELECT count(*) FROM " + Manager.PlayListAdapter.getTitle(which) + " WHERE Title='" + Title + "' AND Artist='" + Artist + "' and Album='" + Album + "'", null);
                                            if (c.moveToNext()) {
                                                cnt = c.getInt(0);
                                            }
                                            c.close();
                                            if (cnt == 0) {
                                                try {
                                                    Manager.db.execSQL("insert into " + Manager.PlayListAdapter.getTitle(which) + " (Title,Artist,Album,ALBUM_ART,Path) values ('" + Title + "','" + Artist + "','" + Album + "','" + AlbumArt + "','" + Path + "')");
                                                    Toast.makeText(Main.this, R.string.AddSuccess, Toast.LENGTH_SHORT).show();
                                                } catch (Exception e) {
                                                    Toast.makeText(Main.this, "Add Failed\nErrorMsg: " + e.getMessage() + "\n\nPLName: " + Manager.CurrentPlayList + "\nTitle: " + Title + "\nArtist: " + Artist + "\nAlbum: " + Album + "\nAlbum Art: " + AlbumArt + "\nPath: " + Path, Toast.LENGTH_LONG).show();
                                                }
                                            } else {
                                                Toast.makeText(Main.this, R.string.SongsAlreadyInPlaylist, Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    }).show();
                                    break;
                            }
                        }
                    }).show();
                    return true;
                }
            });
            invalidateOptionsMenu();
        }
    }

    private void PlaylistsMenu() {
        if (Layout == 1) {
            LibraryPos = lv.getFirstVisiblePosition();
        }
        if (Layout == 3) {
            ClearInfo(Manager.CurrentAlbum);
        }
        if (Layout == 4) {
            ClearInfo(Manager.CurrentArtist);
        }
        if (Layout != 2) {
            Layout = 2;
            ToolbarTop.setTitle(R.string.Playlists);
            if (Manager.CurrentPlayList.isEmpty()) {
                SetPlaylistAdapter();
            } else {
                LoadPlayListContent();
            }
        }
    }

    private void SetPlaylistAdapter() {
        ToolbarTop.setNavigationIcon(null);
        TopArea.removeAllViews();
        getLayoutInflater().inflate(R.layout.newplaylist, TopArea);
        invalidateOptionsMenu();
        findViewById(R.id.NewPlaylistLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final EditText NameBox = new EditText(Main.this);
                new AlertDialog.Builder(Main.this).setTitle(R.string.PlayListName).setView(NameBox).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        int cnt = 0;
                        Cursor c = Manager.db.rawQuery("SELECT count(*) FROM sqlite_master where type='table' and name='" + NameBox.getText().toString() + "'", null);
                        while (c.moveToNext()) {
                            cnt = c.getInt(0);
                        }
                        c.close();
                        if (cnt == 0) {
                            try {
                                Manager.db.execSQL("CREATE TABLE " + NameBox.getText().toString() + " (id integer primary key, Title text, Artist text, Album text, ALBUM_ART text, Path text)");
                                Manager.CurrentPlayList = NameBox.getText().toString();
                                LoadPlayListContent();
                            } catch (Exception e) {
                                Toast.makeText(Main.this, "Error: " + e.getMessage() + "\n\nFailed to create Playlist: " + NameBox.getText().toString(), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(Main.this, R.string.NameExists, Toast.LENGTH_SHORT).show();
                        }
                    }
                }).setNegativeButton(R.string.Cancel, null).show();
            }
        });
        int cnt = 0;
        Cursor c = Manager.db.rawQuery("SELECT count(name) FROM sqlite_master WHERE type='table' AND name!='android_metadata' order by name", null);
        while (c.moveToNext()) {
            cnt = c.getInt(0);
        }
        c.close();
        if (cnt != Manager.PlayListAdapter.getCount()) {
            Manager.LoadPlaylists();
        }
        lv.setAdapter(Manager.PlayListAdapter);
        lv.setSelection(PLPos);
        //謎の脳内リンクで聴いてると2次元嫁と一緒にいる気分になれる曲
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Manager.CurrentPlayList = Manager.PlayListAdapter.getTitle(position);
                PLPos = lv.getFirstVisiblePosition();
                LoadPlayListContent();
            }
        });
        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int position, long l) {
                String[] str = {getString(R.string.Open), getString(R.string.RenamePlaylist), getString(R.string.DeletePlaylist)};
                new AlertDialog.Builder(Main.this).setItems(str, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        switch (which) {
                            case 0:
                                Manager.CurrentPlayList = Manager.PlayListAdapter.getTitle(position);
                                PLPos = lv.getFirstVisiblePosition();
                                LoadPlayListContent();
                                break;
                            case 1:
                                final EditText NameBox = new EditText(Main.this);
                                new AlertDialog.Builder(Main.this).setTitle(R.string.RenamePlaylist).setView(NameBox).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        int cnt = 0;
                                        Cursor c = Manager.db.rawQuery("SELECT count(*) FROM sqlite_master where type='table' and name='" + NameBox.getText().toString() + "'", null);
                                        while (c.moveToNext()) {
                                            cnt = c.getInt(0);
                                        }
                                        c.close();
                                        if (cnt == 0) {
                                            try {
                                                Manager.db.execSQL("alter table " + Manager.PlayListAdapter.getTitle(position) + " rename to " + NameBox.getText().toString());
                                                Manager.PlayListAdapter.Clear();
                                                SetPlaylistAdapter();
                                            } catch (Exception e) {
                                                Toast.makeText(Main.this, "Error: " + e.getMessage() + "\n\nFailed to Rename Playlist: " + NameBox.getText().toString(), Toast.LENGTH_SHORT).show();
                                            }
                                        } else {
                                            Toast.makeText(Main.this, R.string.NameExists, Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }).setNegativeButton(R.string.Cancel, null).show();
                                break;
                            case 2:
                                Manager.db.execSQL("drop table " + Manager.PlayListAdapter.getTitle(position));
                                SetPlaylistAdapter();
                                break;
                        }
                    }
                }).show();
                return true;
            }
        });
    }

    private void LoadPLSongs() {
        Manager.PlayListContentAdapter.Clear();
        Manager.PlayListContentAdapter.notifyDataSetChanged();
        Cursor c = Manager.db.rawQuery("SELECT Title, ALBUM_ART, Path FROM " + Manager.CurrentPlayList + " order by Title", null);
        while (c.moveToNext()) {
            Manager.PlayListContentAdapter.addTitle(Manager.GraveAccentToApostrophe(c.getString(0)));
            Manager.PlayListContentAdapter.addAlbumArt(Manager.GraveAccentToApostrophe(c.getString(1)));
            Manager.PlayListContentAdapter.addPath(Manager.GraveAccentToApostrophe(c.getString(2)));
        }
        c.close();
        if (Manager.PlayListContentAdapter.getCount() > 0) {
            Manager.PlayListContentAdapter.CreateList(this.getApplicationContext());
            PLThumbnail.setImageBitmap(BitmapFactory.decodeFile(Manager.PlayListContentAdapter.getAlbumArt(0)));
        } else {
            PLThumbnail.setImageResource(R.mipmap.ic_music_video_black_48dp);
        }
        lv.setAdapter(Manager.PlayListContentAdapter);
    }

    private void LoadPlayListContent() {
        TopArea.removeAllViews();
        getLayoutInflater().inflate(R.layout.playlistinfo, TopArea);
        TextView Name = findViewById(R.id.Name);
        Name.setText(Manager.CurrentPlayList);
        PLThumbnail = findViewById(R.id.PLThumbnail);
        ToolbarTop.setNavigationIcon(R.mipmap.ic_chevron_left_black_48dp);
        ToolbarTop.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TopArea.removeAllViews();
                Manager.CurrentPlayList = "";
                SetPlaylistAdapter();
            }
        });
        invalidateOptionsMenu();
        LoadPLSongs();
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!Editing) {
                    SetSongInfo(position, 2, Manager.PlayListContentAdapter);
                }
            }
        });
        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int i, long l) {
                if (!Editing) {
                    String[] str = {getString(R.string.Play), getString(R.string.DeleteFromPlaylist)};
                    new AlertDialog.Builder(Main.this).setItems(str, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int which) {
                            if (which == 0) {
                                SetSongInfo(i, 2, Manager.PlayListContentAdapter);
                            } else if (which == 1) {
                                Manager.db.execSQL("delete from " + Manager.CurrentPlayList + " where id=" + (i + 1));
                                LoadPLSongs();
                            }
                        }
                    }).show();
                }
                return true;
            }
        });
    }

    private void AlbumMenu() {
        if (Layout == 1) {
            LibraryPos = lv.getFirstVisiblePosition();
        }
        if (Layout == 2) {
            ClearInfo(Manager.CurrentPlayList);
        }
        if (Layout == 4) {
            ClearInfo(Manager.CurrentArtist);
        }
        if (Layout != 3) {
            Layout = 3;
            ToolbarTop.setTitle(R.string.Albums);
            invalidateOptionsMenu();
            if (Manager.CurrentAlbum.isEmpty()) {
                setAdapter(Manager.AlbumAdapter, AlbumPos);
            } else {
                setContent(Manager.CurrentAlbum, Manager.ACAdapter, "album");
            }
        }
    }

    private void ArtistMenu() {
        if (Layout == 1) {
            LibraryPos = lv.getFirstVisiblePosition();
        }
        if (Layout == 2) {
            ClearInfo(Manager.CurrentPlayList);
        }
        if (Layout == 3) {
            ClearInfo(Manager.CurrentAlbum);
        }
        if (Layout != 4) {
            Layout = 4;
            ToolbarTop.setTitle(R.string.Artists);
            invalidateOptionsMenu();
            if (Manager.CurrentArtist.isEmpty()) {
                setAdapter(Manager.ArtistAdapter, ArtistPos);
            } else {
                setContent(Manager.CurrentArtist, Manager.ArtistC, "artist");
            }
        }
    }

    private void setAdapter(final ListViewAdapter adapter, int pos) {
        ToolbarTop.setNavigationIcon(null);
        lv.setAdapter(adapter);
        lv.setSelection(pos);
        //謎の脳内リンクで聴いてると2次元嫁と一緒にいる気分になれる曲
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (Layout == 3) {
                    Manager.CurrentAlbum = Manager.AlbumAdapter.getTitle(position);
                    AlbumPos = lv.getFirstVisiblePosition();
                    setContent(Manager.CurrentAlbum, Manager.ACAdapter, "album");
                } else if (Layout == 4) {
                    Manager.CurrentArtist = Manager.ArtistAdapter.getTitle(position);
                    ArtistPos = lv.getFirstVisiblePosition();
                    setContent(Manager.CurrentArtist, Manager.ArtistC, "artist");
                }
            }
        });
    }

    private void setContent(String name, final ListViewAdapter adapter, String colname) {
        TopArea.removeAllViews();
        getLayoutInflater().inflate(R.layout.playlistinfo, TopArea);
        TextView Name = findViewById(R.id.Name);
        Name.setText(name);
        PLThumbnail = findViewById(R.id.PLThumbnail);
        ToolbarTop.setNavigationIcon(R.mipmap.ic_chevron_left_black_48dp);
        ToolbarTop.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TopArea.removeAllViews();
                if (Layout == 3) {
                    Manager.CurrentAlbum = "";
                    setAdapter(Manager.AlbumAdapter, AlbumPos);
                } else if (Layout == 4) {
                    Manager.CurrentArtist = "";
                    setAdapter(Manager.ArtistAdapter, ArtistPos);
                }
            }
        });
        adapter.Clear();
        adapter.notifyDataSetChanged();
        Cursor cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, new String[]{"*"}, colname + "='" + name + "'", null, "title");
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    adapter.addTitle(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)));
                    adapter.addAlbumArt(Manager.getCoverArtPath(cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)), this));
                    adapter.addPath(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        if (adapter.getCount() > 0) {
            adapter.CreateList(this.getApplicationContext());
            if (adapter.getAlbumArt(0) != null) {
                PLThumbnail.setImageBitmap(BitmapFactory.decodeFile(adapter.getAlbumArt(0)));
            } else {
                PLThumbnail.setImageResource(R.mipmap.ic_music_video_black_48dp);
            }
        } else {
            PLThumbnail.setImageResource(R.mipmap.ic_music_video_black_48dp);
        }
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SetSongInfo(position, Layout, adapter);
            }
        });
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        if (Layout == 2 && !Manager.CurrentPlayList.isEmpty()) {
            MenuInflater inflater = getMenuInflater();
            if (Editing) {
                inflater.inflate(R.menu.editer, menu);
            } else {
                inflater.inflate(R.menu.playlist, menu);
            }
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.editer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.Add:
                TLIndex.clear();
                List<String> PLC = new ArrayList<>();
                for (int i = 0; i < Manager.PlayListContentAdapter.getCount(); i++) {
                    PLC.add(Manager.PlayListContentAdapter.getTitle(i));
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_multiple_choice);
                for (int All = 0; All < Manager.SongAdapter.getCount(); All++) {
                    //全体からタイトルやIndexを追加するが、追加する物が既にPLCに存在する場合は、
                    // 「削除」ではなく、最初からその分は「追加しない」
                    boolean Exists = false;
                    if (PLC.size() > 0) {
                        for (int ii = 0; ii < PLC.size(); ii++) {
                            //PLCの要素を抜くために要素を検索し、あったら「一時的」PLC内の削除とスキップフラグを立てる
                            if (Manager.SongAdapter.getTitle(All).contains(PLC.get(ii))) {
                                PLC.remove(ii);
                                Exists = true;
                            }
                        }
                    }
                    if (!Exists) {
                        TLIndex.add(All);
                        adapter.add(Manager.SongAdapter.getTitle(All));
                    }
                }
//                    adapter = new SelectListAdapter(getApplicationContext(), R.layout.selectlistitem, TL, TLCover);
                lv.setAdapter(adapter);
                lv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
                EditMode = 1;
                Editing = true;
                invalidateOptionsMenu();
                break;
            case R.id.Delete:
                if (!Manager.CurrentPlayList.isEmpty() && Manager.PlayListContentAdapter.getCount() > 0) {
                    DeleteAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_multiple_choice);
                    for (int i = 0; i < Manager.PlayListContentAdapter.getCount(); i++) {
                        DeleteAdapter.add(Manager.PlayListContentAdapter.getTitle(i));
                    }
                    lv.setAdapter(DeleteAdapter);
                    lv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
                    EditMode = 2;
                    Editing = true;
                    invalidateOptionsMenu();
                }
                break;
            case R.id.Done:
//                ArrayList<CheckedTextView> checked = new ArrayList<CheckedTextView>();
//                View nextChild = null;
//// Find CheckBox inside the parent view
//                for (int i = 0; i < lv.getChildCount(); ++i) {
//                    nextChild = lv.getChildAt(i);
//                    if (nextChild instanceof CheckBox) {
//                        // Do your work;
//                        CheckBox cb = (CheckBox)lv.getChildAt(i);
//                        if(cb.isChecked()){
//                            checked.add(cb);
//                        }
//                        break;
//                    }
//                }
                if (lv.getCheckedItemCount() > 0) {
                    SparseBooleanArray array = lv.getCheckedItemPositions();
                    for (int i = 0; i < array.size(); i++) {
                        int at = array.keyAt(i);
                        if (array.get(at)) {
                            Log.d("example", "選択されている項目:" + lv.getItemAtPosition(at).toString() + " / そのキー" + at);
                            if (EditMode == 1) {
                                String Title = Manager.ApostropheToGraveAccent(Manager.SongAdapter.getTitle(TLIndex.get(at))), Artist = Manager.ApostropheToGraveAccent(Manager.SongAdapter.getArtist(TLIndex.get(at))), Album = Manager.ApostropheToGraveAccent(Manager.SongAdapter.getAlbum(TLIndex.get(at))), AlbumArt = Manager.ApostropheToGraveAccent(Manager.SongAdapter.getAlbumArt(TLIndex.get(at))), Path = Manager.ApostropheToGraveAccent(Manager.SongAdapter.getPath(TLIndex.get(at)));
                                try {
                                    Manager.db.execSQL("insert into " + Manager.CurrentPlayList + " (Title,Artist,Album,ALBUM_ART,Path) values ('" + Title + "','" + Artist + "','" + Album + "','" + AlbumArt + "','" + Path + "')");
                                } catch (Exception e) {
                                    Toast.makeText(this, "Add Failed\nErrorMsg: " + e.getMessage() + "\n\nPLName: " + Manager.CurrentPlayList + "\nTitle: " + Title + "\nArtist: " + Artist + "\nAlbum: " + Album + "\nAlbum Art: " + AlbumArt + "\nPath: " + Path, Toast.LENGTH_LONG).show();
                                }
                            } else {
                                //こっちもindexで消すように！そのindexはatではダメか？
                                Manager.db.execSQL("delete from " + Manager.CurrentPlayList + " where id=" + (at + 1));
                            }
                        }
                    }
                }
                lv.setChoiceMode(ListView.CHOICE_MODE_NONE);
                if (!Manager.CurrentPlayList.isEmpty()) {
                    LoadPLSongs();
                }
                Editing = false;
                invalidateOptionsMenu();
                break;
            case R.id.Cancel:
                lv.setChoiceMode(ListView.CHOICE_MODE_NONE);
                if (!Manager.CurrentPlayList.isEmpty()) {
                    LoadPLSongs();
                }
                Editing = false;
                invalidateOptionsMenu();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    ServiceConnection MusicServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            MusicService.MusicServiceBinder binder = (MusicService.MusicServiceBinder) iBinder;
            Manager.MS = binder.getService();
            try {
                Manager.MS.Player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        Manager.MS.PlayComplete();
                        UpdateSongInfo();
                    }
                });
            } catch (Exception e) {
                Toast.makeText(Main.this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
            Toast.makeText(Main.this, "Connected to Service", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Toast.makeText(Main.this, "Play on background Disabled", Toast.LENGTH_LONG).show();
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == REQUEST_CODE_READ_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 許可するを選択した場合の処理を実装。
                SongsMenu();
            } else {
                // 許可しないを選択した場合を実装
                finish();
            }
            return;
        }
    }

    @Override
    public void onBackPressed() {
        switch (Layout) {
            case 1:
                new AlertDialog.Builder(Main.this).setMessage(R.string.Exit)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // OK button pressed
                                Main.super.onBackPressed();
                            }
                        }).setNegativeButton("Cancel", null).show();
                break;
            case 2:
                if (Manager.CurrentPlayList.isEmpty()) {
                    SongsMenu();
                } else {
                    TopArea.removeAllViews();
                    Manager.CurrentPlayList = "";
                    SetPlaylistAdapter();
                }
                break;
            case 3:
                if (Manager.CurrentAlbum.isEmpty()) {
                    SongsMenu();
                } else {
                    TopArea.removeAllViews();
                    Manager.CurrentAlbum = "";
                    setAdapter(Manager.AlbumAdapter, AlbumPos);
                }
                break;
            case 4:
                if (Manager.CurrentArtist.isEmpty()) {
                    SongsMenu();
                } else {
                    TopArea.removeAllViews();
                    Manager.CurrentArtist = "";
                    setAdapter(Manager.ArtistAdapter, ArtistPos);
                }
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)) {
            int CurrentVolume = Manager.preference.getInt("Volume", 50) - 10;
            float volume = 0;
            if (CurrentVolume > 0) {
                volume = (Manager.preference.getInt("Volume", 50) - 10) / 100f;
            } else {
                CurrentVolume = 0;
            }
            Manager.MS.Player.setVolume(volume, volume);
            Manager.editor.putInt("Volume", CurrentVolume).apply();
            return true;
        } else if ((keyCode == KeyEvent.KEYCODE_VOLUME_UP)) {
            int CurrentVolume = Manager.preference.getInt("Volume", 50) + 10;
            float volume;
            if (CurrentVolume < 100) {
                volume = (Manager.preference.getInt("Volume", 50) + 10) / 100f;
            } else {
                CurrentVolume = 100;
                volume = 100;
            }
            Manager.MS.Player.setVolume(volume, volume);
            Manager.editor.putInt("Volume", CurrentVolume).apply();
            return true;
        } else
            return super.onKeyDown(keyCode, event);
    }

    public void onResume() {
        super.onResume();
        if (SongTitle != null) {
            SongTitle.setText(Manager.MS.PlayingSong);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        unbindService(MusicServiceConnection);
        stopService(MusicService);
        Manager.db.close();
        Manager.SongAdapter.Clear();
        Manager.PlayListAdapter.Clear();
        Manager.PlayListContentAdapter.Clear();
        Manager.UpNextAdapter.Clear();
        Manager.AlbumAdapter.Clear();
        Manager.ACAdapter.Clear();
        Manager.RecentlyPlayed.clear();
    }
}