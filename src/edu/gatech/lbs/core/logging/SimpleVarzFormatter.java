// Copyright (c) 2012, Georgia Tech Research Corporation
// Authors:
//   Peter Pesti (pesti@gatech.edu)
//
package edu.gatech.lbs.core.logging;

import java.io.BufferedWriter;
import java.io.IOException;

public class SimpleVarzFormatter implements IVarzFormatter {
  public void writeTo(BufferedWriter out) throws IOException {
    out.write(Varz.getString("permutationStr"));
    out.write(Varz.getLong("wallPartitionTime") + "\t" + Varz.getDouble("partitionRadius") + "\t" + Varz.getLong("partitionCount") + "\t" + Varz.getLong("partitioningNodeCount") + "\t" + Varz.getDouble("partitioningNodeStdev") + "\t" + Varz.getLong("partitioningNodePairsCount") + "\t");
    out.write("\n");
  }
}
