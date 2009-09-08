// Copyright (c) 2009, Georgia Tech Research Corporation
// Authors:
//   Peter Pesti (pesti@gatech.edu)
//
package edu.gatech.lbs.core.world.roadnet;

import edu.gatech.lbs.core.vector.CartesianVector;

public class ClassedRoadSegment extends RoadSegment {
  protected int roadClassIndex;

  public ClassedRoadSegment(int id, RoadJunction startJunction, RoadJunction endJunction, boolean isDirected, CartesianVector[] points, float speedLimit, int roadClassIndex) {
    super(id, startJunction, endJunction, isDirected, points, speedLimit);

    this.roadClassIndex = roadClassIndex;
  }

  public int getRoadClassIndex() {
    return roadClassIndex;
  }

  public String toString() {
    return "(" + id + " " + roadClassIndex + " " + (isDirected ? "D" : "U") + ")";
  }
}
