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

import static biz.schroeders.mite.constants.MediaTypes.JSON_MEDIA;

import java.lang.reflect.Type;
import java.util.List;
import java.util.stream.Collectors;

import biz.schroeders.mite.JsonRequestEnder;
import biz.schroeders.mite.model.CustomerWrapper;
import biz.schroeders.mite.service.MiteClient;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
                .subscribe(new JsonRequestEnder(context));
    }
}
