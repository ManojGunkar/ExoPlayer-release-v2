package com.manoj.youtube.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.exoplayer2.demo.R;
import com.manoj.youtube.modal.YoutubeModal;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by Manoj on 11/01/2016.
 */
public class MyListAdapter extends BaseAdapter {

    private ArrayList<YoutubeModal.Item> list;
    private Context context;
    private LayoutInflater layoutInflater;
    private final int WIDTH;
    private final int HEIGHT;

    public MyListAdapter(Context context,ArrayList<YoutubeModal.Item> list){
        this.context=context;
        this.list=list;
        this.layoutInflater= (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        WIDTH = (int)dpToPx(128);
        HEIGHT = (int)dpToPx(72);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v=convertView;
        ViewHolder holder=null;
        if (v==null){
            v=layoutInflater.inflate(R.layout.item_list,parent,false);
            holder=new ViewHolder();
            holder.imageView= (ImageView) v.findViewById(R.id.img_icon_list);
            holder.txtTitle= (TextView) v.findViewById(R.id.txt_title_list);
            holder.txtDesc= (TextView) v.findViewById(R.id.txt_desc_list);
            v.setTag(holder);
        }else {
            holder= (ViewHolder) v.getTag();
        }

        String name=list.get(position).getSnippet().getTitle();
        String desc=null;
        if (list.get(position).getSnippet().getDescription()!=null){
            desc=list.get(position).getSnippet().getDescription();
        }else
            desc="";
        String url=list.get(position).getSnippet().getThumbnails().getMedium().getUrl();
        holder.txtTitle.setText(name);
        holder.txtDesc.setText(desc);

        Picasso.with(context).load(url)
                .resize(WIDTH, HEIGHT)
                .onlyScaleDown()
                .into(holder.imageView);

        return v;
    }

    static class ViewHolder{
        ImageView imageView;
        TextView txtTitle;
        TextView txtDesc;
    }

     static float dpToPx(float dp){
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return Math.round(px);
    }

}
