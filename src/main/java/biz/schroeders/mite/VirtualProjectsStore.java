package biz.schroeders.mite;

import java.util.HashMap;
import java.util.Map;

import biz.schroeders.mite.model.VirtualProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class VirtualProjectsStore {
    private static final Logger LOGGER = LoggerFactory.getLogger(VirtualProjectsStore.class);

    private final Map<Integer, Integer> projectToVproject;
    private final Map<Integer, VirtualProject.Builder> vProjects;

    public VirtualProjectsStore() {
        projectToVproject = new HashMap<>();
        projectToVproject.put(1, 1);
        vProjects = new HashMap<>();
        vProjects.put(1, VirtualProject.newBuilder(1, "test vProject"));
    }

    public Integer getBoundTo(final Integer projectId) {
        return projectToVproject.getOrDefault(projectId % 2, 0);
    }

    public VirtualProject.Builder getVprojectBuilder(final Integer id) {
        return vProjects.getOrDefault(id, VirtualProject.newBuilder(0, "not assigned to any vProject"));
    }
}
