package com.github.devbinnooh;

import com.github.devbinnooh.dto.SRoute;
import com.github.devbinnooh.dto.Vehicle;
import com.github.devbinnooh.dto.Weighting;
import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.config.CHProfile;
import com.graphhopper.config.Profile;
import com.graphhopper.reader.osm.GraphHopperOSM;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.util.Parameters;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.geolatte.geom.G2D;
import org.geolatte.geom.Point;

import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Slf4j
@AllArgsConstructor
public class RouterService {


    private String ghLocation ;

    private String osmFile;


    public RouterService()
    {
        try {
            ghLocation = Files.createTempDirectory("routing-temp").toAbsolutePath().toString();
        } catch (IOException e)
        {
          log.error(e.getMessage(),e);
        }

    }

    /**
     * Enum: "car" "bike" "foot" "hike" "mtb" "racingbike" "scooter" "truck" "small_truck"
     * algo  dijkstrabi || dijkstra || dijkstra_one_to_many || astar || astarbi || alternative_route || round_trip
     */
    public SRoute route(Point<G2D> from, Point<G2D> to, List<Vehicle> vehicleList, Weighting weighting)
    {

        final String vehicle = Arrays.stream(vehicleList.toArray())
                .map(v -> ((Vehicle) v).toString().toLowerCase())
                .collect(Collectors.joining(","));

        GraphHopper hopper = createGraphHopper(vehicle).
                setOSMFile(osmFile).
                setProfiles(new Profile("profile").setVehicle(vehicle).setWeighting(weighting.toString().toLowerCase())).
                setStoreOnFlush(true);
        hopper.getCHPreparationHandler()
                .setCHProfiles(new CHProfile("profile"))
                .setDisablingAllowed(true);
        hopper.importOrLoad();
        //24.733194,46.6117523/24.7545563,46.7398257
        GHRequest req = new GHRequest(from.getPosition().getLat(), from.getPosition().getLon(), to.getPosition().getLat(), to.getPosition().getLon())
                .setAlgorithm(Parameters.Algorithms.DIJKSTRA.toLowerCase())
                .setProfile("profile");
        req.putHint(com.graphhopper.util.Parameters.CH.DISABLE, true);
        req.putHint(Parameters.Routing.INSTRUCTIONS, false);
        GHResponse rsp = hopper.route(req);

        SRoute result = null;
        if(rsp != null && rsp.getAll() != null && rsp.getAll().size() > 0)
        {
            result = SRoute.builder().distanceInMeters(rsp.getAll().get(0).getDistance()).duration(Duration.of(rsp.getAll().get(0).getTime(), ChronoUnit.MILLIS)).build();
        }

        log.trace("response: {}", rsp);
        return result;
    }

    public SRoute routeByCarAndFastest(Point<G2D> from, Point<G2D> to)
    {

        return route(from,to,Collections.singletonList(Vehicle.CAR), Weighting.FASTEST);
    }


    private GraphHopperOSM createGraphHopper(String encodingManagerString) {
        GraphHopperOSM hopper = new GraphHopperOSM();
        hopper.setEncodingManager(EncodingManager.create(encodingManagerString));
        hopper.setGraphHopperLocation(ghLocation);
        return hopper;
    }
}
