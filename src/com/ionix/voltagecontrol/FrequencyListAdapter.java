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

	private final VoltageControl m_vctrl;
	private Context m_ctx;
	private ArrayList<ProcessorFrequency> m_freqList = new ArrayList<ProcessorFrequency>();
	
	private int m_minMv = 600;
	private int m_maxMv = 1550;

	public FrequencyListAdapter(VoltageControl voltageControl, Context context) {
		this.m_vctrl = voltageControl;
		this.m_ctx = context;
	}
	
	public void setMinMv(int minMv) {
		m_minMv = minMv;
	}

	public void setMaxMv(int maxMv) {
		m_maxMv = maxMv;
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return m_freqList.get(groupPosition).getMv();
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
				this.m_vctrl.getBaseContext()).inflate(R.layout.module, parent, false);
		final SeekBar seekBar;
		final TextView progressText;
		seekBar = (SeekBar) moduleLayout.findViewById(R.id.freqSB);
		progressText = (TextView) moduleLayout
				.findViewById(R.id.freq_voltageTxt);
		progressText.setText(Integer.toString(m_freqList.get(groupPosition)
				.getMv()) + " mV");
		seekBar.setMax((m_maxMv - m_minMv) / 25);
		seekBar.setProgress((m_freqList.get(groupPosition).getMv() - m_minMv) / 25);
		seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				progressText.setText(Integer.toString(progress * 25 + m_minMv) + " mV");
				m_freqList.get(groupPosition).setMv(progress * 25 + m_minMv);
				m_vctrl.activateApplyButton();
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
		int frequency = m_freqList.get(groupPosition).getFrequency();
		String frequency_s = String.valueOf(frequency);
		String uv = Integer.toString(m_freqList.get(groupPosition).getMv());
		return frequency_s + " Mhz: " + uv + " mV";
	}

	@Override
	public int getGroupCount() {
		return m_freqList.size();
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
			LayoutInflater infalInflater = (LayoutInflater) m_ctx
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
		this.m_freqList = mFqList;
	}
}
