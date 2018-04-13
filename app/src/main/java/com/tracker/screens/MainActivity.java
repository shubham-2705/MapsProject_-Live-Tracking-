package com.tracker.screens;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.google.android.gms.common.api.Api;
import com.google.gson.Gson;
import com.tracker.R;
import com.tracker.listeners.onUpdateViewListener;
import com.tracker.network.NetworkEngine;
import com.tracker.services.GPSService;
import com.tracker.services.TimerService;
import com.tracker.utils.AchievedLatLongDataManager;
import com.tracker.utils.ApiConstants;
import com.tracker.utils.ConnectivityUtils;
import com.tracker.utils.LatLongDataManager;
import com.tracker.utils.PermissionUtil;
import com.tracker.utils.ToastUtil;
import com.tracker.utils.TrackerPreferences;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import response.BaseResponse;
import response.ConfirmUserResponse;
import response.CustomLocation;
import response.GetDataRequestModel;
import response.GetDataResponseModel;
import response.LatLongItem;
import response.SubmitDataRequestModel;

import static android.graphics.Typeface.BOLD;
import static android.graphics.Typeface.ITALIC;

public class MainActivity extends BaseActivity implements View.OnClickListener, onUpdateViewListener {

    TextView tvSpeed, tvUserName, targetLocation_tv, timer_tv, speed_warning_tv, pointsTv, totalpointsTv, km;
    Button btnStart, btnConfirm, btnDownload, btnScan, btnStop, btnSubmit;
    View registerBulb, confirmationBulb, downloadedBulb, submitBulb;
    ArrayList<LatLongItem> latLongItems = new ArrayList<>();
    ArrayList<LatLongItem> cachedLatLongItems = new ArrayList<>();
    ArrayList<CustomLocation> locationObjectItems = new ArrayList<>();
    HashMap<Integer, Integer> achievedlocationHashMap = new HashMap<>();
    public static final int targetLocationRadiusInMetres = 50;
    public static final int startLocationRadiusInMetres = 100;
    public static final int endLocationRadiusInMetres = 100;
    private Location lastLocation, startLocation, endLocation;
    private RelativeLayout rootLayout;
    private LinearLayout warningLayout, btnsLayout, displayLayout;
    private boolean isScreenGreen;
    private long currentTimeStamp = 0;
    private Typeface digitalFont;
    private Dialog dialog;
    private CountDownTimer speedCountDownTimer;
    private int updatedSpeed;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            EventBus.getDefault().register(this);
            digitalFont = Typeface.createFromAsset(getAssets(), "fonts/digital_7_mono.ttf");
        } catch (Exception e) {
            e.printStackTrace();
        }


        try {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

            getStartEndLocationsFromCache();

            if (achievedlocationHashMap.size() == 0) {
                getAchievedDataFromCache();
            }

            tvSpeed = (TextView) findViewById(R.id.tvSpeed);
            km = (TextView) findViewById(R.id.km);
            tvSpeed.setTypeface(digitalFont, BOLD);
            km.setTypeface(digitalFont, BOLD);
            tvUserName = (TextView) findViewById(R.id.tvUserName);
            targetLocation_tv = (TextView) findViewById(R.id.targetLocation_tv);
            speed_warning_tv = (TextView) findViewById(R.id.speed_warning_tv);
            timer_tv = (TextView) findViewById(R.id.timer_tv);
            pointsTv = (TextView) findViewById(R.id.pointsTv);
            totalpointsTv = (TextView) findViewById(R.id.totalpointsTv);
            btnStart = (Button) findViewById(R.id.btnStart);
            btnConfirm = (Button) findViewById(R.id.btnConfirm);
            btnDownload = (Button) findViewById(R.id.btnDownload);
            btnScan = (Button) findViewById(R.id.btnScan);
            btnStop = (Button) findViewById(R.id.btnStop);
            btnSubmit = (Button) findViewById(R.id.btnSubmit);
            registerBulb = findViewById(R.id.registerBulb);
            confirmationBulb = findViewById(R.id.confirmationBulb);
            downloadedBulb = findViewById(R.id.downloadedBulb);
            submitBulb = findViewById(R.id.submitBulb);
            rootLayout = (RelativeLayout) findViewById(R.id.rootLayout);
            warningLayout = (LinearLayout) findViewById(R.id.warningLayout);
            btnsLayout = (LinearLayout) findViewById(R.id.btnsLayout);
            displayLayout = (LinearLayout) findViewById(R.id.displayLayout);
            btnStart.setOnClickListener(this);
            btnConfirm.setOnClickListener(this);
            btnDownload.setOnClickListener(this);
            btnScan.setOnClickListener(this);
            btnStop.setOnClickListener(this);
            btnSubmit.setOnClickListener(this);

            if (!TextUtils.isEmpty(TrackerPreferences.getInstance(this).getString(ApiConstants.Preferenceconstants.USER_ID))) {
                registerBulb.setBackgroundColor(ContextCompat.getColor(this, R.color.green));
            }

            if (!TextUtils.isEmpty(TrackerPreferences.getInstance(this).getString(ApiConstants.Preferenceconstants.USER_NAME))) {
                tvUserName.setText(TrackerPreferences.getInstance(this).getString(ApiConstants.Preferenceconstants.USER_NAME));
            }

            if (TrackerPreferences.getInstance(this).getBoolean(ApiConstants.Preferenceconstants.USER_LOCATION_DATA_FLAG)) {
                downloadedBulb.setBackgroundColor(ContextCompat.getColor(this, R.color.green));
            }

            if (TrackerPreferences.getInstance(this).getBoolean(ApiConstants.Preferenceconstants.USER_CONFIRMATION_FLAG)) {
                confirmationBulb.setBackgroundColor(ContextCompat.getColor(this, R.color.green));
                btnConfirm.setVisibility(View.GONE);
            }

            if (TrackerPreferences.getInstance(this).getInteger(ApiConstants.Preferenceconstants.USER_TARGET_LOCATION_POINTS) > 0) {
                targetLocation_tv.setText("" + TrackerPreferences.getInstance(this).getInteger(ApiConstants.Preferenceconstants.USER_TARGET_LOCATION_POINTS));

            }
            if (TrackerPreferences.getInstance(this).getInteger(ApiConstants.Preferenceconstants.USER_SPEED_LIMIT_POINTS) > 0) {
                speed_warning_tv.setText("" + TrackerPreferences.getInstance(this).getInteger(ApiConstants.Preferenceconstants.USER_SPEED_LIMIT_POINTS));
            }

            if (Math.abs(TrackerPreferences.getInstance(this).getLong(ApiConstants.Preferenceconstants.USER_TIME_SPENT)) > 0) {
                timer_tv.setText("" + Math.abs(TrackerPreferences.getInstance(this).getLong(ApiConstants.Preferenceconstants.USER_TIME_SPENT)));
            }


            if (TrackerPreferences.getInstance(this).getBoolean(ApiConstants.Preferenceconstants.USER_START_CHALLENGE_FLAG)) {
                if (!TrackerPreferences.getInstance(this).getBoolean(ApiConstants.Preferenceconstants.USER_STOP_CHALLENGE)) {
                    if (!isMyServiceRunning(TimerService.class)) {
                        startService(new Intent(MainActivity.this, TimerService.class));
                    }
                }
                warningLayout.setVisibility(View.VISIBLE);
                btnScan.setVisibility(View.VISIBLE);
            }

            if (TrackerPreferences.getInstance(this).getBoolean(ApiConstants.Preferenceconstants.USER_STOP_CHALLENGE)) {

                btnStop.setVisibility(View.GONE);
                btnScan.setVisibility(View.GONE);
                btnSubmit.setVisibility(View.VISIBLE);
                warningLayout.setVisibility(View.VISIBLE);
                totalpointsTv.setVisibility(View.VISIBLE);

                totalpointsTv.setText("Total Points are: " + TrackerPreferences.getInstance(this).getString(ApiConstants.Preferenceconstants.USER_TOTAL_POINTS));

                if (TrackerPreferences.getInstance(this).getBoolean(ApiConstants.Preferenceconstants.USER_SUBMIT_CHALLENGE)) {
                    submitBulb.setBackgroundColor(ContextCompat.getColor(this, R.color.green));
                    btnSubmit.setVisibility(View.GONE);
                    warningLayout.setVisibility(View.GONE);
                    totalpointsTv.setVisibility(View.GONE);
                }

            }

            PermissionUtil.with(MainActivity.this).setCallback(new PermissionUtil.PermissionGrantedListener() {
                @Override
                public void onPermissionResult(boolean isGranted, int requestCode) {
                    if (isGranted) {
                        if (ConnectivityUtils.isGpsEnabled(MainActivity.this)) {
                            if (!isMyServiceRunning(GPSService.class)) {
                                startService(new Intent(MainActivity.this, GPSService.class));
                            }
                        } else {
                            EnableGPSAutoMatically();
                        }
                    }
                }
            }).validate(Manifest.permission.ACCESS_FINE_LOCATION);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {

    }

    @Subscribe
    public void gotTimerCountdown(Long timeSpent) {
        try {

//            long startTime = TrackerPreferences.getInstance(this).getLong(ApiConstants.Preferenceconstants.USER_START_TIME);
//
//            if (startTime > 0) {
//                timeSpent = timeSpent - ((int)(getNow() - startTime));
//            }
            TrackerPreferences.getInstance(this).setLong(ApiConstants.Preferenceconstants.USER_TIME_SPENT, timeSpent);
            timer_tv.setText("" + Math.abs(timeSpent)); // time is coming in negative

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Subscribe
    public void gotlocation(Location location) {
        try {
            if (location != null && location.getLatitude() > 0 && location.getLongitude() > 0 && location.getTime() > 0
                    && location.getSpeed() > -1) {

                currentTimeStamp = location.getTime();
                Log.d("========***",""+getCurrentTimeStamp());

                if (TrackerPreferences.getInstance(this).getBoolean(ApiConstants.Preferenceconstants.USER_LOCATION_DATA_FLAG)
                        && TrackerPreferences.getInstance(this).getBoolean(ApiConstants.Preferenceconstants.USER_START_CHALLENGE_FLAG)) {

                    setSpeed(location);

                    if (locationObjectItems != null && locationObjectItems.size() > 0) {
                        if (achievedlocationHashMap == null || achievedlocationHashMap.size() == 0) {
                            getAchievedDataFromCache();
                        }
                        calculatedDistance(location);
                    } else {
                        getDataFromCache();
                        if (achievedlocationHashMap == null || achievedlocationHashMap.size() == 0) {
                            getAchievedDataFromCache();
                        }
                        calculatedDistance(location);
                    }

                }

                if (TrackerPreferences.getInstance(this).getBoolean(ApiConstants.Preferenceconstants.USER_CONFIRMATION_FLAG)) {
                    if (startLocation != null && endLocation != null) {
                        checkStartAndEndPoints(location);
                    } else {
                        getStartEndLocationsFromCache();
                        checkStartAndEndPoints(location);
                    }
                }

                lastLocation = location;
            } else {
                ToastUtil.showLongToast(this, "GPS not working properly. Please restart your location services!!");
            }
        } catch (Exception e) {
            lastLocation = location;
            e.printStackTrace();
        }
    }

    private void setSpeed(Location location) {
        try {

            // hasSpeed checks and last location calculation check
            int speed = 0;
            if (location.hasSpeed()) {
                speed = ((int) Math.ceil(location.getSpeed() * (18 / 5)));
            } else {
                if (lastLocation != null) {
                    speed = ((int) Math.ceil(lastLocation.getSpeed() * (18 / 5)));
                }
            }

            updatedSpeed = speed;
            if (!TextUtils.isEmpty(TrackerPreferences.getInstance(this).getString(ApiConstants.Preferenceconstants.USER_SPEED_LIMIT))) {
                if (updatedSpeed > (Integer.parseInt(TrackerPreferences.getInstance(this).getString(ApiConstants.Preferenceconstants.USER_SPEED_LIMIT)))) {
                    if (speedCountDownTimer == null) {
                        speedCountDownTimer = new CountDownTimer(10 * 1000, 1000) {
                            @Override
                            public void onTick(long millisUntilFinished) {

                                if (updatedSpeed <= (Integer.parseInt(TrackerPreferences.getInstance(MainActivity.this).getString(ApiConstants.Preferenceconstants.USER_SPEED_LIMIT)))) {
                                    speedCountDownTimer.cancel();
                                    speedCountDownTimer = null;
                                }
                            }

                            @Override
                            public void onFinish() {
                                speed_warning_tv.setText("" + (Integer.parseInt(speed_warning_tv.getText().toString()) + 20));
                                speedCountDownTimer = null;
                            }
                        }.start();
                    }

                }
            }
            tvSpeed.setText(("" + speed + ""));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

    private void calculatedDistance(Location location) {
        for (CustomLocation targetLocation : locationObjectItems) {

            float distance = targetLocation.distanceTo(location);

            if (distance <= targetLocationRadiusInMetres && achievedlocationHashMap.get(targetLocation.getLatLongId()) == 0) {
                achievedlocationHashMap.put(targetLocation.getLatLongId(), 1);


                runOnUiThread(new Runnable() {
                    public void run() {
                        // Update UI elements
                        final HashMap<Integer, Integer> localMap = new HashMap<>();
                        localMap.putAll(achievedlocationHashMap);
                        int locationCounts = 0;
                        Iterator it = localMap.entrySet().iterator();
                        while (it.hasNext()) {
                            Map.Entry pair = (Map.Entry) it.next();
                            if (((int) pair.getValue()) == 1) {
                                locationCounts++;
                            }
                            it.remove(); // avoids a ConcurrentModificationException
                        }

                        targetLocation_tv.setText("" + (locationCounts * 2000));

                    }
                });
            }
        }
    }

    private void checkStartAndEndPoints(Location location) {

        if (location.distanceTo(startLocation) <= startLocationRadiusInMetres) {
            if (!TrackerPreferences.getInstance(this).getBoolean(ApiConstants.Preferenceconstants.USER_LOCATION_DATA_FLAG)) {
                btnDownload.setVisibility(View.VISIBLE);
            }
            if (TrackerPreferences.getInstance(this).getBoolean(ApiConstants.Preferenceconstants.USER_LOCATION_DATA_FLAG) &&
                    !TrackerPreferences.getInstance(this).getBoolean(ApiConstants.Preferenceconstants.USER_START_CHALLENGE_FLAG)) {
                btnStart.setVisibility(View.VISIBLE);
            }
            if (TrackerPreferences.getInstance(this).getBoolean(ApiConstants.Preferenceconstants.USER_GREEN_SCREEN)) {
                rootLayout.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.green));
            }
        } else {
            TrackerPreferences.getInstance(this).setBoolean(ApiConstants.Preferenceconstants.USER_GREEN_SCREEN, false);
            rootLayout.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.white));
            btnDownload.setVisibility(View.GONE);
            btnStart.setVisibility(View.GONE);
        }


        if (location.distanceTo(endLocation) <= endLocationRadiusInMetres) {
            if (!TrackerPreferences.getInstance(this).getBoolean(ApiConstants.Preferenceconstants.USER_STOP_CHALLENGE)
                    && TrackerPreferences.getInstance(this).getBoolean(ApiConstants.Preferenceconstants.USER_START_CHALLENGE_FLAG)) {

                if (Integer.parseInt(targetLocation_tv.getText().toString()) > 0) {
                    btnStop.setVisibility(View.VISIBLE);
                }
            } else {
                btnStop.setVisibility(View.GONE);
            }
        } else {
            btnStop.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        try {
            AchievedLatLongDataManager.getInstance(this).setAchievedlocationObjectItems(achievedlocationHashMap);
            Gson gson = new Gson();
            String json = gson.toJson(achievedlocationHashMap);
            TrackerPreferences.getInstance(this).setString(ApiConstants.Preferenceconstants.USER_ACHIEVED_LAT_LONG_DATA, json);

            TrackerPreferences.getInstance(this).setInteger(ApiConstants.Preferenceconstants.USER_TARGET_LOCATION_POINTS, Integer.parseInt(targetLocation_tv.getText().toString().trim()));
            TrackerPreferences.getInstance(this).setInteger(ApiConstants.Preferenceconstants.USER_SPEED_LIMIT_POINTS, Integer.parseInt(speed_warning_tv.getText().toString().trim()));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onClick(View v) {

        int id = v.getId();

        switch (id) {
            case R.id.btnStart:
                if (!TrackerPreferences.getInstance(this).getBoolean(ApiConstants.Preferenceconstants.USER_START_CHALLENGE_FLAG)) {
                    if (TrackerPreferences.getInstance(this).getBoolean(ApiConstants.Preferenceconstants.USER_LOCATION_DATA_FLAG)) {
                        PermissionUtil.with(MainActivity.this).setCallback(new PermissionUtil.PermissionGrantedListener() {
                            @Override
                            public void onPermissionResult(boolean isGranted, int requestCode) {
                                if (isGranted) {
                                    // permission is granted
                                    if (ConnectivityUtils.isGpsEnabled(MainActivity.this)) {
                                        TrackerPreferences.getInstance(MainActivity.this).setBoolean(ApiConstants.Preferenceconstants.USER_GREEN_SCREEN, true);
                                        TrackerPreferences.getInstance(MainActivity.this).setBoolean(ApiConstants.Preferenceconstants.USER_START_CHALLENGE_FLAG, true);
                                        TrackerPreferences.getInstance(MainActivity.this).setString(ApiConstants.Preferenceconstants.USER_START_CHALLENGE_TIME, getCurrentTimeStamp());
                                        rootLayout.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.green));
                                        warningLayout.setVisibility(View.VISIBLE);
                                        btnScan.setVisibility(View.VISIBLE);
                                        btnStart.setVisibility(View.GONE);
                                        Toast.makeText(MainActivity.this, "Challenge Started", Toast.LENGTH_LONG).show();
                                        if (!isMyServiceRunning(GPSService.class)) {
                                            startService(new Intent(MainActivity.this, GPSService.class));
                                        }
                                        if (!isMyServiceRunning(TimerService.class)) {
                                            startService(new Intent(MainActivity.this, TimerService.class));
                                        }

                                    }
                                }
                            }
                        }).validate(Manifest.permission.ACCESS_FINE_LOCATION);
                    } else {
                        ToastUtil.showLongToast(this, "Please download the city data first!");
                    }
                } else {
                    ToastUtil.showLongToast(this, "You have already started!");
                }
                break;
            case R.id.btnConfirm:
                if (!TrackerPreferences.getInstance(this).getBoolean(ApiConstants.Preferenceconstants.USER_CONFIRMATION_FLAG)) {
                    PermissionUtil.with(MainActivity.this).setCallback(new PermissionUtil.PermissionGrantedListener() {
                        @Override
                        public void onPermissionResult(boolean isGranted, int requestCode) {
                            if (isGranted) {
                                // permission is granted
                                hitApiRequest(ApiConstants.REQUEST_TYPE.CONFIRM_USER);
                            }
                        }
                    }).validate(Manifest.permission.READ_PHONE_STATE);

                } else {
                    ToastUtil.showLongToast(this, "Already Confirmed from Event Desk. Please download Data.");
                }
                break;
            case R.id.btnDownload:
                if (TrackerPreferences.getInstance(this).getBoolean(ApiConstants.Preferenceconstants.USER_CONFIRMATION_FLAG)) {
                    PermissionUtil.with(MainActivity.this).setCallback(new PermissionUtil.PermissionGrantedListener() {
                        @Override
                        public void onPermissionResult(boolean isGranted, int requestCode) {
                            if (isGranted) {
                                // permission is granted
                                hitApiRequest(ApiConstants.REQUEST_TYPE.DOWNLOAD_DATA);
                            }
                        }
                    }).validate(Manifest.permission.READ_PHONE_STATE);

                } else {
                    ToastUtil.showLongToast(this, "First Confirm from Event Desk, then download Data.");
                }

                break;

            case R.id.btnScan:
                PermissionUtil.with(MainActivity.this).setCallback(new PermissionUtil.PermissionGrantedListener() {
                    @Override
                    public void onPermissionResult(boolean isGranted, int requestCode) {
                        if (isGranted) {
                            // permission is granted
                            startActivityForResult(new Intent(MainActivity.this, ScanQRActivity.class), 1000);
                        }
                    }
                }).validate(Manifest.permission.CAMERA);
                break;

            case R.id.btnStop:
                try {
                    TrackerPreferences.getInstance(this).setBoolean(ApiConstants.Preferenceconstants.USER_STOP_CHALLENGE, true);
                    TrackerPreferences.getInstance(this).setString(ApiConstants.Preferenceconstants.USER_STOP_CHALLENGE_TIME, getCurrentTimeStamp());
                    btnStop.setVisibility(View.GONE);
                    btnScan.setVisibility(View.GONE);
                    btnSubmit.setVisibility(View.VISIBLE);
                    totalpointsTv.setVisibility(View.VISIBLE);
                    displayLayout.setVisibility(View.GONE);
                    String totalPoints = "" + (Integer.parseInt(targetLocation_tv.getText().toString().trim()) -
                            Integer.parseInt(speed_warning_tv.getText().toString().trim()) - Integer.parseInt(timer_tv.getText().toString().trim()));
                    totalpointsTv.setText("Total Points are: " + totalPoints);

                    if (speedCountDownTimer != null) {
                        speedCountDownTimer.cancel();
                    }
                    TrackerPreferences.getInstance(this).setInteger(ApiConstants.Preferenceconstants.USER_TARGET_LOCATION_POINTS, Integer.parseInt(targetLocation_tv.getText().toString().trim()));
                    TrackerPreferences.getInstance(this).setInteger(ApiConstants.Preferenceconstants.USER_SPEED_LIMIT_POINTS, Integer.parseInt(speed_warning_tv.getText().toString().trim()));
                    TrackerPreferences.getInstance(this).setString(ApiConstants.Preferenceconstants.USER_TOTAL_POINTS, "" + totalPoints);

                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.btnSubmit:
                try {
                    if (ConnectivityUtils.isNetworkEnabled(this)) {
                        hitApiRequest(ApiConstants.REQUEST_TYPE.SUBMIT_DATA);
                    } else {
                        ToastUtil.showLongToast(this, "Please connect to network to Submit Data!");
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

        }
    }

    public void hitApiRequest(int reqType) {
        try {
            // register
            if (!ConnectivityUtils.isNetworkEnabled(this)) {
                ToastUtil.showShortToast(this, getString(R.string.error_network_not_available));
                return;
            }

            Class clasz = null;
            String url = "";
            showProgressdialog("Please wait...");

            switch (reqType) {
                case ApiConstants.REQUEST_TYPE.CONFIRM_USER:

                    clasz = ConfirmUserResponse.class;

                    // api request
                    url = ApiConstants.Urls.BASE_URL + "getConfirmStatus/" + getImeiNumber(this) + "/" + TrackerPreferences.getInstance(this).getString(ApiConstants.Preferenceconstants.USER_ID);
                    url = url.replace(" ", "%20");
                    Log.v("url-->> ", url);

                    NetworkEngine.with(this).setClassType(clasz).setUrl(url).setRequestType(reqType).setHttpMethodType(Request.Method.GET).setUpdateViewListener(this).build();
                    break;
                case ApiConstants.REQUEST_TYPE.DOWNLOAD_DATA:

                    clasz = GetDataResponseModel.class;

                    // api request
                    url = ApiConstants.Urls.BASE_URL + "get/latLong";
                    url = url.replace(" ", "%20");
                    Log.v("url-->> ", url);

                    GetDataRequestModel getDataRequestModel = new GetDataRequestModel();
                    getDataRequestModel.setImei(getImeiNumber(this));
                    getDataRequestModel.setUserId(Integer.valueOf(TrackerPreferences.getInstance(this).getString(ApiConstants.Preferenceconstants.USER_ID)));
//                    getDataRequestModel.setUserId(1);

                    NetworkEngine.with(this).setClassType(clasz).setUrl(url).setRequestType(reqType).setRequestModel(getDataRequestModel).setHttpMethodType(Request.Method.POST).setUpdateViewListener(this).build();
                    break;
                case ApiConstants.REQUEST_TYPE.SUBMIT_DATA:

                    clasz = BaseResponse.class;

                    // api request
                    url = ApiConstants.Urls.BASE_URL + "add/resultSheet";
                    url = url.replace(" ", "%20");
                    Log.v("url-->> ", url);

                    SubmitDataRequestModel submitDataRequestModel = new SubmitDataRequestModel();
                    submitDataRequestModel.setUserId(Integer.parseInt(TrackerPreferences.getInstance(this).getString(ApiConstants.Preferenceconstants.USER_ID)));
                    submitDataRequestModel.setCluesCovered(getAchivedLocationIdsToSubmit());
                    submitDataRequestModel.setSpeedPenality("" + TrackerPreferences.getInstance(this).getInteger(ApiConstants.Preferenceconstants.USER_SPEED_LIMIT_POINTS));
                    submitDataRequestModel.setTimeTaken("" + Math.abs(TrackerPreferences.getInstance(this).getLong(ApiConstants.Preferenceconstants.USER_TIME_SPENT)));
                    submitDataRequestModel.setAppTotalPoint(TrackerPreferences.getInstance(this).getString(ApiConstants.Preferenceconstants.USER_TOTAL_POINTS));
                    submitDataRequestModel.setEndTime(TrackerPreferences.getInstance(this).getString(ApiConstants.Preferenceconstants.USER_STOP_CHALLENGE_TIME));
                    submitDataRequestModel.setStartTime(TrackerPreferences.getInstance(this).getString(ApiConstants.Preferenceconstants.USER_START_CHALLENGE_TIME));

                    if (TrackerPreferences.getInstance(this).getBoolean(ApiConstants.Preferenceconstants.USER_APP_RESTART_FLAG)) {
                        submitDataRequestModel.setRestartFlag(1);
                        submitDataRequestModel.setRestartTimeStamp(TrackerPreferences.getInstance(this).getString(ApiConstants.Preferenceconstants.USER_APP_RESTART_TIME));
                    } else {
                        submitDataRequestModel.setRestartFlag(0);
                        submitDataRequestModel.setRestartTimeStamp("");
                    }


                    NetworkEngine.with(this).setClassType(clasz).setUrl(url).setRequestType(reqType).setRequestModel(submitDataRequestModel).setHttpMethodType(Request.Method.POST).setUpdateViewListener(this).build();
                    break;
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void updateView(Object responseObject, boolean isSuccess, int reqType) {

        try {
            hideProgressDialog();

            if (!isSuccess) {
                ToastUtil.showShortToast(this, getString(R.string.something_went_wrong));
//                buildAndComm.showOkDialog(UpiCreateVpaActivity.this, (String) responseObject);
            } else {
                switch (reqType) {
                    case ApiConstants.REQUEST_TYPE.CONFIRM_USER:
                        ConfirmUserResponse confirmUserResponse = (ConfirmUserResponse) responseObject;
                        if (confirmUserResponse.getStatusCode().equalsIgnoreCase("2000")) {

                            ArrayList<LatLongItem> startEndItems = confirmUserResponse.getLatLongDetails().getLatLongs();
                            for (LatLongItem startEndItem : startEndItems) {
                                if (startEndItem.getCoordinateType().equalsIgnoreCase("first")) {
                                    startLocation = new Location("");
                                    startLocation.setLatitude(Double.valueOf(startEndItem.getLatitude()));
                                    startLocation.setLongitude(Double.valueOf(startEndItem.getLongitude()));

                                    Gson gson = new Gson();
                                    String json = gson.toJson(startLocation);
                                    TrackerPreferences.getInstance(this).setString(ApiConstants.Preferenceconstants.USER_START_LOCATION, json);

                                } else if (startEndItem.getCoordinateType().equalsIgnoreCase("last")) {
                                    endLocation = new Location("");
                                    endLocation.setLatitude(Double.valueOf(startEndItem.getLatitude()));
                                    endLocation.setLongitude(Double.valueOf(startEndItem.getLongitude()));

                                    Gson gson = new Gson();
                                    String json = gson.toJson(endLocation);
                                    TrackerPreferences.getInstance(this).setString(ApiConstants.Preferenceconstants.USER_LAST_LOCATION, json);

                                }
                            }

                            TrackerPreferences.getInstance(this).setBoolean(ApiConstants.Preferenceconstants.USER_CONFIRMATION_FLAG, true);
                            confirmationBulb.setBackgroundColor(ContextCompat.getColor(this, R.color.green));
                            btnConfirm.setVisibility(View.GONE);
                            if (lastLocation != null) {
                                checkStartAndEndPoints(lastLocation);
                            }
                            ToastUtil.showShortToast(this, confirmUserResponse.getMessage());
                        } else {
                            ToastUtil.showShortToast(this, confirmUserResponse.getMessage());
                        }
                        break;
                    case ApiConstants.REQUEST_TYPE.DOWNLOAD_DATA:
                        GetDataResponseModel getDataResponseModel = (GetDataResponseModel) responseObject;
                        if (getDataResponseModel.getStatusCode().equalsIgnoreCase("2000")) {

                            TrackerPreferences.getInstance(this).setBoolean(ApiConstants.Preferenceconstants.USER_LOCATION_DATA_FLAG, true);
                            downloadedBulb.setBackgroundColor(ContextCompat.getColor(this, R.color.green));

                            TrackerPreferences.getInstance(this).setString(ApiConstants.Preferenceconstants.USER_SPEED_LIMIT, getDataResponseModel.getLatLongDetails().getSpeed());

                            latLongItems = getDataResponseModel.getLatLongDetails().getLatLongs();
                            LatLongDataManager.getInstance(this).setLatLongItemsList(latLongItems);
                            Gson gson = new Gson();
                            String json = gson.toJson(latLongItems);
                            TrackerPreferences.getInstance(this).setString(ApiConstants.Preferenceconstants.USER_LAT_LONG_DATA, json);

                            getDataFromCache();
                            initialiseHashMapwithLocationsIds();

                            btnDownload.setVisibility(View.GONE);
                            btnStart.setVisibility(View.VISIBLE);

                        } else {
                            ToastUtil.showShortToast(this, getDataResponseModel.getMessage());
                        }
                        break;
                    case ApiConstants.REQUEST_TYPE.SUBMIT_DATA:
                        BaseResponse baseResponse = (BaseResponse) responseObject;
                        if (baseResponse.getStatusCode().equalsIgnoreCase("2000")) {
                            TrackerPreferences.getInstance(this).setBoolean(ApiConstants.Preferenceconstants.USER_SUBMIT_CHALLENGE, true);
                            submitBulb.setBackgroundColor(ContextCompat.getColor(this, R.color.green));
                            ToastUtil.showLongToast(this, "Data Submitted Successfully.");
                            btnSubmit.setVisibility(View.GONE);
                            showDialog();
                        } else {
                            ToastUtil.showShortToast(this, baseResponse.getMessage());
                        }
                }
            }
        } catch (Exception e) {
            ToastUtil.showShortToast(this, getString(R.string.something_went_wrong));
            e.printStackTrace();
        }

    }

    private void initialiseHashMapwithLocationsIds() {

        for (LatLongItem latLongItem : latLongItems) {
            achievedlocationHashMap.put(latLongItem.getLatLongId(), 0);
        }
    }

    private void getDataFromCache() {

        locationObjectItems.clear();

        try {
            cachedLatLongItems = LatLongDataManager.getInstance(this).getLatLongItemsList();

            for (LatLongItem latLongItem : cachedLatLongItems) {
                CustomLocation targetLocation = new CustomLocation("");
                targetLocation.setLatitude(Double.valueOf(latLongItem.getLatitude()));
                targetLocation.setLongitude(Double.valueOf(latLongItem.getLongitude()));
                targetLocation.setLatLongId(latLongItem.getLatLongId());
                locationObjectItems.add(targetLocation);
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getAchievedDataFromCache() {

        try {
            if (AchievedLatLongDataManager.getInstance(this).getAchievedlocationObjectItems() != null) {
                achievedlocationHashMap = AchievedLatLongDataManager.getInstance(this).getAchievedlocationObjectItems();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void getStartEndLocationsFromCache() {
        Gson gson = new Gson();
        String startjson = TrackerPreferences.getInstance(this).getString(ApiConstants.Preferenceconstants.USER_START_LOCATION);
        startLocation = gson.fromJson(startjson, Location.class);

        String endjson = TrackerPreferences.getInstance(this).getString(ApiConstants.Preferenceconstants.USER_LAST_LOCATION);
        endLocation = gson.fromJson(endjson, Location.class);

//        startLocation = new Location("");
//        startLocation.setLatitude(Double.valueOf("28.591149"));
//        startLocation.setLongitude(Double.valueOf("77.319105"));
//
//        endLocation = new Location("");
//        endLocation.setLatitude(Double.valueOf("28.591149"));
//        endLocation.setLongitude(Double.valueOf("77.319105"));
    }

    private long getNow() {
        Calendar rightNow = Calendar.getInstance();
        return rightNow.getTimeInMillis() / 1000;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // register event bus
        try {
            if (!EventBus.getDefault().isRegistered(this))
                EventBus.getDefault().register(this);

            boolean hasSubscriber = EventBus.getDefault().hasSubscriberForEvent(Location.class);
            Log.d("=======", "hasSubscriber: " + hasSubscriber);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            EventBus.getDefault().unregister(this);
            Intent i = new Intent(this, TimerService.class);
            stopService(i);

            if (TrackerPreferences.getInstance(this).getBoolean(ApiConstants.Preferenceconstants.USER_START_CHALLENGE_FLAG)) {
                if (!TrackerPreferences.getInstance(this).getBoolean(ApiConstants.Preferenceconstants.USER_APP_RESTART_FLAG)) {
                    TrackerPreferences.getInstance(this).setBoolean(ApiConstants.Preferenceconstants.USER_APP_RESTART_FLAG, true);
                    TrackerPreferences.getInstance(this).setString(ApiConstants.Preferenceconstants.USER_APP_RESTART_TIME, getCurrentTimeStamp());
                }
                clearData();
            }

            if (TrackerPreferences.getInstance(this).getBoolean(ApiConstants.Preferenceconstants.USER_SUBMIT_CHALLENGE)) {
                clearData();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getAchivedLocationIdsToSubmit() {

        final HashMap<Integer, Integer> localMap = new HashMap<>();
        localMap.putAll(achievedlocationHashMap);
        StringBuilder cluesCovered = new StringBuilder("");
        Iterator it = localMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            if (((int) pair.getValue()) == 1) {
                cluesCovered.append((int) pair.getKey()).append(",");
            }
            it.remove(); // avoids a ConcurrentModificationException
        }

        return cluesCovered.toString();
    }

    private String getCurrentTimeStamp() {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
        Date date;
        try {
            if (currentTimeStamp > 0) {
                date = new Date(currentTimeStamp);
            } else {
                date = new Date(System.currentTimeMillis());
            }
            return format.format(date);

        } catch (Exception e) {

            e.printStackTrace();
            return format.format(new Date(System.currentTimeMillis()));
        }
    }

    private void showDialog() {
        dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//        dialog.getWindow().setBackgroundDrawableResource(R.drawable.transparent_bg_image);
        dialog.setContentView(R.layout.thank_you_dialog);

        TextView tvResend = (TextView) dialog.findViewById(R.id.tvResend);
        Button buttonConfirm = (Button) dialog.findViewById(R.id.buttonConfirm);
//
        try {
            buttonConfirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clearData();
                    finish();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
        dialog.setCancelable(false);
        try {
            if (!((Activity) this).isFinishing()) {
                dialog.show();
            }
        } catch (WindowManager.BadTokenException e) {
            e.printStackTrace();
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        // return dialog;
    }



}
