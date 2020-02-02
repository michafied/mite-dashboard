package biz.schroeders.mite.service;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import biz.schroeders.mite.model.Project;
import biz.schroeders.mite.model.ProjectMapping;
import biz.schroeders.mite.model.VirtualProject;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;

public class DefaultVirtualProjectsService implements VirtualProjectsService {
    private final ProjectService projectService;
    private final VirtualProjectsStore virtualProjectsStore;

    public DefaultVirtualProjectsService(final ProjectService projectService,
                                         final VirtualProjectsStore virtualProjectsStore) {
        this.projectService = projectService;
        this.virtualProjectsStore = virtualProjectsStore;
    }

    @Override
    public Observable<VirtualProject> getAllVirtualProjectsShallow() {
        return virtualProjectsStore.getAllVirtualProjects();
    }

    @Override
    public Observable<VirtualProject> getAllVirtualProjects(final Set<String> filters) {
        return projectService.getFilteredActiveProjects(filters)
                .flatMapSingle(project -> virtualProjectsStore
                        .getBoundTo(project.getId())
                        .map(id -> Project.newBuilder(project)
                                .withBoundTo(id)
                                .build()))
                .toMultimap(p -> p.getBoundTo().orElse(0))
                .flattenAsObservable(Map::entrySet)
                .flatMapSingle(entry -> virtualProjectsStore.getVprojectBuilder(entry.getKey())
                        .map(builder -> {
                            entry.getValue().forEach(builder::addProject);
                            return builder.build();
                        }))
                .collect(LinkedList<VirtualProject>::new, LinkedList<VirtualProject>::add)
                .flattenAsObservable(list -> {
                    list.sort(Comparator.comparingInt(VirtualProject::getId).reversed());
                    return list;
                });
    }

    @Override
    public Single<VirtualProject> getOneVirtualProject(final int id) {
        return virtualProjectsStore.getProjectsFor(id)
                .flatMapSingle(pId -> projectService.getProject(pId)
                        .map(project -> Project.newBuilder(project)
                                .withBoundTo(id)
                                .build()))
                .collect(LinkedList<Project>::new, LinkedList<Project>::add)
                .flatMap(projects -> virtualProjectsStore.getVprojectBuilder(id)
                        .map(builder -> {
                            projects.forEach(builder::addProject);
                            return builder.build();
                        }));

    }

    @Override
    public Completable createVirtualProject(final VirtualProject project) {
        return Single.just(project)
                .flatMapCompletable(vp -> virtualProjectsStore.createVirtualProject(vp.getName()));
    }

    @Override
    public Completable deleteVirtualProject(final int id) {
        return virtualProjectsStore.deleteVirtualProject(id);
    }

    @Override
    public Completable createMapping(final ProjectMapping mapping) {
        return Single.just(mapping)
                .flatMapCompletable(m -> virtualProjectsStore.createMapping(m.getvId(), m.getpId()));
    }

    @Override
    public Completable deleteMapping(final ProjectMapping mapping) {
        return Single.just(mapping)
                .flatMapCompletable(m -> virtualProjectsStore.deleteMapping(m.getvId(), m.getpId()));
    }
}
