package biz.schroeders.mite.endpoint;

import static biz.schroeders.mite.constants.MediaTypes.JSON_MEDIA;

import java.lang.reflect.Type;
import java.time.Duration;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import biz.schroeders.mite.JsonRequestEnder;
import biz.schroeders.mite.model.MiteTime;
import biz.schroeders.mite.model.Time;
import biz.schroeders.mite.model.TimeWrapper;
import biz.schroeders.mite.service.MiteClient;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Times {
    private static final Logger LOGGER = LoggerFactory.getLogger(Times.class);
    private static final Gson GSON = new Gson();
    private static final Type TIMES_TYPE = new TypeToken<List<TimeWrapper>>() {
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
        final boolean split = !context.queryParam("split").isEmpty();

        if (split) {
            getSplitted(context);
            return;
        }

        miteClient.<List<TimeWrapper>>get("/time_entries.json?project_id=" + projectId, TIMES_TYPE)
                .map(list -> Duration.ofMinutes(list.stream()
                        .map(TimeWrapper::getTimeEntry)
                        .mapToInt(MiteTime::getMinutes)
                        .sum()))
                .map(Time::new)
                .map(GSON::toJson)
                .subscribe(new JsonRequestEnder(context));
    }

    private void getSplitted(final RoutingContext context) {
        LOGGER.debug("getSplitted");
        final int projectId = Integer.parseInt(context.request().getParam("projectId"));
        miteClient.<List<TimeWrapper>>get("/time_entries.json?project_id=" + projectId, TIMES_TYPE)
                .flattenAsObservable(list -> list.stream()
                        .map(TimeWrapper::getTimeEntry).collect(Collectors.toList()))
                .toMultimap(MiteTime::getServiceName)
                .flattenAsObservable(Map::entrySet)
                .map(entry -> new Time(entry.getKey(), Duration.ofMinutes(entry.getValue().stream().mapToLong(MiteTime::getMinutes).sum())))
                .collect(LinkedList<Time>::new, LinkedList<Time>::add)
                .map(GSON::toJson)
                .subscribe(new JsonRequestEnder(context));
    }
}
