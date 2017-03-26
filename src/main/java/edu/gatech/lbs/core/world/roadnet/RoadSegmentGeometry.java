// Copyright (c) 2012, Georgia Tech Research Corporation
// Authors:
//   Peter Pesti (pesti@gatech.edu)
//
package edu.gatech.lbs.core.world.roadnet;

import edu.gatech.lbs.core.vector.CartesianVector;
import edu.gatech.lbs.core.vector.IVector;

public class RoadSegmentGeometry {

  protected CartesianVector[] points;

  public RoadSegmentGeometry(CartesianVector[] points) {
    this.points = points;
  }

  public CartesianVector getFirstLocation() {
    return points[0];
  }

  public CartesianVector getLastLocation() {
    return points[points.length - 1];
  }

  /**
   * Get total length of segment.
   * All edges in the geometry are rounded with 2 mm precision, thus all junction-to-junction distances
   * in the road network are even; and all closed loop routes have a halfway-point with a distance measured
   * in whole millimeters.
   */
  public int getTotalLength() {
    return getDistanceBetweenPoints(0, points.length);
  }

  public CartesianVector[] getPoints() {
    return points;
  }

  public int getDistanceBetweenPoints(int sindex, int eindex) {
    int length = 0;
    for (int i = sindex; i < eindex - 1; i++) {
      length += points[i].vectorTo(points[i + 1]).getLength() & (~1);
    }
    return length;
  }

  public CartesianVector getLocationAt(int progress) {
    int length = 0;
    int i;
    for (i = 0; length < progress; i++) {
      length += points[i].vectorTo(points[i + 1]).getLength() & (~1);
    }

    // v= last vector + (progress overshoot)*(vector from last point to one-before-last point)
    IVector v = points[i].clone();
    if (length > progress) {
      IVector tn = points[i].vectorTo(points[i - 1]);
      tn.times((length - progress) / (double) (tn.getLength() & (~1)));
      v.add(tn);
    }
    return (CartesianVector) v;
  }

  /**
   * Get the tangent-vector at the given progress. The length of the vector is 1 km (1e6 mm).
   */
  public CartesianVector getTangentAt(int progress) {
    int length = 0;
    int i;
    for (i = 0; length < progress; i++) {
      length += points[i].vectorTo(points[i + 1]).getLength();
    }

    IVector tn;
    if (length > progress || i == points.length - 1) {
      tn = points[i - 1].vectorTo(points[i]);
    } else {
      tn = points[i].vectorTo(points[i + 1]);
    }
    tn.times(1e6 / tn.getLength());
    return (CartesianVector) tn;
  }

  public int getLocationProgress(CartesianVector v) {
    long minDist = -1;
    int minPointIdx = -1;
    for (int i = 0; i < points.length; i++) {
      long dist = v.vectorTo(points[i]).getLength();
      if (minDist < 0 || dist < minDist) {
        minDist = dist;
        minPointIdx = i;
      }
    }

    return getDistanceBetweenPoints(0, minPointIdx);
  }
}
