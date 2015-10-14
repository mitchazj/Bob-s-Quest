package com.orangutandevelopment.bobsquest;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

/**
 * Created by mitch on 5/10/2015.
 */
public class BobView extends View {
    public double BobX = 80;
    public double BobY = 0;
    public Bitmap Bob;
    Paint pImage;

    final int refresh_interval = 40;
    double delta_time = 0;
    Handler h;

    Date last_updated;
    double vertical_speed = 0;
    int times = 40;

    ArrayList<Bug> bugs = new ArrayList<>();
    Random r = new Random();

    public BobView(Context context) {
        super(context);
        init();
    }

    public BobView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BobView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public void init() {
        setWillNotDraw(false);
        Bob = BitmapFactory.decodeResource(getResources(), R.drawable.bob96x70);

        pImage = new Paint();
        pImage.setStyle(Paint.Style.STROKE);
        pImage.setColor(Color.BLACK);

        last_updated = new Date();

        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                vertical_speed = .3;
            }
        });

        h = new Handler();
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                Update();
                h.postDelayed(this, refresh_interval);
            }
        }, refresh_interval);
    }

    public void Update() {
        delta_time = (new Date()).getTime() - last_updated.getTime();

        ++times;
        if (times == 50) {
            times = 0;
            Bug b = new Bug();
            b.Y = r.nextInt(160) + 80;
            b.X = 400;
            bugs.add(b);
        }

        for (int j = 0; j < bugs.size(); ++j) {
            bugs.get(j).X -= 4;
            if (bugs.get(j).X < -30)
                bugs.remove(j);
        }

        BobY -= vertical_speed * delta_time;
        vertical_speed -= .00075 * delta_time;

        if (BobY > 272)
            BobY = 272;

        this.postInvalidate();
        last_updated = new Date();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        pImage.setStyle(Paint.Style.FILL);
        pImage.setColor(Color.argb(255, 220, 220, 220));
        canvas.drawRect(0, 0, 320, 320, pImage);
        pImage.setColor(Color.argb(255, 0, 0, 255));
        for (int j = 0; j < bugs.size(); ++j) {
            Bug b = bugs.get(j);
            canvas.drawRect(b.X, 0, b.X + 30, b.Y - 60, pImage);
            canvas.drawRect(b.X, b.Y + 60, b.X + 30, 320, pImage);
            //canvas.drawOval(b.X, b.Y, b.X + 30, b.Y + 30, pImage);
        }
        pImage.setColor(Color.argb(255, 0, 0, 0));
        canvas.drawBitmap(Bob, new Rect(0, 0, Bob.getWidth(), Bob.getHeight()), new RectF((float) BobX, (float) BobY, (float) BobX + 35, (float) BobY + 48), pImage);
        canvas.drawText("" + vertical_speed, 500, 10, pImage);
        canvas.drawText("" + delta_time, 500, 30, pImage);
        pImage.setStyle(Paint.Style.STROKE);
    }
}
