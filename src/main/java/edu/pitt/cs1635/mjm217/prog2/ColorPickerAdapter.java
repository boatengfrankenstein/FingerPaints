package edu.pitt.cs1635.mjm217.prog2;

import android.content.Context;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class ColorPickerAdapter extends BaseAdapter {
    private Context context;
    private DrawWidget drawSurface;
    private RelativeLayout drawLayout;
    private GridView paintSelect;
    private ImageView paintColor;
    private int width;
    public Integer[] colorsArray = {Color.BLACK, Color.RED, 0xFFFF6600, Color.YELLOW, Color.GREEN, Color.BLUE, 0xFFA020F0};

    public ColorPickerAdapter(Context context, DrawWidget drawSurface, RelativeLayout drawLayout, GridView paintSelect, ImageView paintColor, int width){
        this.context = context;
        this.drawSurface = drawSurface;
        this.drawLayout = drawLayout;
        this.paintSelect = paintSelect;
        this.paintColor = paintColor;
        this.width = width;
    }

    @Override
    public int getCount() {
        return colorsArray.length;
    }

    @Override
    public Object getItem(int position) {
        return colorsArray[position];
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View view, ViewGroup viewGroup) {
        ImageButton color = new ImageButton(context);
        color.setBackgroundColor(colorsArray[position]);
        color.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawSurface.setPaintColor(colorsArray[position]);
                Drawable colorIcon = context.getResources().getDrawable(R.drawable.coloricon);
                ColorFilter color = new LightingColorFilter(colorsArray[position], colorsArray[position]);
                colorIcon.setColorFilter(color);
                paintColor.setImageDrawable(colorIcon);
                drawLayout.removeView(paintSelect);
                drawSurface.invalidate();
            }
        });
        color.setLayoutParams(new GridView.LayoutParams(width/10, width/10));
        return color;
    }
}

