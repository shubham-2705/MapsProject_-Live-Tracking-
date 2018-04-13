package com.tracker.utils;


public interface ApiConstants {

    boolean isMock = false;


    interface Urls {
        String BASE_URL = "http://205.147.110.118:8040/trackerServices/"; // QA
//        String BASE_URL_DEV = "http://103.253.37.61/"; // DEV

//        String BASE_URL = BASE_URL_DEV;


    }

    interface REQUEST_TYPE
    {
        int REGISTER_USER=0;
        int GET_CITIES = 1;
        int CONFIRM_USER = 2;
        int DOWNLOAD_DATA = 3;
        int GET_OTP = 4;
        int RESEND_OTP = 5;
        int SUBMIT_DATA = 6;

    }



    interface Values {

        interface ResponseCodes {

//            String SUCCESS="2000";
//            String FAILURE="4004";
//            String SERVER_ERROR="5000";

        }

        interface UserType {//based on response code
//            String NEW_USER = "1";
//            String ALREADY_EXISTING_USER = "0";
        }

    }

    interface Preferenceconstants {

        String USER_REGISTER_FLAG = "USER_REGISTER_FLAG";
        String USER_ID = "USER_ID";
        String USER_SPEED_LIMIT_POINTS = "USER_SPEED_LIMIT_POINTS";
        String USER_TARGET_LOCATION_POINTS = "USER_TARGET_LOCATION_POINTS";
        String USER_NAME = "USER_NAME";
        String USER_CONFIRMATION_FLAG = "USER_CONFIRMATION_FLAG";
        String USER_LOCATION_DATA_FLAG = "USER_LOCATION_DATA_FLAG";
        String USER_ACHIEVED_LAT_LONG_DATA = "USER_ACHIEVED_LAT_LONG_DATA";
        String USER_SPEED_LIMIT = "USER_SPEED_LIMIT";
        String USER_LAT_LONG_DATA = "USER_LAT_LONG_DATA";
        String USER_START_LOCATION = "USER_START_LOCATION";
        String USER_LAST_LOCATION = "USER_LAST_LOCATION";
        String USER_TIME_SPENT = "USER_TIME_SPENT";
        String USER_START_CHALLENGE_FLAG = "USER_START_CHALLENGE_FLAG";
        String USER_GREEN_SCREEN = "USER_GREEN_SCREEN";
        String USER_STOP_CHALLENGE = "USER_STOP_CHALLENGE";
        String USER_APP_RESTART_FLAG = "USER_APP_RESTART_FLAG";
        String USER_APP_RESTART_TIME = "USER_APP_RESTART_TIME";
        String USER_START_CHALLENGE_TIME = "USER_START_CHALLENGE_TIME";
        String USER_STOP_CHALLENGE_TIME = "USER_STOP_CHALLENGE_TIME";
        String USER_SUBMIT_CHALLENGE = "USER_SUBMIT_CHALLENGE";
        String USER_TOTAL_POINTS = "USER_TOTAL_POINTS";
    }


}
