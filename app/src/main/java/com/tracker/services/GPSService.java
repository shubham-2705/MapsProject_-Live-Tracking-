package com.tracker.services;

import android.Manifest;
import android.content.Intent;
import android.app.Service;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import org.greenrobot.eventbus.EventBus;


/**
 * Created by shubhamlamba on 31/01/18.
 */

public class GPSService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private static final String LOGSERVICE = "#######";
    private Location mLastLocation;
    private Location mCurrentLocation;
    private static final int LOCATION_INTERVAL = 2000;
    private static final float LOCATION_DISTANCE = 1;

    @Override
    public void onCreate() {
        try {
            super.onCreate();
            buildGoogleApiClient();
            Log.i(LOGSERVICE, "onCreate");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            Log.i(LOGSERVICE, "onStartCommand");

            if (!isClientConnected())
                mGoogleApiClient.connect();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return START_STICKY;
    }


    @Override
    public void onConnected(Bundle bundle) {
        Log.i(LOGSERVICE, "onConnected" + bundle);

        try {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (mLastLocation != null)
                onGotLocation(mLastLocation);
        } catch (Exception e) {
            e.printStackTrace();
        }

        startLocationUpdate();
    }

    private void startLocationUpdate() {
        try {
            initLocationRequest();
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onGotLocation(Location location) {
        try {
            if (location == null) return;

            Log.d("======", "" + (location.getSpeed() * (18/5))+ "  " +location.getLatitude() +
                    "  "+location.getLongitude()+ "  "+location.getProvider());
//            Toast.makeText(GPSService.this, "" + (location.getSpeed() * (18/5))+ "  " +location.getLatitude() +
//                    "  "+location.getLongitude(), Toast.LENGTH_SHORT).show();

            EventBus.getDefault().post(location);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onLocationChanged(Location location) {
        try {
            Log.i(LOGSERVICE, "lat " + location.getLatitude());
            Log.i(LOGSERVICE, "lng " + location.getLongitude());
            onGotLocation(location);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void disconnectClient() {

        if (mGoogleApiClient != null && isClientConnected()) {
            stopLocationUpdate();
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        disconnectClient();
        Log.i(LOGSERVICE, "onDestroy - Estou sendo destruido ");

    }


    private void initLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(LOCATION_INTERVAL);
        mLocationRequest.setFastestInterval(LOCATION_INTERVAL);
        mLocationRequest.setSmallestDisplacement(LOCATION_DISTANCE);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

    }


    private void stopLocationUpdate() {
        if (isClientConnected())
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);

    }

    protected synchronized void buildGoogleApiClient() {

        try {

            if (!isGooglePlayServicesAvailable()) {
                //ToDo: when playservices not available
                Toast.makeText(this, "Google Play service not available in device", Toast.LENGTH_SHORT).show();
                return;
            }

            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addOnConnectionFailedListener(this)
                    .addConnectionCallbacks(this)
                    .addApi(LocationServices.API)
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int status = googleAPI.isGooglePlayServicesAvailable(this);
        return (ConnectionResult.SUCCESS == status);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(LOGSERVICE, "onConnectionFailed ");

    }

    public boolean isClientConnected() {
        return mGoogleApiClient != null && mGoogleApiClient.isConnected();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(LOGSERVICE, "onConnectionSuspended " + i);

    }

}