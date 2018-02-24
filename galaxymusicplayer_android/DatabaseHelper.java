package galaxysoftware.galaxymusicplayer_android;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Galaxy on 2017-09-28.
 */

public class DatabaseHelper extends SQLiteOpenHelper {
    public DatabaseHelper(Context context)
    {
        super(context,"PlayLists", null, 1);
    }
    public void onCreate(SQLiteDatabase db){
//        db.execSQL("Create table PlayList()");
//        db.execSQL("CREATE TABLE Library (id integer primary key, Title text, Artist text, Album text, ALBUM_ART text, Path text)");
//        db.execSQL("CREATE TABLE Albums (id integer primary key, Title text, ALBUM_ART text)");
//        db.execSQL("INSERT INTO JobList (Job) VALUES ('剣士')")
    }
    public void onUpgrade(SQLiteDatabase db, int OldVer, int NewVer){
        onCreate(db);
    }
}
