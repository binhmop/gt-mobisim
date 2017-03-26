// Copyright (c) 2012, Georgia Tech Research Corporation
// Authors:
//   Peter Pesti (pesti@gatech.edu)
//
package edu.gatech.lbs.core.world.roadnet;

import edu.gatech.lbs.core.vector.CartesianVector;
import edu.gatech.lbs.core.vector.RoadnetVector;
import edu.gatech.lbs.core.world.roadnet.partition.Partition;

public class RoadSegment {
  protected int id; // unique segment-ID
  protected RoadJunction startJunction; // junction at the start of the segment
  protected RoadJunction endJunction; // junction at the end of the segment
  protected boolean isDirected; // is the traffic directed on this segment?
  protected RoadSegmentGeometry geometry; // physical geometry of the segment
  protected int speedLimit; // [mm/s]

  protected int length = -1; // calculated & cached length [mm]

  protected Partition partition;

  public RoadSegment(int id, RoadJunction startJunction, RoadJunction endJunction, boolean isDirected, CartesianVector[] points, int speedLimit) {
    this.id = id;
    this.startJunction = startJunction;
    this.endJunction = endJunction;
    this.isDirected = isDirected;
    this.speedLimit = speedLimit;

    if (points != null) {
      geometry = new RoadSegmentGeometry(points);
    }
  }

  public int getId() {
    return id;
  }

  public CartesianVector getSourceLocation() {
    return geometry.getFirstLocation();
  }

  public CartesianVector getTargetLocation() {
    return geometry.getLastLocation();
  }

  public CartesianVector getEndLocation(int i) {
    return (i == 0) ? geometry.getFirstLocation() : geometry.getLastLocation();
  }

  public CartesianVector getLocationAt(int progress) {
    return geometry.getLocationAt(progress);
  }

  public CartesianVector getTangentAt(int progress) {
    return geometry.getTangentAt(progress);
  }

  public int getLength() {
    if (length < 0) {
      length = geometry.getTotalLength();
      assert (length % 2 == 0);
    }
    return length;
  }

  public RoadSegmentGeometry getGeometry() {
    return geometry;
  }

  public RoadJunction getSourceJunction() {
    return startJunction;
  }

  public RoadJunction getTargetJunction() {
    return endJunction;
  }

  public RoadJunction getEndJunction(int i) {
    return (i == 0) ? startJunction : endJunction;
  }

  public RoadJunction getOtherJunction(RoadJunction junction) {
    if (startJunction == junction) {
      return endJunction;
    } else {
      return startJunction;
    }
  }

  public int getJunctionIndex(RoadJunction junction) {
    if (startJunction.getId() == junction.getId()) {
      return 0;
    } else if (endJunction.getId() == junction.getId()) {
      return 1;
    } else {
      return -1;
    }
  }

  public RoadnetVector getJunctionLocation(RoadJunction junction) {
    return new RoadnetVector(this, getJunctionIndex(junction) == 0 ? 0 : getLength());
  }

  public boolean isBetween(RoadJunction j0, RoadJunction j1) {
    return (startJunction.getId() == j0.getId() && endJunction.getId() == j1.getId()) || (!isDirected && startJunction.getId() == j1.getId() && endJunction.getId() == j0.getId());
  }

  public boolean isLoop() {
    return startJunction.getId() == endJunction.getId();
  }

  public boolean isDirected() {
    return isDirected;
  }

  public void setPartition(Partition partition) {
    this.partition = partition;
  }

  public Partition getPartition() {
    return partition;
  }

  public int getSpeedLimit() {
    return speedLimit;
  }

  public RoadnetVector getRoadnetLocation(CartesianVector v) {
    return new RoadnetVector(this, geometry.getLocationProgress(v));
  }

  @Override
  public String toString() {
    return String.format("(seg%d%s%s)", id, (isDirected ? "d" : "u"), (isLoop() ? "L" : ""));
  }
}
