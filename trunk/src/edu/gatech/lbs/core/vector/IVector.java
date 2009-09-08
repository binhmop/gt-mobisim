// Copyright (c) 2009, Georgia Tech Research Corporation
// Authors:
//   Peter Pesti (pesti@gatech.edu)
//
package edu.gatech.lbs.core.vector;

import java.io.DataOutputStream;
import java.io.IOException;

public interface IVector {
  public double getLength();

  public IVector times(double d);

  public IVector add(IVector v);

  public IVector vectorTo(IVector v); // = v2 - v

  public IVector clone();

  public CartesianVector toCartesianVector();

  public RoadnetVector toRoadnetVector();

  public void saveTo(DataOutputStream out) throws IOException;

  public byte getTypeCode();
}
