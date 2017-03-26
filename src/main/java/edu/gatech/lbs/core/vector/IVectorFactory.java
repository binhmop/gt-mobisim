// Copyright (c) 2012, Georgia Tech Research Corporation
// Authors:
//   Peter Pesti (pesti@gatech.edu)
//
package edu.gatech.lbs.core.vector;

import java.io.DataInputStream;
import java.io.IOException;

import edu.gatech.lbs.core.world.IWorld;
import edu.gatech.lbs.core.world.roadnet.RoadMap;

public class IVectorFactory {
  public static IVector load(DataInputStream in, IWorld world) throws IOException {
    IVector vector = null;
    byte typeCode = in.readByte();
    if (typeCode == CartesianVector.typeCode) {
      vector = new CartesianVector(in);
    } else if (typeCode == RoadnetVector.typeCode) {
      vector = new RoadnetVector(in, (RoadMap) world);
    } else {
      System.out.println("Unknown vector type code: '" + typeCode + "'.");
      System.exit(-1);
    }
    return vector;
  }
}
