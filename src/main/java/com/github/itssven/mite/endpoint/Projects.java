package com.github.itssven.mite.endpoint;

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

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.github.itssven.mite.JsonRequestEnder;
import com.github.itssven.mite.constants.HttpCodes;
import com.github.itssven.mite.model.Project;
import com.github.itssven.mite.service.ProjectService;
import com.github.itssven.mite.constants.MediaTypes;
import com.google.gson.Gson;
import io.vertx.reactivex.core.buffer.Buffer;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Projects {
    private static final Logger LOGGER = LoggerFactory.getLogger(Projects.class);
    private static final Gson GSON = new Gson();
    private static final String FILTER_KEY = "filter";

    private final ProjectService projectService;

    public Projects(final Router router, final ProjectService projectService) {
        router.get("/")
                .consumes(MediaTypes.JSON_MEDIA)
                .produces(MediaTypes.JSON_MEDIA)
                .handler(this::getAll);
        router.post("/")
                .consumes(MediaTypes.JSON_MEDIA)
                .produces(MediaTypes.JSON_MEDIA)
                .handler(this::create);
        router.get("/:projectId")
                .consumes(MediaTypes.JSON_MEDIA)
                .produces(MediaTypes.JSON_MEDIA)
                .handler(this::getOne);
        router.patch("/:projectId")
                .consumes(MediaTypes.JSON_MEDIA)
                .produces(MediaTypes.JSON_MEDIA)
                .handler(this::archiver);

        this.projectService = projectService;
    }

    private void getAll(final RoutingContext context) {
        LOGGER.debug("getAll");

        final List<String> params = context.queryParam(FILTER_KEY);
        final Set<String> filters = new HashSet<>(params);

        projectService.getFilteredActiveProjects(filters)
                .collect(LinkedList<Project>::new, LinkedList<Project>::add)
                .map(GSON::toJson)
                .subscribe(new JsonRequestEnder(context));
    }

    private void getOne(final RoutingContext context) {
        final int projectId = Integer.parseInt(context.request().getParam("projectId"));
        LOGGER.debug("getOne {}", projectId);

        projectService.getProject(projectId)
                .map(GSON::toJson)
                .subscribe(new JsonRequestEnder(context));
    }

    private void archiver(final RoutingContext context) {
        final int projectId = Integer.parseInt(context.request().getParam("projectId"));
        LOGGER.debug("(un-)archive {}", projectId);

        context.request().toObservable()
                .firstOrError()
                .map(Buffer::toString)
                .map(str -> GSON.fromJson(str, Project.class))
                .flatMapCompletable(json -> projectService.updateArchiveState(projectId, json))
                .subscribe(new JsonRequestEnder(HttpCodes.CREATED, context));
    }

    private void create(final RoutingContext context) {
        LOGGER.debug("create");
        context.request().toObservable()
                .firstOrError()
                .map(Buffer::toString)
                .map(str -> GSON.fromJson(str, Project.class))
                .flatMapCompletable(projectService::createProject)
                .subscribe(new JsonRequestEnder(HttpCodes.CREATED, context));
    }
}
