// Copyright (c) 2012, Georgia Tech Research Corporation
// Authors:
//   Peter Pesti (pesti@gatech.edu)
//
package edu.gatech.lbs.core.vector;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import edu.gatech.lbs.core.world.roadnet.RoadMap;

public class CartesianVector implements IVector {
  public static final byte typeCode = 'f';

  protected long x; // [mm], [mm/s], [mm/s^2]
  protected long y; // [mm], [mm/s], [mm/s^2]

  public CartesianVector(long x, long y) {
    this.x = x;
    this.y = y;
  }

  /*public void set(double phi, double length) {
  	y = Math.sin(phi) * length;
  	x = Math.cos(phi) * length;
  }*/

  public CartesianVector(DataInputStream in) throws IOException {
    x = in.readLong();
    y = in.readLong();
  }

  public void setDimension(int d, long v) {
    switch (d) {
    case 0:
      x = v;
      break;
    case 1:
      y = v;
      break;
    default:
      System.out.println("Invalid dimension number '" + d + "'.");
      System.exit(-1);
    }
  }

  public long getDimension(int d) {
    switch (d) {
    case 0:
      return x;
    case 1:
      return y;
    default:
      System.out.println("Invalid dimension number '" + d + "'.");
      System.exit(-1);
      return -1;
    }
  }

  public void setLength(int length) {
    double theta = Math.atan2(y, x);
    y = (long) (Math.sin(theta) * length);
    x = (long) (Math.cos(theta) * length);
  }

  public long getX() {
    return x;
  }

  public long getY() {
    return y;
  }

  public double getLongitude() { // East-West
    // see: http://en.wikipedia.org/wiki/Geographic_coordinate_system#Expressing_latitude_and_longitude_as_linear_units
    // see: http://www.csgnetwork.com/degreelenllavcalc.html
    double lat = getLatitude();
    double a = 6378137; // [m]
    double b = 6356752.3; // [m]
    double aa = Math.pow(a * Math.cos(lat * Math.PI / 180), 2);
    double bb = Math.pow(b * Math.sin(lat * Math.PI / 180), 2);
    return (x / 1000.0) / (Math.PI / 180 * Math.cos(lat * Math.PI / 180) * Math.sqrt((a * a * aa + b * b * bb) / (aa + bb)));
  }

  public double getLatitude() { // North-South
    return (y / 1000.0) / 110574.2727;
  }

  public IVector times(double d) {
    x *= d;
    y *= d;

    return this;
  }

  public IVector add(IVector v) {
    CartesianVector vv = v.toCartesianVector();
    x += vv.getX();
    y += vv.getY();

    return this;
  }

  public IVector vectorTo(IVector v) {
    assert (v instanceof CartesianVector);

    CartesianVector vv = (CartesianVector) v;
    return new CartesianVector(vv.getX() - x, vv.getY() - y);
  }

  @Override
  public String toString() {
    return "(" + String.format("%.1f m", x / 1000.0) + ", " + String.format("%.1f m", y / 1000.0) + ")";
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof CartesianVector) {
      CartesianVector v = (CartesianVector) o;
      return (v.getX() == x && v.getY() == y);
    }
    return false;
  }

  @Override
  public int hashCode() {
    Long xx = new Long(x);
    Long yy = new Long(y);
    return xx.hashCode() ^ yy.hashCode();
  }

  public long getLength() {
    return (long) Math.sqrt(x * x + y * y);
  }

  public double getThetaRadians() {
    double theta = Math.atan2(y, x);
    return theta >= 0 ? theta : 2 * Math.PI + theta;
  }

  public double getEnclosedRadians(IVector v) {
    assert (v instanceof CartesianVector);

    CartesianVector vv = (CartesianVector) v;
    return Math.acos(x * vv.x + y * vv.y);
  }

  @Override
  public IVector clone() {
    return new CartesianVector(x, y);
  }

  public CartesianVector toCartesianVector() {
    return this;
  }

  public RoadnetVector toRoadnetVector() {
    // TODO: return roadmap.getRoadnetLocation(this);
    return null;
  }

  public RoadnetVector toRoadnetVector(RoadMap roadmap) {
    return roadmap.getRoadnetLocation(this);
  }

  public void saveTo(DataOutputStream out) throws IOException {
    out.writeByte(typeCode);
    out.writeLong(x);
    out.writeLong(y);
  }

  public byte getTypeCode() {
    return typeCode;
  }
}
