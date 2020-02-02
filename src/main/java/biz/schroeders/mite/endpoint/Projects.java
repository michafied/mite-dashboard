package biz.schroeders.mite.endpoint;

import static biz.schroeders.mite.constants.MediaTypes.JSON_MEDIA;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import biz.schroeders.mite.JsonRequestEnder;
import biz.schroeders.mite.constants.HttpCodes;
import biz.schroeders.mite.model.Project;
import biz.schroeders.mite.service.ProjectService;
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
                .consumes(JSON_MEDIA)
                .produces(JSON_MEDIA)
                .handler(this::getAll);
        router.post("/")
                .consumes(JSON_MEDIA)
                .produces(JSON_MEDIA)
                .handler(this::create);
        router.get("/:projectId")
                .consumes(JSON_MEDIA)
                .produces(JSON_MEDIA)
                .handler(this::getOne);
        router.patch("/:projectId")
                .consumes(JSON_MEDIA)
                .produces(JSON_MEDIA)
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
