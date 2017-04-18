package edu.gatech.lbs.sim.tracegenerator.mobilitytrace.individual;


import java.util.Random;
import edu.gatech.lbs.core.vector.IVector;
import edu.gatech.lbs.core.vector.RoadnetVector;
import edu.gatech.lbs.core.world.roadnet.RoadMap;
import edu.gatech.lbs.core.world.roadnet.RoadSegment;
import edu.gatech.lbs.core.world.roadnet.route.Route;
import edu.gatech.lbs.sim.Simulation;
import edu.gatech.lbs.sim.agent.SimAgent;
import edu.gatech.lbs.sim.scheduling.event.SimEvent;
import edu.gatech.lbs.sim.scheduling.event.VelocityChangeEvent;
import edu.gatech.lbs.sim.tracegenerator.mobilitytrace.locationdistribution.ILocationDistribution;
import edu.gatech.lbs.sim.tracegenerator.paramdistribution.IParamDistribution;

public class RoadnetFixedEndpoint_IndividualMobilityModel extends IndividualMobilityModel {

  public static final String xmlName = "FixedEndpointRoadnet";
  public static int routeCount = 2;

  protected RoadnetVector location;
  protected RoadnetVector destination; // next waypoint (junction)
  protected RoadnetVector v; // [mm/s]
  protected Route route;
  protected int routeSegment;
  protected SimAgent agent;

  protected RoadMap roadmap;
  protected ILocationDistribution locationDistribution;
  protected IParamDistribution speedDistribution;
  protected IParamDistribution parkingTimeDistribution;
  protected IParamDistribution stoppingTimeDistribution;
  protected boolean isFixed; // set to true when the next location is set to be the destination
  protected boolean isReached;// check if agent reaches the destination
  protected int count;
  protected Random rnd;
  protected IVector[] initLocations;
  protected IVector[] destLocations;


  public RoadnetFixedEndpoint_IndividualMobilityModel(Simulation sim, SimAgent agent,
      ILocationDistribution locationDistribution, IParamDistribution speedDistribution,
      IParamDistribution parkingTimeDistribution, IParamDistribution stoppingTimeDistribution, long timestamp,
      RoadMap roadmap, IVector[] initLocations, IVector[] destLocations) {
    this.sim = sim;
    this.agent = agent;
    this.roadmap = roadmap;
    this.locationDistribution = locationDistribution;
    this.speedDistribution = speedDistribution;
    this.parkingTimeDistribution = parkingTimeDistribution;
    this.stoppingTimeDistribution = stoppingTimeDistribution;
    this.timestamp = timestamp;
    this.isFixed = false;
    this.isReached = false;
    this.count = 0;
    this.initLocations = initLocations;
    this.destLocations = destLocations;
    this.rnd = new Random();
  }


  protected Route getNextRoute(IVector[] nextLocations) {
    int rndInt = rnd.nextInt(nextLocations.length);
    return roadmap.getShortestRoute(location, nextLocations[rndInt].toRoadnetVector());
  }

  protected void reachEndOfRoute() {
    if (parkingTimeDistribution != null) {
      route = null;
      stopMoving();
    } else {
      planNewRoute();
      if (stoppingTimeDistribution != null) {
        stopMoving();
      } else {
        startMovingOnNewSegment();
      }
    }
  }

  protected void stopMoving() {
    v = new RoadnetVector(location.getRoadSegment(), 0);
  }

  protected void startMovingOnNewSegment() {
    // new segment info:
    RoadSegment seg = route.getSegment(routeSegment);
    boolean isForwardTraversed = route.getDirection(routeSegment);

    // set current destination/waypoint to other end of segment, or the route's end-point:
    if (routeSegment < route.getSegmentCount() - 1) {
      destination = new RoadnetVector(seg, isForwardTraversed ? seg.getLength() : 0);
    } else {
      destination = route.getTarget();
    }

    v = new RoadnetVector(location.getRoadSegment(), (destination.getProgress() > location.getProgress() ? +1 : -1)
        * (int) Math.abs(speedDistribution.getNextValue(location)));
  }

