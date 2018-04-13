package response;

/**
 * Created by shubhamlamba on 2/25/18.
 */

public class SubmitDataRequestModel extends ParentRequestModel {

    private int userId;
    private String cluesCovered;
    private String speedPenality;
    private String timeTaken;
    private String appTotalPoint;
    private String startTime;
    private String endTime;
    private int restartFlag;
    private String restartTimeStamp;

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getCluesCovered() {
        return cluesCovered;
    }

    public void setCluesCovered(String cluesCovered) {
        this.cluesCovered = cluesCovered;
    }

    public String getSpeedPenality() {
        return speedPenality;
    }

    public void setSpeedPenality(String speedPenality) {
        this.speedPenality = speedPenality;
    }

    public String getTimeTaken() {
        return timeTaken;
    }

    public void setTimeTaken(String timeTaken) {
        this.timeTaken = timeTaken;
    }

    public String getAppTotalPoint() {
        return appTotalPoint;
    }

    public void setAppTotalPoint(String appTotalPoint) {
        this.appTotalPoint = appTotalPoint;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public int getRestartFlag() {
        return restartFlag;
    }

    public void setRestartFlag(int restartFlag) {
        this.restartFlag = restartFlag;
    }

    public String getRestartTimeStamp() {
        return restartTimeStamp;
    }

    public void setRestartTimeStamp(String restartTimeStamp) {
        this.restartTimeStamp = restartTimeStamp;
    }
}
