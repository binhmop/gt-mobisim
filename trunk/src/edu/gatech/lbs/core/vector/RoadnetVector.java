// Copyright (c) 2009, Georgia Tech Research Corporation
// Authors:
//   Peter Pesti (pesti@gatech.edu)
//
package edu.gatech.lbs.core.vector;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import edu.gatech.lbs.core.logging.Stat;
import edu.gatech.lbs.core.world.roadnet.RoadMap;
import edu.gatech.lbs.core.world.roadnet.RoadSegment;

public class RoadnetVector implements IVector {
  public static final byte typeCode = 'r';

  private RoadSegment roadsegment;
  private float progress; // [m], [m/s], [m/s^2] along the dimension defined by the road segment

  public RoadnetVector(RoadSegment roadsegment, float progress) {
    this.roadsegment = roadsegment;
    this.progress = progress;
  }

  public RoadnetVector(DataInputStream in, RoadMap roadmap) throws IOException {
    roadsegment = roadmap.getRoadSegment(in.readInt());
    progress = in.readFloat();
  }

  public double getLength() {
    return progress;
  }

  public float getProgress() {
    return progress;
  }

  public RoadSegment getRoadSegment() {
    return roadsegment;
  }

  public void add(float delta) {
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
    out.writeFloat(progress);
  }

  public String toString() {
    return "(seg" + roadsegment.getId() + "@" + Stat.round(progress, 1) + "/" + Stat.round(roadsegment.getLength(), 1) + ")";
  }

  public byte getTypeCode() {
    return typeCode;
  }
}
