// Copyright (c) 2009, Georgia Tech Research Corporation
// Authors:
//   Peter Pesti (pesti@gatech.edu)
//
package edu.gatech.lbs.core.world.roadnet.route;

import edu.gatech.lbs.core.vector.RoadnetVector;

public interface IRoute {
  public double getLength();

  public RoadnetVector getSource();

  public RoadnetVector getTarget();
}
