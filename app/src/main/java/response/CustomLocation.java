package response;

import android.location.Location;

/**
 * Created by shubhamlamba on 2/16/18.
 */

public class CustomLocation extends Location {
    public CustomLocation(String provider) {
        super(provider);
    }

    public CustomLocation(Location l) {
        super(l);
    }

    private int latLongId;

    public int getLatLongId() {
        return latLongId;
    }

    public void setLatLongId(int latLongId) {
        this.latLongId = latLongId;
    }
}
