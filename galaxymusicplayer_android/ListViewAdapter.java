package galaxysoftware.galaxymusicplayer_android;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Galaxy on 2017/03/01.
 */

public class ListViewAdapter extends BaseAdapter {
    static class ViewHolder {
        TextView textView;
        ImageView imageView;
    }

    private LayoutInflater inflater;
    private int itemLayoutId;
    public List<String> Title, Artist, Album, thumb, Path;

    public ListViewAdapter(int itemLayoutId) {
        super();
        this.itemLayoutId = itemLayoutId;
        Title = new ArrayList<>();
        Artist = new ArrayList<>();
        Album = new ArrayList<>();
        thumb = new ArrayList<>();
        Path = new ArrayList<>();
    }

    public void CreateList(Context context) {
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        Title = songs; この2つは過去に引数でListを持ってきた物
//        thumb = covers;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        // 最初だけ View を inflate して、それを再利用する
        if (convertView == null) {
            // activity_main.xml に list.xml を inflate して convertView とする
            convertView = inflater.inflate(itemLayoutId, parent, false);
            // ViewHolder を生成
            holder = new ViewHolder();
            holder.textView = (TextView) convertView.findViewById(R.id.content);
            holder.imageView = (ImageView) convertView.findViewById(R.id.thumbnail);
            convertView.setTag(holder);
        }
        // holder を使って再利用
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        // holder の imageView にセット
        if (thumb.get(position) != null) {
            holder.imageView.setImageBitmap(BitmapFactory.decodeFile(thumb.get(position)));
        } else {
            holder.imageView.setImageResource(R.mipmap.ic_music_video_black_48dp);
        }
        //holder.imageView.setImageResource(ids[position]);
        // 現在の position にあるファイル名リストを holder の textView にセット
        holder.textView.setText(Title.get(position));

        return convertView;
    }

    @Override
    public int getCount() {
        // texts 配列の要素数
        return Title.size();
    }

    public void addTitle(String obj) {
        Title.add(obj);
    }

    public void addArtist(String artist) {
        Artist.add(artist);
    }

    public void addAlbum(String album) {
        Album.add(album);
    }

    public void addAlbumArt(String obj) {
        thumb.add(obj);
    }

    public void addPath(String path) {
        Path.add(path);
    }

    public String getTitle(int position) {
        return Title.get(position);
    }

    public String getArtist(int position) {
        return Artist.get(position);
    }

    public String getAlbum(int position) {
        return Album.get(position);
    }

    public String getAlbumArt(int position) {
        return thumb.get(position);
    }

    public String getPath(int position) {
        return Path.get(position);
    }

    public boolean TitleContains(String title) {
        return Title.contains(title);
    }

    public boolean AlbumArtContains(String Art) {
        return thumb.contains(Art);
    }

    public void Clear() {
        Title.clear();
        Artist.clear();
        Album.clear();
        thumb.clear();
        Path.clear();
    }

    public void Remove(int position) {
        Title.remove(position);
        Artist.remove(position);
        Album.remove(position);
        thumb.remove(position);
        Path.remove(position);
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }
}