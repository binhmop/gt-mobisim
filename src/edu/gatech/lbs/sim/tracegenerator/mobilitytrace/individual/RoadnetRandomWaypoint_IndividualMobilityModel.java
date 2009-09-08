// Copyright (c) 2009, Georgia Tech Research Corporation
// Authors:
//   Peter Pesti (pesti@gatech.edu)
//
package edu.gatech.lbs.sim.tracegenerator.mobilitytrace.individual;

import java.util.List;

import edu.gatech.lbs.core.vector.RoadnetVector;
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
  protected RoadnetVector destination;
  protected RoadnetVector v; // [m/s]
  protected SimAgent agent;

  protected ILocationDistribution locationDistribution;
  protected IParamDistribution speedDistribution;

  public RoadnetRandomWaypoint_IndividualMobilityModel(Simulation sim, SimAgent agent, ILocationDistribution locationDistribution, IParamDistribution speedDistribution, long timestamp) {
    this.sim = sim;
    this.agent = agent;
    this.location = locationDistribution.getNextLocation().toRoadnetVector();
    this.locationDistribution = locationDistribution;
    this.speedDistribution = speedDistribution;
    this.timestamp = timestamp;

    location = null;
  }

  public SimEvent getNextEvent() {
    // set new location:
    if (location != null) {
      // move to destination:
      timestamp += (long) (1000 * location.vectorTo(destination).getLength() / v.getLength());
      location = destination;
    } else {
      // set initial location:
      location = locationDistribution.getNextLocation().toRoadnetVector();

      // set initial speed & destination:
      RoadSegment segment = location.toRoadnetVector().getRoadSegment();
      v = new RoadnetVector(segment, (float) speedDistribution.getNextValue(location));
      destination = new RoadnetVector(segment, v.getLength() > 0 ? segment.getLength() : 0);

      return new VelocityChangeEvent(sim, timestamp, agent, location, v);
    }

    RoadSegment roadsegment = location.getRoadSegment();
    double progress = location.getProgress();
    List<RoadSegment> originatingRoads = null;
    List<RoadSegment> terminatingRoads = null;

    double len = roadsegment.getLength();
    // if at the origin of current segment:
    if (progress == 0 && !roadsegment.isDirected()) {
      originatingRoads = roadsegment.getSourceJunction().getOriginatingRoads();
      terminatingRoads = roadsegment.getSourceJunction().getTerminatingUndirectedRoads();
    }
    // if at termination of current segment:
    else if (progress == len) {
      originatingRoads = roadsegment.getTargetJunction().getOriginatingRoads();
      terminatingRoads = roadsegment.getTargetJunction().getTerminatingUndirectedRoads();
    }

    int originatingRoadsCount = originatingRoads == null ? 0 : originatingRoads.size();
    int terminatingRoadsCount = terminatingRoads == null ? 0 : terminatingRoads.size();
    // if there are outgoing roads, choose one at random:
    if (originatingRoadsCount + terminatingRoadsCount > 0) {
      boolean isSingleExitJunction = (originatingRoadsCount + terminatingRoadsCount == 1);
      int chosenRoadNum;
      // don't choose the entry road, if there are other choices:
      do {
        chosenRoadNum = (int) ((originatingRoadsCount + terminatingRoadsCount) * Math.random());
      } while (!isSingleExitJunction && ((chosenRoadNum < originatingRoadsCount && roadsegment == originatingRoads.get(chosenRoadNum)) || (chosenRoadNum >= originatingRoadsCount && roadsegment == terminatingRoads.get(chosenRoadNum - originatingRoadsCount))));

      // if chosen edge originates at current junction:
      if (chosenRoadNum < originatingRoadsCount) {
        roadsegment = originatingRoads.get(chosenRoadNum);
        location = new RoadnetVector(roadsegment, 0);
        destination = new RoadnetVector(roadsegment, roadsegment.getLength());
      }
      // if chosen edge terminates at current junction:
      else {
        roadsegment = terminatingRoads.get(chosenRoadNum - originatingRoadsCount);
        location = new RoadnetVector(roadsegment, roadsegment.getLength());
        destination = new RoadnetVector(roadsegment, 0);
      }

      v = new RoadnetVector(roadsegment, (destination.getProgress() > location.getProgress() ? +1 : -1) * (float) Math.abs(speedDistribution.getNextValue(location)));
    }
    // if there are no outgoing roads, stay stationary at dead end:
    else {
      v = new RoadnetVector(roadsegment, 0);
      destination = location;
    }

    return new VelocityChangeEvent(sim, timestamp, agent, location, v);
  }
}
