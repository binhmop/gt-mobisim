// Copyright (c) 2012, Georgia Tech Research Corporation
// Authors:
//   Peter Pesti (pesti@gatech.edu)
//
package edu.gatech.lbs.sim.tracegenerator.mobilitytrace.locationdistribution;

import edu.gatech.lbs.core.vector.IVector;

public interface ILocationDistribution {

  public abstract IVector getNextLocation();

}
