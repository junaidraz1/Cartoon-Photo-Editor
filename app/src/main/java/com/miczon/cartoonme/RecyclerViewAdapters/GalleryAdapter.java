package com.miczon.cartoonme.RecyclerViewAdapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.miczon.cartoonme.Listeners.RecyclerViewClickListener;
import com.miczon.cartoonme.R;
import com.miczon.cartoonme.Utils.Utility;

import java.util.ArrayList;

/**
 * @Copyright : Muhammad Junaid Raza
 * @Developer : Muhammad Junaid Raza
 */

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.ImageViewHolder> {
    Context context;
    ArrayList<String> imagePaths;
    ArrayList<String> imageName;
    RecyclerViewClickListener recyclerViewClickListener;

    public GalleryAdapter(Context context, ArrayList<String> imagePaths, ArrayList<String> imageName, RecyclerViewClickListener recyclerViewClickListener) {
        this.context = context;
        this.imagePaths = imagePaths;
        this.imageName = imageName;
        this.recyclerViewClickListener = recyclerViewClickListener;
    }

    @NonNull
    @Override
    public GalleryAdapter.ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_rv_savedfile, parent, false);
        return new GalleryAdapter.ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GalleryAdapter.ImageViewHolder holder, int position) {
        String imagePath = imagePaths.get(holder.getAdapterPosition());
        if (imagePath != null && !imagePath.isEmpty()) {
            Uri contentUri = Utility.getInstance().getImageContentUri(context,imagePath);
            Glide.with(context)
                    .load(contentUri)
                    .into(holder.imageView);
        } else{
            Log.e("ADAPTER", "onBindViewHolder: image path is null");
        }

        holder.tvName.setText(imageName.get(holder.getAdapterPosition()));

        holder.imageView.setOnClickListener(v -> recyclerViewClickListener.itemClick(holder.getAdapterPosition(), imagePaths.get(holder.getAdapterPosition()), "view"));

        holder.menuIv.setOnClickListener(v -> {
            final PopupWindow popupWindow = new PopupWindow(context);
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View view = inflater.inflate(R.layout.layout_savedfile_menu, null);

            popupWindow.setFocusable(true);
            popupWindow.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
            popupWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
            popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            popupWindow.setContentView(view);
            int[] location = new int[2];
            holder.mainLayout.getLocationInWindow(location);

            LinearLayout deleteLayout = view.findViewById(R.id.ll_delete);
            LinearLayout shareLayout = view.findViewById(R.id.ll_share);
            LinearLayout renameLayout = view.findViewById(R.id.ll_rename);

            deleteLayout.setOnClickListener(v1 -> {
                recyclerViewClickListener.itemClick(holder.getAdapterPosition(), imagePaths.get(holder.getAdapterPosition()), "delete");
                popupWindow.dismiss();
            });

            shareLayout.setOnClickListener(v12 -> {
                recyclerViewClickListener.itemClick(holder.getAdapterPosition(), imagePaths.get(holder.getAdapterPosition()), "share");
                popupWindow.dismiss();
            });

            renameLayout.setOnClickListener(v13 -> {
                recyclerViewClickListener.itemClick(holder.getAdapterPosition(), imagePaths.get(holder.getAdapterPosition()), "rename");
                popupWindow.dismiss();
            });

            popupWindow.showAtLocation(holder.mainLayout, Gravity.NO_GRAVITY, location[0] + 20, location[1] + holder.menuLayout.getHeight() - 150);

        });

    }

    @Override
    public int getItemCount() {
        return imagePaths.size();
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView, menuIv;
        RelativeLayout menuLayout, mainLayout;
        TextView tvName;

        public ImageViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.iv_photo);
            tvName = itemView.findViewById(R.id.tv_imageName);
            menuLayout = itemView.findViewById(R.id.rl_menu);
            mainLayout = itemView.findViewById(R.id.rl_image);
            menuIv = itemView.findViewById(R.id.iv_menu);
        }
    }
}
