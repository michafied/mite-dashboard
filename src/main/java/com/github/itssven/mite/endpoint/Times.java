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

import java.lang.reflect.Type;
import java.time.Duration;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.github.itssven.mite.JsonRequestEnder;
import com.github.itssven.mite.model.MiteTime;
import com.github.itssven.mite.model.Time;
import com.github.itssven.mite.model.TimeWrapper;
import com.github.itssven.mite.service.MiteClient;
import com.github.itssven.mite.constants.MediaTypes;
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
                .consumes(MediaTypes.JSON_MEDIA)
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
