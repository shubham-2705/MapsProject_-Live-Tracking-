package com.tracker.utils;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;

import response.LatLongItem;

/**
 * Created by shubhamlamba on 09/02/18.
 */

public class LatLongDataManager {

    private static LatLongDataManager mInstance;
    private Context context;
    private ArrayList<LatLongItem> latLongItemsList;


    private LatLongDataManager() {
        // do nothing
    }

    public synchronized static LatLongDataManager getInstance(Context context) {
        if (mInstance == null)
            mInstance = new LatLongDataManager();
        mInstance.context = context.getApplicationContext();

        Gson gson = new Gson();
        mInstance.latLongItemsList = gson.fromJson(TrackerPreferences.getInstance(context).getString(ApiConstants.Preferenceconstants.USER_LAT_LONG_DATA), new TypeToken<ArrayList<LatLongItem>>() {
        }.getType());

        return mInstance;
    }


    public void clearData() {
        mInstance = null;
    }

    public ArrayList<LatLongItem> getLatLongItemsList() {
        return latLongItemsList;
    }

    public void setLatLongItemsList(ArrayList<LatLongItem> latLongItemsList) {
        this.latLongItemsList = latLongItemsList;
    }

}
