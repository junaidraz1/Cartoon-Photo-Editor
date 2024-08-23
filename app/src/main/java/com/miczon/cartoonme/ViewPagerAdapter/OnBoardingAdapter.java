package com.miczon.cartoonme.ViewPagerAdapter;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.miczon.cartoonme.R;

public class OnBoardingAdapter extends PagerAdapter {

    Context context;
    ImageView onBoardImage, onBoardAnimateImage;
    TextView onBoardTitle1, onBoardTitle2, onBoardDescription;

    int[] images = {R.drawable.on_board_bg_1, R.drawable.on_board_bg_2, R.drawable.on_board_bg_3};
    int[] title1 = {R.string.on_board1_title1, R.string.on_board2_title1, R.string.on_board3_title1};
    int[] title2 = {R.string.on_board1_title2, R.string.on_board2_title2, R.string.on_board3_title2};
    int[] description = {R.string.on_board1_description, R.string.on_board2_description, R.string.on_board3_description};
    int[] realImages = {R.drawable.on_board_animate_bg_1, R.drawable.on_board_animate_bg_2, R.drawable.on_board_animate_bg_3};

    public OnBoardingAdapter(Context context) {
        this.context = context;

    }

    @Override
    public int getCount() {
        return description.length;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {

        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.layout_on_board, container, false);

        onBoardImage = view.findViewById(R.id.iv_image);
        onBoardAnimateImage = view.findViewById(R.id.iv_animateImage);
        onBoardTitle1 = view.findViewById(R.id.tv_title1);
        onBoardTitle2 = view.findViewById(R.id.tv_title2);
        onBoardDescription = view.findViewById(R.id.tv_description);

        onBoardImage.setImageResource(images[position]);
        onBoardAnimateImage.setImageResource(realImages[position]);
        onBoardTitle1.setText(title1[position]);
        onBoardTitle2.setText(title2[position]);
        onBoardDescription.setText(description[position]);

        container.addView(view);

        return view;

    }

    public void startAnimation(int position) {
        if (position >= 0 && position < getCount()) {
            YoYo.with(Techniques.SlideInLeft)
                    .duration(800)
                    .repeat(0)
                    .playOn(onBoardAnimateImage);
        }
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((RelativeLayout) object);
    }
}
