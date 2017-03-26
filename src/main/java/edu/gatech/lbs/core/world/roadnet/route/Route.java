// Copyright (c) 2012, Georgia Tech Research Corporation
// Authors:
//   Peter Pesti (pesti@gatech.edu)
//
package edu.gatech.lbs.core.world.roadnet.route;

import java.util.Iterator;
import java.util.LinkedList;

import edu.gatech.lbs.core.vector.RoadnetVector;
import edu.gatech.lbs.core.world.roadnet.RoadSegment;

public class Route implements IRoute {
  protected RoadnetVector source;
  protected RoadnetVector target;
  protected LinkedList<RoadSegment> segments;
  protected LinkedList<Boolean> direction; // is travel on the segment the same as the segment's vertex directionality?

  protected long length; // cached length [mm]
  protected long traveltime; // cached traversal time, based on maximum speeds [ms]

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

  public void setLength(long len) {
    length = len;
  }

  /**
   * Get the length of the route in meters.
   */
  public long getLength() {
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
   * Get the minimum time in milliseconds, that this route can be traveled from start to finish,
   * always traveling at the speed limit on each segment.
   */
  public long getShortestTravelTime() {
    if (traveltime < 0) {
      if (segments.size() > 1) {
        traveltime = 0;
        RoadSegment seg;

        // from source to first node:
        seg = segments.getFirst();
        traveltime += 1000 * (double) (direction.getFirst() ? seg.getLength() - source.getProgress() : source.getProgress()) / seg.getSpeedLimit();

        // from last node to target:
        seg = segments.getLast();
        traveltime += 1000 * (double) (direction.getLast() ? target.getProgress() : seg.getLength() - target.getProgress()) / seg.getSpeedLimit();

        // from first node to last node:
        for (int i = 1; i < segments.size() - 1; i++) {
          seg = segments.get(i);
          traveltime += 1000 * (double) seg.getLength() / seg.getSpeedLimit();
        }

      } else {
        // source & target are on same segment:
        traveltime = (long) (1000 * Math.abs(source.getProgress() - target.getProgress()) / source.getRoadSegment().getSpeedLimit());
      }
    }

    return traveltime;
  }

  /**
   * Determine whether the given roadnet location is on the trajectory.
   */
  public boolean contains(RoadnetVector v) {
    RoadSegment seg0 = v.getRoadSegment();
    int p0 = v.getProgress();
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

  public String toString() {
    StringBuilder strBuilder = new StringBuilder("[l= " + getLength() + " j=(");
    Iterator<Boolean> dirIterator = direction.iterator();
    for (RoadSegment seg : segments) {
      boolean isSegmentDirectional = dirIterator.next();
      strBuilder.append(seg.getEndJunction(isSegmentDirectional ? 0 : 1).getId() + "-" + seg.getEndJunction(isSegmentDirectional ? 1 : 0).getId() + String.format("(%.2f), ", seg.getLength() / 1000.0));
    }
    strBuilder.append(segments.getLast().getEndJunction(direction.getLast() ? 1 : 0).getId() + ")]");
    return strBuilder.toString();
  }
}
