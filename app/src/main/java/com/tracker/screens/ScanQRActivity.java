package com.tracker.screens;

import android.content.pm.PackageManager;
import android.graphics.PointF;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.dlazaro66.qrcodereaderview.QRCodeReaderView;
import com.tracker.R;
import com.tracker.utils.ToastUtil;
import java.util.Map;

public class ScanQRActivity extends BaseActivity implements QRCodeReaderView.OnQRCodeReadListener {

    private QRCodeReaderView mydecoderview;
    private String scannedStr;
    private String challengeId="", locationName="";
    private CheckBox flashlightCheckBox;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_qr);

        flashlightCheckBox = (CheckBox) findViewById(R.id.flash_light);

        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            flashlightCheckBox.setVisibility(View.VISIBLE);
            flashlightCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                    mydecoderview.setTorchEnabled(isChecked);
                }
            });
        } else {
            flashlightCheckBox.setVisibility(View.GONE);
        }


        mydecoderview = (QRCodeReaderView) findViewById(R.id.qrdecoderview);
        mydecoderview.setBackCamera();
        mydecoderview.setOnQRCodeReadListener(this);
        mydecoderview.setQRDecodingEnabled(true);
    }

    @Override
    public synchronized void onQRCodeRead(String text, PointF[] points) {

        scannedStr = text;
        if (!TextUtils.isEmpty(scannedStr)) {

            try {
                Map<String, String> localMap = splitQuery(scannedStr);

                if (localMap.size() > 0) {
                    challengeId = localMap.get("challengeId");
                    locationName = localMap.get("locationName");

                    ToastUtil.showLongToast(this, challengeId+" "+locationName);
                    finish();


                } else {
                    ToastUtil.showShortToast(this, "Invalid QR Code");
                    finish();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            ToastUtil.showLongToast(this, "Unable to process QR Code");
        }
    }
}
