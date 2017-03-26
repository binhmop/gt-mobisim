// Copyright (c) 2012, Georgia Tech Research Corporation
// Authors:
//   Peter Pesti (pesti@gatech.edu)
//
package edu.gatech.lbs.core.query;

import java.io.DataInputStream;
import java.io.IOException;

public class LocationBasedQueryFactory {
  public static LocationBasedQuery load(DataInputStream in) throws IOException {
    LocationBasedQuery query = null;

    byte typeCode = in.readByte();
    if (typeCode == ShortestRouteRangeQuery.typeCode) {
      query = new ShortestRouteRangeQuery(in);
    } else {
      System.out.println("Unknown query type code: '" + typeCode + "'.");
      System.exit(-1);
    }

    return query;
  }
}
