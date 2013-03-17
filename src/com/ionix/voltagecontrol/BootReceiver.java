package com.ionix.voltagecontrol;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class BootReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
			SharedPreferences prefs = context.getSharedPreferences(
					"ionix_vctrl", Context.MODE_PRIVATE);
			boolean isApplyOnBoot = prefs.getInt("apply_on_boot", 0) == 1;
			if (isApplyOnBoot) {
				String uvString = prefs.getString("uv_string", null);
				if (null != uvString) {
					Utils.fileWrite(VoltageControl.UV_TAB_FILE, uvString);
				}
			}
		}
	}
}
