package com.tracker.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.view.View;

import java.lang.ref.WeakReference;


public class PermissionUtil {

    private WeakReference<Context> activity;
    private static PermissionUtil instance;
    private PermissionGrantedListener permissionGrantedListener;
    private static int mRequestCode = 9000;
    private String message = "Please Grant Permissions";

    private PermissionUtil() {
        // do nothing
    }

    public static PermissionUtil with(Context context) {
        if (instance == null) {
            instance = new PermissionUtil();
        }
        instance.activity=new WeakReference<Context>(context);
        return instance;
    }

    public PermissionUtil setCallback(PermissionGrantedListener listener) {
        permissionGrantedListener = listener;
        return instance;
    }

    public PermissionUtil setRequestCode(int requestCode) {
        this.mRequestCode = requestCode;
        return instance;
    }

    public PermissionUtil setRationaleMessage(String message) {
        this.message = message;
        return instance;
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == mRequestCode) {

            // for multiple permissions case
//            int permissionCheck = PackageManager.PERMISSION_GRANTED;
//            for (int permission : grantResults) {
//                permissionCheck = permissionCheck + permission;
//            }
//            if ((grantResults.length > 0) && permissionCheck == PackageManager.PERMISSION_GRANTED) {

                // Check if the only required permission has been granted
            if ((grantResults.length > 0) && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                updateResult(true);
            } else {
                //Permission not granted
                updateResult(false);

                if (activity.get()!=null) {
                    Snackbar.make(((Activity)activity.get()).findViewById(android.R.id.content), "Enable Permissions from settings",
                            Snackbar.LENGTH_INDEFINITE).setAction("ENABLE",
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent();
                                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                    intent.addCategory(Intent.CATEGORY_DEFAULT);
                                    intent.setData(Uri.parse("package:" + activity.get().getPackageName()));
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                                    activity.get().startActivity(intent);
                                }
                            }).show();
                }
            }

        }
    }

    public void validate(final String... permissions) {
        for (final String permission : permissions) {
            if (activity.get() != null && ActivityCompat.checkSelfPermission(activity.get(), permission) != PackageManager.PERMISSION_GRANTED) {
                // permission has not been granted.
                if (ActivityCompat.shouldShowRequestPermissionRationale(((Activity) activity.get()), permission)) {
                    //This is called if user has denied the permission before
                    Snackbar.make(((Activity) activity.get()).findViewById(android.R.id.content), message, Snackbar.LENGTH_INDEFINITE)
                            .setAction("OK", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    ActivityCompat.requestPermissions(((Activity) activity.get()), permissions, mRequestCode);
                                }
                            })
                            .show();
                } else {
                    ActivityCompat.requestPermissions(((Activity) activity.get()), new String[]{permission}, mRequestCode);
                }
            } else {
                updateResult(true);
            }
        }
    }


    private void updateResult(boolean isGranted) {
        if (permissionGrantedListener != null) {
            permissionGrantedListener.onPermissionResult(isGranted, mRequestCode);
        }
    }

    public interface PermissionGrantedListener {
        void onPermissionResult(boolean isGranted, int requestCode);
    }
}
