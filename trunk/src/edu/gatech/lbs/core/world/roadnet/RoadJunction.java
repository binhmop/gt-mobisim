// Copyright (c) 2012, Georgia Tech Research Corporation
// Authors:
//   Peter Pesti (pesti@gatech.edu)
//
package edu.gatech.lbs.core.world.roadnet;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import edu.gatech.lbs.core.vector.CartesianVector;
import edu.gatech.lbs.core.vector.RoadnetVector;

public class RoadJunction {
  protected int id; // unique junction-ID
  protected List<RoadSegment> outRoads; // outgoing (originating) roads
  protected List<RoadSegment> inRoads; // incoming (terminating) roads

  // Secondary list of segments reachable from this junction, in clockwise order.
  // Undirected loops are listed twice.
  protected List<RoadSegment> reachableSegmentsClockwise;
  protected List<Double> segmentThetas;

  public RoadJunction(int id) {
    this.id = id;
    outRoads = new ArrayList<RoadSegment>(1);
    inRoads = new ArrayList<RoadSegment>(1);
    reachableSegmentsClockwise = new LinkedList<RoadSegment>();
    segmentThetas = new LinkedList<Double>();
  }

  public int getId() {
    return id;
  }

  public int getIndegree() {
    return inRoads.size();
  }

  public int getOutdegree() {
    return outRoads.size();
  }

  public int getDegree() {
    return getIndegree() + getOutdegree();
  }

  public void addOriginatingRoad(RoadSegment roadsegment) {
    outRoads.add(roadsegment);
    addReachableSegment(roadsegment, 0);
  }

  public void addTerminatingRoad(RoadSegment roadsegment) {
    inRoads.add(roadsegment);
    if (!roadsegment.isDirected()) {
      addReachableSegment(roadsegment, 1);
    }
  }

  /**
   * Get the theta angle of the segment's tangent vector (pointing away from the junction) at this junction.
   */
  protected double getSegmentTheta(RoadSegment seg, int endIdx) {
    CartesianVector tangentOut = (endIdx == 0 ? seg.getTangentAt(0) : seg.getTangentAt(seg.getLength()).times(-1).toCartesianVector());
    return tangentOut.getThetaRadians();
  }

  protected int getSegmentClockwiseIndex(double theta) {
    int idx = 0;
    for (Double theta2 : segmentThetas) {
      if (theta2 <= theta) {
        return idx;
      }
      idx++;
    }
    return idx;
  }

  protected void addReachableSegment(RoadSegment seg, int endIdx) {
    double theta = getSegmentTheta(seg, endIdx);
    int idx = getSegmentClockwiseIndex(theta);
    reachableSegmentsClockwise.add(idx, seg);
    segmentThetas.add(idx, theta);
  }

  public void removeRoad(RoadSegment roadsegment) {
    int endIdx = 0;
    if (!inRoads.remove(roadsegment)) {
      if (!outRoads.remove(roadsegment)) {
        return;
      }
      endIdx = 1;
    }

    double theta = getSegmentTheta(roadsegment, endIdx);
    int idx = getSegmentClockwiseIndex(theta);
    if (idx < reachableSegmentsClockwise.size()) {
      reachableSegmentsClockwise.remove(idx);
      segmentThetas.remove(idx);
    }
  }

  public List<RoadSegment> getOriginatingRoads() {
    return outRoads;
  }

  public List<RoadSegment> getTerminatingRoads() {
    return inRoads;
  }

  /**
   * Get all segments that could be entered on by traffic, starting from this junction.
   * Segments are in clockwise order. Undirected loops are listed twice.
   */
  public List<RoadSegment> getReachableRoads() {
    return reachableSegmentsClockwise;
  }

  public List<RoadSegment> getAllRoads(boolean isRepeatingLoops) {
    List<RoadSegment> roads = new LinkedList<RoadSegment>(inRoads);
    if (isRepeatingLoops) {
      roads.addAll(outRoads);
    } else {
      for (RoadSegment seg : outRoads) {
        if (!seg.isLoop()) {
          roads.add(seg);
        }
      }
    }
    return roads;
  }

  public List<RoadSegment> getRoadsOriginatingAt(RoadJunction otherJunction) {
    List<RoadSegment> roads = new ArrayList<RoadSegment>();
    for (RoadSegment seg : inRoads) {
      if (seg.getSourceJunction() == otherJunction && !roads.contains(seg)) {
        roads.add(seg);
      }
    }
    return roads;
  }

  public List<RoadSegment> getRoadsTerminatingAt(RoadJunction otherJunction) {
    List<RoadSegment> roads = new ArrayList<RoadSegment>();
    for (RoadSegment seg : outRoads) {
      if (seg.getTargetJunction() == otherJunction && !roads.contains(seg)) {
        roads.add(seg);
      }
    }
    return roads;
  }

  /**
   * Get the location of junction in (x,y) coordinates.
   */
  public CartesianVector getCartesianLocation() {
    if (inRoads.isEmpty() && outRoads.isEmpty()) {
      return null;
    }
    RoadSegment oneRoadsegment = inRoads.isEmpty() ? outRoads.get(0) : inRoads.get(0);
    return oneRoadsegment.getEndLocation(oneRoadsegment.getJunctionIndex(this));
  }

  /**
   * Get the location of junction in (segmentID, progress) coordinates.
   * WARNING: Several valid coordinates could represent the same location; one is returned.
   */
  public RoadnetVector getRoadnetLocation() {
    if (inRoads.isEmpty() && outRoads.isEmpty()) {
      return null;
    }

    if (inRoads.isEmpty()) {
      return new RoadnetVector(outRoads.get(0), 0);
    } else {
      RoadSegment seg = inRoads.get(0);
      return new RoadnetVector(seg, seg.getLength());

    }
  }

  @Override
  public String toString() {
    StringBuilder str = new StringBuilder();
    str.append("jun" + id + "\n");
    for (RoadSegment seg : getReachableRoads()) {
      str.append(" " + seg + "\n");
    }
    return str.toString();
  }
}
