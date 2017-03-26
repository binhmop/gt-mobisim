// Copyright (c) 2012, Georgia Tech Research Corporation
// Authors:
//   Peter Pesti (pesti@gatech.edu)
//
package edu.gatech.lbs.sim.scheduling.event;

import edu.gatech.lbs.core.vector.IVector;

public interface IMobilityChangeEvent {
  public IVector getLocation();

  public IVector getVelocity();

  public IVector getAcceleration();

  public long getTimestamp();
}
