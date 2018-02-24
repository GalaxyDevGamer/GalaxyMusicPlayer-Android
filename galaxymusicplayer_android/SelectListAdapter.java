package galaxysoftware.galaxymusicplayer_android;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Galaxy on 2017/03/01.
 */

public class SelectListAdapter extends BaseAdapter {
    static class ViewHolder {
        TextView textView;
        ImageView imageView;
        CheckBox checkBox;
    }

    private LayoutInflater inflater;
    private int itemLayoutId;
    List<String> titles, thumb;

    public SelectListAdapter(Context context, int itemLayoutId, List<String> scenes, List<String> photos) {
        super();
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.itemLayoutId = itemLayoutId;
        this.titles = scenes;
        this.thumb = photos;
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
            holder.checkBox = (CheckBox)convertView.findViewById(R.id.checkbox);
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
        holder.textView.setText(titles.get(position));
        return convertView;
    }

    @Override
    public int getCount() {
        // texts 配列の要素数
        return titles.size();
    }
    public void Clear(){
        titles.clear();
        thumb.clear();
    }
    public void Remove(int position){
        titles.remove(position);
        thumb.remove(position);
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