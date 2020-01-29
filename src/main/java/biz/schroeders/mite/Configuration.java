package biz.schroeders.mite;

import java.io.Closeable;
import java.util.Optional;
import java.util.regex.Pattern;

import biz.schroeders.mite.model.MiteApi;
import biz.schroeders.mite.model.Project;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.web.common.template.TemplateEngine;
import io.vertx.reactivex.ext.web.templ.freemarker.FreeMarkerTemplateEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Configuration implements Closeable {
    private static final Logger LOGGER = LoggerFactory.getLogger(Configuration.class);
    private static final String TEMPLATE_CONFIG_KEY = "templateConfig";
    private static final String PROJECT_SPEC_KEY = "projectSpec";
    private static final String NAME_MATCHES_KEY = "nameMatches";

    private final int myPort;
    private final MiteApi miteApi;
    private final TemplateEngine templateEngine;
    private final JsonObject templateConfig;
    private final String company;
    private final JsonObject jdbcConfig;
    private final String token;

    public Configuration(final Vertx vertx) {
        LOGGER.debug("create config");
        try {
            final JsonObject config = vertx.getOrCreateContext().config();
            LOGGER.info("{}", config);
            myPort = config.getInteger("myPort", 9090);
            final String miteHost = config.getString("miteHost");
            miteApi = new MiteApi(miteHost, 443);
            templateEngine = FreeMarkerTemplateEngine.create(vertx)
                    .setMaxCacheSize(1000);
            templateConfig = config.getJsonObject(TEMPLATE_CONFIG_KEY, new JsonObject()
                    .put(PROJECT_SPEC_KEY, new JsonObject()
                            .put(NAME_MATCHES_KEY, ".+")));

            jdbcConfig = config.getJsonObject("jdbcConfig", new JsonObject()
                    .put("url", "jdbc:hsqldb:file:./db/mite-dashboard;shutdown=true;encoding=UTF-8;sql.syntax_mys=true")
                    .put("driver_class", "org.hsqldb.jdbcDriver")
                    .put("max_pool_size", 30));

            if (templateConfig.getJsonObject(PROJECT_SPEC_KEY) != null
                    && templateConfig.getJsonObject(PROJECT_SPEC_KEY).getString(NAME_MATCHES_KEY) != null) {
                Project.register(Pattern.compile(templateConfig
                        .getJsonObject(PROJECT_SPEC_KEY)
                        .getString(NAME_MATCHES_KEY)));
            }

            token = config.getString("miteApiToken", null);

            company = config.getString("companyName", "");
        } catch (final Exception e) {
            LOGGER.error("exception", e);
            throw new IllegalArgumentException(e);
        }
    }

    public String getCompany() {
        return company;
    }

    public int getMyPort() {
        return myPort;
    }

    public MiteApi getMiteApi() {
        return miteApi;
    }

    public TemplateEngine getTemplateEngine() {
        return templateEngine;
    }

    public JsonObject getTemplateConfig() {
        return templateConfig;
    }

    public JsonObject getJdbcConfig() {
        return jdbcConfig;
    }

    public Optional<String> getToken() {
        return Optional.ofNullable(token);
    }

    @Override
    public void close() {
        LOGGER.debug("close(...)");
    }
}
