// Copyright (c) 2009, Georgia Tech Research Corporation
// Authors:
//   Peter Pesti (pesti@gatech.edu)
//
package edu.gatech.lbs.sim.tracegenerator.mobilitytrace.locationdistribution;

import edu.gatech.lbs.core.vector.CartesianVector;
import edu.gatech.lbs.core.vector.IVector;
import edu.gatech.lbs.core.world.BoundingBox;

public class UniformLocationDistribution implements ILocationDistribution {
  public static final String xmlName = "uniform";

  private BoundingBox worldBox;

  public UniformLocationDistribution(BoundingBox worldLimits) {
    this.worldBox = worldLimits;
  }

  public IVector getNextLocation() {
    return new CartesianVector(worldBox.getWestBoundary() + Math.random() * (worldBox.getWidth()), worldBox.getSouthBoundary() + Math.random() * (worldBox.getHeight()));
  }
}
