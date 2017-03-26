// Copyright (c) 2012, Georgia Tech Research Corporation
// Authors:
//   Peter Pesti (pesti@gatech.edu)
//
package edu.gatech.lbs.sim.config.paramparser;

public class TimeParser extends IParamParser {

  /**
   * Parse & return time period value in milliseconds [ms].
   * Range: +/- 24.8 days
   */
  public int parse(String text) {
    String[] tokens = getParamTokens(text);
    double t = -1;

    try {
      if (tokens[1].equalsIgnoreCase("ms") || tokens[1].equalsIgnoreCase("millisec") || tokens[1].equalsIgnoreCase("millisecond") || tokens[1].equalsIgnoreCase("milliseconds")) {
        t = Long.parseLong(tokens[0]);
      } else if (tokens[1].equalsIgnoreCase("s") || tokens[1].equalsIgnoreCase("sec") || tokens[1].equalsIgnoreCase("second") || tokens[1].equalsIgnoreCase("seconds")) {
        t = Double.parseDouble(tokens[0]) * 1e3;
      } else if (tokens[1].equalsIgnoreCase("m") || tokens[1].equalsIgnoreCase("min") || tokens[1].equalsIgnoreCase("minute") || tokens[1].equalsIgnoreCase("minutes")) {
        t = Double.parseDouble(tokens[0]) * 1e3 * 60;
      } else if (tokens[1].equalsIgnoreCase("h") || tokens[1].equalsIgnoreCase("hr") || tokens[1].equalsIgnoreCase("hour") || tokens[1].equalsIgnoreCase("hours")) {
        t = Double.parseDouble(tokens[0]) * 1e3 * 3600;
      } else {
        System.out.println("Unknown unit of measurement '" + tokens[1] + "'.");
        System.exit(-1);
      }
    } catch (NumberFormatException e) {
      System.out.println("Failed to parse number from string '" + tokens[0] + "'.");
      System.exit(-1);
    }

    return (int) t;
  }

}
