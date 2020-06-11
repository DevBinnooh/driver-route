package com.github.devbinnooh;

import com.github.devbinnooh.conf.DRServerConfig;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;

import java.util.concurrent.TimeUnit;

public class DriveRoutesApp extends AbstractVerticle {

    public static void main(String[] args) {
        DRServerConfig serverConfig = new DRServerConfig();

        VertxOptions vertxOptions = new VertxOptions()
                //10 mins to initalize blocking executions
                .setMaxWorkerExecuteTime( TimeUnit.SECONDS.toNanos(600));
        Vertx vertx = Vertx.vertx(vertxOptions);
        vertx.deployVerticle(new RouterVerticle(serverConfig));


    }
}
