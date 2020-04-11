package com.github.itssven.mite;

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

import com.github.itssven.mite.constants.HttpCodes;
import com.github.itssven.mite.endpoint.Customers;
import com.github.itssven.mite.endpoint.Mite;
import com.github.itssven.mite.endpoint.Projects;
import com.github.itssven.mite.endpoint.Times;
import com.github.itssven.mite.endpoint.VirtualProjects;
import com.github.itssven.mite.service.DefaultProjectService;
import com.github.itssven.mite.service.DefaultVirtualProjectsService;
import com.github.itssven.mite.service.MiteClient;
import com.github.itssven.mite.service.ProjectService;
import com.github.itssven.mite.service.VirtualProjectsService;
import com.github.itssven.mite.service.VirtualProjectsStore;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.plugins.RxJavaPlugins;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.reactivex.core.RxHelper;
import io.vertx.reactivex.core.http.HttpServerRequest;
import io.vertx.reactivex.ext.dropwizard.MetricsService;
import io.vertx.reactivex.ext.jdbc.JDBCClient;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MiteServer extends io.vertx.reactivex.core.AbstractVerticle {
    public static final String MITE_TOKEN_MAP = "mite-map";
    public static final String MITE_TOKEN_KEY = "mite-key";
    private static final Logger LOGGER = LoggerFactory.getLogger(MiteServer.class);
    private Configuration configuration;
    private JDBCClient jdbcClient;

    @Override
    public Completable rxStart() {
        LOGGER.info("rxStart");

        configuration = new Configuration(vertx);
        jdbcClient = JDBCClient.createShared(vertx, configuration.getJdbcConfig());
        configuration.getToken().ifPresent(
                token -> vertx.sharedData()
                        .rxGetLocalAsyncMap(MITE_TOKEN_MAP)
                        .flatMapCompletable(map -> map.rxPut(MITE_TOKEN_KEY, token))
                        .blockingAwait()
        );

        RxJavaPlugins.setComputationSchedulerHandler(s -> RxHelper.scheduler(vertx));
        RxJavaPlugins.setIoSchedulerHandler(s -> RxHelper.blockingScheduler(vertx));
        RxJavaPlugins.setNewThreadSchedulerHandler(s -> RxHelper.scheduler(vertx));

        final Router router = Router.router(vertx);

        final WebClientOptions options = new WebClientOptions()
                .setSsl(true)
                .setDefaultHost(configuration.getMiteApi().getHost())
                .setDefaultPort(configuration.getMiteApi().getPort())
                .setLogActivity(false)
                .setKeepAlive(true)
                .setFollowRedirects(true);
        final MiteClient miteClient = new MiteClient(vertx, options, configuration);

        router.route("/*").handler(this::log);
        router.route("/*")
                .handler(ctx -> ctx.vertx().sharedData()
                        .rxGetLocalAsyncMap(MITE_TOKEN_MAP)
                        .flatMapMaybe(map -> map.rxGet(MITE_TOKEN_KEY))
                        .isEmpty()
                        .subscribe(
                                notToken -> handleToken(ctx, !notToken),
                                err -> {
                                    LOGGER.error("", err);
                                    ctx.fail(HttpCodes.INTERNAL_SERVER_ERROR);
                                }));

        final Router mite = Router.router(vertx);
        final Router projects = Router.router(vertx);
        final Router customers = Router.router(vertx);
        final Router times = Router.router(vertx);
        final Router vProjects = Router.router(vertx);

        router.mountSubRouter("/", mite)
                .mountSubRouter("/projects", projects)
                .mountSubRouter("/customers", customers)
                .mountSubRouter("/times", times)
                .mountSubRouter("/vProjects", vProjects);

        final VirtualProjectsStore virtualProjectsStore = new VirtualProjectsStore(jdbcClient);
        final ProjectService projectService = new DefaultProjectService(miteClient);
        final VirtualProjectsService virtualProjectsService = new DefaultVirtualProjectsService(projectService,
                virtualProjectsStore);

        new Mite(mite, configuration.getTemplateEngine(), configuration.getTemplateConfig());
        new Projects(projects, projectService);
        new Customers(customers, miteClient);
        new Times(times, miteClient);
        new VirtualProjects(vProjects, virtualProjectsService);
        final MetricsService metricsService = MetricsService.create(vertx);

        return vertx.createHttpServer()
                .requestHandler(router)
                .rxListen(configuration.getMyPort())
                .map(httpServer -> router.get("/metrics").handler(ctx ->
                        Single.just(metricsService)
                                .map(service -> metricsService.getMetricsSnapshot("vertx.http.servers"))
                                .map(Object::toString)
                                .subscribe(new JsonRequestEnder(ctx))
                ))
                .ignoreElement();
    }

    @Override
    public Completable rxStop() {
        return Completable.fromRunnable(() -> configuration.close())
                .andThen(Completable.fromRunnable(jdbcClient::close));
    }

    private void log(final RoutingContext context) {
        final HttpServerRequest request = context.request();
        final String path = request.path();
        final String method = request.rawMethod();
        LOGGER.info("log: {} {}", method, path);
        context.next();
    }

    private void handleToken(final RoutingContext context, final boolean tokenAvailable) {
        LOGGER.debug("Token found: {}", tokenAvailable);
        if (tokenAvailable) {
            context.next();
        } else {
            final HttpServerRequest request = context.request();
            final String path = request.path();
            final String method = request.rawMethod();
            if (("POST".equals(method) && path.startsWith("/token"))
                    || ("GET".equals(method) && (path.matches("/(?:token|css|js).*")))) {
                context.next();
            } else {
                context.response()
                        .setStatusCode(HttpCodes.MOVED_TEMPORARILY)
                        .putHeader("Location", "./token.html")
                        .end();
            }
        }
    }
}
