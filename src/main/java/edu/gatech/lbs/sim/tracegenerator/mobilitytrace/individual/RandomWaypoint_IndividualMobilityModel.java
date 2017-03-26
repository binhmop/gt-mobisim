// Copyright (c) 2012, Georgia Tech Research Corporation
// Authors:
//   Peter Pesti (pesti@gatech.edu)
//
package edu.gatech.lbs.sim.tracegenerator.mobilitytrace.individual;

import edu.gatech.lbs.core.vector.CartesianVector;
import edu.gatech.lbs.sim.Simulation;
import edu.gatech.lbs.sim.agent.SimAgent;
import edu.gatech.lbs.sim.scheduling.event.SimEvent;
import edu.gatech.lbs.sim.scheduling.event.VelocityChangeEvent;
import edu.gatech.lbs.sim.tracegenerator.mobilitytrace.locationdistribution.ILocationDistribution;
import edu.gatech.lbs.sim.tracegenerator.paramdistribution.IParamDistribution;

public class RandomWaypoint_IndividualMobilityModel extends IndividualMobilityModel {
  public static final String xmlName = "RandomWaypoint";

  protected CartesianVector location;
  protected CartesianVector destination;
  protected CartesianVector v; // [mm/s]
  protected SimAgent agent;

  private ILocationDistribution locationDistribution;
  private IParamDistribution speedDistribution;

  public RandomWaypoint_IndividualMobilityModel(Simulation sim, SimAgent agent, ILocationDistribution locationDistribution, IParamDistribution speedDistribution, long timestamp) {
    this.sim = sim;
    this.agent = agent;
    this.timestamp = timestamp;
    this.locationDistribution = locationDistribution;
    this.speedDistribution = speedDistribution;

    location = null;
  }

  public SimEvent getNextEvent() {
    // set new location:
    if (location != null) {
      // move to destination:
      timestamp += (long) (1000 * (double) location.vectorTo(destination).getLength() / v.getLength());
      location = destination;
    } else {
      // set initial location:
      location = locationDistribution.getNextLocation().toCartesianVector();
    }

    // set new destination:
    destination = locationDistribution.getNextLocation().toCartesianVector();
    v = location.vectorTo(destination).toCartesianVector();
    v.setLength(speedDistribution.getNextValue(location));

    return new VelocityChangeEvent(sim, timestamp, agent, location, v);
  }
}
