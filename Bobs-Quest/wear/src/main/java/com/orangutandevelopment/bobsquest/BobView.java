package com.orangutandevelopment.bobsquest;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

/**
 * Created by mitch on 5/10/2015.
 */
public class BobView extends View {
    public Bitmap Bob_Image;
    public Bitmap Background_Image;
    public Bitmap Star_Image;
    public Bitmap Cloud_Image;
    private Paint mPaint;
    private Typeface tf;

    final int refresh_interval = 40;
    private double delta_time = 0;
    private Date last_updated;
    private int times = 40;
    private Handler h;

    GameObject Bob = new GameObject();
    ArrayList<GameObject> walls = new ArrayList<>();
    ArrayList<GameObject> stars = new ArrayList<>();
    ArrayList<GameObject> clouds = new ArrayList<>();

    boolean game_started = false;
    boolean game_over = false;
    boolean bobbing = false; //Used to make Bob "bob" up and down before the game starts.
    Random r = new Random();

    ArrayList<OnGameFinishedListener> mListeners = new ArrayList<>();
    public void addListener(OnGameFinishedListener listener) {
        mListeners.add(listener);
    }

    int score = 0;

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

    public void NewGame() {
        times = 40;
        Bob.X = 95;
        Bob.Y = 140;
        walls.clear();
        score = 0;
        game_started = false;
        bobbing = true;
        game_over = false;
    }

    private void EndGame() {
        Vibrator v = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(100);
        Bob.Vertical_Speed = 0; //So that we fall straight away
        game_over = true;
    }

