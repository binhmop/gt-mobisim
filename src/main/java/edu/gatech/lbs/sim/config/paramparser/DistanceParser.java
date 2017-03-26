// Copyright (c) 2012, Georgia Tech Research Corporation
// Authors:
//   Peter Pesti (pesti@gatech.edu)
//
package edu.gatech.lbs.sim.config.paramparser;

public class DistanceParser extends IParamParser {

  /**
   * Parse & return distance value in millimeters [mm].
   * Range: +/- 2147 km
   */
  public int parse(String text) {
    String[] tokens = getParamTokens(text);
    double d = -1;
    try {
      d = Double.parseDouble(tokens[0]);
    } catch (NumberFormatException e) {
      System.out.println("Failed to parse number from string '" + tokens[0] + "'.");
      System.exit(-1);
    }

    if (tokens[1].equalsIgnoreCase("m") || tokens[1].equalsIgnoreCase("meter") || tokens[1].equalsIgnoreCase("meters")) {
      d *= 1e3;
    } else if (tokens[1].equalsIgnoreCase("km") || tokens[1].equalsIgnoreCase("kilometer") || tokens[1].equalsIgnoreCase("kilometers")) {
      d *= 1e6;
    } else if (tokens[1].equalsIgnoreCase("mi") || tokens[1].equalsIgnoreCase("mile") || tokens[1].equalsIgnoreCase("miles")) {
      d *= 1609.344 * 1e3;
    } else {
      System.out.println("Unknown unit of measurement '" + tokens[1] + "'.");
      System.exit(-1);
    }

    return (int) d;
  }

}
