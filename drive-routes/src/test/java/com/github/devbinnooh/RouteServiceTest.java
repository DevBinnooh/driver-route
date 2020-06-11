package com.github.devbinnooh;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.config.Profile;
import com.graphhopper.reader.osm.GraphHopperOSM;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.util.Helper;
import org.geolatte.geom.G2D;
import org.geolatte.geom.Geometries;
import org.geolatte.geom.crs.CoordinateReferenceSystems;
import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;

import static com.graphhopper.util.Parameters.Algorithms.ALT_ROUTE;

/**
 *
 * --keep="highway=motorway =motorway_link =trunk =trunk_link =primary =primary_link =secondary =secondary_link =tertiary =tertiary_link =living_street =residential =residential_link =unclassified "  --drop-author --drop-version
 */
public class RouteServiceTest {

    public static final String DIR = "files";

    // map locations
    private static final String BAYREUTH = DIR + "/north-bayreuth.osm.gz";
    private static final String OSM_TEST_FILE = DIR + "/test2.osm.bz2";

    // when creating GH instances make sure to use this as the GH location such that it will be cleaned between tests
    private static final String GH_LOCATION = "target/graphhopper-it-gh";

    @BeforeEach
    @AfterEach
    public void setup() {
        Helper.removeDir(new File(GH_LOCATION));
    }

    @Test
    public void testRiyadhDifferentAlgorithms() {

        RouterService service = new RouterService(GH_LOCATION, OSM_TEST_FILE);
        org.geolatte.geom.Point<G2D> fromPoint = Geometries.mkPoint(new G2D(46.6117523, 24.733194), CoordinateReferenceSystems.WGS84);
        org.geolatte.geom.Point<G2D> toPoint = Geometries.mkPoint(new G2D(46.7398257, 24.7545563), CoordinateReferenceSystems.WGS84);


        /*GHResponse rsp = service.route(fromPoint,toPoint,Collections.singletonList(Vehicle.CAR), Weighting.FASTEST);
        assertFalse(rsp.getErrors().toString(), rsp.hasErrors());

        PathWrapper res = rsp.getBest();
        assertEquals(19135.5, res.getDistance(), .1);
        assertEquals(726893.0, res.getTime(), 10);
        assertEquals(62, res.getPoints().getSize());

        assertEquals(24.7331632, res.getWaypoints().getLat(0), 1e-7);
        assertEquals(24.7549888 , res.getWaypoints().getLat(1), 1e-7);*/
    }

    @Test
    public void testAlternativeRoutesCar() {
        final String profile = "profile";
        final String vehicle = "car";
        final String weighting = "fastest";

        GraphHopper hopper = createGraphHopper(vehicle).
                setOSMFile(BAYREUTH).
                setProfiles(new Profile(profile).setVehicle(vehicle).setWeighting(weighting));
        hopper.importOrLoad();

        GHRequest req = new GHRequest(50.023513, 11.548862, 49.969441, 11.537876).
                setAlgorithm(ALT_ROUTE).setProfile(profile);
        req.putHint("alternative_route.max_paths", 3);
        GHResponse rsp = hopper.route(req);
        Assert.assertFalse(rsp.getErrors().toString(), rsp.hasErrors());

        Assert.assertEquals(3, rsp.getAll().size());
        // directly via obergräfenthal
        Assert.assertEquals(870, rsp.getAll().get(0).getTime() / 1000);
        // via ramsenthal -> lerchenhof
        Assert.assertEquals(913, rsp.getAll().get(1).getTime() / 1000);
        // via neudrossenfeld
        Assert.assertEquals(958, rsp.getAll().get(2).getTime() / 1000);
    }

    private static GraphHopperOSM createGraphHopper(String encodingManagerString) {
        GraphHopperOSM hopper = new GraphHopperOSM();
        hopper.setEncodingManager(EncodingManager.create(encodingManagerString));
        hopper.setGraphHopperLocation(GH_LOCATION);
        return hopper;
    }
}
