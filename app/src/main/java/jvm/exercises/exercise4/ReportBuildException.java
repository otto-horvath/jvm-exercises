package jvm.exercises.exercise4;

public class ReportBuildException extends RuntimeException {
    private final String taskTitle;

    public ReportBuildException(String taskTitle, Throwable cause) {
        super("Report task failed: " + taskTitle, cause);
        this.taskTitle = taskTitle;
    }

    public String taskTitle() {
        return taskTitle;
    }
}
