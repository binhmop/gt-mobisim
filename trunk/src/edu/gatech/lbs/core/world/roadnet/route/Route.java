// Copyright (c) 2009, Georgia Tech Research Corporation
// Authors:
//   Peter Pesti (pesti@gatech.edu)
//
package edu.gatech.lbs.core.world.roadnet.route;

import java.util.LinkedList;

import edu.gatech.lbs.core.vector.RoadnetVector;
import edu.gatech.lbs.core.world.roadnet.RoadSegment;

public class Route implements IRoute {
  protected RoadnetVector source;
  protected RoadnetVector target;
  protected LinkedList<RoadSegment> segments;
  protected LinkedList<Boolean> direction; // is travel on the segment the same as the segment's vertex directionality?

  protected double length; // cached length [km]
  protected double traveltime; // cached traversal time, based on maximum speeds [s]

  public Route(RoadnetVector source, RoadnetVector target) {
    segments = new LinkedList<RoadSegment>();
    direction = new LinkedList<Boolean>();

    this.source = source;
    this.target = target;

    length = -1;
    traveltime = -1;
  }

  public void addLastSegment(RoadSegment seg, boolean isForwardTraversed) {
    segments.addLast(seg);
    direction.addLast(isForwardTraversed);
  }

  public void addFirstSegment(RoadSegment seg, boolean isForwardTraversed) {
    segments.addFirst(seg);
    direction.addFirst(isForwardTraversed);
  }

  public void setLength(double len) {
    length = len;
  }

  /**
   * Get the length of the route in meters.
   */
  public double getLength() {
    if (length < 0) {
      if (segments.size() > 1) {
        length = 0;
        RoadSegment seg;

        // from source to first node:
        seg = segments.getFirst();
        length += direction.getFirst() ? seg.getLength() - source.getProgress() : source.getProgress();

        // from last node to target:
        seg = segments.getLast();
        length += direction.getLast() ? target.getProgress() : seg.getLength() - target.getProgress();

        // from first node to last node:
        for (int i = 1; i < segments.size() - 1; i++) {
          length += segments.get(i).getLength();
        }

      } else {
        // source & target are on same segment:
        length = Math.abs(source.getProgress() - target.getProgress());
      }
    }

    return length;
  }

  /**
   * Get the minimum time in seconds, that this route can be travelled from start to finish, always traveling at the speed limit on each segment.
   */
  public double getShortestTravelTime() {
    if (traveltime < 0) {
      if (segments.size() > 1) {
        traveltime = 0;
        RoadSegment seg;

        // from source to first node:
        seg = segments.getFirst();
        traveltime += (direction.getFirst() ? seg.getLength() - source.getProgress() : source.getProgress()) / seg.getSpeedLimit();

        // from last node to target:
        seg = segments.getLast();
        traveltime += (direction.getLast() ? target.getProgress() : seg.getLength() - target.getProgress()) / seg.getSpeedLimit();

        // from first node to last node:
        for (int i = 1; i < segments.size() - 1; i++) {
          seg = segments.get(i);
          traveltime += seg.getLength() / seg.getSpeedLimit();
        }

      } else {
        // source & target are on same segment:
        traveltime = Math.abs(source.getProgress() - target.getProgress()) / source.getRoadSegment().getSpeedLimit();
      }
    }

    return traveltime;
  }

  /**
   * Determine whether the given roadnet location is on the trajectory.
   */
  public boolean contains(RoadnetVector v) {
    RoadSegment seg0 = v.getRoadSegment();
    double p0 = v.getProgress();
    int isOnRoute = 0;

    for (RoadSegment seg : segments) {
      if (seg0 == seg) {
        // more examination needed, if location is on first/last segment
        if (seg0 == source.getRoadSegment() || seg0 == target.getRoadSegment()) {
          // check location against source:
          if (seg0 == source.getRoadSegment() && ((direction.getFirst() && p0 >= source.getProgress()) || (!direction.getFirst() && p0 <= source.getProgress()))) {
            isOnRoute++;
          }
          // check location against target:
          if (seg0 == target.getRoadSegment() && ((direction.getLast() && p0 <= target.getProgress()) || (!direction.getLast() && p0 >= target.getProgress()))) {
            isOnRoute++;
          }
          // need to check against both, if source & target are on the same segment:
          if (segments.size() == 1) {
            isOnRoute = (isOnRoute == 2 ? 1 : 0);
          }
        } else {
          isOnRoute = 1;
        }
        break;
      }
    }
    return isOnRoute != 0;
  }

  public RoadnetVector getSource() {
    return source;
  }

  public RoadnetVector getTarget() {
    return target;
  }

  public int getSegmentCount() {
    return segments.size();
  }

  public RoadSegment getSegment(int i) {
    return segments.get(i);
  }

  public boolean getDirection(int i) {
    return direction.get(i);
  }

}
