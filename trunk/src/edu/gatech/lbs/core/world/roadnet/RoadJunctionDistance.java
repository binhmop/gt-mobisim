package edu.gatech.lbs.core.world.roadnet;

public class RoadJunctionDistance implements Comparable<RoadJunctionDistance> {
  public RoadJunction junction;
  public double distance;

  public RoadJunctionDistance(RoadJunction junction, double distance) {
    this.junction = junction;
    this.distance = distance;
  }

  public int compareTo(RoadJunctionDistance d2) {
    double diff = distance - d2.distance;
    // primary method of comparison is distance, but use junctionId as tiebreaker:
    if (diff == 0) {
      return junction.getId() - d2.junction.getId();
    }
    return (int) Math.signum(diff);
  }
}
