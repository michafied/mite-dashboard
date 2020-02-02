package biz.schroeders.mite.endpoint;

import static biz.schroeders.mite.constants.MediaTypes.JSON_MEDIA;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import biz.schroeders.mite.JsonRequestEnder;
import biz.schroeders.mite.model.ProjectMapping;
import biz.schroeders.mite.model.VirtualProject;
import biz.schroeders.mite.service.VirtualProjectsService;
import com.google.gson.Gson;
import io.reactivex.Observable;
import io.vertx.reactivex.core.buffer.Buffer;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VirtualProjects {
    private static final Logger LOGGER = LoggerFactory.getLogger(VirtualProjects.class);
    private static final String FILTER_KEY = "filter";
    private static final String SHALLOW_KEY = "shallow";
    private static final Gson GSON = new Gson();

    private final VirtualProjectsService virtualProjectsService;

    public VirtualProjects(final Router router, final VirtualProjectsService virtualProjectsService) {
        router.get("/")
                .consumes(JSON_MEDIA)
                .handler(this::getAllVirtualProjects);
        router.post("/")
                .consumes(JSON_MEDIA)
                .handler(this::createVirtualProject);
        router.delete("/:vId")
                .consumes(JSON_MEDIA)
                .handler(this::deleteVirtualProject);
        router.post("/mapping")
                .consumes(JSON_MEDIA)
                .handler(this::createMapping);
        router.delete("/mapping")
                .consumes(JSON_MEDIA)
                .handler(this::deleteMapping);

        this.virtualProjectsService = virtualProjectsService;
    }

    private void getAllVirtualProjects(final RoutingContext context) {
        final List<String> params = context.queryParam(FILTER_KEY);
        final Set<String> filters = new HashSet<>(params);
        final boolean shallow = !context.queryParam(SHALLOW_KEY).isEmpty();

        LOGGER.debug("getAllVirtualProjects shallow = {}", shallow);

        final Observable<VirtualProject> vp = shallow
                ? virtualProjectsService.getAllVirtualProjectsShallow()
                : virtualProjectsService.getAllVirtualProjects(filters);
        vp.collect(LinkedList<VirtualProject>::new, LinkedList<VirtualProject>::add)
                .map(GSON::toJson)
                .subscribe(new JsonRequestEnder(context));
    }

    private void createVirtualProject(final RoutingContext context) {
        LOGGER.debug("createVirtualProject");
        context.request().toObservable()
                .firstOrError()
                .map(Buffer::toString)
                .map(str -> GSON.fromJson(str, VirtualProject.class))
                .flatMapCompletable(virtualProjectsService::createVirtualProject)
                .subscribe(new JsonRequestEnder(context));
    }

    private void deleteVirtualProject(final RoutingContext context) {
        LOGGER.debug("deleteVirtualProject");
        final int vId = Integer.parseInt(context.request().getParam("vId"));
        virtualProjectsService.deleteVirtualProject(vId)
                .subscribe(new JsonRequestEnder(context));
    }

    private void createMapping(final RoutingContext context) {
        LOGGER.debug("createMapping");
        context.request().toObservable()
                .firstOrError()
                .map(Buffer::toString)
                .map(str -> GSON.fromJson(str, ProjectMapping.class))
                .flatMapCompletable(virtualProjectsService::createMapping)
                .subscribe(new JsonRequestEnder(context));
    }

    private void deleteMapping(final RoutingContext context) {
        LOGGER.debug("deleteMapping");
        context.request().toObservable()
                .firstOrError()
                .map(Buffer::toString)
                .map(str -> GSON.fromJson(str, ProjectMapping.class))
                .flatMapCompletable(virtualProjectsService::deleteMapping)
                .subscribe(new JsonRequestEnder(context));
    }
}
