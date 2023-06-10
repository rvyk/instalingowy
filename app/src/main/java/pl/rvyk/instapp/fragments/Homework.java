package pl.rvyk.instapp.fragments;

public class Homework {
    private String title;
    private String deadline;
    private String homeworkLink;
    private String grade;
    private boolean isDone;

    public Homework(String title, String deadline, String homeworkLink, String grade, boolean isDone) {
        this.title = title;
        this.deadline = deadline;
        this.homeworkLink = homeworkLink;
        this.grade = grade;
        this.isDone = isDone;
    }

    public String getTitle() {
        return title;
    }

    public String getDeadline() {
        return deadline;
    }

    public String getHomeworkLink() {
        return homeworkLink;
    }

    public String getGrade() {
        return grade;
    }

    public boolean isDone() {
        return isDone;
    }
}

