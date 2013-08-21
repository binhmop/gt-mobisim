// Copyright (c) 2012, Georgia Tech Research Corporation
// Authors:
//   Peter Pesti (pesti@gatech.edu)
//
package edu.gatech.lbs.core.world;

import edu.gatech.lbs.core.vector.CartesianVector;

public class BoundingBox {
  private long x0; // west->east
  private long y0; // south->north
  private long width = -1;
  private long height = -1;

  public BoundingBox() {
  }

  public BoundingBox(long x0, long y0, long width, long height) {
    this.x0 = x0;
    this.y0 = y0;
    this.width = width;
    this.height = height;

  }

  public long getWestBoundary() {
    return x0;
  }

  public long getEastBoundary() {
    return x0 + width;
  }

  public long getSouthBoundary() {
    return y0;
  }

  public long getNorthBoundary() {
    return y0 + height;
  }

  public long getDimension(int d) {
    return d == 0 ? width : height;
  }

  public void includePoint(long x, long y) {
    if (width < 0 && height < 0) {
      x0 = x;
      y0 = y;
      width = 0;
      height = 0;
    } else {
      x0 = Math.min(x0, x);
      width = Math.max(width, x - x0);
      y0 = Math.min(y0, y);
      height = Math.max(height, y - y0);
    }
  }

  public long getX0() {
    return x0;
  }

  public long getY0() {
    return y0;
  }

  public long getWidth() {
    return width;
  }

  public long getHeight() {
    return height;
  }

  public long getArea() {
    return width * height;
  }

  public boolean contains(CartesianVector location) {
    return location.getX() >= x0 && location.getX() < getEastBoundary() && location.getY() >= y0 && location.getY() < getNorthBoundary();
  }

  public BoundingBox clone() {
    return new BoundingBox(x0, y0, width, height);
  }

  public String toString() {
    return String.format("x0= %.2f km, ", x0 / 1e6) + String.format("y0= %.2f km, ", y0 / 1e6) + String.format("width= %.2f km, ", width / 1e6) + String.format("height= %.2f km", height / 1e6);
  }

}
