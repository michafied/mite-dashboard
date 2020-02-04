package biz.schroeders.mite.service;

import static biz.schroeders.mite.MiteServer.MITE_TOKEN_KEY;
import static biz.schroeders.mite.MiteServer.MITE_TOKEN_MAP;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

import biz.schroeders.mite.ApiError;
import biz.schroeders.mite.Configuration;
import biz.schroeders.mite.constants.HttpCodes;
import com.google.gson.Gson;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.core.buffer.Buffer;
import io.vertx.reactivex.ext.web.client.HttpRequest;
import io.vertx.reactivex.ext.web.client.WebClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MiteClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(MiteClient.class);
    private static final Gson GSON = new Gson();
    private static final String USER_AGENT = "mite-team-dashboard/v1.1";

    private final Vertx vertx;
    private final WebClient webClient;

    public MiteClient(final Vertx vertx, final WebClientOptions options, final Configuration config) {
        this.vertx = vertx;
        final String companyUserAgent = config.getCompany().isEmpty()
                ? USER_AGENT
                : config.getCompany().concat(" - ").concat(USER_AGENT);
        options.setUserAgent(companyUserAgent)
                .setUserAgentEnabled(true);
        webClient = WebClient.create(vertx, options);
    }

    public <T> Single<T> get(final String endpoint, final Map<String, String> query, final Type type) {
        return token()
                .map(token -> webClient.get(endpoint)
                        .putHeader("Accept", "*/*")
                        .putHeader("Content-Type", "application/json")
                        .putHeader("X-MiteApiKey", token))
                .map(request -> {
                    query.entrySet().forEach(entry -> request.addQueryParam(entry.getKey(), entry.getValue()));
                    return request;
                })
                .flatMap(HttpRequest<Buffer>::rxSend)
                .map(response -> {
                    final Buffer bodyBuffer = response.body();
                    if (response.statusCode() != HttpCodes.OK) {
                        LOGGER.error("{}", bodyBuffer);
                        throw new ApiError(response.statusMessage(), response.statusCode());
                    } else {
                        final String body = bodyBuffer.toString(StandardCharsets.UTF_8);
                        LOGGER.debug("mite: {}", body);
                        return GSON.fromJson(body, type);
                    }
                });
    }

    public <T> Single<T> get(final String endpoint, final Type type) {
        return get(endpoint, Collections.emptyMap(), type);
    }

    public <T> Completable post(final String endpoint, final T data) {
        return token()
                .flatMap(token -> webClient.post(endpoint)
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
