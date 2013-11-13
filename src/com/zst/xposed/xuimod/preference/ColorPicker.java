/*
 * Copyright (C) 2013 XuiMod
 * Copyright (C) 2012 The CyanogenMod Project
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zst.xposed.xuimod.preference;

import com.zst.xposed.xuimod.Common;
import com.zst.xposed.xuimod.R;
import com.zst.xposed.xuimod.preference.colorpicker.ColorSettingsDialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

public class ColorPicker extends Preference implements OnClickListener {
	
	SharedPreferences mPref;
	ImageView mColorBox;
	Resources mRes;
	String mDefaultColor;

	public ColorPicker(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		mDefaultColor = attrs.getAttributeValue(null, "defaultValue");
	}

	@Override
	protected void onBindView(View view) {
		super.onBindView(view);		
		mColorBox = (ImageView) view.findViewById(android.R.id.icon);
		mPref = getPreferenceManager().getSharedPreferences();
		mRes = getContext().getResources();
		view.setOnClickListener(this);
		refreshColorBox();
	}

	@Override
	public void onClick(View v) {
		final ColorSettingsDialog d = new ColorSettingsDialog(getContext(), getColor(), mDefaultColor);
		final DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which){
				case AlertDialog.BUTTON_POSITIVE:
					setColor(d.getColorString());
					break;
				case AlertDialog.BUTTON_NEUTRAL:
					setColor(mDefaultColor);
					break;
				}
				Common.settingsChanged(getContext());
			}
		};
		d.setButton(AlertDialog.BUTTON_POSITIVE, mRes.getString(android.R.string.ok), listener);
		d.setButton(AlertDialog.BUTTON_NEUTRAL, mRes.getString(R.string.settings_default), listener);
		d.setButton(AlertDialog.BUTTON_NEGATIVE, mRes.getString(android.R.string.cancel), listener);
		d.show();
	}
		
	public String getColorString() {
		return Integer.toHexString(getColor());
	}
	
	public int getColor() {
		String str = mPref.getString(getKey(), mDefaultColor);
		return Common.parseColorFromString(str, mDefaultColor);
	}
	
	private void refreshColorBox(){
		if (mColorBox != null) {
			mColorBox.setBackgroundColor(getColor());
			mColorBox.setVisibility(View.VISIBLE);
		}
	}
	public void setColor(String clr) {
		mPref.edit().putString(getKey(), clr).commit();
		refreshColorBox();
	}
}