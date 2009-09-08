// Copyright (c) 2009, Georgia Tech Research Corporation
// Authors:
//   Peter Pesti (pesti@gatech.edu)
//
package edu.gatech.lbs.core.query;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ShortestRouteRangeQuery extends LocationBasedQuery {
  public static final String xmlName = "range";
  public static final byte typeCode = 1;

  public float range; // [m] or [sec]

  public ShortestRouteRangeQuery(float range) {
    this.range = range;
  }

  public ShortestRouteRangeQuery(DataInputStream in) throws IOException {
    range = in.readFloat();
  }

  public LocationBasedQuery clone() {
    ShortestRouteRangeQuery lbq = new ShortestRouteRangeQuery(range);
    lbq.setKey(key.clone());
    return lbq;
  }

  public void saveTo(DataOutputStream out) throws IOException {
    out.writeByte(typeCode);
    out.writeFloat(range);
  }

  public byte getTypeCode() {
    return typeCode;
  }
}
