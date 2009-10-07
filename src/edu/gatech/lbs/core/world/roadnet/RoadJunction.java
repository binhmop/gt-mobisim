// Copyright (c) 2009, Georgia Tech Research Corporation
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

  public RoadJunction(int id) {
    this.id = id;
    outRoads = new ArrayList<RoadSegment>(1);
    inRoads = new ArrayList<RoadSegment>(1);
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
  }

  public void addTerminatingRoad(RoadSegment roadsegment) {
    inRoads.add(roadsegment);
  }

  public void removeRoad(RoadSegment roadsegment) {
    if (!inRoads.remove(roadsegment)) {
      outRoads.remove(roadsegment);
    }
  }

  public List<RoadSegment> getOriginatingRoads() {
    return outRoads;
  }

  public List<RoadSegment> getTerminatingRoads() {
    return inRoads;
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
   * Get the roads whose vertex-direction terminates at this junction, but whose traffic-direction
   * allows traffic to enter the segment from this junction.
   * 
   */
  public List<RoadSegment> getTerminatingUndirectedRoads() {
    List<RoadSegment> undirectedInRoads = new ArrayList<RoadSegment>();
    for (RoadSegment segment : inRoads) {
      if (!segment.isDirected()) {
        undirectedInRoads.add(segment);
      }
    }
    return undirectedInRoads;
  }

  /**
   * Get all roads that could be entered on by traffic, starting from this junction.
   * 
   */
  public List<RoadSegment> getReachableRoads() {
    List<RoadSegment> roads = getTerminatingUndirectedRoads();
    roads.addAll(outRoads);
    return roads;
  }

  /**
   * Get the location of junction in (x,y) coordinates.
   * 
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
}
