// Copyright (c) 2009, Georgia Tech Research Corporation
// Authors:
//   Peter Pesti (pesti@gatech.edu)
//
package edu.gatech.lbs.sim.tracegenerator.paramdistribution;

import edu.gatech.lbs.core.vector.IVector;

public interface IParamDistribution {

  public double getNextValue(IVector location);

}
