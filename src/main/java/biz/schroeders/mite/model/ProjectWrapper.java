package biz.schroeders.mite.model;

public class ProjectWrapper {
    private final MiteProject project;

    public ProjectWrapper(final MiteProject project) {
        this.project = project;
    }

    public MiteProject getProject() {
        return project;
    }
}
