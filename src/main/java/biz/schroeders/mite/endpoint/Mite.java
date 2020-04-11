package biz.schroeders.mite.endpoint;

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

import static biz.schroeders.mite.MiteServer.MITE_TOKEN_KEY;
import static biz.schroeders.mite.MiteServer.MITE_TOKEN_MAP;
import static biz.schroeders.mite.constants.MediaTypes.CONTENT_TYPE;
import static biz.schroeders.mite.constants.MediaTypes.HTML_MEDIA;
import static biz.schroeders.mite.constants.MediaTypes.JSON_MEDIA;

import biz.schroeders.mite.ApiError;
import biz.schroeders.mite.constants.HttpCodes;
import biz.schroeders.mite.request.Request;
import biz.schroeders.mite.request.TokenRequest;
import com.google.gson.Gson;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.common.template.TemplateEngine;
import io.vertx.reactivex.ext.web.handler.StaticHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Mite {
    private static final Logger LOGGER = LoggerFactory.getLogger(Mite.class);
    private static final Gson GSON = new Gson();
    private static final Handler<RoutingContext> STATIC_RESOURCES = StaticHandler.create("mite-root")
            .setAllowRootFileSystemAccess(false);

    public Mite(final Router router, final TemplateEngine engine, final JsonObject templateConfig) {
        router.get("/dashboard")
                .handler(ctx -> engine.rxRender(templateConfig, "templates/dashboard.ftl")
                        .subscribe(ctx.response().putHeader(CONTENT_TYPE, HTML_MEDIA)::end, err -> LOGGER.error("", err)));
        router.get("/details")
                .handler(ctx -> {
                    final String id = ctx.queryParam("id").isEmpty() ? "" : ctx.queryParam("id").get(0);
                    engine.rxRender(new JsonObject()
                            .put("id", id), "templates/project-details.ftl")
                            .subscribe(ctx.response().putHeader(CONTENT_TYPE, HTML_MEDIA)::end, err -> LOGGER.error("", err));
                });
        router.get("/vDetails")
                .handler(ctx -> {
                    final String id = ctx.queryParam("id").isEmpty() ? "" : ctx.queryParam("id").get(0);
                    engine.rxRender(new JsonObject()
                            .put("id", id), "templates/vproject-details.ftl")
                            .subscribe(ctx.response().putHeader(CONTENT_TYPE, HTML_MEDIA)::end, err -> LOGGER.error("", err));
                });
        router.get("/*")
                .handler(STATIC_RESOURCES);
        router.post("/token")
                .consumes(JSON_MEDIA)
                .handler(this::saveToken);
    }

    private void saveToken(final RoutingContext context) {
        context.request()
                .toObservable()
                .map(buffer -> GSON.fromJson(buffer.toString(), TokenRequest.class))
                .map(Request::validate)
                .flatMapCompletable(token -> context.vertx()
                        .sharedData()
                        .<String, String>rxGetLocalAsyncMap(MITE_TOKEN_MAP)
                        .flatMapCompletable(map -> map.rxPut(MITE_TOKEN_KEY, token.getToken())))
                .subscribe(
                        () -> {
                            LOGGER.info("token saved");
                            context.response().end();
                        },
                        err -> {
                            LOGGER.error("could not save token", err);
                            if (err instanceof ApiError) {
                                context.response()
                                        .setStatusCode(((ApiError) err).getHttpCode())
                                        .putHeader(CONTENT_TYPE, JSON_MEDIA)
                                        .end(err.getMessage());
                            } else {
                                context.response().setStatusCode(HttpCodes.INTERNAL_SERVER_ERROR).end();
                            }
                        });
    }
}
