// Copyright (c) 2012, Georgia Tech Research Corporation
// Authors:
//   Peter Pesti (pesti@gatech.edu)
//
package edu.gatech.lbs.sim.config.paramparser;

public abstract class IParamParser {

  public abstract int parse(String text);

  protected static String[] getParamTokens(String text) {
    String[] tokens = text.split(" ");
    switch (tokens.length) {
    case 0:
      System.out.println("Empty value for '" + text + "'.");
      System.exit(-1);
    case 1:
      System.out.println("Missing unit of measurement for '" + text + "'.");
      System.exit(-1);
    case 2:
      break;
    default:
      System.out.println("Too many tokens for '" + text + "'.");
      System.exit(-1);
    }
    return tokens;
  }
}
