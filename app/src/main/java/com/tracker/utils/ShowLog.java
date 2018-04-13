package com.tracker.utils;

import android.util.Log;

public class ShowLog {
	private static boolean isDebugOn = true;

	public static void e(String TAG, String msg) {

		if (isDebugOn) {
			if (TAG == null || msg == null) {
				return;
			}
			
			Log.e(TAG, msg);
			
		}
	}

	public static void v(String TAG, String msg) {
		if (isDebugOn) {
			if (TAG == null || msg == null) {
				return;
			}
			Log.v(TAG, msg);
		}
	}

	public static void d(String TAG, String msg) {
		if (isDebugOn) {
			if (TAG == null || msg == null) {
				return;
			}
			Log.d(TAG, msg);
		}
	}

	public static void i(String TAG, String msg) {
		if (isDebugOn) {
			if (TAG == null || msg == null) {
				return;
			}
			Log.i(TAG, msg);
		}
	}
}
