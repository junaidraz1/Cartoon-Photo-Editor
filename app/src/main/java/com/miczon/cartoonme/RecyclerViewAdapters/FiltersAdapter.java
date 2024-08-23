package com.miczon.cartoonme.RecyclerViewAdapters;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.makeramen.roundedimageview.RoundedImageView;
import com.miczon.cartoonme.Listeners.RecyclerViewClickListener;
import com.miczon.cartoonme.Manager.PrefsManager;
import com.miczon.cartoonme.R;

import java.util.ArrayList;

/**
 * @Copyright : Muhammad Junaid Raza
 * @Developer : Muhammad Junaid Raza
 */

public class FiltersAdapter extends RecyclerView.Adapter<FiltersAdapter.ViewHolder> {

    String TAG = "StyleAdapter";
    Activity activity;
    ArrayList<Integer> filterPreview;
    ArrayList<String> filterNames;
    RecyclerViewClickListener recyclerViewClickListener;
    int selectedItemPosition = -1;
    PrefsManager prefsManager;


    public FiltersAdapter(Activity activity, ArrayList<Integer> filterPreview, ArrayList<String> filterNames, RecyclerViewClickListener recyclerViewClickListener) {
        this.activity = activity;
        this.filterPreview = filterPreview;
        this.filterNames = filterNames;
        this.recyclerViewClickListener = recyclerViewClickListener;
    }

    @NonNull
    @Override
    public FiltersAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_style, parent, false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        prefsManager = new PrefsManager(holder.itemView.getContext());

        RequestOptions requestOptions = new RequestOptions()
                .placeholder(R.drawable.ic_loading)
//                .error(R.drawable.ic_imageload_failed)
                .centerCrop();

        Glide.with(holder.itemView.getContext())
                .load(filterPreview.get(holder.getAdapterPosition()))
                .apply(requestOptions)
                .timeout(60000)
                .into(holder.styleImage);

        if (!prefsManager.getIsPremium()) {
            if (holder.getAdapterPosition() > 5 && (holder.getAdapterPosition() % 6 == 0) && !prefsManager.getUnlockedItems().contains(holder.getAdapterPosition())) {
                holder.premiumLayout.setVisibility(View.VISIBLE);
                holder.ivPremium.setImageResource(R.drawable.ic_reward);

            } else if (holder.getAdapterPosition() > 4 && !prefsManager.getUnlockedItems().contains(holder.getAdapterPosition())) {
                holder.premiumLayout.setVisibility(View.VISIBLE);
                holder.ivPremium.setImageResource(R.drawable.ic_premium_icon);

            } else {
                holder.premiumLayout.setVisibility(View.GONE);
            }
        } else {
            holder.premiumLayout.setVisibility(View.GONE);
        }

        holder.tvName.setText(filterNames.get(holder.getAdapterPosition()));

        holder.selectFilterLayout.setOnClickListener(v -> {
            recyclerViewClickListener.itemClick(holder.getAdapterPosition(), "", "");

            handleItemSelection(holder.getAdapterPosition());
            holder.updateSelectedState(selectedItemPosition == holder.getAdapterPosition(), activity.getApplicationContext());
        });

        holder.updateSelectedState(selectedItemPosition == position, activity.getApplicationContext());
    }

    private void handleItemSelection(int position) {
        int previousSelectedItem = selectedItemPosition;
        selectedItemPosition = position;
        notifyItemChanged(previousSelectedItem);
        notifyItemChanged(selectedItemPosition);
    }

    public void clearSelection() {
        int previousSelectedItem = selectedItemPosition;
        selectedItemPosition = -1;

        notifyItemChanged(previousSelectedItem);
    }

    @Override
    public int getItemCount() {
        return filterPreview.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        RoundedImageView styleImage;
        TextView tvName;
        ImageView ivPremium;
        RelativeLayout selectFilterLayout, premiumLayout;
        ProgressBar progressBar;

        public ViewHolder(View itemView) {
            super(itemView);
            styleImage = itemView.findViewById(R.id.iv_filterThumbnail);
            premiumLayout = itemView.findViewById(R.id.rl_premium);
            ivPremium = itemView.findViewById(R.id.iv_premium);
            tvName = itemView.findViewById(R.id.tv_filterName);
            selectFilterLayout = itemView.findViewById(R.id.rl_filter);
            progressBar = itemView.findViewById(R.id.pb_progressBar);
        }

        public void updateSelectedState(boolean isSelected, Context context) {
            if (isSelected) {
                selectFilterLayout.setBackgroundTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.turquoise)));
                tvName.setTextColor(context.getResources().getColor(R.color.turquoise));

            } else {
                selectFilterLayout.setBackgroundTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.vDarkBlue)));
                tvName.setTextColor(context.getResources().getColor(R.color.white));
            }
        }
    }
}
