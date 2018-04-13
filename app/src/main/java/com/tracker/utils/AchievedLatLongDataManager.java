package com.tracker.utils;

import android.content.Context;
import android.location.Location;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.HashMap;

import response.LatLongItem;

/**
 * Created by shubhamlamba on 11/02/18.
 */

public class AchievedLatLongDataManager {

    private static AchievedLatLongDataManager mInstance;
    private Context context;
    private HashMap<Integer, Integer> achievedlocationHashMap = new HashMap<>();


    private AchievedLatLongDataManager() {
        // do nothing
    }

    public synchronized static AchievedLatLongDataManager getInstance(Context context) {
        if (mInstance == null)
            mInstance = new AchievedLatLongDataManager();
        mInstance.context = context.getApplicationContext();

        Gson gson = new Gson();
        mInstance.achievedlocationHashMap = gson.fromJson(TrackerPreferences.getInstance(context).getString(ApiConstants.Preferenceconstants.USER_ACHIEVED_LAT_LONG_DATA), new TypeToken<HashMap<Integer, Integer>>(){}.getType());

        return mInstance;
    }


    public void clearData() {
        mInstance = null;
    }

    public HashMap<Integer, Integer> getAchievedlocationObjectItems() {
        return achievedlocationHashMap;
    }

    public void setAchievedlocationObjectItems(HashMap<Integer, Integer> achievedlocationObjectItems) {
        this.achievedlocationHashMap = achievedlocationObjectItems;
    }

}
