package com.ionix.voltagecontrol;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import android.util.Log;

public class Utils {
	public static String fileRead(String fname) {
		BufferedReader br;
		String line = null;
		StringBuilder sb = new StringBuilder();

		Log.d("ionix_vctrl", "Reading " + fname);

		try {
			br = new BufferedReader(new FileReader(fname));
			try {
				while (true) {
					line = br.readLine();
					if (null != line) {
						sb.append(line);
						sb.append(" ");
					} else {
						break;
					}
				}
			} finally {
				br.close();
			}
		} catch (Exception e) {
			Log.e("ionix_vctrl", "IO Exception when reading /sys/ file", e);
		}

		Log.e("ionix_vctrl", "Data: " + sb.toString());

		return sb.toString();
	}

	public static boolean fileWrite(String fname, String value) {
		try {
			FileWriter fw = new FileWriter(fname);
			try {
				fw.write(value);
			} finally {
				fw.close();
			}
		} catch (IOException e) {
			String Error = "Error writing to " + fname + ". Exception: ";
			Log.e("ionix_vctrl", Error, e);
			return false;
		}
		return true;
	}

}
