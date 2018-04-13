package com.tracker.screens;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.tracker.R;
import com.tracker.listeners.onUpdateViewListener;
import com.tracker.network.NetworkEngine;
import com.tracker.utils.ApiConstants;
import com.tracker.utils.ConnectivityUtils;
import com.tracker.utils.PermissionUtil;
import com.tracker.utils.ToastUtil;
import com.tracker.utils.TrackerPreferences;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.Calendar;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import response.BaseResponse;
import response.CityItemResponse;
import response.CityResponse;
import response.GetOtpRequest;
import response.RegisterRequestModel;
import response.RegisterResponse;

public class RegistrationActivity extends BaseActivity implements onUpdateViewListener {

    private HashMap<String, Integer> citiesMap = new HashMap<>();
    private HashMap<String, Integer> crewMap = new HashMap<>();
    private ArrayList<String> citiesList = new ArrayList<>();
    private ArrayList<String> crewList = new ArrayList<>();
    private int selectedCityId = 0, selectedCrewId = 0;
    private EditText edtname, edtMobile, edtEmail, edtDob;
    private EditText editTextOtp;
    private Spinner spnCity, spnCrew;
    private Button btnRegister, btnDone;
    private String userId, mOtp;
    private Calendar myCalendar = Calendar.getInstance();
    private TextView tvMessage;
    private LinearLayout formLayout;
    private Dialog otpDialog;
    public static final String MOB_NO_REG_EX = "^([6,7,8,9]{1}+[0-9]{9})$";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        try {
            EventBus.getDefault().register(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!TrackerPreferences.getInstance(this).getBoolean(ApiConstants.Preferenceconstants.USER_REGISTER_FLAG)) {
            EnableGPSAutoMatically();
            edtname = (EditText) findViewById(R.id.edtname);
            edtMobile = (EditText) findViewById(R.id.edtMobile);
            edtEmail = (EditText) findViewById(R.id.edtEmail);
            edtDob = (EditText) findViewById(R.id.edtDob);
            spnCity = (Spinner) findViewById(R.id.spnCity);
            spnCrew = (Spinner) findViewById(R.id.spnCrew);
            btnRegister = (Button) findViewById(R.id.btnRegister);
            btnDone = (Button) findViewById(R.id.btnDone);
            tvMessage = (TextView) findViewById(R.id.tvMessage);
            formLayout = (LinearLayout) findViewById(R.id.formLayout);

            edtDob.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    hideKeypad();

//                    Calendar tempCalendar = Calendar.getInstance();
//                    tempCalendar.add(Calendar.DAY_OF_YEAR,-18);
//                    tempCalendar.add(Calendar.DAY_OF_MONTH, myCalendar.getTime().getMonth());
                    DatePickerDialog datePickerDialog = new DatePickerDialog(RegistrationActivity.this,R.style.DialogTheme,  date, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH));
//                    datePickerDialog.getDatePicker().setMinDate(tempCalendar.getTimeInMillis());
                    datePickerDialog.show();
                }
            });

