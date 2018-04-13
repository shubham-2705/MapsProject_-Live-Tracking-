package response;

import java.util.ArrayList;

/**
 * Created by shubhamlamba on 08/02/18.
 */

public class CityResponse extends BaseResponse {

    private ArrayList<CityItemResponse> cityData;

    public ArrayList<CityItemResponse> getCityData() {
        return cityData;
    }

    public void setCityData(ArrayList<CityItemResponse> cityData) {
        this.cityData = cityData;
    }
}
