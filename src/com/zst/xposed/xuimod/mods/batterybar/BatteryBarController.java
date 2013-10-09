package com.zst.xposed.xuimod.mods.batterybar;

import com.zst.xposed.xuimod.Common;

import de.robv.android.xposed.XSharedPreferences;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.BatteryManager;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class BatteryBarController extends LinearLayout {
    private static int LAST_BATTERY_LEVEL = 0;

    BatteryBarView mainBar;
    BatteryBarView alternateStyleBar;

    public static final int STYLE_REGULAR = 0;
    public static final int STYLE_SYMMETRIC = 1;

    int mStyle = STYLE_REGULAR;
    int mLocation = 0;

    protected final static int CURRENT_LOC = 1;
    int mLocationToLookFor = 1;

    private int mBatteryLevel = 0;
    private boolean mBatteryCharging = false;

    boolean isAttached = false;
    boolean isVertical = false;

    public BatteryBarController(Context context ) {
        this(context, null);
        mLocationToLookFor = 1;
    }
    public BatteryBarController(Context context, AttributeSet attrs) {
        super(context, attrs);
        mLocationToLookFor = 1;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!isAttached) {
            isVertical = (getLayoutParams().height == LayoutParams.MATCH_PARENT);

            isAttached = true;
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_BATTERY_CHANGED);
            getContext().registerReceiver(mIntentReceiver, filter);

            updateSettings();
        }
    }

    private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (Intent.ACTION_BATTERY_CHANGED.equals(action)) {
                mBatteryLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
                mBatteryCharging = intent.getIntExtra(BatteryManager.EXTRA_STATUS, 0) == BatteryManager.BATTERY_STATUS_CHARGING;
                LAST_BATTERY_LEVEL = mBatteryLevel;
            }
        }
    };

   

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (isAttached) {
            getHandler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    updateSettings();
                }
            }, 500);

        }
    }

    public void addBars() {
        DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
        XSharedPreferences pref = new XSharedPreferences(Common.MY_PACKAGE_NAME);
        String height =pref.getString(Common.KEY_BATTERYBAR_HEIGHT, Common.DEFAULT_BATTERYBAR_HEIGHT);
        		height= height.replaceAll( "[^\\d]", "" );//Remove all non-numeric characters.
        float dp = Integer.parseInt(height);
        int pixels = (int) ((metrics.density * dp) + 0.5);

        ViewGroup.LayoutParams params = (ViewGroup.LayoutParams) getLayoutParams();

        if (isVertical)
            params.width = pixels;
        else
            params.height = pixels;
        setLayoutParams(params);

        if (isVertical)
            params.width = pixels;
        else
            params.height = pixels;
        setLayoutParams(params);
        
        mBatteryLevel = LAST_BATTERY_LEVEL;
        if (mStyle == STYLE_REGULAR) {
        	this.addView(new BatteryBarView(this.getContext(), mBatteryCharging, mBatteryLevel, isVertical),
                    new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                            LayoutParams.MATCH_PARENT, 1));
        } else if (mStyle == STYLE_SYMMETRIC) {
        	BatteryBarView bar1 = new BatteryBarView(this.getContext(), mBatteryCharging, mBatteryLevel, isVertical);
        	BatteryBarView bar2 = new BatteryBarView(this.getContext(), mBatteryCharging, mBatteryLevel, isVertical);

            if (isVertical) {
                bar2.setRotation(180);
                addView(bar2, (new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                        LayoutParams.MATCH_PARENT, 1)));
                addView(bar1, (new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                        LayoutParams.MATCH_PARENT, 1)));
            } else {
                bar1.setRotation(180);
                addView(bar1, (new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                        LayoutParams.MATCH_PARENT, 1)));
                addView(bar2, (new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                        LayoutParams.MATCH_PARENT, 1)));
            }
        }
    }
    public void removeBars() {
        removeAllViews();
    }
    public void updateSettings() {
    	XSharedPreferences pref = new XSharedPreferences(Common.MY_PACKAGE_NAME);
    	mStyle = pref.getBoolean(Common.KEY_BATTERYBAR_STYLE, Common.DEFAULT_BATTERYBAR_STYLE) ? STYLE_SYMMETRIC : STYLE_REGULAR ;
        mLocation = 1;
        if (isLocationValid(mLocation)) {
            removeBars();
            addBars();
            setVisibility(View.VISIBLE);
        } else {
            removeBars();
            setVisibility(View.GONE);
        }
    }
    protected boolean isLocationValid(int location) {
        return mLocationToLookFor == location;
    }
}