package biz.schroeders.mite.model;

public class ProjectMapping {
    private final Integer vId;
    private final Integer pId;

    public ProjectMapping(final Integer vId, final Integer pId) {
        this.vId = vId;
        this.pId = pId;
    }

    public Integer getvId() {
        return vId;
    }

    public Integer getpId() {
        return pId;
    }
}
