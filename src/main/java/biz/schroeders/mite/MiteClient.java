package biz.schroeders.mite;

import static biz.schroeders.mite.MiteServer.MITE_TOKEN_KEY;
import static biz.schroeders.mite.MiteServer.MITE_TOKEN_MAP;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

import biz.schroeders.mite.constants.HttpCodes;
import com.google.gson.Gson;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.core.buffer.Buffer;
import io.vertx.reactivex.ext.web.client.WebClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MiteClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(MiteClient.class);
    private static final Gson GSON = new Gson();
    private static final String USER_AGENT = "mite-team-dashboard/v1.1";

    private final Vertx vertx;
    private final WebClient webClient;
    private final String companyUserAgent;

    public MiteClient(final Vertx vertx, final WebClientOptions options, final Configuration config) {
        this.vertx = vertx;
        webClient = WebClient.create(vertx, options);
        companyUserAgent = config.getCompany().isEmpty()
                ? USER_AGENT
                : config.getCompany().concat(" - ").concat(USER_AGENT);
    }

    public <T> Single<T> get(final String endpoint, final Type type) {
        return token()
                .flatMap(token -> webClient.get(endpoint)
                        .putHeader("User-Agent", companyUserAgent)
                        .putHeader("Accept", "*/*")
                        .putHeader("Content-Type", "application/json")
                        .putHeader("X-MiteApiKey", token)
                        .rxSend())
                .map(response -> {
                    final String body = response.body().toString(StandardCharsets.UTF_8);
                    if (response.statusCode() != HttpCodes.OK) {
                        LOGGER.error("{}", body);
                        throw new ApiError(response.statusMessage(), response.statusCode());
                    } else {
                        LOGGER.debug("mite: {}", body);
                        return GSON.fromJson(body, type);
                    }
                });
    }

    public <T> Completable post(final String endpoint, final T data) {
        return token()
                .flatMap(token -> webClient.post(endpoint)
                        .putHeader("User-Agent", USER_AGENT)
                        .putHeader("Accept", "*/*")
                        .putHeader("Content-Type", "application/json")
                        .putHeader("X-MiteApiKey", token)
                        .rxSendBuffer(Buffer.buffer(GSON.toJson(data))))
                .map(response -> {
                    final String body = response.body().toString(StandardCharsets.UTF_8);
                    if (response.statusCode() != HttpCodes.CREATED) {
                        LOGGER.error("{}", body);
                        throw new ApiError(response.statusMessage(), response.statusCode());
                    }
                    return "";
                })
                .ignoreElement();
    }

    public <T> Completable patch(final String endpoint, final T data) {
        return token()
                .flatMap(token -> webClient.patch(endpoint)
                        .putHeader("User-Agent", USER_AGENT)
                        .putHeader("Accept", "*/*")
                        .putHeader("Content-Type", "application/json")
                        .putHeader("X-MiteApiKey", token)
                        .rxSendBuffer(Buffer.buffer(GSON.toJson(data))))
                .map(response -> {
                    if (response.statusCode() != HttpCodes.OK) {
                        LOGGER.error("not OK - {}", response.statusCode());
                        throw new ApiError(response.statusMessage(), response.statusCode());
                    }
                    return "";
                })
                .ignoreElement();
    }

    private Single<String> token() {
        return vertx.sharedData()
                .<String, String>rxGetLocalAsyncMap(MITE_TOKEN_MAP)
                .flatMapMaybe(map -> map.rxGet(MITE_TOKEN_KEY))
                .toSingle();
    }
}
