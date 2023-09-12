package pl.rvyk.instapp.api;

public class InstalingLoginResult {
    private int accountType;
    private boolean success;
    private String message;
    private String phpsessid;
    private boolean premium;
    private String name;
    private String email;
    private boolean childGetRaports;
    private boolean instalingNotifications;
    private String instalingVersion;
    private String appid;
    private String studentid;
    private String buttonText;
    private boolean todaySessionCompleted;
    private int homeworkCount;
    private int exerciseCount;

    public int getAccountType() {
        return accountType;
    }

    public void setAccountType(int accountType) {
        this.accountType = accountType;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPhpsessid() {
        return phpsessid;
    }

    public void setPhpsessid(String phpsessid) {
        this.phpsessid = phpsessid;
    }

    public boolean isPremium() {
        return premium;
    }

    public void setPremium(boolean premium) {
        this.premium = premium;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isChildGetRaports() {
        return childGetRaports;
    }

    public void setChildGetRaports(boolean childGetRaports) {
        this.childGetRaports = childGetRaports;
    }

    public boolean isInstalingNotifications() {
        return instalingNotifications;
    }

    public void setInstalingNotifications(boolean instalingNotifications) {
        this.instalingNotifications = instalingNotifications;
    }

    public String getInstalingVersion() {
        return instalingVersion;
    }

    public void setInstalingVersion(String instalingVersion) {
        this.instalingVersion = instalingVersion;
    }

    public String getAppid() {
        return appid;
    }

    public void setAppid(String appid) {
        this.appid = appid;
    }

    public String getStudentid() {
        return studentid;
    }

    public void setStudentid(String studentid) {
        this.studentid = studentid;
    }

    public String getButtonText() {
        return buttonText;
    }

    public void setButtonText(String buttonText) {
        this.buttonText = buttonText;
    }

    public boolean isTodaySessionCompleted() {
        return todaySessionCompleted;
    }

    public void setTodaySessionCompleted(boolean todaySessionCompleted) {
        this.todaySessionCompleted = todaySessionCompleted;
    }

    public int getHomeworkCount() {
        return homeworkCount;
    }

    public void setHomeworkCount(int homeworkCount) {
        this.homeworkCount = homeworkCount;
    }

    public int getExerciseCount() {
        return exerciseCount;
    }

    public void setExerciseCount(int exerciseCount) {
        this.exerciseCount = exerciseCount;
    }
}

