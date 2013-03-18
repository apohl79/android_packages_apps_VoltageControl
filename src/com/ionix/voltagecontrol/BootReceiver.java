package com.ionix.voltagecontrol;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.util.Log;

public class BootReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
			SharedPreferences prefs = context.getSharedPreferences(
					"ionix_vctrl", Context.MODE_PRIVATE);
			boolean isApplyOnBoot = prefs.getInt("apply_on_boot", 0) == 1;
			Log.d("ionix_vctrl", "BootReceiver called: isApplyOnBoot=" + isApplyOnBoot);
			if (isApplyOnBoot) {
				String uvString = prefs.getString("uv_string", null);
				Log.d("ionix_vctrl", "BootReceiver called: uvString=" + uvString);
				if (null != uvString) {
					Utils.fileWrite(VoltageControl.UV_TAB_FILE, uvString);
				}
			}
		}
	}
}
