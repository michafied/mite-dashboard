package biz.schroeders.mite.endpoint;

import static biz.schroeders.mite.constants.MediaTypes.CONTENT_TYPE;
import static biz.schroeders.mite.constants.MediaTypes.JSON_MEDIA;

import java.lang.reflect.Type;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import biz.schroeders.mite.ApiError;
import biz.schroeders.mite.MiteClient;
import biz.schroeders.mite.constants.HttpCodes;
import biz.schroeders.mite.model.CustomerWrapper;

import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;

public class Customers {
    private static final Logger LOGGER = LoggerFactory.getLogger(Customers.class);
    private static final Gson GSON = new Gson();
    private static final Type CUSTOMERS_TYPE = new TypeToken<List<CustomerWrapper>>() {
    }.getType();

    private final MiteClient miteClient;

    public Customers(final Router router, final MiteClient miteClient) {
        router.get("/")
                .consumes(JSON_MEDIA)
                .handler(this::getAll);

        this.miteClient = miteClient;
    }

    private void getAll(final RoutingContext context) {
        LOGGER.debug("getAll");
        miteClient.<List<CustomerWrapper>>get("/customers.json", CUSTOMERS_TYPE)
                .map(customerWrappers -> customerWrappers
                        .stream()
                        .map(CustomerWrapper::getCustomer)
                        .collect(Collectors.toList()))
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
