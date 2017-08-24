package com.globaldelight.boom.app.share;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.globaldelight.boom.R;
import com.globaldelight.boom.business.VideoAd;
import com.globaldelight.boom.view.RegularTextView;

import java.util.List;

import static com.globaldelight.boom.app.fragments.ShareFragment.ACTION_SHARE_FAILED;
import static com.globaldelight.boom.app.fragments.ShareFragment.ACTION_SHARE_SUCCESS;

/**
 * Created by Manoj Kumar on 8/24/2017.
 */

public class ShareAdapter extends RecyclerView.Adapter<ShareAdapter.ViewHolder> {

    public interface Callback {
        void onItemSelected();
    }

    private List<ShareItem> itemList;
    private Context context;
    private Callback callback;

    public ShareAdapter(Context context, List<ShareItem> itemList, Callback callback){
        this.itemList=itemList;
        this.context=context;
        this.callback = callback;
    }

    @Override
    public ShareAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.share_dailog, parent, false);
        final ShareAdapter.ViewHolder holder = new ShareAdapter.ViewHolder(itemView);
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
                callback.onItemSelected();
            }
        });

        return holder;
    }


    @Override
    public void onBindViewHolder(ShareAdapter.ViewHolder holder, final int position) {
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
