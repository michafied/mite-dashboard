package biz.schroeders.mite.endpoint;

import static biz.schroeders.mite.constants.MediaTypes.CONTENT_TYPE;
import static biz.schroeders.mite.constants.MediaTypes.JSON_MEDIA;

import java.lang.reflect.Type;
import java.time.Duration;
import java.util.List;

import biz.schroeders.mite.ApiError;
import biz.schroeders.mite.MiteClient;
import biz.schroeders.mite.constants.HttpCodes;
import biz.schroeders.mite.model.MiteTime;
import biz.schroeders.mite.model.Time;
import biz.schroeders.mite.model.TimeWrapper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Times {
    private static final Logger LOGGER = LoggerFactory.getLogger(Times.class);
    private static final Gson GSON = new Gson();
    private static final Type CUSTOMERS_TYPE = new TypeToken<List<TimeWrapper>>() {
    }.getType();

    private final MiteClient miteClient;

    public Times(final Router router, final MiteClient miteClient) {
        router.get("/:projectId")
                .consumes(JSON_MEDIA)
                .handler(this::getOne);

        this.miteClient = miteClient;
    }

    private void getOne(final RoutingContext context) {
        LOGGER.debug("getOne");
        final int projectId = Integer.parseInt(context.request().getParam("projectId"));
        miteClient.<List<TimeWrapper>>get("/time_entries.json?project_id=" + projectId, CUSTOMERS_TYPE)
                .map(list -> Duration.ofMinutes(list.stream()
                        .map(TimeWrapper::getTimeEntry)
                        .mapToInt(MiteTime::getMinutes)
                        .sum()))
                .map(Time::new)
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
}
