package biz.schroeders.mite.service;

import java.util.Set;

import biz.schroeders.mite.model.Project;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;


public interface ProjectService {
    Observable<Project> getFilteredActiveProjects(Set<String> filters);

    Single<Project> getProject(int id);

    Completable createProject(Project project);

    Completable updateArchiveState(int id, Project project);

    // Completable updateProject(String projectJson);
}
