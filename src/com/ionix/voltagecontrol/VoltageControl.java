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

	private ExpandableListAdapter m_frequencyAdapter;
	private ArrayList<ProcessorFrequency> m_freqList = new ArrayList<ProcessorFrequency>();

	public static int MIN_MV = 0;
	public static int MAX_MV = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		m_frequencyAdapter = new FrequencyListAdapter(this,
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

		((Button) findViewById(R.id.plus_button))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						addMvAll(25);
					}
				});

		((Button) findViewById(R.id.minus_button))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						addMvAll(-25);
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
				((FrequencyListAdapter) m_frequencyAdapter)
						.setFrequencies(m_freqList);
				setListAdapter(m_frequencyAdapter);
			}
		};

		new Thread(new Runnable() {
			public void run() {
				String uvFreqValues = Utils.fileRead(UV_TAB_FILE);
				String[] uvTable = uvFreqValues.split(" ");
				// get min/max voltage values
				if (uvTable[0].startsWith("min:")
						&& uvTable[1].startsWith("max:")) {
					MIN_MV = Integer.parseInt(uvTable[0].substring(4));
					MAX_MV = Integer.parseInt(uvTable[1].substring(4));
				} else {
					Log.e("ionix_vctrl", "Error parsing " + UV_TAB_FILE
							+ ": min/max values expected in first line");
					return;
				}
				for (int i = 2; i < uvTable.length; i += 2) {
					Integer freq = Integer.parseInt(uvTable[i]);
					Integer mv = Integer.parseInt(uvTable[i + 1]);
					m_freqList.add(new ProcessorFrequency(freq, mv));
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
		for (int i = 0; i < m_freqList.size(); i++) {
			sb.append(m_freqList.get(i).getMv() + " ");
		}
		return sb.toString();
	}

	public void addMvAll(int mv) {
		boolean updated = false;
		for (int i = 0; i < m_freqList.size(); i++) {
			updated |= m_freqList.get(i).addMv(mv);
		}
		if (updated) {
			// update the list view
			getExpandableListView().invalidateViews();
			// activate the apply button
			((Button) findViewById(R.id.apply_button)).setEnabled(true);
		}
	}

	public void setUvString(String uvValues) {
		String[] uvArray = uvValues.split(" ");
		if (m_freqList.size() <= uvArray.length) {
			for (int i = 0; i < m_freqList.size(); i++) {
				m_freqList.get(i).setMv(Integer.parseInt(uvArray[i]));
			}
		}
	}

	public void activateApplyButton() {
		((Button) findViewById(R.id.apply_button)).setEnabled(true);
	}
}
