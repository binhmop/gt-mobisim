// Copyright (c) 2012, Georgia Tech Research Corporation
// Authors:
//   Peter Pesti (pesti@gatech.edu)
//
package edu.gatech.lbs.core.logging;

public class Logz {
  public static StringBuffer outLog = new StringBuffer();

  public static void print(String s) {
    System.out.print(s);
    outLog.append(s);
  }

  public static void println(String s) {
    System.out.println(s);
    outLog.append(s + "\n");
  }

  public static void println() {
    println("");
  }
}
