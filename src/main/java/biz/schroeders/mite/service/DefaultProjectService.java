package biz.schroeders.mite.service;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import biz.schroeders.mite.model.MiteProject;
import biz.schroeders.mite.model.Project;
import biz.schroeders.mite.model.ProjectWrapper;
import com.google.gson.reflect.TypeToken;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;


public class DefaultProjectService implements ProjectService {
    private static final Type PROJECTS_TYPE = new TypeToken<List<ProjectWrapper>>() {
    }.getType();
    private final MiteClient miteClient;

    public DefaultProjectService(final MiteClient miteClient) {
        this.miteClient = miteClient;
    }


    @Override
    public Observable<Project> getFilteredActiveProjects(final Set<String> filters) {
        return miteClient.<List<ProjectWrapper>>get("/projects.json", PROJECTS_TYPE)
                .flattenAsObservable(projectWrapper -> projectWrapper
                        .stream()
                        .collect(Collectors.toList()))
                .map(ProjectWrapper::getProject)
                .map(MiteProject::toProject)
                .filter(p -> filters.contains("empty") || p.getBudget() > 0);
    }

    @Override
    public Single<Project> getProject(final int id) {
        return miteClient.<ProjectWrapper>get("/projects/" + id + ".json", ProjectWrapper.class)
                .map(ProjectWrapper::getProject)
                .map(MiteProject::toProject);
    }

    @Override
    public Completable createProject(final Project project) {
        return Single.just(project)
                .map(Project::validate)
                .map(Project::toMite)
                .map(ProjectWrapper::new)
                .flatMapCompletable(p -> miteClient.post("/projects.json", p));
    }

    @Override
    public Completable updateArchiveState(final int id, final Project project) {
        return Single.just(project)
                .map(Project::toArchivable)
                .map(ProjectWrapper::new)
                .flatMapCompletable(p -> miteClient.patch("/projects/" + id + ".json", p));
    }

    @Override
    public Observable<Project> findProjectsByName(final String name) {
        return miteClient.<List<ProjectWrapper>>get("/projects.json", Collections.singletonMap("name", name), PROJECTS_TYPE)
                .flattenAsObservable(projectWrapper -> projectWrapper
                        .stream()
                        .collect(Collectors.toList()))
                .mergeWith(miteClient.<List<ProjectWrapper>>get("/projects/archived.json", Collections.singletonMap("name", name), PROJECTS_TYPE)
                        .flattenAsObservable(projectWrapper -> projectWrapper
                                .stream()
                                .collect(Collectors.toList())))
                .map(ProjectWrapper::getProject)
                .map(MiteProject::toProject);
    }
}
