package com.ionix.voltagecontrol;


public class ProcessorFrequency {
	private int m_freq;
	private int m_mv;
		
	public int getMv() {
		return m_mv;
	}

	public void setMv(int mv) {
		m_mv = mv;
	}

	public ProcessorFrequency(int freq, int mv) {
		m_freq = freq;
		m_mv = mv;
	}

	public int getFrequency() {
		return m_freq;
	}

	public void setFrequency(int freq) {
		m_freq = freq;
	}
}