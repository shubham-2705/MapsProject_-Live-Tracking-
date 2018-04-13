package com.tracker.network;

import java.io.IOException;
import java.net.SocketException;
import java.net.URISyntaxException;
import java.net.UnknownHostException;

import com.android.volley.NoConnectionError;
import com.android.volley.TimeoutError;
import com.google.gson.JsonSyntaxException;


public class VolleyExceptionUtil {


	/**
	 * @param pException
	 * @return
	 */
	public static String getErrorMessage(Exception pException) {
		if (pException instanceof URISyntaxException) {
			return "Request URL is not valid.";
		}
		if (pException instanceof UnknownHostException) {
			return "No host server found for requested URL.";
		}
		if (pException instanceof SocketException || pException instanceof TimeoutError  || pException instanceof JsonSyntaxException) {
			return "Communication Failure with server.\n Try Again.";
		}
		if (pException instanceof IOException || pException instanceof NoConnectionError) {
			return "Communication Failure with server.\n Please Check GPRS Settings in Handset.";
		}
		return "We are experiencing network problem.\nPlease Try Again later.";
	}
}