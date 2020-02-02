package biz.schroeders.mite.endpoint;

import static biz.schroeders.mite.constants.MediaTypes.CONTENT_TYPE;
import static biz.schroeders.mite.constants.MediaTypes.JSON_MEDIA;

import java.util.LinkedList;

import biz.schroeders.mite.ApiError;
import biz.schroeders.mite.JsonRequestEnder;
import biz.schroeders.mite.VirtualProjectsStore;
import biz.schroeders.mite.constants.HttpCodes;
import biz.schroeders.mite.model.VirtualProject;
import com.google.gson.Gson;
import io.vertx.reactivex.core.buffer.Buffer;
import io.vertx.reactivex.ext.jdbc.JDBCClient;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VirtualProjects {
    private static final Logger LOGGER = LoggerFactory.getLogger(VirtualProjects.class);
    private static final Gson GSON = new Gson();

    private final VirtualProjectsStore virtualProjectsStore;

    public VirtualProjects(final Router router, final JDBCClient jdbcClient) {
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

        virtualProjectsStore = new VirtualProjectsStore(jdbcClient);
    }

    private void getAllVirtualProjects(final RoutingContext context) {
        LOGGER.debug("getAllVirtualProjects");
        virtualProjectsStore.getAllVirtualProjects()
                .collect(LinkedList<VirtualProject>::new, LinkedList<VirtualProject>::add)
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

    private void createVirtualProject(final RoutingContext context) {
        LOGGER.debug("createVirtualProject");
        context.request().toObservable()
                .firstOrError()
                .map(Buffer::toString)
                .map(str -> GSON.fromJson(str, VirtualProject.class))
                .flatMapCompletable(vp -> virtualProjectsStore.createVirtualProject(vp.getName()))
                .subscribe(new JsonRequestEnder(context));
    }

    private void deleteVirtualProject(final RoutingContext context) {
        LOGGER.debug("deleteVirtualProject");
        final int vId = Integer.parseInt(context.request().getParam("vId"));
        virtualProjectsStore.deleteVirtualProject(vId)
                .subscribe(new JsonRequestEnder(context));
    }

    private void createMapping(final RoutingContext context) {
        LOGGER.debug("createMapping");
        context.request().toObservable()
                .firstOrError()
                .map(Buffer::toString)
                .map(str -> GSON.fromJson(str, Mapping.class))
                .flatMapCompletable(m -> virtualProjectsStore.createMapping(m.getvId(), m.getpId()))
                .subscribe(new JsonRequestEnder(context));
    }

    private void deleteMapping(final RoutingContext context) {
        LOGGER.debug("deleteMapping");
        context.request().toObservable()
                .firstOrError()
                .map(Buffer::toString)
                .map(str -> GSON.fromJson(str, Mapping.class))
                .flatMapCompletable(m -> virtualProjectsStore.deleteMapping(m.getvId(), m.getpId()))
                .subscribe(new JsonRequestEnder(context));
    }

    private static class Mapping {
        private final Integer vId;
        private final Integer pId;

        public Mapping(final Integer vId, final Integer pId) {
            this.vId = vId;
            this.pId = pId;
        }

        public Integer getvId() {
            return vId;
        }

        public Integer getpId() {
            return pId;
        }
    }
}
