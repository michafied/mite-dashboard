package biz.schroeders.mite.endpoint;

import static biz.schroeders.mite.constants.MediaTypes.JSON_MEDIA;

import java.lang.reflect.Type;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import biz.schroeders.mite.JsonRequestEnder;
import biz.schroeders.mite.MiteClient;
import biz.schroeders.mite.VirtualProjectsStore;
import biz.schroeders.mite.constants.HttpCodes;
import biz.schroeders.mite.model.MiteProject;
import biz.schroeders.mite.model.Project;
import biz.schroeders.mite.model.ProjectWrapper;
import biz.schroeders.mite.model.VirtualProject;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.vertx.reactivex.core.buffer.Buffer;
import io.vertx.reactivex.ext.jdbc.JDBCClient;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Projects {
    private static final Logger LOGGER = LoggerFactory.getLogger(Projects.class);
    private static final Gson GSON = new Gson();
    private static final Type PROJECTS_TYPE = new TypeToken<List<ProjectWrapper>>() {
    }.getType();

    private static final String FILTER_KEY = "filter";

    private final MiteClient miteClient;
    private final VirtualProjectsStore virtualProjectsStore;

    public Projects(final Router router, final MiteClient miteClient, final JDBCClient jdbcClient) {
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

        this.miteClient = miteClient;
        virtualProjectsStore = new VirtualProjectsStore(jdbcClient);
    }

    private void getAll(final RoutingContext context) {
        LOGGER.debug("getAll");
        final List<String> params = context.queryParam(FILTER_KEY);
        final Set<String> filters = new HashSet<>(params);

        miteClient.<List<ProjectWrapper>>get("/projects.json", PROJECTS_TYPE)
                .flattenAsObservable(projectWrapper -> projectWrapper
                        .stream()
                        .map(ProjectWrapper::getProject)
                        .map(MiteProject::toProject)
                        .filter(p -> filters.contains("empty") || p.getBudget() > 0)
                        .collect(Collectors.toList()))
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
                .map(list -> {
                    list.sort(Comparator.comparingInt(VirtualProject::getId).reversed());
                    return list;
                })
                .map(GSON::toJson)
                .subscribe(new JsonRequestEnder(context));
    }

    private void getOne(final RoutingContext context) {
        LOGGER.debug("getOne");
        final int projectId = Integer.parseInt(context.request().getParam("projectId"));
        miteClient.<ProjectWrapper>get("/projects/" + projectId + ".json", ProjectWrapper.class)
                .map(ProjectWrapper::getProject)
                .map(MiteProject::toProject)
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
                .map(Project::toMite)
                .map(ProjectWrapper::new)
                .flatMapCompletable(project -> miteClient.<ProjectWrapper>patch("/projects/" + projectId + ".json", project))
                .subscribe(new JsonRequestEnder(HttpCodes.CREATED, context));
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
                .subscribe(new JsonRequestEnder(HttpCodes.CREATED, context));
    }
}
