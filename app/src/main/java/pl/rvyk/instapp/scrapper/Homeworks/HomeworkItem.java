package pl.rvyk.instapp.scrapper.Homeworks;

public class HomeworkItem {
    private String title;
    private String deadline;
    private String homeworkLink;
    private String grade;

    public HomeworkItem() {

    }

    public HomeworkItem(String title, String deadline, String homeworkLink, String grade) {
        this.title = title;
        this.deadline = deadline;
        this.homeworkLink = homeworkLink;
        this.grade = grade;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDeadline() {
        return deadline;
    }

    public void setDeadline(String deadline) {
        this.deadline = deadline;
    }

    public String getHomeworkLink() {
        return homeworkLink;
    }

    public void setHomeworkLink(String homeworkLink) {
        this.homeworkLink = homeworkLink;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }
}
