package com.globaldelight.boom.app.share;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.globaldelight.boom.R;
import com.globaldelight.boom.app.adapters.utils.EqualizerDialogAdapter;
import com.globaldelight.boom.view.RegularTextView;

import java.util.List;

/**
 * Created by Manoj Kumar on 8/24/2017.
 */

public class ShareApater extends RecyclerView.Adapter<ShareApater.ViewHolder> {

    private List<ShareItem> itemList;
    private Context context;

    public ShareApater(Context context,List<ShareItem> itemList){
        this.itemList=itemList;
        this.context=context;
    }

    @Override
    public ShareApater.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.share_dailog, parent, false);
        final ShareApater.ViewHolder holder = new ShareApater.ViewHolder(itemView);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final int position = holder.getAdapterPosition();
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.setPackage(itemList.get(position).pkgName);
                sendIntent.putExtra(Intent.EXTRA_TEXT, "Boom Application share text!!!!!");
                sendIntent.setType("text/plain");
                context.startActivity(sendIntent);
            }
        });

        return holder;
    }


    @Override
    public void onBindViewHolder(ShareApater.ViewHolder holder, final int position) {
        holder.txtName.setText(itemList.get(position).text);
        holder.imgIcon.setImageDrawable(itemList.get(position).icon);

    }



    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        public RegularTextView txtName;
        public ImageView imgIcon;

        public ViewHolder(View itemView) {
            super(itemView);
            txtName = (RegularTextView) itemView.findViewById(R.id.txt_name);
            imgIcon = (ImageView) itemView.findViewById(R.id.img_icon);
        }
    }
}
