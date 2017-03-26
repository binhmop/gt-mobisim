// Copyright (c) 2012, Georgia Tech Research Corporation
// Authors:
//   Peter Pesti (pesti@gatech.edu)
//
package edu.gatech.lbs.sim.config.paramparser;

public class SpeedParser extends IParamParser {

  /**
   * Parse & return speed value in millimeters/second [mm/s].
   * Range: +/- 7.7e6 km/h
   */
  public int parse(String text) {
    String[] tokens = getParamTokens(text);
    double v = -1;
    try {
      v = Double.parseDouble(tokens[0]);
    } catch (NumberFormatException e) {
      System.out.println("Failed to parse number from string '" + tokens[0] + "'.");
      System.exit(-1);
    }

    if (tokens[1].equalsIgnoreCase("m/s")) {
      v *= 1e3;
    } else if (tokens[1].equalsIgnoreCase("km/h") || tokens[1].equalsIgnoreCase("kph")) {
      v *= 1e6 / 3600;
    } else if (tokens[1].equalsIgnoreCase("mi/h") || tokens[1].equalsIgnoreCase("mph")) {
      v *= 1e3 * 1609.344 / 3600;
    } else {
      System.out.println("Unknown unit of measurement '" + tokens[1] + "'.");
      System.exit(-1);
    }

    return (int) v;
  }

}
