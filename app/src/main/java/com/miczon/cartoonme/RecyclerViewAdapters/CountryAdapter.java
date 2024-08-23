package com.miczon.cartoonme.RecyclerViewAdapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.makeramen.roundedimageview.RoundedImageView;
import com.miczon.cartoonme.Listeners.RecyclerViewClickListener;
import com.miczon.cartoonme.Manager.PrefsManager;
import com.miczon.cartoonme.R;

import java.util.ArrayList;

/**
 * @Copyright : Muhammad Junaid Raza
 * @Developer : Muhammad Junaid Raza
 */

public class CountryAdapter extends RecyclerView.Adapter<CountryAdapter.ImageViewHolder> {
    public String TAG = "CountryAdapter";
    Context context;
    ArrayList<String> countries;
    ArrayList<Integer> countryFlags;
    RecyclerViewClickListener recyclerViewClickListener;
    int newSelectedPosition = 0, oldSelectedPosition;
    PrefsManager prefsManager;

    public CountryAdapter(Context context, ArrayList<String> countries, ArrayList<Integer> countryFlags, RecyclerViewClickListener recyclerViewClickListener) {
        this.context = context;
        this.countries = countries;
        this.countryFlags = countryFlags;
        this.recyclerViewClickListener = recyclerViewClickListener;
        prefsManager = new PrefsManager(context);
        oldSelectedPosition = prefsManager.getLangPos();
        newSelectedPosition = oldSelectedPosition;
    }

    // method for filtering our recyclerview items.
    public void filterList(ArrayList<String> filteredCountries, ArrayList<Integer> filteredCountryFlags) {
        this.countries = filteredCountries;
        this.countryFlags = filteredCountryFlags;

        for (String country : filteredCountries) {
            if (!country.equalsIgnoreCase(prefsManager.getSelectedLanguage())) {
                Log.e(TAG, "filterList: working");
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CountryAdapter.ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_rv_countries, parent, false);
        return new CountryAdapter.ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CountryAdapter.ImageViewHolder holder, int position) {
        Integer imagePath = countryFlags.get(position);
        Glide.with(context.getApplicationContext())
                .load(imagePath)
                .into(holder.imageView);

        holder.tvName.setText(countries.get(holder.getAdapterPosition()));

        boolean isSelected = position == newSelectedPosition;
        holder.rbSelectedCountry.setChecked(isSelected);

        holder.languageLayout.setOnClickListener(v -> {
            if (!isSelected) {
                int oldSelected = newSelectedPosition;
                newSelectedPosition = holder.getAdapterPosition();

                notifyItemChanged(oldSelected);
                notifyItemChanged(newSelectedPosition);

                if (countries != null && countries.size() > 0) {
                    recyclerViewClickListener.itemClick(newSelectedPosition, "", countries.get(newSelectedPosition));
                }


                notifyDataSetChanged(); // Notify data set changed to reflect the changes
            }
        });
    }

    @Override
    public int getItemCount() {
        return countries.size();
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        RoundedImageView imageView;
        TextView tvName;
        RelativeLayout languageLayout;
        RadioButton rbSelectedCountry;

        public ImageViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.iv_flag);
            tvName = itemView.findViewById(R.id.tv_CountryName);
            languageLayout = itemView.findViewById(R.id.rl_top);
            rbSelectedCountry = itemView.findViewById(R.id.rb_selectLanguage);
        }
    }
}