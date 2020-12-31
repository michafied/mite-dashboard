package com.github.itssven.mite.service;

/*
    This file is part of mite-dashboard.

    mite-dashboard is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    mite-dashboard is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with mite-dashboard.  If not, see <http://www.gnu.org/licenses/>.
*/

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.github.itssven.mite.model.MiteProject;
import com.github.itssven.mite.model.Project;
import com.github.itssven.mite.model.ProjectWrapper;
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
                .flattenAsObservable(projectWrappers -> projectWrappers)
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
    public Completable updateProject(final int id, final Project project) {
        return Single.just(project)
                .map(Project::toMitePatch)
                .map(ProjectWrapper::new)
                .flatMapCompletable(p -> miteClient.patch("/projects/" + id + ".json", p));
    }

    @Override
    public Observable<Project> findProjectsByName(final String name) {
        return miteClient.<List<ProjectWrapper>>get("/projects.json", Collections.singletonMap("name", name), PROJECTS_TYPE)
                .flattenAsObservable(projectWrappers -> projectWrappers)
                .mergeWith(miteClient.<List<ProjectWrapper>>get("/projects/archived.json", Collections.singletonMap("name", name), PROJECTS_TYPE)
                        .flattenAsObservable(projectWrappers -> projectWrappers))
                .map(ProjectWrapper::getProject)
                .map(MiteProject::toProject);
    }
}
