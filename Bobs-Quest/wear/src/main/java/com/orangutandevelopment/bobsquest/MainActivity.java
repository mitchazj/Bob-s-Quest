package com.orangutandevelopment.bobsquest;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.BoxInsetLayout;
import android.support.wearable.view.DismissOverlayView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends WearableActivity {

    private BobView mBobView;
    private FrameLayout mGameOver;
    private ImageButton mNewGame;
    private CoolFontTextView mTxScore;

    private DismissOverlayView mDismissOverlay;
    private GestureDetector mDetector;

    private int TopScore = 0;
    public static final String PREFS_NAME = "BobsQuestPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBobView = (BobView) findViewById(R.id.bob_view);
        mGameOver = (FrameLayout) findViewById(R.id.overlay_view);
        mNewGame = (ImageButton) findViewById(R.id.btn_new);
        mTxScore = (CoolFontTextView) findViewById(R.id.tx_score);
        mDismissOverlay = (DismissOverlayView) findViewById(R.id.dismiss_overlay);

        TopScore = this.getSharedPreferences(PREFS_NAME, 0).getInt("Top_Score", 0);

        mDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            public void onLongPress(MotionEvent ev) {
                mDismissOverlay.show();
            }
        });

        mBobView.addListener(new OnGameFinishedListener() {
            @Override
            public void onEvent(int score) {
                if (score > TopScore)
                    TopScore = score;

                mTxScore.setText("Score: " + score + " | Top: " + TopScore);
                mGameOver.setVisibility(View.VISIBLE);
            }
        });

        mNewGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGameOver.setVisibility(View.GONE);
                mBobView.NewGame();
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        SharedPreferences settings = this.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("Top_Score", TopScore);
        editor.commit();
    }

    @Override
    public boolean dispatchTouchEvent (MotionEvent e) {
        return mDetector.onTouchEvent(e) || super.dispatchTouchEvent(e);
    }
}
