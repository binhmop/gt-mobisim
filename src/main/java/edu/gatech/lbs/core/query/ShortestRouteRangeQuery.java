// Copyright (c) 2012, Georgia Tech Research Corporation
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

  public int radius; // [mm] or [msec]

  public ShortestRouteRangeQuery(int range) {
    this.radius = range;
  }

  public ShortestRouteRangeQuery(DataInputStream in) throws IOException {
    radius = in.readInt();
  }

  public LocationBasedQuery clone() {
    ShortestRouteRangeQuery lbq = new ShortestRouteRangeQuery(radius);
    lbq.setKey(key.clone());
    return lbq;
  }

  public void saveTo(DataOutputStream out) throws IOException {
    out.writeByte(typeCode);
    out.writeInt(radius);
  }

  public byte getTypeCode() {
    return typeCode;
  }
}
