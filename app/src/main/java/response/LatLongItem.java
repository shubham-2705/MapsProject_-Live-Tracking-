package response;

/**
 * Created by shubhamlamba on 09/02/18.
 */

public class LatLongItem {

    private String latitude;
    private String longitude;
    private String coordinateType;
    private int latLongId;

    public int getLatLongId() {
        return latLongId;
    }

    public void setLatLongId(int latLongId) {
        this.latLongId = latLongId;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getCoordinateType() {
        return coordinateType;
    }

    public void setCoordinateType(String coordinateType) {
        this.coordinateType = coordinateType;
    }
}
