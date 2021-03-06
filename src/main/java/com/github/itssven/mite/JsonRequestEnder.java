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

import static com.github.itssven.mite.constants.MediaTypes.CONTENT_TYPE;
import static com.github.itssven.mite.constants.MediaTypes.JSON_MEDIA;

import java.util.concurrent.atomic.AtomicReference;

import com.github.itssven.mite.constants.HttpCodes;
import io.reactivex.CompletableObserver;
import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;
import io.reactivex.exceptions.CompositeException;
import io.reactivex.exceptions.Exceptions;
import io.reactivex.internal.disposables.DisposableHelper;
import io.reactivex.plugins.RxJavaPlugins;
import io.vertx.reactivex.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class JsonRequestEnder extends AtomicReference<Disposable>
        implements SingleObserver<String>, CompletableObserver, Disposable {
    private static final Logger LOGGER = LoggerFactory.getLogger(JsonRequestEnder.class);

    private final int resultCode;
    private final transient RoutingContext context;

    public JsonRequestEnder(final RoutingContext context) {
        this(HttpCodes.OK, context);
    }

    public JsonRequestEnder(final int resultCode, final RoutingContext context) {
        this.resultCode = resultCode;
        this.context = context;
    }

    @Override
    public void onSubscribe(final Disposable d) {
        DisposableHelper.setOnce(this, d);
    }

    @Override
    public void onSuccess(final String s) {
        LOGGER.debug("onSuccess(s)");
        lazySet(DisposableHelper.DISPOSED);
        try {
            context.response()
                    .setStatusCode(resultCode)
                    .putHeader(CONTENT_TYPE, JSON_MEDIA)
                    .end(s);
        } catch (final Throwable ex) {
            Exceptions.throwIfFatal(ex);
            RxJavaPlugins.onError(ex);
        }
    }

    @Override
    public void onError(final Throwable e) {
        LOGGER.debug("onError()", e);
        lazySet(DisposableHelper.DISPOSED);
        try {
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
        } catch (final Throwable ex) {
            Exceptions.throwIfFatal(ex);
            RxJavaPlugins.onError(new CompositeException(e, ex));
        }
    }

    @Override
    public void onComplete() {
        LOGGER.debug("onComplete()");
        lazySet(DisposableHelper.DISPOSED);
        try {
            context.response()
                    .setStatusCode(resultCode)
                    .putHeader(CONTENT_TYPE, JSON_MEDIA)
                    .end();
        } catch (final Throwable ex) {
            Exceptions.throwIfFatal(ex);
            RxJavaPlugins.onError(ex);
        }
    }

    @Override
    public void dispose() {
        DisposableHelper.dispose(this);
    }

    @Override
    public boolean isDisposed() {
        return get() == DisposableHelper.DISPOSED;
    }
}
