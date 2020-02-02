package biz.schroeders.mite;

import io.vertx.core.Launcher;
import io.vertx.core.VertxOptions;
import io.vertx.ext.dropwizard.DropwizardMetricsOptions;

public class Starter extends Launcher {
    public static void main(final String[] args) {
        new Starter().dispatch(args);
    }

    @Override
    public void beforeStartingVertx(final VertxOptions options) {
        options.setMetricsOptions(new DropwizardMetricsOptions().setEnabled(true));
    }
}