    public void init() {
        setWillNotDraw(false);

        //Grab resources
        Bob_Image = BitmapFactory.decodeResource(getResources(), R.drawable.bob_w);
        Background_Image = BitmapFactory.decodeResource(getResources(), R.drawable.bg);
        Cloud_Image = BitmapFactory.decodeResource(getResources(), R.drawable.cloud_t);
        Star_Image = BitmapFactory.decodeResource(getResources(), R.drawable.star_t);

        tf = Typeface.createFromAsset(getContext().getAssets(), "Munro.ttf");

        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(Color.BLACK);
        mPaint.setTypeface(tf);

        //Set up game elements
        Bob.X = 95;
        Bob.Y = 140;
        Bob.scaling = .25;

        for (int j = 0; j < 50; ++j) {
            GameObject s = new GameObject();
            s.Y = r.nextInt(320);
            s.X = r.nextInt(340);
            s.scaling = r.nextDouble() * .25;
            stars.add(s);
        }


        //Touch events
        this.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (!game_over) {
                    game_started = true;
                    Bob.Vertical_Speed = .3;
                }
                return false;
            }
        });

        //Game loop
        last_updated = new Date();
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
        //How long since last update?
        delta_time = (new Date()).getTime() - last_updated.getTime();
        ++times;

        if (!game_over) {
            //Add new elements
            if (times == 50) {
                times = 0;

                if (game_started) {
                    //Walls
                    GameObject w = new GameObject();
                    w.Y = r.nextInt(160) + 80;
                    w.X = 340;
                    walls.add(w);
                }

                //Clouds
                GameObject c = new GameObject();
                c.Y = r.nextInt(180) + 120;
                c.X = 340;
                c.scaling = r.nextDouble();
                clouds.add(c);
            }
            if (times == 10 || times == 20 || times == 30 || times == 40 || times == 50 || times == 0) {
                //Stars
                GameObject s = new GameObject();
                s.Y = r.nextInt(320);
                s.X = 340;
                s.scaling = r.nextDouble() * .25;
                stars.add(s);

                if (!game_started)
                    bobbing = !bobbing;
            }

            //Bob physics
            RectF Bob_Space = getGameObjectRect(Bob, Bob_Image);
            if (game_started) {
                Bob.Y -= Bob.Vertical_Speed * delta_time;
                Bob.Vertical_Speed -= .00075 * delta_time;
                if (Bob.Y > 320 - Bob_Space.height())
                    Bob.Y = 320 - Bob_Space.height();
                if (Bob.Y < 0)
                    EndGame();
            } else {
                Bob.Y += bobbing ? .5 : -.5;
            }

            //Move current objects and remove old objects
            for (int j = 0; j < walls.size(); ++j) {
                walls.get(j).X -= 4;
                if (walls.get(j).X < -30)
                    walls.remove(j);
                if (Math.abs(walls.get(j).X - Bob.X) < 2)
                    ++score;
                else {
                    //Collisions!
                    if (game_started) {
                        RectF[] wall_space = getWallSpaceRect(walls.get(j));
                        for (RectF r : wall_space) {
                            if (RectF.intersects(r, Bob_Space)) {
                                EndGame();
                                break;
                            }
                        }
                    }
                }
            }
            for (int j = 0; j < stars.size(); ++j) {
                stars.get(j).X -= 1;
                if (stars.get(j).X < -30)
                    stars.remove(j);
            }
            for (int j = 0; j < clouds.size(); ++j) {
                clouds.get(j).X -= 2;
                if (clouds.get(j).X < -1 * clouds.get(j).scaling * Cloud_Image.getWidth())
                    clouds.remove(j);
            }
        } else {
            if (Bob.Y > 320 && Bob.X != -1) {
                Bob.X = -1;
                for (OnGameFinishedListener hl : mListeners)
                    hl.onEvent(score);
            } else if (Bob.X != -1){
                Bob.Y -= Bob.Vertical_Speed * delta_time;
                Bob.Vertical_Speed -= .00075 * delta_time;
            }
        }

        //Update UI!
        this.postInvalidate();
        last_updated = new Date();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mPaint.setColor(Color.argb(255, 0, 0, 0));

        //Background
        canvas.drawBitmap(Background_Image, new Rect(0, 0, Background_Image.getWidth(), Background_Image.getHeight()), new Rect(0, 0, canvas.getWidth(), canvas.getHeight()), mPaint);

        //Clouds and stars
        for (int j = 0; j < stars.size(); ++j) {
            drawGameObject(canvas, mPaint, stars.get(j), Star_Image);
        }
        for (int j = 0; j < clouds.size(); ++j) {
            drawGameObject(canvas, mPaint, clouds.get(j), Cloud_Image);
        }

        //Walls
        for (int j = 0; j < walls.size(); ++j) {
            GameObject w = walls.get(j);
            //Top half
            canvas.drawRect((float) w.X + 3, 0, (float) w.X + 14, (float) w.Y - 60, mPaint);
            mPaint.setColor(Color.argb(255, 230, 250, 252));
            canvas.drawRect((float) w.X, 0, (float) w.X + 2, (float) w.Y - 60, mPaint);
            canvas.drawRect((float)w.X + 15, 0, (float)w.X + 17, (float)w.Y - 60, mPaint);
            canvas.drawRect((float) w.X - 4, (float) w.Y - 60, (float) w.X + 21, (float) w.Y - 54, mPaint);
            mPaint.setColor(Color.argb(255, 0, 0, 0));

            //Bottom half
            canvas.drawRect((float) w.X, (float) w.Y + 60, (float) w.X + 17, 320, mPaint);
            mPaint.setColor(Color.argb(255, 230, 250, 252));
            canvas.drawRect((float) w.X, (float) w.Y + 60, (float) w.X + 2, 320, mPaint);
            canvas.drawRect((float) w.X + 15, (float)w.Y + 60, (float) w.X + 17, 320, mPaint);
            canvas.drawRect((float) w.X - 4, (float) w.Y + 60, (float) w.X + 21, (float) w.Y + 66, mPaint);
            mPaint.setColor(Color.argb(255, 0, 0, 0));
        }

        //Bob
        drawGameObject(canvas, mPaint, Bob, Bob_Image);

        //Ready?
        mPaint.setColor(Color.argb(255, 255, 255, 255));
        if (!game_started) {
            mPaint.setTextSize(32);
            drawCenterVertical(canvas, mPaint, "Ready?", (float) Bob.X + 45);
        } else {
            mPaint.setTextSize(48);
            drawCenterHorizontal(canvas, mPaint, "" + score, 10);
        }
    }

    private void drawGameObject(Canvas canvas, Paint paint, GameObject object, Bitmap picture) {
        canvas.drawBitmap(picture, new Rect(0, 0, picture.getWidth(), picture.getHeight()), getGameObjectRect(object, picture), paint);
    }

    private RectF getGameObjectRect(GameObject object, Bitmap picture) {
        return new RectF((float) object.X, (float) object.Y, (float) object.X + (float)(object.scaling * picture.getWidth()), (float) object.Y + (float)(object.scaling * picture.getHeight()));
    }

    private RectF[] getWallSpaceRect(GameObject wall) {
        RectF top = new RectF((float) wall.X, 0, (float) wall.X + 17, (float) wall.Y - 54);
        RectF bottom = new RectF((float) wall.X, (float)wall.Y + 60, (float) wall.X + 17, 320);
        return new RectF[] {top, bottom};
    }

    private void drawCenterHorizontal(Canvas canvas, Paint paint, String text, float y) {
        int cHeight = canvas.getClipBounds().height();
        int cWidth = canvas.getClipBounds().width();
        Rect r = new Rect();
        paint.setTextAlign(Paint.Align.LEFT);
        paint.getTextBounds(text, 0, text.length(), r);
        float x = cWidth / 2f - r.width() / 2f - r.left;
        y += paint.getTextSize();
        canvas.drawText(text, x, y, paint);
    }

    private void drawCenterVertical(Canvas canvas, Paint paint, String text, float x) {
        int cHeight = canvas.getClipBounds().height();
        Rect r = new Rect();
        paint.setTextAlign(Paint.Align.LEFT);
        paint.getTextBounds(text, 0, text.length(), r);
        x -= r.left;
        float y = cHeight / 2f + r.height() / 2f - r.bottom;
        canvas.drawText(text, x, y, paint);
    }
}
