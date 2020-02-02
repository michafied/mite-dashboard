package biz.schroeders.mite.service;

import java.util.Set;

import biz.schroeders.mite.model.ProjectMapping;
import biz.schroeders.mite.model.VirtualProject;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;

public interface VirtualProjectsService {

    Observable<VirtualProject> getAllVirtualProjectsShallow();

    Observable<VirtualProject> getAllVirtualProjects(Set<String> filters);

    Single<VirtualProject> getOneVirtualProject(int id);

    Completable createVirtualProject(VirtualProject vProject);

    Completable deleteVirtualProject(int id);

    Completable createMapping(ProjectMapping mapping);

    Completable deleteMapping(ProjectMapping mapping);
}
