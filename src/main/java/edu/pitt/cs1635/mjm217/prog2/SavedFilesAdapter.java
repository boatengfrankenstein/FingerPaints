package edu.pitt.cs1635.mjm217.prog2;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

class IconView extends View {
    ArrayList<Stroke> strokeList = new ArrayList<Stroke>();
    Paint paint = new Paint();

    public IconView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setStrokes(ArrayList<Stroke> strokeList, int width) {
        this.strokeList.clear();
        paint.setAntiAlias(true);
        paint.setStrokeWidth(5);
        paint.setStrokeCap(Paint.Cap.ROUND);

        float scale = (float) width/(float) RecognizeView.width;
        for (Stroke stroke : strokeList) {
            ArrayList<Line> tempLineArray = new ArrayList<Line>();
            for (Line line : stroke.lineStroke) {
                Line tempLine = new Line(line.x1 * scale, line.x2 * scale, line.y1 * scale, line.y2 * scale);
                tempLineArray.add(tempLine);
            }
            Stroke tempStroke = new Stroke(tempLineArray);
            tempStroke.paintColor = stroke.paintColor;
            this.strokeList.add(tempStroke);
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        for(Stroke stroke : strokeList) {
            paint.setColor(stroke.paintColor);
            for(Line line : stroke.lineStroke) {
                canvas.drawLine(line.x1, line.y1, line.x2, line.y2, paint);
            }
        }
    }
}

public class SavedFilesAdapter extends ArrayAdapter {
    ArrayList<RecognizeView.DrawingSave> saveArray = new ArrayList<RecognizeView.DrawingSave>();
    Context context;
    int width;
    HashMap<Integer, Boolean> selectedMap = new HashMap<Integer, Boolean>();

    public SavedFilesAdapter(Context context, int width, ArrayList<RecognizeView.DrawingSave> saveArray){
        super(context, R.layout.save_row, saveArray);
        this.saveArray = saveArray;
        this.context = context;
        this.width = width;
    }

    @Override
    public int getCount() {
        return saveArray.size();
    }

    @Override
    public Object getItem(int position) {
        RecognizeView.DrawingSave drawingSave = saveArray.get(position);
        return drawingSave;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    private static class ViewHolder {
        IconView savedImage;
        TextView savedText;
    }

    @Override
    public View getView(final int position, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        final RecognizeView.DrawingSave savedDrawing = saveArray.get(position);

        if(view == null) {
            viewHolder = new ViewHolder();
            view = LayoutInflater.from(context).inflate(R.layout.save_row, null);
            viewHolder.savedImage = (IconView) view.findViewById(R.id.saved_icon);
            viewHolder.savedImage.setBackgroundColor(Color.WHITE);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, width);
            viewHolder.savedImage.setLayoutParams(params);
            viewHolder.savedText = (TextView) view.findViewById(R.id.saved_text);
            viewHolder.savedText.setGravity(Gravity.CENTER);
            view.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder) view.getTag();
        }

        if (selectedMap.get(position) != null) {
            view.setBackgroundColor(Color.GRAY);
        }
        else {
            view.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.list_selector));
        }

        viewHolder.savedImage.setStrokes(savedDrawing.savedStrokes, width);
        viewHolder.savedImage.invalidate();
        viewHolder.savedText.setText(savedDrawing.recognized);

        return view;
    }
    
    //Multi-choice selection/deletion
    public void setNewSelection(int position, boolean value) {
        selectedMap.put(position, value);
        notifyDataSetChanged();
    }

    public boolean isPositionChecked(int position) {
        Boolean result = selectedMap.get(position);
        return result == null ? false : result;
    }

    public Set<Integer> getCurrentCheckedPosition() {
        return selectedMap.keySet();
    }

    public void removeSelection(int position) {
        selectedMap.remove(position);
        notifyDataSetChanged();
    }

    public void clearSelection() {
        selectedMap = new HashMap<Integer, Boolean>();
        notifyDataSetChanged();
    }
}

