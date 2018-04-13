package com.tracker.screens;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.tracker.services.GPSService;
import com.tracker.utils.ApiConstants;
import com.tracker.utils.ConnectivityUtils;
import com.tracker.utils.PermissionUtil;
import com.tracker.utils.TrackerPreferences;

import org.apache.http.NameValuePair;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.google.firebase.crash.FirebaseCrash.log;


@SuppressLint("Registered")
public class BaseActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private ProgressDialog progressDialog;
    public static String TAG = "----TRACKER";
    public boolean startChallenge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

    }

    public void setToolbar(int toolbarColor, Toolbar myToolbar, String title) {
//        myToolbar.setBackgroundColor(ContextCompat.getColor(this, toolbarColor));
//
//        myToolbar.setTitle("");
//        setSupportActionBar(myToolbar);
//
//        TextView toolbar_title = (TextView) myToolbar.findViewById(R.id.toolbar_title);
//        if (TextUtils.isEmpty(title)) {
//            toolbar_title.setBackgroundResource(R.drawable.oxigen_wallet);
//        } else {
//            toolbar_title.setBackgroundColor(ContextCompat.getColor(this, R.color.transparent));
//        }
//        toolbar_title.setText(title);


        myToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

//        if (BuildConfig.DEBUG && this instanceof EnterMobileActivity) {
//            toolbar_title.setOnLongClickListener(new View.OnLongClickListener() {
//                @Override
//                public boolean onLongClick(View v) {
//                    showLinkDialog();
//                    return false;
//                }
//            });
//        }
    }

//    public void setToolbar(Toolbar myToolbar, String title) {
//        setToolbar(R.color.white_color, myToolbar, title);
//    }

    public void showKeypad() {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }

    public void hideDialogKeypad() {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    public void hideKeypad() {
        InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        // check if no view has focus:
        View v = getCurrentFocus();
        if (v == null)
            return;

        inputManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    public void openUrlinBrowser(String url) {
        Uri uri = Uri.parse(url); // missing 'http://' will cause crashed
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }


    public float convertPixelsToDp(float px, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float dp = px / ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return dp;
    }

    public void showProgressdialog(String message) {
        showProgressdialog(message, false);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionUtil.with(this).onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    public void showProgressdialog(String message, boolean isCancelable) {
        if (progressDialog != null) {
            progressDialog.cancel();
            progressDialog = null;
        }
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(message);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(isCancelable);
//        progressDialog.setProgress(0);
        progressDialog.show();
    }

    public void hideProgressDialog() {
//        removeProgressDialog(false);
        if (progressDialog != null) {
            progressDialog.cancel();
            progressDialog = null;
        }
    }

    public String getImeiNumber(Context context) {
        try {
            if (context != null) {
                TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                if (telephonyManager != null) {
                    return telephonyManager.getDeviceId();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
//            if(com.paytm.utility.CJRAppCommonUtility.isDebug) e.printStackTrace();
        }
        return null;
    }

    public void EnableGPSAutoMatically() {
        GoogleApiClient googleApiClient = null;
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API).addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this).build();
            googleApiClient.connect();
            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setInterval(2000);
            locationRequest.setFastestInterval(2000);
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(locationRequest);

            // **************************
            builder.setAlwaysShow(true); // this is the key ingredient
            // **************************

            PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi
                    .checkLocationSettings(googleApiClient, builder.build());
            result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
                @Override
                public void onResult(LocationSettingsResult result) {
                    final Status status = result.getStatus();
                    final LocationSettingsStates state = result
                            .getLocationSettingsStates();
                    switch (status.getStatusCode()) {
                        case LocationSettingsStatusCodes.SUCCESS:
                            // All location settings are satisfied. The client can
                            // initialize location
                            // requests here.
                            break;
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            toast("Please press OK to enable GPS");
                            // Location settings are not satisfied. But could be
                            // fixed by showing the user
                            // a dialog.
                            try {
                                // Show the dialog by calling
                                // startResolutionForResult(),
                                // and check the result in onActivityResult().
                                status.startResolutionForResult(BaseActivity.this, 1000);

                            } catch (IntentSender.SendIntentException e) {
                                // Ignore the error.
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            toast("Setting change not allowed");
                            // Location settings are not satisfied. However, we have
                            // no way to fix the
                            // settings so we won't show the dialog.
                            break;
                    }
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1000) {
            if (resultCode == RESULT_OK && ConnectivityUtils.isGpsEnabled(this)) {
                Toast.makeText(this, "GPS turned ON", Toast.LENGTH_SHORT).show();
                if (!isMyServiceRunning(GPSService.class)) {
                    startService(new Intent(BaseActivity.this, GPSService.class));
                }
            }
            if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Please turn your GPS ON", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {
        toast("Suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        toast("Failed");
    }

    private void toast(String message) {
        try {
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
        } catch (Exception ex) {
            log("Window has been closed");
        }
    }

    public boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static Map<String, String> splitQuery(String url) throws UnsupportedEncodingException {
        Map<String, String> query_pairs = new LinkedHashMap<String, String>();
        String query = url;
        String[] pairs = query.split("&");
        if (pairs.length > 1) {
            for (String pair : pairs) {
                int idx = pair.indexOf("=");
                query_pairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"), URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
            }
        }
        return query_pairs;
    }

    public void clearData() {
        String[] prefsList = {
                ApiConstants.Preferenceconstants.USER_REGISTER_FLAG,
                ApiConstants.Preferenceconstants.USER_ID,
                ApiConstants.Preferenceconstants.USER_SPEED_LIMIT_POINTS,
                ApiConstants.Preferenceconstants.USER_TARGET_LOCATION_POINTS,
                ApiConstants.Preferenceconstants.USER_NAME,
                ApiConstants.Preferenceconstants.USER_CONFIRMATION_FLAG,
                ApiConstants.Preferenceconstants.USER_LOCATION_DATA_FLAG,
                ApiConstants.Preferenceconstants.USER_ACHIEVED_LAT_LONG_DATA,
                ApiConstants.Preferenceconstants.USER_SPEED_LIMIT,
                ApiConstants.Preferenceconstants.USER_LAT_LONG_DATA,
                ApiConstants.Preferenceconstants.USER_START_LOCATION,
                ApiConstants.Preferenceconstants.USER_LAST_LOCATION,
                ApiConstants.Preferenceconstants.USER_TIME_SPENT,
                ApiConstants.Preferenceconstants.USER_START_CHALLENGE_FLAG,
                ApiConstants.Preferenceconstants.USER_GREEN_SCREEN,
                ApiConstants.Preferenceconstants.USER_STOP_CHALLENGE,
                ApiConstants.Preferenceconstants.USER_APP_RESTART_FLAG,
                ApiConstants.Preferenceconstants.USER_APP_RESTART_TIME,
                ApiConstants.Preferenceconstants.USER_START_CHALLENGE_TIME,
                ApiConstants.Preferenceconstants.USER_STOP_CHALLENGE_TIME,
                ApiConstants.Preferenceconstants.USER_SUBMIT_CHALLENGE,
                ApiConstants.Preferenceconstants.USER_TOTAL_POINTS,
        };

        TrackerPreferences.getInstance(this).removeKey(prefsList);
    }

}
