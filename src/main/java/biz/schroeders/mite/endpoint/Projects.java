package biz.schroeders.mite.endpoint;

import static biz.schroeders.mite.constants.MediaTypes.CONTENT_TYPE;
import static biz.schroeders.mite.constants.MediaTypes.JSON_MEDIA;

import java.lang.reflect.Type;
import java.util.List;
import java.util.stream.Collectors;

import biz.schroeders.mite.ApiError;
import biz.schroeders.mite.MiteClient;
import biz.schroeders.mite.constants.HttpCodes;
import biz.schroeders.mite.model.MiteProject;
import biz.schroeders.mite.model.Project;
import biz.schroeders.mite.model.ProjectWrapper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.vertx.reactivex.core.buffer.Buffer;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Projects {
    private static final Logger LOGGER = LoggerFactory.getLogger(Projects.class);
    private static final Gson GSON = new Gson();
    private static final Type PROJECTS_TYPE = new TypeToken<List<ProjectWrapper>>() {
    }.getType();

    private final MiteClient miteClient;

    public Projects(final Router router, final MiteClient miteClient) {
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
       /*
       router.put("/:projectId")
                .consumes(JSON_MEDIA)
                .produces(JSON_MEDIA)
                .handler(this::test2);
        */

        this.miteClient = miteClient;
    }

    private void getAll(final RoutingContext context) {
        LOGGER.debug("getAll");
        miteClient.<List<ProjectWrapper>>get("/projects.json", PROJECTS_TYPE)
                .map(projectWrapper -> projectWrapper
                        .stream()
                        .map(ProjectWrapper::getProject)
                        .map(MiteProject::toProject)
                        .collect(Collectors.toList()))
                .map(GSON::toJson)
                .subscribe(context.response().putHeader(CONTENT_TYPE, JSON_MEDIA)::end,
                        e -> {
                            if (e instanceof ApiError) {
                                context.response()
                                        .setStatusCode(((ApiError) e).getHttpCode())
                                        .putHeader(CONTENT_TYPE, JSON_MEDIA)
                                        .end(e.getMessage());
                            } else {
                                LOGGER.error("", e);
                                context.response()
                                        .setStatusCode(HttpCodes.INTERNAL_SERVER_ERROR)
                                        .end();
                            }
                        });
    }

    private void getOne(final RoutingContext context) {
        LOGGER.debug("getOne");
        final int projectId = Integer.parseInt(context.request().getParam("projectId"));
        miteClient.<ProjectWrapper>get("/projects/" + projectId + ".json", ProjectWrapper.class)
                .map(ProjectWrapper::getProject)
                .map(MiteProject::toProject)
                .map(GSON::toJson)
                .subscribe(context.response().putHeader(CONTENT_TYPE, JSON_MEDIA)::end,
                        e -> {
                            if (e instanceof ApiError) {
                                context.response()
                                        .setStatusCode(((ApiError) e).getHttpCode())
                                        .putHeader(CONTENT_TYPE, JSON_MEDIA)
                                        .end(e.getMessage());
                            } else {
                                LOGGER.error("error", e);
                                context.response()
                                        .setStatusCode(HttpCodes.INTERNAL_SERVER_ERROR)
                                        .end();
                            }
                        });
    }

    private void create(final RoutingContext context) {
        LOGGER.debug("create");
        context.request().toObservable()
                .firstOrError()
                .map(Buffer::toString)
                .map(str -> GSON.fromJson(str, Project.class))
                .map(Project::validate)
                .map(Project::toMite)
                .map(ProjectWrapper::new)
                .flatMapCompletable(project -> miteClient.<ProjectWrapper>post("/projects.json", project))
                .subscribe(() -> context.response()
                                .setStatusCode(HttpCodes.CREATED)
                                .end(),
                        e -> {
                            if (e instanceof ApiError) {
                                context.response()
                                        .setStatusCode(((ApiError) e).getHttpCode())
                                        .putHeader(CONTENT_TYPE, JSON_MEDIA)
                                        .end(e.getMessage());
                            } else {
                                LOGGER.error("error", e);
                                context.response()
                                        .setStatusCode(HttpCodes.INTERNAL_SERVER_ERROR)
                                        .end();
                            }
                        });
    }
}
