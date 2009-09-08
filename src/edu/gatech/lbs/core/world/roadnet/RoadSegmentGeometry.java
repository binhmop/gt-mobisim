// Copyright (c) 2009, Georgia Tech Research Corporation
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

  public float getTotalLength() {
    return getDistanceBetweenPoints(0, points.length);
  }

  public CartesianVector[] getPoints() {
    return points;
  }

  public float getDistanceBetweenPoints(int sindex, int eindex) {
    float length = 0;
    for (int i = sindex; i < eindex - 1; i++) {
      length += points[i].vectorTo(points[i + 1]).getLength();
    }
    return length;
  }

  public CartesianVector getLocationAt(float progress) {
    float length = 0;
    int i;
    for (i = 0; length < progress; i++) {
      length += points[i].vectorTo(points[i + 1]).getLength();
    }

    // v= last vector + (progress overshoot)*(vector from last point to one-before-last point)
    IVector v = points[i].clone();
    if (length > progress) {
      IVector tn = points[i].vectorTo(points[i - 1]);
      tn.times((length - progress) / tn.getLength());
      v.add(tn);
    }
    return (CartesianVector) v;
  }

  public CartesianVector getTangentAt(float progress) {
    float length = 0;
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
    tn.times(1.0 / tn.getLength());
    return (CartesianVector) tn;
  }

  public float getLocationProgress(CartesianVector v) {
    float minDist = -1;
    int minPointIdx = -1;
    for (int i = 0; i < points.length; i++) {
      float dist = (float) v.vectorTo(points[i]).getLength();
      if (minDist < 0 || dist < minDist) {
        minDist = dist;
        minPointIdx = i;
      }
    }

    return getDistanceBetweenPoints(0, minPointIdx);
  }
}
