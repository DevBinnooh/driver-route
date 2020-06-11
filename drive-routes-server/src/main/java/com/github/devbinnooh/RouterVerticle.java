package com.github.devbinnooh;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.devbinnooh.conf.DRServerConfig;
import com.github.devbinnooh.dto.SRoute;
import io.vertx.core.*;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import lombok.extern.slf4j.Slf4j;
import org.geolatte.geom.G2D;
import org.geolatte.geom.Geometries;
import org.geolatte.geom.crs.CoordinateReferenceSystems;

@Slf4j
public class RouterVerticle extends AbstractVerticle {


    private ObjectMapper mapper = new ObjectMapper();

    private DRServerConfig config;

    public RouterVerticle (DRServerConfig config)
    {
        this.config = (config!=null?config:new DRServerConfig());
    }

    /**
     * Initialise the verticle.<p>
     * This is called by Vert.x when the verticle instance is deployed. Don't call it yourself.
     *
     * @param vertx   the deploying Vert.x instance
     * @param context the context of the verticle
     */
    @Override
    public void init(Vertx vertx, Context context) {
        super.init(vertx, context);
        WorkerExecutor executor = vertx.createSharedWorkerExecutor("my-worker-pool");
        executor.executeBlocking(promise -> {
            String index_generated_files = config.getString("java.io.tmpdir") + "/graphhopper-graphs";
            String osmFilePath = config.getString("osm-file");
            RouterService routerService = new RouterService(index_generated_files, osmFilePath);
            log.info("loading router Service ... (this might take time)");

            SRoute result = routerService.routeByCarAndFastest(Geometries.mkPoint(new G2D(config.getDouble("test-distance-point-from-lng"), config.getDouble("test-distance-point-from-lat")), CoordinateReferenceSystems.WGS84),
                    Geometries.mkPoint(new G2D(config.getDouble("test-distance-point-to-lng"), config.getDouble("test-distance-point-to-lat")), CoordinateReferenceSystems.WGS84));
            promise.complete(result);
        }, res -> {
            log.info("initilzing routing service and the result is: " + res.result());
        });
        mapper.registerModule(new JavaTimeModule());
        //TODO configurable
        mapper.disable(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    }

    public void start(Promise<Void> startPromise) throws Exception {
        Router router = Router.router(vertx);

        String index_generated_files = config.getString("java.io.tmpdir") + "/graphhopper-graphs";
        String osmFilePath = config.getString("osm-file");
        RouterService routerService = new RouterService(index_generated_files, osmFilePath);
        // Bind "/" to our hello message - so we are still compatible.
        router.route("/distance").handler(routingContext -> {
            Double fromLng = Double.parseDouble(routingContext.request().getParam("from-lng"));
            Double fromLat = Double.parseDouble(routingContext.request().getParam("from-lat"));
            Double toLng = Double.parseDouble(routingContext.request().getParam("to-lng"));
            Double toLat = Double.parseDouble(routingContext.request().getParam("to-lat"));
            org.geolatte.geom.Point<G2D> fromPoint = Geometries.mkPoint(new G2D(fromLng, fromLat), CoordinateReferenceSystems.WGS84);
            org.geolatte.geom.Point<G2D> toPoint = Geometries.mkPoint(new G2D(toLng, toLat), CoordinateReferenceSystems.WGS84);

            SRoute result = routerService.routeByCarAndFastest(fromPoint,toPoint);

            log.info("result is: {}", toJson(result));

            HttpServerResponse response = routingContext.response();
            response
                    .putHeader("content-type", "application/json")
                    .end(toJson(result));
        }).failureHandler((f) -> {
            Throwable failure = f.failure();
            log.error(f.getBodyAsString(), failure);
        });

        // Create the HTTP server and pass the "accept" method to the request handler.
        vertx
                .createHttpServer()
                .requestHandler(router::accept)
                .listen(
                        // Retrieve the port from the configuration,
                        // default to 8080.
                        config().getInteger("http.port", config.getInt("server-port")),
                        result -> {
                            if (result.succeeded()) {
                                startPromise.complete();
                            } else {
                                startPromise.fail(result.cause());
                            }
                        }
                );
    }

    public String toJson(Object o) {
        try {
            String json = mapper.writeValueAsString(o);
            return json;
        } catch (JsonProcessingException var3) {
            log.error(var3.getLocalizedMessage(), var3);
            return "{}";
        }
    }
}
