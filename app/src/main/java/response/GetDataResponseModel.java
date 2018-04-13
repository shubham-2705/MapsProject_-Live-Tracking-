package response;

import java.util.ArrayList;

/**
 * Created by shubhamlamba on 09/02/18.
 */

public class GetDataResponseModel extends BaseResponse {

    private LatLongDetails latLongDetails;

    public LatLongDetails getLatLongDetails() {
        return latLongDetails;
    }

    public void setLatLongDetails(LatLongDetails latLongDetails) {
        this.latLongDetails = latLongDetails;
    }

    public class LatLongDetails {
        private String speed;
        private ArrayList<LatLongItem> latLongs;

        public String getSpeed() {
            return speed;
        }

        public void setSpeed(String speed) {
            this.speed = speed;
        }

        public ArrayList<LatLongItem> getLatLongs() {
            return latLongs;
        }

        public void setLatLongs(ArrayList<LatLongItem> latLongs) {
            this.latLongs = latLongs;
        }
    }
}
