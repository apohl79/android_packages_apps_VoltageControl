package com.ionix.voltagecontrol;

import java.util.ArrayList;

import android.app.ExpandableListActivity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ExpandableListAdapter;
import android.widget.Switch;

public class VoltageControl extends ExpandableListActivity {
	public static final String UV_TAB_FILE = "/sys/devices/system/cpu/cpu0/cpufreq/UV_mV_table";

	private ExpandableListAdapter frequencyAdapter;
	String max_freq;
	protected int max_freq_id;
	private ArrayList<ProcessorFrequency> mFqList = new ArrayList<ProcessorFrequency>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		frequencyAdapter = new FrequencyListAdapter(this,
				this.getApplicationContext());

		// Apply uv changes
		((Button) findViewById(R.id.apply_button))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						// let the kernel know
						String uvString = getUvString();
						Utils.fileWrite(UV_TAB_FILE, uvString);
						v.setEnabled(false);
						// update shared prefs for restore on/after boot
						SharedPreferences prefs = getSharedPreferences(
								"ionix_vctrl", Context.MODE_PRIVATE);
						Editor e = prefs.edit();
						e.putString("uv_string", uvString);
						e.commit();
					}
				});

		((Button) findViewById(R.id.restore_button))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						SharedPreferences prefs = getSharedPreferences(
								"ionix_vctrl", Context.MODE_PRIVATE);
						String uvValues = prefs.getString("uv_string", null);
						if (null != uvValues) {
							// restore uv values
							setUvString(uvValues);
							v.setEnabled(false);
							// update the list view
							getExpandableListView().invalidateViews();
							// activate the apply button
							((Button) findViewById(R.id.apply_button))
									.setEnabled(true);
						}
					}
				});

		// Apply on boot switch
		SharedPreferences prefs = getSharedPreferences("ionix_vctrl",
				Context.MODE_PRIVATE);
		boolean isApplyOnBoot = prefs.getInt("apply_on_boot", 0) == 1;
		Switch swBoot = (Switch) findViewById(R.id.switch_boot);
		if (isApplyOnBoot) {
			swBoot.setChecked(true);
		}
		swBoot.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				SharedPreferences prefs = getSharedPreferences("ionix_vctrl",
						Context.MODE_PRIVATE);
				int val = isChecked ? 1 : 0;
				Editor e = prefs.edit();
				e.putInt("apply_on_boot", val);
				e.commit();
			}
		});

		final Handler uIRefreshHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				((FrequencyListAdapter) frequencyAdapter)
						.setFrequencies(mFqList);
				setListAdapter(frequencyAdapter);
			}
		};

		new Thread(new Runnable() {
			public void run() {
				String uvFreqValues = Utils.fileRead(UV_TAB_FILE);
				String[] uv_table = uvFreqValues.split(" ");
				for (int i = 0; i < uv_table.length; i += 2) {
					Integer freq = Integer.parseInt(uv_table[i]);
					Integer mv = Integer.parseInt(uv_table[i + 1]);
					mFqList.add(new ProcessorFrequency(freq, mv));
				}
				SharedPreferences prefs = getSharedPreferences("ionix_vctrl",
						Context.MODE_PRIVATE);
				String uvValuesPrefs = prefs.getString("uv_string", null);
				if (null != uvValuesPrefs
						&& !uvValuesPrefs.equals(getUvString())) {
					// Stored settings are different from kernel settings, so
					// activate the restore button.
					((Button) findViewById(R.id.restore_button))
							.setEnabled(true);
				}
				uIRefreshHandler.sendEmptyMessage(0);
				return;
			}
		}).start();
	}

	public String getUvString() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < mFqList.size(); i++) {
			sb.append(mFqList.get(i).getMv() + " ");
		}
		return sb.toString();
	}

	public void setUvString(String uvValues) {
		String[] uvArray = uvValues.split(" ");
		if (mFqList.size() <= uvArray.length) {
			for (int i = 0; i < mFqList.size(); i++) {
				mFqList.get(i).setMv(Integer.parseInt(uvArray[i]));
			}
		}
	}

	public void activateApplyButton() {
		((Button) findViewById(R.id.apply_button)).setEnabled(true);
	}
}
