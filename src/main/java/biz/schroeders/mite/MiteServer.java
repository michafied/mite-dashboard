package biz.schroeders.mite;

import java.util.Arrays;

import biz.schroeders.mite.constants.HttpCodes;
import biz.schroeders.mite.endpoint.Customers;
import biz.schroeders.mite.endpoint.Mite;
import biz.schroeders.mite.endpoint.Projects;
import biz.schroeders.mite.endpoint.Times;
import io.reactivex.Completable;
import io.reactivex.plugins.RxJavaPlugins;
import io.vertx.core.Launcher;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.reactivex.core.RxHelper;
import io.vertx.reactivex.core.http.HttpServerRequest;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MiteServer extends io.vertx.reactivex.core.AbstractVerticle {
    public static final String MITE_TOKEN_MAP = "mite-map";
    public static final String MITE_TOKEN_KEY = "mite-key";
    private static final Logger LOGGER = LoggerFactory.getLogger(MiteServer.class);
    private Configuration configuration;

    public static void main(final String[] args) {
        final String[] augmentedArgs = new String[args.length + 2];
        augmentedArgs[0] = "run";
        augmentedArgs[1] = MiteServer.class.getCanonicalName();
        for (int i = 0; i < args.length; ++i) {
            augmentedArgs[i + 2] = args[i];
        }
        LOGGER.info("main {}", Arrays.asList(augmentedArgs));
        Launcher.main(augmentedArgs);
    }

    @Override
    public Completable rxStart() {
        LOGGER.info("rxStart");

        configuration = new Configuration(vertx);

        RxJavaPlugins.setComputationSchedulerHandler(s -> RxHelper.scheduler(vertx));
        RxJavaPlugins.setIoSchedulerHandler(s -> RxHelper.blockingScheduler(vertx));
        RxJavaPlugins.setNewThreadSchedulerHandler(s -> RxHelper.scheduler(vertx));

        final Router router = Router.router(vertx);

        final WebClientOptions options = new WebClientOptions()
                .setSsl(true)
                .setDefaultHost(configuration.getMiteApi().getHost())
                .setDefaultPort(configuration.getMiteApi().getPort())
                .setLogActivity(LOGGER.isTraceEnabled())
                .setKeepAlive(true);
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

        router.mountSubRouter("/mite", mite)
                .mountSubRouter("/projects", projects)
                .mountSubRouter("/customers", customers)
                .mountSubRouter("/times", times);

        new Mite(mite, configuration.getTemplateEngine(), configuration.getTemplateConfig());
        new Projects(projects, miteClient);
        new Customers(customers, miteClient);
        new Times(times, miteClient);

        return vertx.createHttpServer()
                .requestHandler(router)
                .rxListen(configuration.getMyPort())
                .ignoreElement();
    }

    @Override
    public Completable rxStop() {
        return Completable.fromRunnable(() -> configuration.close());
    }

    private void log(final RoutingContext context) {
        final HttpServerRequest request = context.request();
        final String path = request.path();
        final String method = request.rawMethod();
        LOGGER.debug("log: {} {}", method, path);
        context.next();
    }

    private void handleToken(final RoutingContext context, final boolean tokenAvailable) {
        LOGGER.info("Token found: {}", tokenAvailable);
        if (tokenAvailable) {
            context.next();
        } else {
            final HttpServerRequest request = context.request();
            final String path = request.path();
            final String method = request.rawMethod();
            if (("POST".equals(method) && path.startsWith("/mite/token"))
                    || ("GET".equals(method) && (path.matches("/mite/(?:token|css|js).*")))) {
                context.next();
            } else {
                context.response()
                        .setStatusCode(HttpCodes.MOVED_TEMPORARILY)
                        .putHeader("Location", "/mite/token.html")
                        .end();
            }
        }
    }
}
