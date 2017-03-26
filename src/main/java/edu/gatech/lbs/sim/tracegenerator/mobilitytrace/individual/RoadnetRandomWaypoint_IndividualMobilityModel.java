// Copyright (c) 2012, Georgia Tech Research Corporation
// Authors:
//   Peter Pesti (pesti@gatech.edu)
//
package edu.gatech.lbs.sim.tracegenerator.mobilitytrace.individual;

import java.util.List;

import edu.gatech.lbs.core.vector.RoadnetVector;
import edu.gatech.lbs.core.world.roadnet.RoadJunction;
import edu.gatech.lbs.core.world.roadnet.RoadSegment;
import edu.gatech.lbs.sim.Simulation;
import edu.gatech.lbs.sim.agent.SimAgent;
import edu.gatech.lbs.sim.scheduling.event.SimEvent;
import edu.gatech.lbs.sim.scheduling.event.VelocityChangeEvent;
import edu.gatech.lbs.sim.tracegenerator.mobilitytrace.locationdistribution.ILocationDistribution;
import edu.gatech.lbs.sim.tracegenerator.paramdistribution.IParamDistribution;

public class RoadnetRandomWaypoint_IndividualMobilityModel extends IndividualMobilityModel {
  public static final String xmlName = "RandomWaypointRoadnet";

  protected RoadnetVector location;
  protected RoadnetVector destination; // next waypoint (junction)
  protected RoadnetVector v; // [mm/s]
  protected SimAgent agent;

  protected ILocationDistribution locationDistribution;
  protected IParamDistribution speedDistribution;
  protected IParamDistribution stoppingTimeDistribution;

  public RoadnetRandomWaypoint_IndividualMobilityModel(Simulation sim, SimAgent agent, ILocationDistribution locationDistribution, IParamDistribution speedDistribution, IParamDistribution stoppingTimeDistribution, long timestamp) {
    this.sim = sim;
    this.agent = agent;

    this.locationDistribution = locationDistribution;
    this.speedDistribution = speedDistribution;
    this.stoppingTimeDistribution = stoppingTimeDistribution;
    this.timestamp = timestamp;
  }

  protected void stopMoving() {
    v = new RoadnetVector(location.getRoadSegment(), 0);
  }

  protected void startMovingOnNewSegment() {
    v = new RoadnetVector(location.getRoadSegment(), (destination.getProgress() > location.getProgress() ? +1 : -1) * (int) Math.abs(speedDistribution.getNextValue(location)));
  }

  public SimEvent getNextEvent() {
    // set initial location, speed & destination::
    if (location == null) {
      location = locationDistribution.getNextLocation().toRoadnetVector();

      RoadSegment segment = location.toRoadnetVector().getRoadSegment();
      v = new RoadnetVector(segment, speedDistribution.getNextValue(location));
      destination = new RoadnetVector(segment, v.getProgress() > 0 ? segment.getLength() : 0);

      return new VelocityChangeEvent(sim, timestamp, agent, location, v);
    }

    // if we have nowhere to go, there are no events:
    if (location == destination) {
      return null;
    }

    // if we are not moving (but have somewhere to go), we keep stopped for a bit:
    if (v.getLength() == 0) {
      // stop at intersection before starting to move:
      if (stoppingTimeDistribution != null) {
        timestamp += stoppingTimeDistribution.getNextValue(location);
      }
      startMovingOnNewSegment();

      return new VelocityChangeEvent(sim, timestamp, agent, location, v);
    }

    // if we are moving, we travel to our destination:
    timestamp += (long) (1000 * (double) location.vectorTo(destination).getLength() / v.getLength());
    location = destination;

    RoadSegment roadsegment = location.getRoadSegment();
    int progress = location.getProgress();

    RoadJunction jun = null;
    // if at the origin of current segment:
    if (progress == 0 && !roadsegment.isDirected()) {
      jun = roadsegment.getSourceJunction();
    }
    // if at termination of current segment:
    else if (progress == roadsegment.getLength()) {
      jun = roadsegment.getTargetJunction();
    }

    // if there are outgoing roads, choose one at random:
    List<RoadSegment> segments = (jun == null ? null : jun.getReachableRoads());
    if (jun != null && !segments.isEmpty()) {
      // Don't choose the entry road, if there are other choices.
      RoadSegment seg2;
      do {
        seg2 = segments.get((int) (segments.size() * Math.random()));
      } while (segments.size() > 1 && roadsegment == seg2);
      roadsegment = seg2;

      // if chosen edge originates at current junction:
      if (jun == roadsegment.getSourceJunction()) {
        location = new RoadnetVector(roadsegment, 0);
        destination = new RoadnetVector(roadsegment, roadsegment.getLength());
      }
      // if chosen edge terminates at current junction:
      else {
        location = new RoadnetVector(roadsegment, roadsegment.getLength());
        destination = new RoadnetVector(roadsegment, 0);
      }

      if (stoppingTimeDistribution != null) {
        stopMoving();
      } else {
        startMovingOnNewSegment();
      }
    }
    // if there are no outgoing roads, stay stationary at dead end:
    else {
      stopMoving();
      destination = location;
    }

    return new VelocityChangeEvent(sim, timestamp, agent, location, v);
  }
}
