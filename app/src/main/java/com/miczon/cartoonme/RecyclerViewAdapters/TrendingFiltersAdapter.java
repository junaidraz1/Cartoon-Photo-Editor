package com.miczon.cartoonme.RecyclerViewAdapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.miczon.cartoonme.Listeners.RecyclerViewClickListener;
import com.miczon.cartoonme.Manager.PrefsManager;
import com.miczon.cartoonme.R;

import java.util.ArrayList;

public class TrendingFiltersAdapter extends RecyclerView.Adapter<TrendingFiltersAdapter.ImageViewHolder> {
    Context context;
    ArrayList<Integer> filterPreview;
    RecyclerViewClickListener recyclerViewClickListener;
    PrefsManager prefsManager;

    public TrendingFiltersAdapter(Context context, ArrayList<Integer> filterPreview, RecyclerViewClickListener recyclerViewClickListener) {
        this.context = context;
        this.filterPreview = filterPreview;
        this.recyclerViewClickListener = recyclerViewClickListener;
    }


    @NonNull
    @Override
    public TrendingFiltersAdapter.ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_rv_trending_filters, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TrendingFiltersAdapter.ImageViewHolder holder, int position) {
        prefsManager = new PrefsManager(holder.itemView.getContext());

        holder.ivFilterPreview.setImageResource(filterPreview.get(holder.getAdapterPosition()));

        holder.ivFilterPreview.setOnClickListener(v -> recyclerViewClickListener.itemClick(holder.getAdapterPosition(), "", ""));

        if (!prefsManager.getIsPremium()) {

            if (position == 0) {
                holder.ivPremium.setVisibility(View.GONE);

            } else {
                holder.ivPremium.setVisibility(View.VISIBLE);
                if (position == 2 || position == 14 || position == 10 || position == 13 || position == 15) {
                    holder.ivPremium.setImageResource(R.drawable.ic_reward);

                } else if (position == 1 || position == 3 || position == 5 || position == 6 ||
                        position == 7 || position == 8 || position == 9 || position == 11 ||
                        position == 12 || position == 4) {
                    holder.ivPremium.setImageResource(R.drawable.ic_premium_icon);
                } else {
                    holder.ivPremium.setVisibility(View.GONE);
                }
            }
        } else {
            holder.ivPremium.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return filterPreview.size();
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {

        ImageView ivFilterPreview, ivPremium;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);

            ivFilterPreview = itemView.findViewById(R.id.iv_trendingFilter);
            ivPremium = itemView.findViewById(R.id.iv_premium);

        }
    }
}
