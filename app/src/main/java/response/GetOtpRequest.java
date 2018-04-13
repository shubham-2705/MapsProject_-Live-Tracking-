package response;

/**
 * Created by shubhamlamba on 2/21/18.
 */

public class GetOtpRequest extends ParentRequestModel {

    String msisdn;
    int requestType;

    public String getMsisdn() {
        return msisdn;
    }

    public void setMsisdn(String msisdn) {
        this.msisdn = msisdn;
    }

    public int getRequestType() {
        return requestType;
    }

    public void setRequestType(int requestType) {
        this.requestType = requestType;
    }
}
