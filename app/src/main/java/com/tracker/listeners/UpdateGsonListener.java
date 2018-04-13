package com.tracker.listeners;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.tracker.network.VolleyExceptionUtil;

public class UpdateGsonListener<T> implements Listener<T>, ErrorListener {

    private final Context mContext;
    private int reqType;
    private onUpdateViewListener onUpdateViewListener;

    public UpdateGsonListener(Context context, onUpdateViewListener onUpdateView, int reqType) {
        this.reqType = reqType;
        this.onUpdateViewListener = onUpdateView;
        mContext = context.getApplicationContext();
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        onUpdateViewListener.updateView(VolleyExceptionUtil.getErrorMessage(error), false, reqType);
    }

    @Override
    public void onResponse(final Object responseObj) {
        if (mContext == null || onUpdateViewListener == null) {
            return;
        }
        try {
            onUpdateViewListener.updateView(responseObj, true, reqType);
        } catch (Exception ex) {
            ex.printStackTrace();
            onUpdateViewListener.updateView(responseObj, false, reqType);
        }
    }

}