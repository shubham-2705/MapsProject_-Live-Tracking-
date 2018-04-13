package response;

/**
 * Created by shubhamlamba on 09/02/18.
 */

public class GetDataRequestModel extends ParentRequestModel {

    private int userId;
    private String imei;

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}
