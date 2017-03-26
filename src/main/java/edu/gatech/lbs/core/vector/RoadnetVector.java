// Copyright (c) 2012, Georgia Tech Research Corporation
// Authors:
//   Peter Pesti (pesti@gatech.edu)
//
package edu.gatech.lbs.core.vector;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import edu.gatech.lbs.core.world.roadnet.RoadMap;
import edu.gatech.lbs.core.world.roadnet.RoadSegment;

public class RoadnetVector implements IVector {
  public static final byte typeCode = 'r';

  protected RoadSegment roadsegment;
  protected int progress; // [mm], [mm/s], [mm/s^2] along the dimension defined by the road segment

  public RoadnetVector(RoadSegment roadsegment, int progress) {
    this.roadsegment = roadsegment;
    this.progress = progress;
  }

  public RoadnetVector(DataInputStream in, RoadMap roadmap) throws IOException {
    roadsegment = roadmap.getRoadSegment(in.readInt());
    progress = in.readInt();
  }

  public long getLength() {
    return progress;
  }

  public int getProgress() {
    return progress;
  }

  public RoadSegment getRoadSegment() {
    return roadsegment;
  }

  public void add(int delta) {
    progress += delta;
  }

  public IVector times(double d) {
    progress *= d;
    return this;
  }

  public IVector add(IVector v) {
    assert (v instanceof RoadnetVector);
    RoadnetVector vv = (RoadnetVector) v;
    assert (vv.getRoadSegment() == roadsegment);

    progress += vv.getProgress();
    return this;
  }

  public IVector vectorTo(IVector v) {
    assert (v instanceof RoadnetVector);
    RoadnetVector vv = (RoadnetVector) v;
    assert (vv.getRoadSegment() == roadsegment);

    return new RoadnetVector(roadsegment, vv.getProgress() - progress);
  }

  @Override
  public IVector clone() {
    return new RoadnetVector(roadsegment, progress);
  }

  public CartesianVector toCartesianVector() {
    return roadsegment.getLocationAt(progress);
  }

  public RoadnetVector toRoadnetVector() {
    return this;
  }

  public CartesianVector toTangentVector() {
    return roadsegment.getTangentAt(progress);
  }

  public void saveTo(DataOutputStream out) throws IOException {
    out.writeByte(typeCode);
    out.writeInt(roadsegment.getId());
    out.writeInt(progress);
  }

  @Override
  public String toString() {
    return String.format("(seg%d%s@%d/%d)", roadsegment.getId(), (roadsegment.isLoop() ? "L" : ""), progress, roadsegment.getLength());
  }

  /**
   * Simple equality-check.
   * Returns false for two locations that represent the same junction, if using different segments.
   */
  @Override
  public boolean equals(Object o) {
    if (o instanceof RoadnetVector) {
      RoadnetVector v = (RoadnetVector) o;
      return (v.getRoadSegment().getId() == getRoadSegment().getId() && v.getProgress() == getProgress());
    }
    return false;
  }

  public byte getTypeCode() {
    return typeCode;
  }
}
