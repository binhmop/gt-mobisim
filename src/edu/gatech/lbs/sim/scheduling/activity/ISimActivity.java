// Copyright (c) 2012, Georgia Tech Research Corporation
// Authors:
//   Peter Pesti (pesti@gatech.edu)
//
package edu.gatech.lbs.sim.scheduling.activity;

import edu.gatech.lbs.sim.Simulation;

public interface ISimActivity {
  public void scheduleOn(Simulation sim);

  public void cleanup();
}
