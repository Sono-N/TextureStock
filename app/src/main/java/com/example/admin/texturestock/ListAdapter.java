package com.example.admin.texturestock;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Path;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import android.content.res.Resources;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

//public class ListAdapter extends BaseAdapter {
public class ListAdapter extends ArrayAdapter<File> {
    private LayoutInflater layoutInflater;
    private Context mContext;
    //private String name;
    private ArrayList<File> Paths;


    public ListAdapter(Context c, int id, ArrayList<File> paths) {
        super(c, id, paths);
        this.layoutInflater = (LayoutInflater) c.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE
        );
        mContext = c;
        Paths = paths;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = layoutInflater.inflate(
                    R.layout.list,
                    parent,
                    false
            );
        }
        //User user = (User) getItem(position);
        File file = Paths.get(position);
        String name = file.getName();

        //((ImageView) convertView.findViewById(R.id.icon)).setImageResource(R.drawable.karitop);
        Picasso.with(mContext).load(file).fit().centerCrop().into((ImageView) convertView.findViewById(R.id.icon));
        //((TextView) convertView.findViewById(R.id.name)).setText("Name");
        ((TextView) convertView.findViewById(R.id.comment))
                .setText(name);
        return convertView;
    }

    /*
    private Context mContext;

    private LayoutInflater inflater = null;
    private int itemlayoutId;
    private ArrayList<String> mPhotos;       // 画像のファイルパス


    static class ViewHolder {
        ImageView thumb;
        TextView name;
    }

    @Override
    public int getCount() {
        return mPhotos.size();
    }*/
    /*@Override
    public Object getItem(int position) {
        //return position;
        return null;
    }*/
    /*
    @Override
    public long getItemId(int position) {
        //return position;
        return 0;
    }

    public ListAdapter(Context context, int itemLayoutId, ArrayList<File> Paths){
        super(context, itemLayoutId, Paths);
        */
        //this.inflater = LayoutInflater.from(context);
        //this.inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        //this.itemlayoutId = itemLayoutId;
        //this.mContext = context;
        //this.context = context;
        /*ArrayList<String> filepaths = new ArrayList<String>();
        for(int i=0; i<Paths.length; i++) {
             filepaths.add(Paths[i].getAbsolutePath());
        }
        mPhotos =filepaths;*/
    //}

    // getViewメソッドをOverride
    //@Override
    //public View getView(int position, View convertView, ViewGroup parent) {
        // レイアウトを作成
        //convertView = inflater.inflate(R.layout.list ,parent,false);

        //ViewHolder holder;

       // if (convertView == null) {
            //convertView = inflater.inflate(layoutId, null);
            //convertView = inflater.inflate(R.layout.list, null);
         //   convertView = inflater.inflate(R.layout.list, parent, false);
            /*holder = new ViewHolder();
            holder.thumb = convertView.findViewById(R.id.texture_thumbnail);
            holder.name = convertView.findViewById(R.id.texture_name);
            convertView.setTag(holder);*/

        //}
        /*else{
            holder = (ViewHolder) convertView.getTag();
        }*/

        //((ImageView)convertView.findViewById(R.id.icon)).setImageResource(R.drawable.karitop);
        //((TextView)convertView.findViewById(R.id.name)).setText("testName");
        //((TextView)convertView.findViewById(R.id.comment)).setText("testComment");

        //Bitmap bmp1 = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.karitop);

        /*
        holder.thumb.setImageResource(R.drawable.karitop);
        holder.name.setText("test");
        */
        //Picasso.with(mContext).load( mPhotos.get(position)).fit().centerCrop().into(holder.thumb);
        //holder.name.setText(mPhotos.get(position).toString());

        //return convertView;
    //}
}
