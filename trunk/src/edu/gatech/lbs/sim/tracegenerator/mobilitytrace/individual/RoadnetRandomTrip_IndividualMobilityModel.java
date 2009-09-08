// Copyright (c) 2009, Georgia Tech Research Corporation
// Authors:
//   Peter Pesti (pesti@gatech.edu)
//
package edu.gatech.lbs.sim.tracegenerator.mobilitytrace.individual;

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

public class RoadnetRandomTrip_IndividualMobilityModel extends IndividualMobilityModel {
  public static final String xmlName = "RandomTripRoadnet";

  protected RoadnetVector location;
  protected RoadnetVector destination; // next waypoint (junction)
  protected RoadnetVector v; // [m/s]
  protected Route route;
  protected int routeSegment;
  protected SimAgent agent;

  protected RoadMap roadmap;
  protected ILocationDistribution locationDistribution;
  protected IParamDistribution speedDistribution;
  protected IParamDistribution parkingTimeDistribution;

  public RoadnetRandomTrip_IndividualMobilityModel(Simulation sim, SimAgent agent, ILocationDistribution locationDistribution, IParamDistribution speedDistribution, IParamDistribution parkingTimeDistribution, long timestamp, RoadMap roadmap) {
    this.sim = sim;
    this.agent = agent;

    location = null;
    destination = null;
    this.roadmap = roadmap;
    this.locationDistribution = locationDistribution;
    this.speedDistribution = speedDistribution;
    this.parkingTimeDistribution = parkingTimeDistribution;
    this.timestamp = timestamp;

    route = null;
  }

  protected void makeNewRoute() {
    // make a new route plan:
    route = roadmap.getShortestRoute(location, locationDistribution.getNextLocation().toRoadnetVector());
    routeSegment = 0;
  }

  public SimEvent getNextEvent() {
    // set new location:
    if (location == null) {
      // set initial location:
      location = locationDistribution.getNextLocation().toRoadnetVector();
    }

    if (v != null && v.getLength() != 0) {
      // move to destination:
      timestamp += (long) (1000 * location.vectorTo(destination).getLength() / v.getLength());
      location = destination;

      // move to next segment in route:
      if (route != null && routeSegment < route.getSegmentCount() - 1) {
        routeSegment++;
        // new segment info:
        RoadSegment seg = route.getSegment(routeSegment);
        boolean isForwardTraversed = route.getDirection(routeSegment);

        // set current location to correct end of segment:
        location = new RoadnetVector(seg, isForwardTraversed ? 0 : seg.getLength());
      }
      // if reached final segment of route, then park:
      else if (parkingTimeDistribution != null) {
        v = new RoadnetVector(location.getRoadSegment(), 0);
        return new VelocityChangeEvent(sim, timestamp, agent, location, v);
      } else {
        makeNewRoute();
      }
    } else {
      // park a bit:
      if (v != null && parkingTimeDistribution != null) {
        timestamp += (long) (1000 * parkingTimeDistribution.getNextValue(location));
      }
      makeNewRoute();
    }

    // new segment info:
    RoadSegment seg = route.getSegment(routeSegment);
    boolean isForwardTraversed = route.getDirection(routeSegment);

    // set current destination/waypoint to other end of segment, or the route's end-point:
    if (routeSegment < route.getSegmentCount() - 1) {
      destination = new RoadnetVector(seg, isForwardTraversed ? seg.getLength() : 0);
    } else {
      destination = route.getTarget();
    }

    v = new RoadnetVector(location.getRoadSegment(), (destination.getProgress() > location.getProgress() ? +1 : -1) * (float) Math.abs(speedDistribution.getNextValue(location)));

    return new VelocityChangeEvent(sim, timestamp, agent, location, v);
  }
}
