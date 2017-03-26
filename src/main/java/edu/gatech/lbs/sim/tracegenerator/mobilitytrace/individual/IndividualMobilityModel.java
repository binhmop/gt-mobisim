// Copyright (c) 2012, Georgia Tech Research Corporation
// Authors:
//   Peter Pesti (pesti@gatech.edu)
//
package edu.gatech.lbs.sim.tracegenerator.mobilitytrace.individual;

import edu.gatech.lbs.sim.Simulation;
import edu.gatech.lbs.sim.scheduling.event.SimEvent;

public abstract class IndividualMobilityModel {
  protected Simulation sim;
  protected long timestamp; // [ms]

  public abstract SimEvent getNextEvent();

}
