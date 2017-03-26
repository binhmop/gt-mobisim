// Copyright (c) 2012, Georgia Tech Research Corporation
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
  protected RoadnetVector v; // [mm/s]
  protected Route route;
  protected int routeSegment;
  protected SimAgent agent;

  protected RoadMap roadmap;
  protected ILocationDistribution locationDistribution;
  protected IParamDistribution speedDistribution;
  protected IParamDistribution parkingTimeDistribution;
  protected IParamDistribution stoppingTimeDistribution;

  public RoadnetRandomTrip_IndividualMobilityModel(Simulation sim, SimAgent agent, ILocationDistribution locationDistribution, IParamDistribution speedDistribution, IParamDistribution parkingTimeDistribution, IParamDistribution stoppingTimeDistribution, long timestamp, RoadMap roadmap) {
    this.sim = sim;
    this.agent = agent;

    this.roadmap = roadmap;
    this.locationDistribution = locationDistribution;
    this.speedDistribution = speedDistribution;
    this.parkingTimeDistribution = parkingTimeDistribution;
    this.stoppingTimeDistribution = stoppingTimeDistribution;
    this.timestamp = timestamp;
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

    v = new RoadnetVector(location.getRoadSegment(), (destination.getProgress() > location.getProgress() ? +1 : -1) * (int) Math.abs(speedDistribution.getNextValue(location)));
  }

  protected void planNewRoute() {
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
