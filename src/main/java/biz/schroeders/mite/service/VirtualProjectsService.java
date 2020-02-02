package biz.schroeders.mite.service;

import java.util.Set;

import biz.schroeders.mite.model.ProjectMapping;
import biz.schroeders.mite.model.VirtualProject;
import io.reactivex.Completable;
import io.reactivex.Observable;

public interface VirtualProjectsService {

    Observable<VirtualProject> getAllVirtualProjectsShallow();

    Observable<VirtualProject> getAllVirtualProjects(Set<String> filters);

    Completable createVirtualProject(VirtualProject vProject);

    Completable deleteVirtualProject(int id);

    Completable createMapping(ProjectMapping mapping);

    Completable deleteMapping(ProjectMapping mapping);
}