  protected void planNewRoute() {
    // make a new route plan:

    // check if agent has reached destination
    if (isReached) {
      RoadnetVector tmpDest = new RoadnetVector(location.getRoadSegment(), (int) (rnd.nextDouble() * location
          .getRoadSegment().getLength()));
      route = roadmap.getShortestRoute(location, tmpDest);
    } else {
      for (int i = 0; i < destLocations.length; i++) {
        if (location.getRoadSegment().getId() == destLocations[i].toRoadnetVector().getRoadSegment().getId()) {
          isReached = true;
          RoadnetVector tmpDest = new RoadnetVector(location.getRoadSegment(), (int) (rnd.nextDouble() * location
              .getRoadSegment().getLength()));
          route = roadmap.getShortestRoute(location, tmpDest);
          break;
        }

      }
      if (!isReached) {
        if (!isFixed) route = roadmap.getShortestRoute(location, locationDistribution.getNextLocation()
            .toRoadnetVector());
        else {
          // Random rnd = new Random();
          int rndInt = rnd.nextInt(destLocations.length);
          route = roadmap.getShortestRoute(location, destLocations[rndInt].toRoadnetVector());
        }
      }
    }
    routeSegment = 0;
  }

  public SimEvent getNextEvent() {
    // set new location:
    if (location == null) {// set initial location:
      if (!isFixed) {
        // option 1: select random initial locations from hotspots
        if (initLocations == null) {
          location = locationDistribution.getNextLocation().toRoadnetVector();
        } else {
          // option 2: select from initialLocations
          if (agent.getSimAgentId() < 2) // let the first two agents have the same route
          location = initLocations[0].toRoadnetVector();
          else {
            int rndInt = rnd.nextInt(initLocations.length);
            location = initLocations[rndInt].toRoadnetVector();
          }
        }
      } else {
        if (agent.getSimAgentId() < 2) // let the first two agents have the same route
        location = destLocations[0].toRoadnetVector();
        else {
          int rndInt = rnd.nextInt(destLocations.length);
          location = destLocations[rndInt].toRoadnetVector();
          isFixed = false;
        }
      }
    }

    if (v == null) {
      reachEndOfRoute();
      return new VelocityChangeEvent(sim, timestamp, agent, location, v);
    }

    // we are currently moving:
    if (v.getLength() != 0) {
      // move to destination:
      timestamp += (long) (1000 * (double) location.vectorTo(destination).getLength() / v.getLength());
      location = destination;

      // move to next segment in route:
      if (route != null && routeSegment < route.getSegmentCount() - 1) {
        routeSegment++;
        // new segment info:
        RoadSegment seg = route.getSegment(routeSegment);
        boolean isForwardTraversed = route.getDirection(routeSegment);

        // set current location to correct end of segment:
        location = new RoadnetVector(seg, isForwardTraversed ? 0 : seg.getLength());

        if (stoppingTimeDistribution != null) {
          stopMoving();
        } else {
          startMovingOnNewSegment();
        }
      }
      // if reached final segment of route:
      else {
        count++;
        // the second new route will be the fixed destination
        if (count == routeCount) isFixed = true;
        reachEndOfRoute();
      }
    }
    // we are not currently moving:
    else {
      // if we are not in a route, we need to plan a new one:
      if (route == null) {
        // park before starting to move:
        if (parkingTimeDistribution != null) {
          timestamp += parkingTimeDistribution.getNextValue(location);
        }
        planNewRoute();
      }

      // stop at intersection before starting to move:
      if (stoppingTimeDistribution != null) {
        timestamp += stoppingTimeDistribution.getNextValue(location);
      }

      startMovingOnNewSegment();
    }

    return new VelocityChangeEvent(sim, timestamp, agent, location, v);
  }
}
