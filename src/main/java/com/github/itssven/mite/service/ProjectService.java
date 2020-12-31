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

import java.util.Set;

import com.github.itssven.mite.model.Project;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;


public interface ProjectService {
    Observable<Project> getFilteredActiveProjects(Set<String> filters);

    Single<Project> getProject(int id);

    Completable createProject(Project project);

    Completable updateProject(int id, Project project);

    Observable<Project> findProjectsByName(String name);

}
