package com.qboxus.musictok.ActivitesFragment.LiveStreaming;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.facebook.drawee.view.SimpleDraweeView;
import com.qboxus.musictok.ActivitesFragment.LiveStreaming.activities.LiveActivity;
import com.qboxus.musictok.Interfaces.AdapterClickListener;
import com.qboxus.musictok.R;
import com.qboxus.musictok.SimpleClasses.Variables;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class LiveUserViewAdapter extends RecyclerView.Adapter<LiveUserViewAdapter.CustomViewHolder> {

    ArrayList<LiveUserModel> dataList;
    AdapterClickListener adapterClickListener;

    public LiveUserViewAdapter(ArrayList<LiveUserModel> userDatalist, AdapterClickListener adapterClickListener) {
        this.dataList = userDatalist;
        this.adapterClickListener = adapterClickListener;

    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewtype) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_live_view_layout, null);
        return new CustomViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    class CustomViewHolder extends RecyclerView.ViewHolder {

        public TextView name;
        public CircleImageView image;

        public CustomViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.tvName);
            image = view.findViewById(R.id.ivProfile);

        }

        public void bind(final int pos, final LiveUserModel item,
                         final AdapterClickListener adapterClickListener) {
            itemView.setOnClickListener(v -> {
                adapterClickListener.onItemClick(v, pos, item);

            });

        }

    }

    @Override
    public void onBindViewHolder(final CustomViewHolder holder, final int i) {

        final LiveUserModel item = dataList.get(i);

        holder.bind(i, item, adapterClickListener);

        holder.name.setText(item.getUser_name()+ " "+holder.itemView.getContext().getString(R.string.joined));
        if (item.getUser_picture() != null && !item.getUser_picture().equals("")) {
            Uri uri = Uri.parse(item.getUser_picture());

            Glide.with(holder.itemView.getContext()).load(uri)
                    .error(R.drawable.ic_user_icon)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.image);
        }

    }

}