//            btnRegister.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    if (validate()) {
//                        PermissionUtil.with(RegistrationActivity.this).setCallback(new PermissionUtil.PermissionGrantedListener() {
//                            @Override
//                            public void onPermissionResult(boolean isGranted, int requestCode) {
//                                if (isGranted) {
//                                    PermissionUtil.with(RegistrationActivity.this).setCallback(new PermissionUtil.PermissionGrantedListener() {
//                                        @Override
//                                        public void onPermissionResult(boolean isGranted, int requestCode) {
//                                            if (isGranted) {
//                                                // permission is granted
//                                                hitApiRequest(ApiConstants.REQUEST_TYPE.REGISTER_USER);
//                                            }
//                                        }
//                                    }).validate(Manifest.permission.ACCESS_FINE_LOCATION);
//                                }
//                            }
//                        }).validate(Manifest.permission.READ_PHONE_STATE);
//
//                    }
//                }
//            });
            btnRegister.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (validate()) {
                        PermissionUtil.with(RegistrationActivity.this).setCallback(new PermissionUtil.PermissionGrantedListener() {
                            @Override
                            public void onPermissionResult(boolean isGranted, int requestCode) {
                                if (isGranted) {
                                    PermissionUtil.with(RegistrationActivity.this).setCallback(new PermissionUtil.PermissionGrantedListener() {
                                        @Override
                                        public void onPermissionResult(boolean isGranted, int requestCode) {
                                            if (isGranted) {
                                                PermissionUtil.with(RegistrationActivity.this).setCallback(new PermissionUtil.PermissionGrantedListener() {
                                                    @Override
                                                    public void onPermissionResult(boolean isGranted, int requestCode) {
                                                        if (isGranted) {
                                                            // permission is granted
                                                            hitApiRequest(ApiConstants.REQUEST_TYPE.GET_OTP);
                                                        }
                                                    }
                                                }).validate(Manifest.permission.READ_SMS);
                                            }
                                        }
                                    }).validate(Manifest.permission.ACCESS_FINE_LOCATION);
                                }
                            }
                        }).validate(Manifest.permission.READ_PHONE_STATE);
                    }
                }
            });

            btnDone.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });

            setupToolbar();
            citiesList.add("Select your city");
            crewList.add("Select your Crew Type");
            crewList.add("Female");
            crewList.add("Mixed");

            crewMap.put("Female",1);
            crewMap.put("Mixed",2);

            setCrewSpinner();
            hitApiRequest(ApiConstants.REQUEST_TYPE.GET_CITIES);
        } else {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    private boolean validate() {

        if (!isValidMobileNo(edtMobile.getText().toString())) {

            ToastUtil.showShortToast(this, "Enter valid Mobile Number");
            return false;
        }

        if (TextUtils.isEmpty(edtname.getText().toString())) {
            ToastUtil.showShortToast(this, "Enter valid Name");
            return false;

        }
        if (edtDob.getText().toString().trim().length() == 0 || getAge(myCalendar.getTimeInMillis())) {
            ToastUtil.showLongToast(this, "Please enter correct Date of birth");
            return false;
        }

        if (TextUtils.isEmpty(edtEmail.getText().toString()) || !Patterns.EMAIL_ADDRESS.matcher(edtEmail.getText().toString()).matches()) {
            ToastUtil.showShortToast(this, "Enter valid Email");
            return false;
        }

        if (selectedCityId < 1) {
            ToastUtil.showShortToast(this, "Select City");
            return false;
        }
        if (selectedCrewId < 1) {
            ToastUtil.showShortToast(this, "Select Crew Type");
            return false;
        }
        return true;
    }

    private void setupToolbar() {
        Toolbar mToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        mToolbar.setTitle("    Registration");
        setSupportActionBar(mToolbar);

//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

    }

    DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, monthOfYear);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateDOB();
        }
    };

    private void updateDOB() {

        String myFormat = "dd MMM yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.getDefault());
        edtDob.setText(sdf.format(myCalendar.getTime()));

    }

    private boolean getAge(long selectedMilli) {
        Date dateOfBirth = new Date(selectedMilli);
        Calendar dob = Calendar.getInstance();
        dob.setTime(dateOfBirth);
        Calendar today = Calendar.getInstance();
        int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);
        if (today.get(Calendar.MONTH) < dob.get(Calendar.MONTH)) {
            age--;
        } else if (today.get(Calendar.MONTH) == dob.get(Calendar.MONTH)
                && today.get(Calendar.DAY_OF_MONTH) < dob
                .get(Calendar.DAY_OF_MONTH)) {
            age--;
        }

        if (age < 18) {
            ToastUtil.showShortToast(this, "Age cannot be less than 18 years");
            return  true;
            //do something
        } else {
            return false;
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }

        return super.onOptionsItemSelected(item);
    }

    private void hitApiRequest(int reqType) {
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
                case ApiConstants.REQUEST_TYPE.GET_CITIES:

                    clasz = CityResponse.class;

                    // api request
                    url = ApiConstants.Urls.BASE_URL + "master/getCities/app";
                    url = url.replace(" ", "%20");
                    Log.v("url-->> ", url);

                    NetworkEngine.with(this).setClassType(clasz).setUrl(url).setRequestType(reqType).setHttpMethodType(Request.Method.GET).setUpdateViewListener(this).build();
                    break;

                case ApiConstants.REQUEST_TYPE.GET_OTP:

                    clasz = BaseResponse.class;

                    // api request
                    url = ApiConstants.Urls.BASE_URL + "generate/otp";
                    url = url.replace(" ", "%20");
                    Log.v("url-->> ", url);

                    GetOtpRequest getOtpRequest = new GetOtpRequest();
                    getOtpRequest.setMsisdn(edtMobile.getText().toString());
                    getOtpRequest.setRequestType(0);

                    NetworkEngine.with(this).setClassType(clasz).setUrl(url).setRequestType(reqType).setRequestModel(getOtpRequest).setHttpMethodType(Request.Method.POST).setUpdateViewListener(this).build();
                    break;


                case ApiConstants.REQUEST_TYPE.RESEND_OTP:

                    clasz = BaseResponse.class;

                    // api request
                    url = ApiConstants.Urls.BASE_URL + "generate/otp";
                    url = url.replace(" ", "%20");
                    Log.v("url-->> ", url);

                    GetOtpRequest getOtpRequest1 = new GetOtpRequest();
                    getOtpRequest1.setMsisdn(edtMobile.getText().toString());
                    getOtpRequest1.setRequestType(1);

                    NetworkEngine.with(this).setClassType(clasz).setUrl(url).setRequestType(reqType).setRequestModel(getOtpRequest1).setHttpMethodType(Request.Method.POST).setUpdateViewListener(this).build();
                    break;
                case ApiConstants.REQUEST_TYPE.REGISTER_USER:

                    clasz = RegisterResponse.class;

                    // api request
                    url = ApiConstants.Urls.BASE_URL + "user/register";
                    url = url.replace(" ", "%20");
                    Log.v("url-->> ", url);

                    RegisterRequestModel registerRequestModel = new RegisterRequestModel();
                    registerRequestModel.setCityId(selectedCityId);


                    Date date = new Date(System.currentTimeMillis());
                    DateFormat format = new SimpleDateFormat("dd MMMM, hh:mm a", Locale.ENGLISH);
                    registerRequestModel.setDeviceDateTime(format.format(date));

                    registerRequestModel.setImei(getImeiNumber(this));
                    registerRequestModel.setMsisdn(edtMobile.getText().toString());
                    registerRequestModel.setUserName(edtname.getText().toString());
                    registerRequestModel.setEmailId(edtEmail.getText().toString().trim());
                    registerRequestModel.setOneTimePassword(mOtp);

                    registerRequestModel.setCrewType(selectedCrewId);
                    registerRequestModel.setDateOfBirth(edtDob.getText().toString().trim());

                    NetworkEngine.with(this).setClassType(clasz).setUrl(url).setRequestType(reqType).setRequestModel(registerRequestModel).setHttpMethodType(Request.Method.POST).setUpdateViewListener(this).build();
                    break;

                default:
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
                    case ApiConstants.REQUEST_TYPE.GET_CITIES:
                        CityResponse cityResponse = (CityResponse) responseObject;
                        if (cityResponse.getStatusCode().equalsIgnoreCase("2000")) {
                            try {
                                ArrayList<CityItemResponse> cityItemResponses = new ArrayList<>();
                                cityItemResponses = cityResponse.getCityData();

                                for (CityItemResponse cityItemResponse : cityItemResponses) {
                                    citiesMap.put(cityItemResponse.getCityName(), cityItemResponse.getCityId());
                                    citiesList.add(cityItemResponse.getCityName());
                                }
                                setSpinner();

                            } catch (Exception e) {
                                e.printStackTrace();
                                hideProgressDialog();
                            }
                        }
                        break;

                    case ApiConstants.REQUEST_TYPE.REGISTER_USER:
                        RegisterResponse registerResponse = (RegisterResponse) responseObject;
                        if (registerResponse.getStatusCode().equalsIgnoreCase("2000")) {
                            userId = registerResponse.getVariable();
                            TrackerPreferences.getInstance(this).setString(ApiConstants.Preferenceconstants.USER_ID, userId);
                            TrackerPreferences.getInstance(this).setBoolean(ApiConstants.Preferenceconstants.USER_REGISTER_FLAG, true);
                            TrackerPreferences.getInstance(this).setInteger(ApiConstants.Preferenceconstants.USER_SPEED_LIMIT_POINTS, 0);
                            TrackerPreferences.getInstance(this).setInteger(ApiConstants.Preferenceconstants.USER_TARGET_LOCATION_POINTS, 0);
                            TrackerPreferences.getInstance(this).setString(ApiConstants.Preferenceconstants.USER_NAME, edtname.getText().toString());
                            formLayout.setVisibility(View.GONE);
                            tvMessage.setVisibility(View.VISIBLE);
                            btnDone.setVisibility(View.VISIBLE);

                        } else {
                            // user already exists
                            ToastUtil.showLongToast(this, registerResponse.getMessage());
                        }
                        break;

                    case ApiConstants.REQUEST_TYPE.GET_OTP:
                        BaseResponse baseResponse = (BaseResponse) responseObject;
                        if (baseResponse.getStatusCode().equalsIgnoreCase("2000")) {
                            showOTPdialog();
                        } else {
                            ToastUtil.showLongToast(this, baseResponse.getMessage());
                        }
                    case ApiConstants.REQUEST_TYPE.RESEND_OTP:
                        BaseResponse baseResponse1 = (BaseResponse) responseObject;
                        if (baseResponse1.getStatusCode().equalsIgnoreCase("2000")) {
//                            showOTPdialog();
                        } else {
                            ToastUtil.showLongToast(this, baseResponse1.getMessage());
                        }
                }
            }
        } catch (Exception e) {
            ToastUtil.showShortToast(this, getString(R.string.something_went_wrong));
            e.printStackTrace();
        }

    }

    private void showOTPdialog() {
        otpDialog = new Dialog(this);
        otpDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//        dialog.getWindow().setBackgroundDrawableResource(R.drawable.transparent_bg_image);
        otpDialog.setContentView(R.layout.otp_dialog);

        TextView tvResend = (TextView) otpDialog.findViewById(R.id.tvResend);
        editTextOtp = (EditText) otpDialog.findViewById(R.id.editTextOtp);
        Button buttonConfirm = (Button) otpDialog.findViewById(R.id.buttonConfirm);
//
        try {
            buttonConfirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!(editTextOtp.getText().toString().trim().length() < 4)) {
                        otpDialog.dismiss();
                        mOtp = editTextOtp.getText().toString().trim();
                        hitApiRequest(ApiConstants.REQUEST_TYPE.REGISTER_USER);
                    } else {
                        ToastUtil.showShortToast(RegistrationActivity.this, "Please enter valid OTP");
                    }
                }
            });

            tvResend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    hideKeypad();
                    hitApiRequest(ApiConstants.REQUEST_TYPE.RESEND_OTP);

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }


        otpDialog.setCancelable(false);
        try {
            if (!((Activity) this).isFinishing()) {
                otpDialog.show();
            }
        } catch (WindowManager.BadTokenException e) {
            e.printStackTrace();
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        // return dialog;
    }

    @Subscribe
    public void otpReceived(String otp) {
        try {
            if (editTextOtp != null) {
                editTextOtp.setText(otp);
                editTextOtp.setSelection(editTextOtp.getText().toString().length());

                if (!(editTextOtp.getText().toString().trim().length() < 4)) {
                    otpDialog.dismiss();
                    mOtp = editTextOtp.getText().toString().trim();
                    hitApiRequest(ApiConstants.REQUEST_TYPE.REGISTER_USER);
                } else {
                    ToastUtil.showShortToast(RegistrationActivity.this, "Please enter valid OTP");
                }
            }


        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void setSpinner() {
        // Initializing an ArrayAdapter

        final ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item, citiesList) {
            @Override
            public boolean isEnabled(int position) {
                if (position == 0) {
                    // Disable the first item from Spinner
                    // First item will be use for hint
                    return false;
                } else {
                    return true;
                }
            }

            @Override
            public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if (position == 0) {
                    // Set the hint text color gray
                    tv.setTextColor(getResources().getColor(R.color.text_input_label_color));
                } else {
                    tv.setTextColor(getResources().getColor(R.color.dark_black));
                }
                return view;
            }
        };
        spinnerArrayAdapter.setDropDownViewResource(R.layout.spinner_item);
        spnCity.setAdapter(spinnerArrayAdapter);

        spnCity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItemText = (String) parent.getItemAtPosition(position);
                // If user change the default selection
                // First item is disable and it is used for hint
                if (position > 0) {
                    // Notify the selected item text
//                    rlyVpatext.setVisibility(View.VISIBLE);
                    selectedCityId = citiesMap.get(selectedItemText);

                } else {

//                    rlyVpatext.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    public void setCrewSpinner() {
        // Initializing an ArrayAdapter

        Log.d("=======", "5555"+crewList.size());
        final ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item, crewList) {
            @Override
            public boolean isEnabled(int position) {
                if (position == 0) {
                    // Disable the first item from Spinner
                    // First item will be use for hint
                    return false;
                } else {
                    return true;
                }
            }

            @Override
            public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if (position == 0) {
                    // Set the hint text color gray
                    tv.setTextColor(getResources().getColor(R.color.text_input_label_color));
                } else {
                    tv.setTextColor(getResources().getColor(R.color.dark_black));
                }
                return view;
            }
        };
        spinnerArrayAdapter.setDropDownViewResource(R.layout.spinner_item);
        spnCrew.setAdapter(spinnerArrayAdapter);

        spnCrew.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItemText = (String) parent.getItemAtPosition(position);
                // If user change the default selection
                // First item is disable and it is used for hint
                if (position > 0) {
                    // Notify the selected item text
//                    rlyVpatext.setVisibility(View.VISIBLE);
                    selectedCrewId = crewMap.get(selectedItemText);

                } else {

//                    rlyVpatext.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    public static boolean isValidMobileNo(String MobileNo) {
        if (TextUtils.isEmpty(MobileNo)) {
            //       if (context != null) {
            //          if (showDialog)
            //             CJRAppUtility.showAlert(
            //                   context,
            //                   context.getResources().getString(
            //                         R.string.message_title),
            //                   context.getResources().getString(
            //                         R.string.mob_no_empty_error));
            //       }

        } else {
            Pattern p = Pattern.compile(MOB_NO_REG_EX);
            Matcher m = p.matcher(MobileNo);
            if (m.find()) {
                return true;
            } else {
                //          if (context != null) {
                //             if (showDialog)
                //                CJRAppUtility.showAlert(context, context.getResources()
                //                      .getString(R.string.message_title), context
                //                      .getResources()
                //                      .getString(R.string.mob_no_error));
                //          }
            }
        }
        return false;
    }
}
