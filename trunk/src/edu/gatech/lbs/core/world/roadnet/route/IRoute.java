// Copyright (c) 2012, Georgia Tech Research Corporation
// Authors:
//   Peter Pesti (pesti@gatech.edu)
//
package edu.gatech.lbs.core.world.roadnet.route;

import edu.gatech.lbs.core.vector.RoadnetVector;

public interface IRoute {
  public long getLength();

  public RoadnetVector getSource();

  public RoadnetVector getTarget();
}
