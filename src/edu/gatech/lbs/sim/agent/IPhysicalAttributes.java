// Copyright (c) 2012, Georgia Tech Research Corporation
// Authors:
//   Peter Pesti (pesti@gatech.edu)
//
package edu.gatech.lbs.sim.agent;

import edu.gatech.lbs.core.vector.IVector;

public interface IPhysicalAttributes {

  public abstract IVector getLocation();

  public abstract IVector getVelocity();

  public abstract IVector getAcceleration();

}
