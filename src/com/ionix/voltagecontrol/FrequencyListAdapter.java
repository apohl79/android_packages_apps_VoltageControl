package com.ionix.voltagecontrol;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

public class FrequencyListAdapter extends BaseExpandableListAdapter {

	private final VoltageControl voltageControl;
	
	private final int MIN_MV = 600;
	private final int MAX_MV = 1550;

	private Context context;

	ArrayList<ProcessorFrequency> mFqList = new ArrayList<ProcessorFrequency>();

	public FrequencyListAdapter(VoltageControl voltageControl, Context context) {
		this.voltageControl = voltageControl;
		this.context = context;
	}
	
	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return mFqList.get(groupPosition).getMv();
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return 1;
	}

	@Override
	public View getChildView(final int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {

		LinearLayout moduleLayout = (LinearLayout) LayoutInflater.from(
				this.voltageControl.getBaseContext()).inflate(R.layout.module, parent, false);
		final SeekBar seekBar;
		final TextView progressText;
		seekBar = (SeekBar) moduleLayout.findViewById(R.id.freqSB);
		progressText = (TextView) moduleLayout
				.findViewById(R.id.freq_voltageTxt);
		progressText.setText(Integer.toString(mFqList.get(groupPosition)
				.getMv()) + " mV");
		seekBar.setMax((MAX_MV - MIN_MV) / 25);
		seekBar.setProgress((mFqList.get(groupPosition).getMv() - MIN_MV) / 25);
		seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				progressText.setText(Integer.toString(progress * 25 + MIN_MV) + " mV");
				mFqList.get(groupPosition).setMv(progress * 25 + MIN_MV);
				voltageControl.activateApplyButton();
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
		});

		return moduleLayout;
	}

	@Override
	public Object getGroup(int groupPosition) {
		int frequency = mFqList.get(groupPosition).getFrequency();
		String frequency_s = String.valueOf(frequency);
		String uv = Integer.toString(mFqList.get(groupPosition).getMv());
		return frequency_s + " Mhz: " + uv + " mV";
	}

	@Override
	public int getGroupCount() {
		return mFqList.size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		String group = (String) getGroup(groupPosition);
		if (convertView == null) {
			LayoutInflater infalInflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = infalInflater.inflate(R.layout.group, null);
		}
		TextView tv = (TextView) convertView.findViewById(R.id.tvGroup);
		tv.setText(group);
		return convertView;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

	public void setFrequencies(ArrayList<ProcessorFrequency> mFqList) {
		this.mFqList = mFqList;
	}
}
