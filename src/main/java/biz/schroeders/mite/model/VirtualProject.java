package biz.schroeders.mite.model;

import java.util.ArrayList;
import java.util.List;

import biz.schroeders.mite.ApiError;
import biz.schroeders.mite.constants.HttpCodes;
import biz.schroeders.mite.request.Request;

public class VirtualProject implements Request<VirtualProject> {
    private final Integer id;
    private final String name;
    private final List<Project> children = new ArrayList<>();

    private VirtualProject(final Integer id, final String name, final List<Project> children) {
        this.id = id;
        this.name = name;
        this.children.addAll(children);
    }

    public static Builder newBuilder(final Integer pId, final String pName) {
        return new Builder()
                .withId(pId)
                .withName(pName);
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<Project> getChildren() {
        return children;
    }

    @Override
    public VirtualProject validate() {
        if (name == null || name.isEmpty()) {
            throw new ApiError("Name needs to be set", HttpCodes.BAD_REQUEST);
        }
        return this;
    }


    public static final class Builder {
        private final List<Project> children = new ArrayList<>();
        private Integer id;
        private String name;

        private Builder() {
        }

        public Builder withId(final Integer val) {
            id = val;
            return this;
        }

        public Builder withName(final String val) {
            name = val;
            return this;
        }

        public Builder addProject(final Project val) {
            children.add(val);
            return this;
        }

        public VirtualProject build() {
            return new VirtualProject(id, name, children);
        }
    }
}
