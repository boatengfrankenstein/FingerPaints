package edu.pitt.cs1635.mjm217.prog2;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;

import java.io.Serializable;
import java.util.ArrayList;

class SerializePaint extends Paint implements Serializable {
    public SerializePaint() {
        super();
    }
}

class Stroke implements Serializable {
    SerializePaint strokeColor = new SerializePaint();
    ArrayList<Line> lineStroke = new ArrayList<Line>();
    int paintColor;

    public Stroke(ArrayList<Line> lineArray) {
        for(Line line : lineArray) {
            Line tempLine = new Line(line.x1, line.x2, line.y1, line.y2);
            lineStroke.add(tempLine);
        }
    }
}

class Line implements Serializable {
    float x1, y1, x2, y2;

    public Line(float x1, float x2, float y1, float y2) {
        this.x1 = x1;
        this.x2 = x2;
        this.y1 = y1;
        this.y2 = y2;
    }
}

public class DrawWidget extends View {
    ArrayList<Line> lineList;
    ArrayList<Stroke> strokeList;
    Paint paint = new Paint();
    float x1, x2, y1, y2;

    public DrawWidget(Context context) {
        super(context);
        paint.setAntiAlias(true);
        paint.setStrokeWidth(20);
        paint.setStrokeCap(Paint.Cap.ROUND);
        lineList = new ArrayList<Line>();
        strokeList = new ArrayList<Stroke>();
        this.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        x1 = motionEvent.getX();
                        y1 = motionEvent.getY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        x2 = motionEvent.getX();
                        y2 = motionEvent.getY();
                        if(Math.abs(x2 - x1) > 10 || Math.abs(y2 - y1) > 10) {
                            Line line = new Line(x1, x2, y1, y2);
                            lineList.add(line);
                            x1 = x2;
                            y1 = y2;
                            invalidate();
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        Stroke stroke = new Stroke(lineList);
                        stroke.strokeColor.set(paint);
                        stroke.paintColor = paint.getColor();
                        strokeList.add(stroke);
                        lineList.clear();
                        break;
                }
                return true;
            }
        });
    }

    public void clear() {
        lineList.clear();
        strokeList.clear();
        invalidate();
    }

    public void undo() {
        if(!strokeList.isEmpty()) {
            strokeList.remove(strokeList.size() - 1);
            invalidate();
        }
    }

    public void setPaintColor(int paintColor) {
        paint.setColor(paintColor);
    }

    @Override
    public void onDraw(Canvas canvas) {
        for(Stroke stroke : strokeList) {
            stroke.strokeColor.setColor(stroke.paintColor);
            for(Line line : stroke.lineStroke) {
                canvas.drawLine(line.x1, line.y1, line.x2, line.y2, stroke.strokeColor);
            }
        }
        for(Line line : lineList) {
            canvas.drawLine(line.x1, line.y1, line.x2, line.y2, paint);
        }
    }
}
