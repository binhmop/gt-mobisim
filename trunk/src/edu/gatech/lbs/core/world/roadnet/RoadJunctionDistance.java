// Copyright (c) 2012, Georgia Tech Research Corporation
// Authors:
//   Peter Pesti (pesti@gatech.edu)
//
package edu.gatech.lbs.core.world.roadnet;

public class RoadJunctionDistance implements Comparable<RoadJunctionDistance> {
  public RoadJunction junction;
  public int distance;

  public RoadJunctionDistance(RoadJunction junction, int distance) {
    this.junction = junction;
    this.distance = distance;
  }

  public int compareTo(RoadJunctionDistance d2) {
    int diff = distance - d2.distance;
    // primary method of comparison is distance, but use junctionId as tiebreaker:
    if (diff == 0) {
      return junction.getId() - d2.junction.getId();
    }
    return (int) Math.signum(diff);
  }

  public boolean equals(RoadJunctionDistance d2) {
    return (junction.getId() == d2.junction.getId()) && (distance == d2.distance);
  }
}
