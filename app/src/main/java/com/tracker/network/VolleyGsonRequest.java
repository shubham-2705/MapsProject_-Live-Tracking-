package com.tracker.network;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.tracker.BuildConfig;
import com.tracker.listeners.UpdateGsonListener;
import com.tracker.utils.ShowLog;

import java.io.UnsupportedEncodingException;


public class VolleyGsonRequest<T> extends Request<T> {

    public static final String TAG = VolleyGsonRequest.class.getSimpleName();
    private final Context mContext;
    private final String requestBody;
    private final boolean encryptionRequired;
    private Gson mGson;
    private UpdateGsonListener<T> listener;
    private Class classz;
    private final String PROTOCOL_CHARSET = "utf-8";


    private VolleyGsonRequest(Context context, int httpMethod, String url, UpdateGsonListener<T> listener, Class classz, String requestBody,  boolean encryptionRequired) {
        super(httpMethod, url, listener);
        this.listener = listener;
        mGson = new Gson();
        mContext = context;
        this.classz = classz;
        this.requestBody = requestBody;
        this.encryptionRequired = encryptionRequired;
    }

    public static <T> VolleyGsonRequest processRequest(Context context,int httpMethod,  String url, UpdateGsonListener<T> updateListener, Class clasz, String requestBody, boolean encryptionRequired) {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, url + "");
        }

        if (httpMethod == NetworkEngine.NOT_SET) {
            httpMethod = TextUtils.isEmpty(requestBody) ? Method.GET : Method.POST;
        }

        return new VolleyGsonRequest(context,httpMethod, url, updateListener, clasz, requestBody,encryptionRequired);
    }


    public String getBodyContentType() {
        return "application/json";
    }

    @Override
    public byte[] getBody() {
        try {
            if (BuildConfig.DEBUG)
                ShowLog.e(TAG, requestBody + "");
            return requestBody == null ? null : requestBody.getBytes(PROTOCOL_CHARSET);
        } catch (UnsupportedEncodingException uee) {
            ShowLog.e(TAG, "Unsupported Encoding while trying to get the bytes of %s usingp %s" + "---requestBod---" + requestBody);
            return null;
        }
    }


//    @Override
//    public Map<String, String> getHeaders() throws AuthFailureError {
//
//        if (BuildConfig.DEBUG)
//            Log.e(TAG, paramsHeader.toString());
//        return paramsHeader;
//    }

    @Override
    protected Response parseNetworkResponse(NetworkResponse response) {
        try {
            String json = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
            if (BuildConfig.DEBUG)
                Log.d(TAG, "Response :: " + (response == null || response.data == null ? null : new String(response.data)));
            return Response.success(mGson.fromJson(json, classz), HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException ex) {
            ex.printStackTrace();
            return Response.error(new ParseError(ex));
        } catch (JsonSyntaxException ex) {
            ex.printStackTrace();
            return Response.error(new ParseError(ex));
        } catch (Exception e) {
            e.printStackTrace();
            return Response.error(new ParseError(e));
        }
    }

    @Override
    protected void deliverResponse(Object response) {
        if (listener != null) {
            listener.onResponse(response);
        }
    }

}