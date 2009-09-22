// Copyright (c) 2009, Georgia Tech Research Corporation
// Authors:
//   Peter Pesti (pesti@gatech.edu)
//
package edu.gatech.lbs.core.world;

import edu.gatech.lbs.core.logging.Stat;
import edu.gatech.lbs.core.vector.CartesianVector;

public class BoundingBox {
  private double x0; // west->east
  private double y0; // south->north
  private double width = -1;
  private double height = -1;

  public BoundingBox() {
  }

  public BoundingBox(double x0, double y0, double width, double height) {
    this.x0 = x0;
    this.y0 = y0;
    this.width = width;
    this.height = height;

  }

  public double getWestBoundary() {
    return x0;
  }

  public double getEastBoundary() {
    return x0 + width;
  }

  public double getSouthBoundary() {
    return y0;
  }

  public double getNorthBoundary() {
    return y0 + height;
  }

  public double getDimension(int d) {
    return d == 0 ? width : height;
  }

  public void includePoint(double x, double y) {
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

  public double getX0() {
    return x0;
  }

  public double getY0() {
    return y0;
  }

  public double getWidth() {
    return width;
  }

  public double getHeight() {
    return height;
  }

  public double getArea() {
    return width * height;
  }

  public boolean contains(CartesianVector location) {
    return location.getX() >= x0 && location.getX() < getEastBoundary() && location.getY() >= y0 && location.getY() < getNorthBoundary();
  }

  public BoundingBox clone() {
    return new BoundingBox(x0, y0, width, height);
  }

  public String toString() {
    return "x0= " + Stat.round(x0, 1) + ", y0= " + Stat.round(y0, 1) + ", width= " + Stat.round(width, 1) + " m, height = " + Stat.round(height, 1) + " m";
  }

}
